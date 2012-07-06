/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TermsIpOrdinalsFacetCollector.java 2012-7-6 14:30:41 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.terms.ip;

import gnu.trove.set.hash.TLongHashSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.PriorityQueue;

import cn.com.rebirth.commons.collect.BoundedTreeSet;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.FieldData.OrdinalInDocProc;
import cn.com.rebirth.search.core.index.field.data.longs.LongFieldData;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.terms.TermsFacet;
import cn.com.rebirth.search.core.search.facet.terms.support.EntryPriorityQueue;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.ImmutableSet;

/**
 * The Class TermsIpOrdinalsFacetCollector.
 *
 * @author l.xue.nong
 */
public class TermsIpOrdinalsFacetCollector extends AbstractFacetCollector {

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

	/** The min count. */
	private final int minCount;

	/** The field data type. */
	private final FieldDataType fieldDataType;

	/** The field data. */
	private LongFieldData fieldData;

	/** The aggregators. */
	private final List<ReaderAggregator> aggregators;

	/** The current. */
	private ReaderAggregator current;

	/** The missing. */
	long missing;

	/** The total. */
	long total;

	/** The excluded. */
	private final TLongHashSet excluded;

	/**
	 * Instantiates a new terms ip ordinals facet collector.
	 *
	 * @param facetName the facet name
	 * @param fieldName the field name
	 * @param size the size
	 * @param comparatorType the comparator type
	 * @param allTerms the all terms
	 * @param context the context
	 * @param excluded the excluded
	 */
	public TermsIpOrdinalsFacetCollector(String facetName, String fieldName, int size,
			TermsFacet.ComparatorType comparatorType, boolean allTerms, SearchContext context,
			ImmutableSet<String> excluded) {
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

		if (excluded == null || excluded.isEmpty()) {
			this.excluded = null;
		} else {
			this.excluded = new TLongHashSet(excluded.size());
			for (String s : excluded) {
				this.excluded.add(Long.parseLong(s));
			}
		}

		if (allTerms) {
			minCount = -1;
		} else {
			minCount = 0;
		}

		this.aggregators = new ArrayList<ReaderAggregator>(context.searcher().subReaders().length);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		if (current != null) {
			missing += current.counts[0];
			total += current.total - current.counts[0];
			if (current.values.length > 1) {
				aggregators.add(current);
			}
		}
		fieldData = (LongFieldData) fieldDataCache.cache(fieldDataType, reader, indexFieldName);
		current = new ReaderAggregator(fieldData);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		fieldData.forEachOrdinalInDoc(doc, current);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		if (current != null) {
			missing += current.counts[0];
			total += current.total - current.counts[0];

			if (current.values.length > 1) {
				aggregators.add(current);
			}
		}

		AggregatorPriorityQueue queue = new AggregatorPriorityQueue(aggregators.size());

		for (ReaderAggregator aggregator : aggregators) {
			if (aggregator.nextPosition()) {
				queue.add(aggregator);
			}
		}

		if (size < EntryPriorityQueue.LIMIT) {

			EntryPriorityQueue ordered = new EntryPriorityQueue(size, comparatorType.comparator());

			while (queue.size() > 0) {
				ReaderAggregator agg = queue.top();
				long value = agg.current;
				int count = 0;
				do {
					count += agg.counts[agg.position];
					if (agg.nextPosition()) {
						agg = queue.updateTop();
					} else {

						queue.pop();
						agg = queue.top();
					}
				} while (agg != null && value == agg.current);

				if (count > minCount) {
					if (excluded == null || !excluded.contains(value)) {
						InternalIpTermsFacet.LongEntry entry = new InternalIpTermsFacet.LongEntry(value, count);
						ordered.insertWithOverflow(entry);
					}
				}
			}
			InternalIpTermsFacet.LongEntry[] list = new InternalIpTermsFacet.LongEntry[ordered.size()];
			for (int i = ordered.size() - 1; i >= 0; i--) {
				list[i] = (InternalIpTermsFacet.LongEntry) ordered.pop();
			}

			for (ReaderAggregator aggregator : aggregators) {
				CacheRecycler.pushIntArray(aggregator.counts);
			}

			return new InternalIpTermsFacet(facetName, comparatorType, size, Arrays.asList(list), missing, total);
		}

		BoundedTreeSet<InternalIpTermsFacet.LongEntry> ordered = new BoundedTreeSet<InternalIpTermsFacet.LongEntry>(
				comparatorType.comparator(), size);

		while (queue.size() > 0) {
			ReaderAggregator agg = queue.top();
			long value = agg.current;
			int count = 0;
			do {
				count += agg.counts[agg.position];
				if (agg.nextPosition()) {
					agg = queue.updateTop();
				} else {

					queue.pop();
					agg = queue.top();
				}
			} while (agg != null && value == agg.current);

			if (count > minCount) {
				if (excluded == null || !excluded.contains(value)) {
					InternalIpTermsFacet.LongEntry entry = new InternalIpTermsFacet.LongEntry(value, count);
					ordered.add(entry);
				}
			}
		}

		for (ReaderAggregator aggregator : aggregators) {
			CacheRecycler.pushIntArray(aggregator.counts);
		}

		return new InternalIpTermsFacet(facetName, comparatorType, size, ordered, missing, total);
	}

	/**
	 * The Class ReaderAggregator.
	 *
	 * @author l.xue.nong
	 */
	public static class ReaderAggregator implements OrdinalInDocProc {

		/** The values. */
		final long[] values;

		/** The counts. */
		final int[] counts;

		/** The position. */
		int position = 0;

		/** The current. */
		long current = Integer.MIN_VALUE;

		/** The total. */
		int total;

		/**
		 * Instantiates a new reader aggregator.
		 *
		 * @param fieldData the field data
		 */
		public ReaderAggregator(LongFieldData fieldData) {
			this.values = fieldData.values();
			this.counts = CacheRecycler.popIntArray(fieldData.values().length);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.FieldData.OrdinalInDocProc#onOrdinal(int, int)
		 */
		@Override
		public void onOrdinal(int docId, int ordinal) {
			counts[ordinal]++;
			total++;
		}

		/**
		 * Next position.
		 *
		 * @return true, if successful
		 */
		public boolean nextPosition() {
			if (++position >= values.length) {
				return false;
			}
			current = values[position];
			return true;
		}
	}

	/**
	 * The Class AggregatorPriorityQueue.
	 *
	 * @author l.xue.nong
	 */
	public static class AggregatorPriorityQueue extends PriorityQueue<ReaderAggregator> {

		/**
		 * Instantiates a new aggregator priority queue.
		 *
		 * @param size the size
		 */
		public AggregatorPriorityQueue(int size) {
			initialize(size);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.util.PriorityQueue#lessThan(java.lang.Object, java.lang.Object)
		 */
		@Override
		protected boolean lessThan(ReaderAggregator a, ReaderAggregator b) {
			return a.current < b.current;
		}
	}
}
