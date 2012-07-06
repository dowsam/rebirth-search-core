/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BoundedValueHistogramFacetCollector.java 2012-3-29 15:01:38 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.histogram.bounded;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.NumericFieldData;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class BoundedValueHistogramFacetCollector.
 *
 * @author l.xue.nong
 */
public class BoundedValueHistogramFacetCollector extends AbstractFacetCollector {

	
	/** The key index field name. */
	private final String keyIndexFieldName;

	
	/** The value index field name. */
	private final String valueIndexFieldName;

	
	/** The interval. */
	private final long interval;

	
	/** The comparator type. */
	private final HistogramFacet.ComparatorType comparatorType;

	
	/** The field data cache. */
	private final FieldDataCache fieldDataCache;

	
	/** The key field data type. */
	private final FieldDataType keyFieldDataType;

	
	/** The key field data. */
	private NumericFieldData keyFieldData;

	
	/** The value field data type. */
	private final FieldDataType valueFieldDataType;

	
	/** The histo proc. */
	private final HistogramProc histoProc;

	
	/**
	 * Instantiates a new bounded value histogram facet collector.
	 *
	 * @param facetName the facet name
	 * @param keyFieldName the key field name
	 * @param valueFieldName the value field name
	 * @param interval the interval
	 * @param from the from
	 * @param to the to
	 * @param comparatorType the comparator type
	 * @param context the context
	 */
	public BoundedValueHistogramFacetCollector(String facetName, String keyFieldName, String valueFieldName,
			long interval, long from, long to, HistogramFacet.ComparatorType comparatorType, SearchContext context) {
		super(facetName);
		this.interval = interval;
		this.comparatorType = comparatorType;
		this.fieldDataCache = context.fieldDataCache();

		MapperService.SmartNameFieldMappers smartMappers = context.smartFieldMappers(keyFieldName);
		if (smartMappers == null || !smartMappers.hasMapper()) {
			throw new FacetPhaseExecutionException(facetName, "No mapping found for field [" + keyFieldName + "]");
		}

		
		if (smartMappers.explicitTypeInNameWithDocMapper()) {
			setFilter(context.filterCache().cache(smartMappers.docMapper().typeFilter()));
		}

		keyIndexFieldName = smartMappers.mapper().names().indexName();
		keyFieldDataType = smartMappers.mapper().fieldDataType();

		smartMappers = context.smartFieldMappers(valueFieldName);
		if (smartMappers == null || !smartMappers.hasMapper()) {
			throw new FacetPhaseExecutionException(facetName, "No mapping found for value_field [" + valueFieldName
					+ "]");
		}
		valueIndexFieldName = smartMappers.mapper().names().indexName();
		valueFieldDataType = smartMappers.mapper().fieldDataType();

		long normalizedFrom = (((long) ((double) from / interval)) * interval);
		long normalizedTo = (((long) ((double) to / interval)) * interval);
		if ((to % interval) != 0) {
			normalizedTo += interval;
		}
		long offset = -normalizedFrom;
		int size = (int) ((normalizedTo - normalizedFrom) / interval);

		histoProc = new HistogramProc(from, to, interval, offset, size);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		keyFieldData.forEachValueInDoc(doc, histoProc);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		keyFieldData = (NumericFieldData) fieldDataCache.cache(keyFieldDataType, reader, keyIndexFieldName);
		histoProc.valueFieldData = (NumericFieldData) fieldDataCache.cache(valueFieldDataType, reader,
				valueIndexFieldName);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		return new InternalBoundedFullHistogramFacet(facetName, comparatorType, interval, -histoProc.offset,
				histoProc.size, histoProc.entries, true);
	}

	
	/**
	 * The Class HistogramProc.
	 *
	 * @author l.xue.nong
	 */
	public static class HistogramProc implements NumericFieldData.LongValueInDocProc {

		
		/** The from. */
		final long from;

		
		/** The to. */
		final long to;

		
		/** The interval. */
		final long interval;

		
		/** The offset. */
		final long offset;

		
		/** The size. */
		final int size;

		
		/** The entries. */
		final Object[] entries;

		
		/** The value field data. */
		NumericFieldData valueFieldData;

		
		/** The value aggregator. */
		final ValueAggregator valueAggregator = new ValueAggregator();

		
		/**
		 * Instantiates a new histogram proc.
		 *
		 * @param from the from
		 * @param to the to
		 * @param interval the interval
		 * @param offset the offset
		 * @param size the size
		 */
		public HistogramProc(long from, long to, long interval, long offset, int size) {
			this.from = from;
			this.to = to;
			this.interval = interval;
			this.offset = offset;
			this.size = size;
			this.entries = CacheRecycler.popObjectArray(size);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.field.data.NumericFieldData.LongValueInDocProc#onValue(int, long)
		 */
		@Override
		public void onValue(int docId, long value) {
			if (value <= from || value > to) { 
				return;
			}
			int index = ((int) ((value + offset) / interval));
			InternalBoundedFullHistogramFacet.FullEntry entry = (InternalBoundedFullHistogramFacet.FullEntry) entries[index];
			if (entry == null) {
				entry = new InternalBoundedFullHistogramFacet.FullEntry(index, 0, Double.POSITIVE_INFINITY,
						Double.NEGATIVE_INFINITY, 0, 0);
				entries[index] = entry;
			}
			entry.count++;
			valueAggregator.entry = entry;
			valueFieldData.forEachValueInDoc(docId, valueAggregator);
		}

		
		/**
		 * The Class ValueAggregator.
		 *
		 * @author l.xue.nong
		 */
		public static class ValueAggregator implements NumericFieldData.DoubleValueInDocProc {

			
			/** The entry. */
			InternalBoundedFullHistogramFacet.FullEntry entry;

			
			/* (non-Javadoc)
			 * @see cn.com.summall.search.core.index.field.data.NumericFieldData.DoubleValueInDocProc#onValue(int, double)
			 */
			@Override
			public void onValue(int docId, double value) {
				entry.totalCount++;
				entry.total += value;
				if (value < entry.min) {
					entry.min = value;
				}
				if (value > entry.max) {
					entry.max = value;
				}
			}
		}
	}
}