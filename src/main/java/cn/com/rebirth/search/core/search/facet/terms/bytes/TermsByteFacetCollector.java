/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TermsByteFacetCollector.java 2012-7-6 14:29:23 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.terms.bytes;

import gnu.trove.iterator.TByteIntIterator;
import gnu.trove.map.hash.TByteIntHashMap;
import gnu.trove.set.hash.TByteHashSet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.commons.collect.BoundedTreeSet;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.bytes.ByteFieldData;
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
 * The Class TermsByteFacetCollector.
 *
 * @author l.xue.nong
 */
public class TermsByteFacetCollector extends AbstractFacetCollector {

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
	private ByteFieldData fieldData;

	/** The aggregator. */
	private final StaticAggregatorValueProc aggregator;

	/** The script. */
	private final SearchScript script;

	/**
	 * Instantiates a new terms byte facet collector.
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
	public TermsByteFacetCollector(String facetName, String fieldName, int size,
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
					+ "] doesn't have a type, can't run terms short facet collector on it");
		}

		if (smartMappers.explicitTypeInNameWithDocMapper()) {
			setFilter(context.filterCache().cache(smartMappers.docMapper().typeFilter()));
		}

		if (smartMappers.mapper().fieldDataType() != FieldDataType.DefaultTypes.BYTE) {
			throw new RebirthIllegalArgumentException("Field [" + fieldName
					+ "] is not of byte type, can't run terms short facet collector on it");
		}

		this.indexFieldName = smartMappers.mapper().names().indexName();
		this.fieldDataType = smartMappers.mapper().fieldDataType();

		if (script != null) {
			this.script = context.scriptService().search(context.lookup(), scriptLang, script, params);
		} else {
			this.script = null;
		}

		if (this.script == null && excluded.isEmpty()) {
			aggregator = new StaticAggregatorValueProc(CacheRecycler.popByteIntMap());
		} else {
			aggregator = new AggregatorValueProc(CacheRecycler.popByteIntMap(), excluded, this.script);
		}

		if (allTerms) {
			try {
				for (IndexReader reader : context.searcher().subReaders()) {
					ByteFieldData fieldData = (ByteFieldData) fieldDataCache.cache(fieldDataType, reader,
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
		fieldData = (ByteFieldData) fieldDataCache.cache(fieldDataType, reader, indexFieldName);
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
		TByteIntHashMap facets = aggregator.facets();
		if (facets.isEmpty()) {
			CacheRecycler.pushByteIntMap(facets);
			return new InternalByteTermsFacet(facetName, comparatorType, size,
					ImmutableList.<InternalByteTermsFacet.ByteEntry> of(), aggregator.missing(), aggregator.total());
		} else {
			if (size < EntryPriorityQueue.LIMIT) {
				EntryPriorityQueue ordered = new EntryPriorityQueue(size, comparatorType.comparator());
				for (TByteIntIterator it = facets.iterator(); it.hasNext();) {
					it.advance();
					ordered.insertWithOverflow(new InternalByteTermsFacet.ByteEntry(it.key(), it.value()));
				}
				InternalByteTermsFacet.ByteEntry[] list = new InternalByteTermsFacet.ByteEntry[ordered.size()];
				for (int i = ordered.size() - 1; i >= 0; i--) {
					list[i] = (InternalByteTermsFacet.ByteEntry) ordered.pop();
				}
				CacheRecycler.pushByteIntMap(facets);
				return new InternalByteTermsFacet(facetName, comparatorType, size, Arrays.asList(list),
						aggregator.missing(), aggregator.total());
			} else {
				BoundedTreeSet<InternalByteTermsFacet.ByteEntry> ordered = new BoundedTreeSet<InternalByteTermsFacet.ByteEntry>(
						comparatorType.comparator(), size);
				for (TByteIntIterator it = facets.iterator(); it.hasNext();) {
					it.advance();
					ordered.add(new InternalByteTermsFacet.ByteEntry(it.key(), it.value()));
				}
				CacheRecycler.pushByteIntMap(facets);
				return new InternalByteTermsFacet(facetName, comparatorType, size, ordered, aggregator.missing(),
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
		private final TByteHashSet excluded;

		/**
		 * Instantiates a new aggregator value proc.
		 *
		 * @param facets the facets
		 * @param excluded the excluded
		 * @param script the script
		 */
		public AggregatorValueProc(TByteIntHashMap facets, Set<String> excluded, SearchScript script) {
			super(facets);
			if (excluded == null || excluded.isEmpty()) {
				this.excluded = null;
			} else {
				this.excluded = new TByteHashSet(excluded.size());
				for (String s : excluded) {
					this.excluded.add(Byte.parseByte(s));
				}
			}
			this.script = script;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.search.facet.terms.bytes.TermsByteFacetCollector.StaticAggregatorValueProc#onValue(int, byte)
		 */
		@Override
		public void onValue(int docId, byte value) {
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
					value = ((Number) scriptValue).byteValue();
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
	public static class StaticAggregatorValueProc implements ByteFieldData.ValueInDocProc, ByteFieldData.ValueProc {

		/** The facets. */
		private final TByteIntHashMap facets;

		/** The missing. */
		private int missing;

		/** The total. */
		private int total;

		/**
		 * Instantiates a new static aggregator value proc.
		 *
		 * @param facets the facets
		 */
		public StaticAggregatorValueProc(TByteIntHashMap facets) {
			this.facets = facets;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.bytes.ByteFieldData.ValueProc#onValue(byte)
		 */
		@Override
		public void onValue(byte value) {
			facets.putIfAbsent(value, 0);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.bytes.ByteFieldData.ValueInDocProc#onValue(int, byte)
		 */
		@Override
		public void onValue(int docId, byte value) {
			facets.adjustOrPutValue(value, 1, 1);
			total++;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.bytes.ByteFieldData.ValueInDocProc#onMissing(int)
		 */
		@Override
		public void onMissing(int docID) {
			missing++;
		}

		/**
		 * Facets.
		 *
		 * @return the t byte int hash map
		 */
		public final TByteIntHashMap facets() {
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
