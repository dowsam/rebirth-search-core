/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FieldsTermsStringFacetCollector.java 2012-7-6 14:30:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.terms.strings;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.commons.collect.BoundedTreeSet;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldData;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.facet.terms.support.EntryPriorityQueue;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * The Class FieldsTermsStringFacetCollector.
 *
 * @author l.xue.nong
 */
public class FieldsTermsStringFacetCollector extends AbstractFacetCollector {

	/** The field data cache. */
	private final FieldDataCache fieldDataCache;

	/** The index fields names. */
	private final String[] indexFieldsNames;

	/** The comparator type. */
	private final InternalStringTermsFacet.ComparatorType comparatorType;

	/** The size. */
	private final int size;

	/** The number of shards. */
	private final int numberOfShards;

	/** The fields data type. */
	private final FieldDataType[] fieldsDataType;

	/** The fields data. */
	private FieldData[] fieldsData;

	/** The aggregator. */
	private final StaticAggregatorValueProc aggregator;

	/** The script. */
	private final SearchScript script;

	/**
	 * Instantiates a new fields terms string facet collector.
	 *
	 * @param facetName the facet name
	 * @param fieldsNames the fields names
	 * @param size the size
	 * @param comparatorType the comparator type
	 * @param allTerms the all terms
	 * @param context the context
	 * @param excluded the excluded
	 * @param pattern the pattern
	 * @param scriptLang the script lang
	 * @param script the script
	 * @param params the params
	 */
	public FieldsTermsStringFacetCollector(String facetName, String[] fieldsNames, int size,
			InternalStringTermsFacet.ComparatorType comparatorType, boolean allTerms, SearchContext context,
			ImmutableSet<String> excluded, Pattern pattern, String scriptLang, String script, Map<String, Object> params) {
		super(facetName);
		this.fieldDataCache = context.fieldDataCache();
		this.size = size;
		this.comparatorType = comparatorType;
		this.numberOfShards = context.numberOfShards();

		fieldsDataType = new FieldDataType[fieldsNames.length];
		fieldsData = new FieldData[fieldsNames.length];
		indexFieldsNames = new String[fieldsNames.length];

		for (int i = 0; i < fieldsNames.length; i++) {
			MapperService.SmartNameFieldMappers smartMappers = context.smartFieldMappers(fieldsNames[i]);
			if (smartMappers == null || !smartMappers.hasMapper()) {
				this.indexFieldsNames[i] = fieldsNames[i];
				this.fieldsDataType[i] = FieldDataType.DefaultTypes.STRING;
			} else {
				this.indexFieldsNames[i] = smartMappers.mapper().names().indexName();
				this.fieldsDataType[i] = smartMappers.mapper().fieldDataType();
			}

		}

		if (script != null) {
			this.script = context.scriptService().search(context.lookup(), scriptLang, script, params);
		} else {
			this.script = null;
		}

		if (excluded.isEmpty() && pattern == null && this.script == null) {
			aggregator = new StaticAggregatorValueProc(CacheRecycler.<String> popObjectIntMap());
		} else {
			aggregator = new AggregatorValueProc(CacheRecycler.<String> popObjectIntMap(), excluded, pattern,
					this.script);
		}

		if (allTerms) {
			try {
				for (int i = 0; i < fieldsNames.length; i++) {
					for (IndexReader reader : context.searcher().subReaders()) {
						FieldData fieldData = fieldDataCache.cache(fieldsDataType[i], reader, indexFieldsNames[i]);
						fieldData.forEachValue(aggregator);
					}
				}
			} catch (Exception e) {
				throw new FacetPhaseExecutionException(facetName, "failed to load all terms", e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#setScorer(org.apache.lucene.search.Scorer)
	 */
	@Override
	public void setScorer(Scorer scorer) throws IOException {
		if (script != null) {
			script.setScorer(scorer);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		for (int i = 0; i < indexFieldsNames.length; i++) {
			fieldsData[i] = fieldDataCache.cache(fieldsDataType[i], reader, indexFieldsNames[i]);
		}
		if (script != null) {
			script.setNextReader(reader);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		for (FieldData fieldData : fieldsData) {
			fieldData.forEachValueInDoc(doc, aggregator);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		TObjectIntHashMap<String> facets = aggregator.facets();
		if (facets.isEmpty()) {
			CacheRecycler.pushObjectIntMap(facets);
			return new InternalStringTermsFacet(facetName, comparatorType, size,
					ImmutableList.<InternalStringTermsFacet.StringEntry> of(), aggregator.missing(), aggregator.total());
		} else {
			if (size < EntryPriorityQueue.LIMIT) {
				EntryPriorityQueue ordered = new EntryPriorityQueue(size, comparatorType.comparator());
				for (TObjectIntIterator<String> it = facets.iterator(); it.hasNext();) {
					it.advance();
					ordered.insertWithOverflow(new InternalStringTermsFacet.StringEntry(it.key(), it.value()));
				}
				InternalStringTermsFacet.StringEntry[] list = new InternalStringTermsFacet.StringEntry[ordered.size()];
				for (int i = ordered.size() - 1; i >= 0; i--) {
					list[i] = ((InternalStringTermsFacet.StringEntry) ordered.pop());
				}
				CacheRecycler.pushObjectIntMap(facets);
				return new InternalStringTermsFacet(facetName, comparatorType, size, Arrays.asList(list),
						aggregator.missing(), aggregator.total());
			} else {
				BoundedTreeSet<InternalStringTermsFacet.StringEntry> ordered = new BoundedTreeSet<InternalStringTermsFacet.StringEntry>(
						comparatorType.comparator(), size);
				for (TObjectIntIterator<String> it = facets.iterator(); it.hasNext();) {
					it.advance();
					ordered.add(new InternalStringTermsFacet.StringEntry(it.key(), it.value()));
				}
				CacheRecycler.pushObjectIntMap(facets);
				return new InternalStringTermsFacet(facetName, comparatorType, size, ordered, aggregator.missing(),
						aggregator.total());
			}
		}
	}

	/**
	 * The Class AggregatorValueProc.
	 *
	 * @author l.xue.nong
	 */
	public static class AggregatorValueProc extends StaticAggregatorValueProc {

		/** The excluded. */
		private final ImmutableSet<String> excluded;

		/** The matcher. */
		private final Matcher matcher;

		/** The script. */
		private final SearchScript script;

		/**
		 * Instantiates a new aggregator value proc.
		 *
		 * @param facets the facets
		 * @param excluded the excluded
		 * @param pattern the pattern
		 * @param script the script
		 */
		public AggregatorValueProc(TObjectIntHashMap<String> facets, ImmutableSet<String> excluded, Pattern pattern,
				SearchScript script) {
			super(facets);
			this.excluded = excluded;
			this.matcher = pattern != null ? pattern.matcher("") : null;
			this.script = script;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.terms.strings.FieldsTermsStringFacetCollector.StaticAggregatorValueProc#onValue(int, java.lang.String)
		 */
		@Override
		public void onValue(int docId, String value) {
			if (excluded != null && excluded.contains(value)) {
				return;
			}
			if (matcher != null && !matcher.reset(value).matches()) {
				return;
			}
			if (script != null) {
				script.setNextDocId(docId);
				script.setNextVar("term", value);
				Object scriptValue = script.run();
				if (scriptValue == null) {
					return;
				}
				if (scriptValue instanceof Boolean) {
					if (!((Boolean) scriptValue)) {
						return;
					}
				} else {
					value = scriptValue.toString();
				}
			}
			super.onValue(docId, value);
		}
	}

	/**
	 * The Class StaticAggregatorValueProc.
	 *
	 * @author l.xue.nong
	 */
	public static class StaticAggregatorValueProc implements FieldData.StringValueInDocProc, FieldData.StringValueProc {

		/** The facets. */
		private final TObjectIntHashMap<String> facets;

		/** The missing. */
		private int missing;

		/** The total. */
		private int total;

		/**
		 * Instantiates a new static aggregator value proc.
		 *
		 * @param facets the facets
		 */
		public StaticAggregatorValueProc(TObjectIntHashMap<String> facets) {
			this.facets = facets;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.FieldData.StringValueProc#onValue(java.lang.String)
		 */
		@Override
		public void onValue(String value) {
			facets.putIfAbsent(value, 0);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.FieldData.StringValueInDocProc#onValue(int, java.lang.String)
		 */
		@Override
		public void onValue(int docId, String value) {
			facets.adjustOrPutValue(value, 1, 1);
			total++;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.FieldData.StringValueInDocProc#onMissing(int)
		 */
		@Override
		public void onMissing(int docId) {
			missing++;
		}

		/**
		 * Facets.
		 *
		 * @return the t object int hash map
		 */
		public final TObjectIntHashMap<String> facets() {
			return facets;
		}

		/**
		 * Missing.
		 *
		 * @return the int
		 */
		public final int missing() {
			return this.missing;
		}

		/**
		 * Total.
		 *
		 * @return the int
		 */
		public final int total() {
			return this.total;
		}
	}
}
