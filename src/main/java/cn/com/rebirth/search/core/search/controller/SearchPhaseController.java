/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchPhaseController.java 2012-7-6 14:30:12 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.controller;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.FieldDoc;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.ShardFieldDocSortedHitQueue;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.util.PriorityQueue;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.trove.ExtTIntArrayList;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.search.SearchShardTarget;
import cn.com.rebirth.search.core.search.dfs.AggregatedDfs;
import cn.com.rebirth.search.core.search.dfs.DfsSearchResult;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetProcessors;
import cn.com.rebirth.search.core.search.facet.InternalFacets;
import cn.com.rebirth.search.core.search.fetch.FetchSearchResult;
import cn.com.rebirth.search.core.search.fetch.FetchSearchResultProvider;
import cn.com.rebirth.search.core.search.internal.InternalSearchHit;
import cn.com.rebirth.search.core.search.internal.InternalSearchHits;
import cn.com.rebirth.search.core.search.internal.InternalSearchResponse;
import cn.com.rebirth.search.core.search.query.QuerySearchResult;
import cn.com.rebirth.search.core.search.query.QuerySearchResultProvider;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

/**
 * The Class SearchPhaseController.
 *
 * @author l.xue.nong
 */
public class SearchPhaseController extends AbstractComponent {

	/** The query result ordering. */
	public static Ordering<QuerySearchResultProvider> QUERY_RESULT_ORDERING = new Ordering<QuerySearchResultProvider>() {
		@Override
		public int compare(@Nullable QuerySearchResultProvider o1, @Nullable QuerySearchResultProvider o2) {
			int i = o1.shardTarget().index().compareTo(o2.shardTarget().index());
			if (i == 0) {
				i = o1.shardTarget().shardId() - o2.shardTarget().shardId();
			}
			return i;
		}
	};

	/** The Constant EMPTY. */
	private static final ShardDoc[] EMPTY = new ShardDoc[0];

	/** The facet processors. */
	private final FacetProcessors facetProcessors;

	/** The optimize single shard. */
	private final boolean optimizeSingleShard;

	/**
	 * Instantiates a new search phase controller.
	 *
	 * @param settings the settings
	 * @param facetProcessors the facet processors
	 */
	@Inject
	public SearchPhaseController(Settings settings, FacetProcessors facetProcessors) {
		super(settings);
		this.facetProcessors = facetProcessors;
		this.optimizeSingleShard = componentSettings.getAsBoolean("optimize_single_shard", true);
	}

	/**
	 * Optimize single shard.
	 *
	 * @return true, if successful
	 */
	public boolean optimizeSingleShard() {
		return optimizeSingleShard;
	}

	/**
	 * Aggregate dfs.
	 *
	 * @param results the results
	 * @return the aggregated dfs
	 */
	public AggregatedDfs aggregateDfs(Iterable<DfsSearchResult> results) {
		TObjectIntHashMap<Term> dfMap = new TObjectIntHashMap<Term>(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1);
		long aggMaxDoc = 0;
		for (DfsSearchResult result : results) {
			for (int i = 0; i < result.freqs().length; i++) {
				dfMap.adjustOrPutValue(result.terms()[i], result.freqs()[i], result.freqs()[i]);
			}
			aggMaxDoc += result.maxDoc();
		}
		return new AggregatedDfs(dfMap, aggMaxDoc);
	}

