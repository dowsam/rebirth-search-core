/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TermsLongFacetCollector.java 2012-7-6 14:29:02 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.terms.longs;

import gnu.trove.iterator.TLongIntIterator;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.set.hash.TLongHashSet;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.commons.collect.BoundedTreeSet;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.longs.LongFieldData;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.facet.terms.TermsFacet;
import cn.com.rebirth.search.core.search.facet.terms.support.EntryPriorityQueue;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * The Class TermsLongFacetCollector.
 *
 * @author l.xue.nong
 */
public class TermsLongFacetCollector extends AbstractFacetCollector {

	/** The cache. */
	static ThreadLocal<ThreadLocals.CleanableValue<Deque<TLongIntHashMap>>> cache = new ThreadLocal<ThreadLocals.CleanableValue<Deque<TLongIntHashMap>>>() {
		@Override
		protected ThreadLocals.CleanableValue<Deque<TLongIntHashMap>> initialValue() {
			return new ThreadLocals.CleanableValue<Deque<TLongIntHashMap>>(new ArrayDeque<TLongIntHashMap>());
		}
	};

	/** The field data cache. */
	private final FieldDataCache fieldDataCache;

	/** The index field name. */
	private final String indexFieldName;

	/** The comparator type. */
	private final TermsFacet.ComparatorType comparatorType;

	/** The size. */
	private final int size;

	/** The number of shards. */
	private final int numberOfShards;

	/** The field data type. */
	private final FieldDataType fieldDataType;

	/** The field data. */
	private LongFieldData fieldData;

	/** The aggregator. */
	private final StaticAggregatorValueProc aggregator;

	/** The script. */
	private final SearchScript script;

