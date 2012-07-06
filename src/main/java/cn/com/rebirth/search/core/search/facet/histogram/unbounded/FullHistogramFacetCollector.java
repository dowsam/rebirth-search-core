/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FullHistogramFacetCollector.java 2012-3-29 15:01:30 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.histogram.unbounded;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.trove.ExtTLongObjectHashMap;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.NumericFieldData;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class FullHistogramFacetCollector.
 *
 * @author l.xue.nong
 */
public class FullHistogramFacetCollector extends AbstractFacetCollector {

	
	/** The index field name. */
	private final String indexFieldName;

	
	/** The comparator type. */
	private final HistogramFacet.ComparatorType comparatorType;

	
	/** The field data cache. */
	private final FieldDataCache fieldDataCache;

	
	/** The field data type. */
	private final FieldDataType fieldDataType;

	
	/** The field data. */
	private NumericFieldData fieldData;

	
	/** The histo proc. */
	private final HistogramProc histoProc;

	
	/**
	 * Instantiates a new full histogram facet collector.
	 *
	 * @param facetName the facet name
	 * @param fieldName the field name
	 * @param interval the interval
	 * @param comparatorType the comparator type
	 * @param context the context
	 */
	public FullHistogramFacetCollector(String facetName, String fieldName, long interval,
			HistogramFacet.ComparatorType comparatorType, SearchContext context) {
		super(facetName);
		this.comparatorType = comparatorType;
		this.fieldDataCache = context.fieldDataCache();

		MapperService.SmartNameFieldMappers smartMappers = context.smartFieldMappers(fieldName);
		if (smartMappers == null || !smartMappers.hasMapper()) {
			throw new FacetPhaseExecutionException(facetName, "No mapping found for field [" + fieldName + "]");
		}

		
		if (smartMappers.explicitTypeInNameWithDocMapper()) {
			setFilter(context.filterCache().cache(smartMappers.docMapper().typeFilter()));
		}

		FieldMapper mapper = smartMappers.mapper();

		indexFieldName = mapper.names().indexName();
		fieldDataType = mapper.fieldDataType();

		histoProc = new HistogramProc(interval);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		fieldData.forEachValueInDoc(doc, histoProc);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		fieldData = (NumericFieldData) fieldDataCache.cache(fieldDataType, reader, indexFieldName);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		return new InternalFullHistogramFacet(facetName, comparatorType, histoProc.entries, true);
	}

	
	/**
	 * Bucket.
	 *
	 * @param value the value
	 * @param interval the interval
	 * @return the long
	 */
	public static long bucket(double value, long interval) {
		return (((long) (value / interval)) * interval);
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
			long bucket = bucket(value, interval);
			InternalFullHistogramFacet.FullEntry entry = entries.get(bucket);
			if (entry == null) {
				entry = new InternalFullHistogramFacet.FullEntry(bucket, 1, value, value, 1, value);
				entries.put(bucket, entry);
			} else {
				entry.count++;
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