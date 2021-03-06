/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CountDateHistogramFacetCollector.java 2012-7-6 14:29:19 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.datehistogram;

import gnu.trove.map.hash.TLongLongHashMap;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.joda.TimeZoneRounding;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.longs.LongFieldData;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class CountDateHistogramFacetCollector.
 *
 * @author l.xue.nong
 */
public class CountDateHistogramFacetCollector extends AbstractFacetCollector {

	/** The index field name. */
	private final String indexFieldName;

	/** The comparator type. */
	private final DateHistogramFacet.ComparatorType comparatorType;

	/** The field data cache. */
	private final FieldDataCache fieldDataCache;

	/** The field data type. */
	private final FieldDataType fieldDataType;

	/** The field data. */
	private LongFieldData fieldData;

	/** The histo proc. */
	private final DateHistogramProc histoProc;

	/**
	 * Instantiates a new count date histogram facet collector.
	 *
	 * @param facetName the facet name
	 * @param fieldName the field name
	 * @param tzRounding the tz rounding
	 * @param comparatorType the comparator type
	 * @param context the context
	 */
	public CountDateHistogramFacetCollector(String facetName, String fieldName, TimeZoneRounding tzRounding,
			DateHistogramFacet.ComparatorType comparatorType, SearchContext context) {
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
		histoProc = new DateHistogramProc(tzRounding);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		fieldData.forEachValueInDoc(doc, histoProc);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		fieldData = (LongFieldData) fieldDataCache.cache(fieldDataType, reader, indexFieldName);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		return new InternalCountDateHistogramFacet(facetName, comparatorType, histoProc.counts(), true);
	}

	/**
	 * The Class DateHistogramProc.
	 *
	 * @author l.xue.nong
	 */
	public static class DateHistogramProc implements LongFieldData.LongValueInDocProc {

		/** The counts. */
		private final TLongLongHashMap counts = CacheRecycler.popLongLongMap();

		/** The tz rounding. */
		private final TimeZoneRounding tzRounding;

		/**
		 * Instantiates a new date histogram proc.
		 *
		 * @param tzRounding the tz rounding
		 */
		public DateHistogramProc(TimeZoneRounding tzRounding) {
			this.tzRounding = tzRounding;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData.LongValueInDocProc#onValue(int, long)
		 */
		@Override
		public void onValue(int docId, long value) {
			counts.adjustOrPutValue(tzRounding.calc(value), 1, 1);
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