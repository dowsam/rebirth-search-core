/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ValueHistogramFacetCollector.java 2012-3-29 15:01:51 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.histogram.unbounded;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.trove.ExtTLongObjectHashMap;
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
 * The Class ValueHistogramFacetCollector.
 *
 * @author l.xue.nong
 */
public class ValueHistogramFacetCollector extends AbstractFacetCollector {

	
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
	 * Instantiates a new value histogram facet collector.
	 *
	 * @param facetName the facet name
	 * @param keyFieldName the key field name
	 * @param valueFieldName the value field name
	 * @param interval the interval
	 * @param comparatorType the comparator type
	 * @param context the context
	 */
	public ValueHistogramFacetCollector(String facetName, String keyFieldName, String valueFieldName, long interval,
			HistogramFacet.ComparatorType comparatorType, SearchContext context) {
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

		histoProc = new HistogramProc(interval);
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
		return new InternalFullHistogramFacet(facetName, comparatorType, histoProc.entries, true);
	}

	
	/**
	 * The Class HistogramProc.
	 *
	 * @author l.xue.nong
	 */
	public static class HistogramProc implements NumericFieldData.DoubleValueInDocProc {

		
		/** The interval. */
		final long interval;

		
		/** The entries. */
		final ExtTLongObjectHashMap<InternalFullHistogramFacet.FullEntry> entries = CacheRecycler.popLongObjectMap();

		
		/** The value field data. */
		NumericFieldData valueFieldData;

		
		/** The value aggregator. */
		final ValueAggregator valueAggregator = new ValueAggregator();

		
		/**
		 * Instantiates a new histogram proc.
		 *
		 * @param interval the interval
		 */
		public HistogramProc(long interval) {
			this.interval = interval;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.field.data.NumericFieldData.DoubleValueInDocProc#onValue(int, double)
		 */
		@Override
		public void onValue(int docId, double value) {
			long bucket = FullHistogramFacetCollector.bucket(value, interval);
			InternalFullHistogramFacet.FullEntry entry = entries.get(bucket);
			if (entry == null) {
				entry = new InternalFullHistogramFacet.FullEntry(bucket, 0, Double.POSITIVE_INFINITY,
						Double.NEGATIVE_INFINITY, 0, 0);
				entries.put(bucket, entry);
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
			InternalFullHistogramFacet.FullEntry entry;

			
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