	/**
	 * Sort docs.
	 *
	 * @param results1 the results1
	 * @return the shard doc[]
	 */
	public ShardDoc[] sortDocs(Collection<? extends QuerySearchResultProvider> results1) {
		if (results1.isEmpty()) {
			return EMPTY;
		}

		if (optimizeSingleShard) {
			boolean canOptimize = false;
			QuerySearchResult result = null;
			if (results1.size() == 1) {
				canOptimize = true;
				result = results1.iterator().next().queryResult();
			} else {

				for (QuerySearchResultProvider queryResult : results1) {
					if (queryResult.queryResult().topDocs().scoreDocs.length > 0) {
						if (result != null) {
							canOptimize = false;
							break;
						}
						canOptimize = true;
						result = queryResult.queryResult();
					}
				}
			}
			if (canOptimize) {
				ScoreDoc[] scoreDocs = result.topDocs().scoreDocs;
				if (scoreDocs.length < result.from()) {
					return EMPTY;
				}
				int resultDocsSize = result.size();
				if ((scoreDocs.length - result.from()) < resultDocsSize) {
					resultDocsSize = scoreDocs.length - result.from();
				}
				if (result.topDocs() instanceof TopFieldDocs) {
					ShardDoc[] docs = new ShardDoc[resultDocsSize];
					for (int i = 0; i < resultDocsSize; i++) {
						ScoreDoc scoreDoc = scoreDocs[result.from() + i];
						docs[i] = new ShardFieldDoc(result.shardTarget(), scoreDoc.doc, scoreDoc.score,
								((FieldDoc) scoreDoc).fields);
					}
					return docs;
				} else {
					ShardDoc[] docs = new ShardDoc[resultDocsSize];
					for (int i = 0; i < resultDocsSize; i++) {
						ScoreDoc scoreDoc = scoreDocs[result.from() + i];
						docs[i] = new ShardScoreDoc(result.shardTarget(), scoreDoc.doc, scoreDoc.score);
					}
					return docs;
				}
			}
		}

		List<? extends QuerySearchResultProvider> results = QUERY_RESULT_ORDERING.sortedCopy(results1);

		QuerySearchResultProvider queryResultProvider = results.get(0);

		int totalNumDocs = 0;

		int queueSize = queryResultProvider.queryResult().from() + queryResultProvider.queryResult().size();
		if (queryResultProvider.includeFetch()) {

			queueSize *= results.size();
		}
		PriorityQueue queue;
		if (queryResultProvider.queryResult().topDocs() instanceof TopFieldDocs) {

			TopFieldDocs fieldDocs = (TopFieldDocs) queryResultProvider.queryResult().topDocs();
			for (int i = 0; i < fieldDocs.fields.length; i++) {
				boolean allValuesAreNull = true;
				boolean resolvedField = false;
				for (QuerySearchResultProvider resultProvider : results) {
					for (ScoreDoc doc : resultProvider.queryResult().topDocs().scoreDocs) {
						FieldDoc fDoc = (FieldDoc) doc;
						if (fDoc.fields[i] != null) {
							allValuesAreNull = false;
							if (fDoc.fields[i] instanceof String) {
								fieldDocs.fields[i] = new SortField(fieldDocs.fields[i].getField(), SortField.STRING,
										fieldDocs.fields[i].getReverse());
							}
							resolvedField = true;
							break;
						}
					}
					if (resolvedField) {
						break;
					}
				}
				if (!resolvedField && allValuesAreNull && fieldDocs.fields[i].getField() != null) {

					fieldDocs.fields[i] = new SortField(fieldDocs.fields[i].getField(), SortField.STRING,
							fieldDocs.fields[i].getReverse());
				}
			}
			queue = new ShardFieldDocSortedHitQueue(fieldDocs.fields, queueSize);

			for (QuerySearchResultProvider resultProvider : results) {
				QuerySearchResult result = resultProvider.queryResult();
				ScoreDoc[] scoreDocs = result.topDocs().scoreDocs;
				totalNumDocs += scoreDocs.length;
				for (ScoreDoc doc : scoreDocs) {
					ShardFieldDoc nodeFieldDoc = new ShardFieldDoc(result.shardTarget(), doc.doc, doc.score,
							((FieldDoc) doc).fields);
					if (queue.insertWithOverflow(nodeFieldDoc) == nodeFieldDoc) {

						break;
					}
				}
			}
		} else {
			queue = new ScoreDocQueue(queueSize);
			for (QuerySearchResultProvider resultProvider : results) {
				QuerySearchResult result = resultProvider.queryResult();
				ScoreDoc[] scoreDocs = result.topDocs().scoreDocs;
				totalNumDocs += scoreDocs.length;
				for (ScoreDoc doc : scoreDocs) {
					ShardScoreDoc nodeScoreDoc = new ShardScoreDoc(result.shardTarget(), doc.doc, doc.score);
					if (queue.insertWithOverflow(nodeScoreDoc) == nodeScoreDoc) {

						break;
					}
				}
			}

		}

		int resultDocsSize = queryResultProvider.queryResult().size();
		if (queryResultProvider.includeFetch()) {

			resultDocsSize *= results.size();
		}
		if (totalNumDocs < queueSize) {
			resultDocsSize = totalNumDocs - queryResultProvider.queryResult().from();
		}

		if (resultDocsSize <= 0) {
			return EMPTY;
		}

		ShardDoc[] shardDocs = new ShardDoc[resultDocsSize];
		for (int i = resultDocsSize - 1; i >= 0; i--)

			shardDocs[i] = (ShardDoc) queue.pop();
		return shardDocs;
	}

