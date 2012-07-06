/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core QueryPhase.java 2012-3-29 15:00:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.query;

import java.util.Map;

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.commons.lucene.search.DeletionAwareConstantScoreQuery;
import cn.com.rebirth.search.commons.lucene.search.Queries;
import cn.com.rebirth.search.commons.lucene.search.function.BoostScoreFunction;
import cn.com.rebirth.search.commons.lucene.search.function.FunctionScoreQuery;
import cn.com.rebirth.search.core.action.search.SearchType;
import cn.com.rebirth.search.core.index.query.ParsedQuery;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.SearchPhase;
import cn.com.rebirth.search.core.search.facet.FacetPhase;
import cn.com.rebirth.search.core.search.internal.ContextIndexSearcher;
import cn.com.rebirth.search.core.search.internal.ScopePhase;
import cn.com.rebirth.search.core.search.internal.SearchContext;
import cn.com.rebirth.search.core.search.sort.SortParseElement;
import cn.com.rebirth.search.core.search.sort.TrackScoresParseElement;

import com.google.common.collect.ImmutableMap;

/**
 * The Class QueryPhase.
 *
 * @author l.xue.nong
 */
public class QueryPhase implements SearchPhase {

	/** The facet phase. */
	private final FacetPhase facetPhase;

	/**
	 * Instantiates a new query phase.
	 *
	 * @param facetPhase the facet phase
	 */
	@Inject
	public QueryPhase(FacetPhase facetPhase) {
		this.facetPhase = facetPhase;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.SearchPhase#parseElements()
	 */
	@Override
	public Map<String, ? extends SearchParseElement> parseElements() {
		ImmutableMap.Builder<String, SearchParseElement> parseElements = ImmutableMap.builder();
		parseElements.put("from", new FromParseElement()).put("size", new SizeParseElement())
				.put("indices_boost", new IndicesBoostParseElement())
				.put("indicesBoost", new IndicesBoostParseElement()).put("query", new QueryParseElement())
				.put("queryBinary", new QueryBinaryParseElement()).put("query_binary", new QueryBinaryParseElement())
				.put("filter", new FilterParseElement()).put("filterBinary", new FilterBinaryParseElement())
				.put("filter_binary", new FilterBinaryParseElement()).put("sort", new SortParseElement())
				.put("trackScores", new TrackScoresParseElement()).put("track_scores", new TrackScoresParseElement())
				.put("min_score", new MinScoreParseElement()).put("minScore", new MinScoreParseElement())
				.put("timeout", new TimeoutParseElement()).put("similarity", new SimilarityParseElement())
				.putAll(facetPhase.parseElements());
		return parseElements.build();
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.SearchPhase#preProcess(cn.com.summall.search.core.search.internal.SearchContext)
	 */
	@Override
	public void preProcess(SearchContext context) {
		if (context.query() == null) {
			context.parsedQuery(ParsedQuery.MATCH_ALL_PARSED_QUERY);
		}
		if (context.queryBoost() != 1.0f) {
			context.parsedQuery(new ParsedQuery(new FunctionScoreQuery(context.query(), new BoostScoreFunction(context
					.queryBoost())), context.parsedQuery()));
		}
		Filter searchFilter = context.mapperService().searchFilter(context.types());
		if (searchFilter != null) {
			if (Queries.isMatchAllQuery(context.query())) {
				Query q = new DeletionAwareConstantScoreQuery(context.filterCache().cache(searchFilter));
				q.setBoost(context.query().getBoost());
				context.parsedQuery(new ParsedQuery(q, context.parsedQuery()));
			} else {
				context.parsedQuery(new ParsedQuery(new FilteredQuery(context.query(), context.filterCache().cache(
						searchFilter)), context.parsedQuery()));
			}
		}
		facetPhase.preProcess(context);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.SearchPhase#execute(cn.com.summall.search.core.search.internal.SearchContext)
	 */
	public void execute(SearchContext searchContext) throws QueryPhaseExecutionException {
		searchContext.queryResult().searchTimedOut(false);

		if (searchContext.scopePhases() != null) {

			try {
				searchContext.idCache().refresh(searchContext.searcher().subReaders());
			} catch (Exception e) {
				throw new QueryPhaseExecutionException(searchContext, "Failed to refresh id cache for child queries", e);
			}

			for (ScopePhase scopePhase : searchContext.scopePhases()) {
				if (scopePhase instanceof ScopePhase.TopDocsPhase) {
					ScopePhase.TopDocsPhase topDocsPhase = (ScopePhase.TopDocsPhase) scopePhase;
					topDocsPhase.clear();
					int numDocs = (searchContext.from() + searchContext.size());
					if (numDocs == 0) {
						numDocs = 1;
					}
					try {
						numDocs *= topDocsPhase.factor();
						while (true) {
							if (topDocsPhase.scope() != null) {
								searchContext.searcher().processingScope(topDocsPhase.scope());
							}
							TopDocs topDocs = searchContext.searcher().search(topDocsPhase.query(), numDocs);
							if (topDocsPhase.scope() != null) {

								searchContext.searcher().processedScope();
							}
							topDocsPhase.processResults(topDocs, searchContext);

							if (topDocsPhase.numHits() >= (searchContext.from() + searchContext.size())) {
								break;
							}

							if (topDocs.totalHits <= numDocs) {
								break;
							}

							numDocs *= topDocsPhase.incrementalFactor();
							if (numDocs > topDocs.totalHits) {
								numDocs = topDocs.totalHits;
							}
						}
					} catch (Exception e) {
						throw new QueryPhaseExecutionException(searchContext, "Failed to execute child query ["
								+ scopePhase.query() + "]", e);
					}
				} else if (scopePhase instanceof ScopePhase.CollectorPhase) {
					try {
						ScopePhase.CollectorPhase collectorPhase = (ScopePhase.CollectorPhase) scopePhase;

						if (!collectorPhase.requiresProcessing()) {
							continue;
						}
						if (scopePhase.scope() != null) {
							searchContext.searcher().processingScope(scopePhase.scope());
						}
						Collector collector = collectorPhase.collector();
						searchContext.searcher().search(collectorPhase.query(), collector);
						collectorPhase.processCollector(collector);
						if (collectorPhase.scope() != null) {

							searchContext.searcher().processedScope();
						}
					} catch (Exception e) {
						throw new QueryPhaseExecutionException(searchContext, "Failed to execute child query ["
								+ scopePhase.query() + "]", e);
					}
				}
			}
		}

		searchContext.searcher().processingScope(ContextIndexSearcher.Scopes.MAIN);
		try {
			searchContext.queryResult().from(searchContext.from());
			searchContext.queryResult().size(searchContext.size());

			Query query = searchContext.query();

			TopDocs topDocs;
			int numDocs = searchContext.from() + searchContext.size();
			if (numDocs == 0) {

				numDocs = 1;
			}

			if (searchContext.searchType() == SearchType.COUNT) {
				TotalHitCountCollector collector = new TotalHitCountCollector();
				searchContext.searcher().search(query, collector);
				topDocs = new TopDocs(collector.getTotalHits(), Lucene.EMPTY_SCORE_DOCS, 0);
			} else if (searchContext.searchType() == SearchType.SCAN) {
				topDocs = searchContext.scanContext().execute(searchContext);
			} else if (searchContext.sort() != null) {
				topDocs = searchContext.searcher().search(query, null, numDocs, searchContext.sort());
			} else {
				topDocs = searchContext.searcher().search(query, numDocs);
			}
			searchContext.queryResult().topDocs(topDocs);
		} catch (Exception e) {
			throw new QueryPhaseExecutionException(searchContext, "Failed to execute main query", e);
		} finally {
			searchContext.searcher().processedScope();
		}

		facetPhase.execute(searchContext);
	}
}
