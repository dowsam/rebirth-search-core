/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CountHistogramFacetCollector.java 2012-3-29 15:02:06 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.histogram.unbounded;

import gnu.trove.map.hash.TLongLongHashMap;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

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
 * The Class CountHistogramFacetCollector.
 *
 * @author l.xue.nong
 */
public class CountHistogramFacetCollector extends AbstractFacetCollector {

	
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
	 * Instantiates a new count histogram facet collector.
	 *
	 * @param facetName the facet name
	 * @param fieldName the field name
	 * @param interval the interval
	 * @param comparatorType the comparator type
	 * @param context the context
	 */
	public CountHistogramFacetCollector(String facetName, String fieldName, long interval,
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
		return new InternalCountHistogramFacet(facetName, comparatorType, histoProc.counts(), true);
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
		private final long interval;

		
		/** The counts. */
		private final TLongLongHashMap counts = CacheRecycler.popLongLongMap();

		
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
			counts.adjustOrPutValue(bucket, 1, 1);
		}

		
		/**
		 * Counts.
		 *
		 * @return the t long long hash map
		 */
		public TLongLongHashMap counts() {
			return counts;
		}
	}
}