	/**
	 * Doc ids to load.
	 *
	 * @param shardDocs the shard docs
	 * @return the map
	 */
	public Map<SearchShardTarget, ExtTIntArrayList> docIdsToLoad(ShardDoc[] shardDocs) {
		Map<SearchShardTarget, ExtTIntArrayList> result = Maps.newHashMap();
		for (ShardDoc shardDoc : shardDocs) {
			ExtTIntArrayList list = result.get(shardDoc.shardTarget());
			if (list == null) {
				list = new ExtTIntArrayList();
				result.put(shardDoc.shardTarget(), list);
			}
			list.add(shardDoc.docId());
		}
		return result;
	}

	/**
	 * Merge.
	 *
	 * @param sortedDocs the sorted docs
	 * @param queryResults the query results
	 * @param fetchResults the fetch results
	 * @return the internal search response
	 */
	public InternalSearchResponse merge(ShardDoc[] sortedDocs,
			Map<SearchShardTarget, ? extends QuerySearchResultProvider> queryResults,
			Map<SearchShardTarget, ? extends FetchSearchResultProvider> fetchResults) {

		boolean sorted = false;
		int sortScoreIndex = -1;
		QuerySearchResult querySearchResult;
		try {
			querySearchResult = Iterables.get(queryResults.values(), 0).queryResult();
		} catch (IndexOutOfBoundsException e) {

			return InternalSearchResponse.EMPTY;
		}

		if (querySearchResult.topDocs() instanceof TopFieldDocs) {
			sorted = true;
			TopFieldDocs fieldDocs = (TopFieldDocs) querySearchResult.queryResult().topDocs();
			for (int i = 0; i < fieldDocs.fields.length; i++) {
				if (fieldDocs.fields[i].getType() == SortField.SCORE) {
					sortScoreIndex = i;
				}
			}
		}

		InternalFacets facets = null;
		if (!queryResults.isEmpty()) {

			if (querySearchResult.facets() != null && querySearchResult.facets().facets() != null
					&& !querySearchResult.facets().facets().isEmpty()) {
				List<Facet> aggregatedFacets = Lists.newArrayList();
				List<Facet> namedFacets = Lists.newArrayList();
				for (Facet facet : querySearchResult.facets()) {

					namedFacets.clear();
					for (QuerySearchResultProvider queryResultProvider : queryResults.values()) {
						for (Facet facet1 : queryResultProvider.queryResult().facets()) {
							if (facet.name().equals(facet1.name())) {
								namedFacets.add(facet1);
							}
						}
					}
					Facet aggregatedFacet = facetProcessors.processor(facet.type()).reduce(facet.name(), namedFacets);
					aggregatedFacets.add(aggregatedFacet);
				}
				facets = new InternalFacets(aggregatedFacets);
			}
		}

		long totalHits = 0;
		float maxScore = Float.NEGATIVE_INFINITY;
		boolean timedOut = false;
		for (QuerySearchResultProvider queryResultProvider : queryResults.values()) {
			if (queryResultProvider.queryResult().searchTimedOut()) {
				timedOut = true;
			}
			totalHits += queryResultProvider.queryResult().topDocs().totalHits;
			if (!Float.isNaN(queryResultProvider.queryResult().topDocs().getMaxScore())) {
				maxScore = Math.max(maxScore, queryResultProvider.queryResult().topDocs().getMaxScore());
			}
		}
		if (Float.isInfinite(maxScore)) {
			maxScore = Float.NaN;
		}

		for (FetchSearchResultProvider fetchSearchResultProvider : fetchResults.values()) {
			fetchSearchResultProvider.fetchResult().initCounter();
		}

		List<InternalSearchHit> hits = new ArrayList<InternalSearchHit>();
		if (!fetchResults.isEmpty()) {
			for (ShardDoc shardDoc : sortedDocs) {
				FetchSearchResultProvider fetchResultProvider = fetchResults.get(shardDoc.shardTarget());
				if (fetchResultProvider == null) {
					continue;
				}
				FetchSearchResult fetchResult = fetchResultProvider.fetchResult();
				int index = fetchResult.counterGetAndIncrement();
				if (index < fetchResult.hits().internalHits().length) {
					InternalSearchHit searchHit = fetchResult.hits().internalHits()[index];
					searchHit.score(shardDoc.score());
					searchHit.shard(fetchResult.shardTarget());

					if (sorted) {
						FieldDoc fieldDoc = (FieldDoc) shardDoc;
						searchHit.sortValues(fieldDoc.fields);
						if (sortScoreIndex != -1) {
							searchHit.score(((Number) fieldDoc.fields[sortScoreIndex]).floatValue());
						}
					}

					hits.add(searchHit);
				}
			}
		}

		InternalSearchHits searchHits = new InternalSearchHits(hits.toArray(new InternalSearchHit[hits.size()]),
				totalHits, maxScore);
		return new InternalSearchResponse(searchHits, facets, timedOut);
	}
}