	/**
	 * Instantiates a new terms long facet collector.
	 *
	 * @param facetName the facet name
	 * @param fieldName the field name
	 * @param size the size
	 * @param comparatorType the comparator type
	 * @param allTerms the all terms
	 * @param context the context
	 * @param excluded the excluded
	 * @param scriptLang the script lang
	 * @param script the script
	 * @param params the params
	 */
	public TermsLongFacetCollector(String facetName, String fieldName, int size,
			TermsFacet.ComparatorType comparatorType, boolean allTerms, SearchContext context,
			ImmutableSet<String> excluded, String scriptLang, String script, Map<String, Object> params) {
		super(facetName);
		this.fieldDataCache = context.fieldDataCache();
		this.size = size;
		this.comparatorType = comparatorType;
		this.numberOfShards = context.numberOfShards();

		MapperService.SmartNameFieldMappers smartMappers = context.smartFieldMappers(fieldName);
		if (smartMappers == null || !smartMappers.hasMapper()) {
			throw new RebirthIllegalArgumentException("Field [" + fieldName
					+ "] doesn't have a type, can't run terms long facet collector on it");
		}

		if (smartMappers.explicitTypeInNameWithDocMapper()) {
			setFilter(context.filterCache().cache(smartMappers.docMapper().typeFilter()));
		}

		if (smartMappers.mapper().fieldDataType() != FieldDataType.DefaultTypes.LONG) {
			throw new RebirthIllegalArgumentException("Field [" + fieldName
					+ "] is not of long type, can't run terms long facet collector on it");
		}

		this.indexFieldName = smartMappers.mapper().names().indexName();
		this.fieldDataType = smartMappers.mapper().fieldDataType();

		if (script != null) {
			this.script = context.scriptService().search(context.lookup(), scriptLang, script, params);
		} else {
			this.script = null;
		}

		if (this.script == null && excluded.isEmpty()) {
			aggregator = new StaticAggregatorValueProc(CacheRecycler.popLongIntMap());
		} else {
			aggregator = new AggregatorValueProc(CacheRecycler.popLongIntMap(), excluded, this.script);
		}

		if (allTerms) {
			try {
				for (IndexReader reader : context.searcher().subReaders()) {
					LongFieldData fieldData = (LongFieldData) fieldDataCache.cache(fieldDataType, reader,
							indexFieldName);
					fieldData.forEachValue(aggregator);
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
		fieldData = (LongFieldData) fieldDataCache.cache(fieldDataType, reader, indexFieldName);
		if (script != null) {
			script.setNextReader(reader);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		fieldData.forEachValueInDoc(doc, aggregator);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		TLongIntHashMap facets = aggregator.facets();
		if (facets.isEmpty()) {
			CacheRecycler.pushLongIntMap(facets);
			return new InternalLongTermsFacet(facetName, comparatorType, size,
					ImmutableList.<InternalLongTermsFacet.LongEntry> of(), aggregator.missing(), aggregator.total());
		} else {
			if (size < EntryPriorityQueue.LIMIT) {
				EntryPriorityQueue ordered = new EntryPriorityQueue(size, comparatorType.comparator());
				for (TLongIntIterator it = facets.iterator(); it.hasNext();) {
					it.advance();
					ordered.insertWithOverflow(new InternalLongTermsFacet.LongEntry(it.key(), it.value()));
				}
				InternalLongTermsFacet.LongEntry[] list = new InternalLongTermsFacet.LongEntry[ordered.size()];
				for (int i = ordered.size() - 1; i >= 0; i--) {
					list[i] = (InternalLongTermsFacet.LongEntry) ordered.pop();
				}
				CacheRecycler.pushLongIntMap(facets);
				return new InternalLongTermsFacet(facetName, comparatorType, size, Arrays.asList(list),
						aggregator.missing(), aggregator.total());
			} else {
				BoundedTreeSet<InternalLongTermsFacet.LongEntry> ordered = new BoundedTreeSet<InternalLongTermsFacet.LongEntry>(
						comparatorType.comparator(), size);
				for (TLongIntIterator it = facets.iterator(); it.hasNext();) {
					it.advance();
					ordered.add(new InternalLongTermsFacet.LongEntry(it.key(), it.value()));
				}
				CacheRecycler.pushLongIntMap(facets);
				return new InternalLongTermsFacet(facetName, comparatorType, size, ordered, aggregator.missing(),
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

		/** The script. */
		private final SearchScript script;

		/** The excluded. */
		private final TLongHashSet excluded;

		/**
		 * Instantiates a new aggregator value proc.
		 *
		 * @param facets the facets
		 * @param excluded the excluded
		 * @param script the script
		 */
		public AggregatorValueProc(TLongIntHashMap facets, Set<String> excluded, SearchScript script) {
			super(facets);
			this.script = script;
			if (excluded == null || excluded.isEmpty()) {
				this.excluded = null;
			} else {
				this.excluded = new TLongHashSet(excluded.size());
				for (String s : excluded) {
					this.excluded.add(Long.parseLong(s));
				}
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.terms.longs.TermsLongFacetCollector.StaticAggregatorValueProc#onValue(int, long)
		 */
		@Override
		public void onValue(int docId, long value) {
			if (excluded != null && excluded.contains(value)) {
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
					value = ((Number) scriptValue).longValue();
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
	public static class StaticAggregatorValueProc implements LongFieldData.ValueInDocProc, LongFieldData.ValueProc {

		/** The facets. */
		private final TLongIntHashMap facets;

		/** The missing. */
		private int missing;

		/** The total. */
		private int total;

		/**
		 * Instantiates a new static aggregator value proc.
		 *
		 * @param facets the facets
		 */
		public StaticAggregatorValueProc(TLongIntHashMap facets) {
			this.facets = facets;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.longs.LongFieldData.ValueProc#onValue(long)
		 */
		@Override
		public void onValue(long value) {
			facets.putIfAbsent(value, 0);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.longs.LongFieldData.ValueInDocProc#onValue(int, long)
		 */
		@Override
		public void onValue(int docId, long value) {
			facets.adjustOrPutValue(value, 1, 1);
			total++;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.longs.LongFieldData.ValueInDocProc#onMissing(int)
		 */
		@Override
		public void onMissing(int docId) {
			missing++;
		}

		/**
		 * Facets.
		 *
		 * @return the t long int hash map
		 */
		public final TLongIntHashMap facets() {
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
