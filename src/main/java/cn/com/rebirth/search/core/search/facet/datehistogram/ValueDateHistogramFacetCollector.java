/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ValueDateHistogramFacetCollector.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.datehistogram;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.joda.TimeZoneRounding;
import cn.com.rebirth.commons.trove.ExtTLongObjectHashMap;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.NumericFieldData;
import cn.com.rebirth.search.core.index.field.data.longs.LongFieldData;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class ValueDateHistogramFacetCollector.
 *
 * @author l.xue.nong
 */
public class ValueDateHistogramFacetCollector extends AbstractFacetCollector {

	/** The key index field name. */
	private final String keyIndexFieldName;

	/** The value index field name. */
	private final String valueIndexFieldName;

	/** The comparator type. */
	private final DateHistogramFacet.ComparatorType comparatorType;

	/** The field data cache. */
	private final FieldDataCache fieldDataCache;

	/** The key field data type. */
	private final FieldDataType keyFieldDataType;

	/** The key field data. */
	private LongFieldData keyFieldData;

	/** The value field data type. */
	private final FieldDataType valueFieldDataType;

	/** The histo proc. */
	private final DateHistogramProc histoProc;

	/**
	 * Instantiates a new value date histogram facet collector.
	 *
	 * @param facetName the facet name
	 * @param keyFieldName the key field name
	 * @param valueFieldName the value field name
	 * @param tzRounding the tz rounding
	 * @param comparatorType the comparator type
	 * @param context the context
	 */
	public ValueDateHistogramFacetCollector(String facetName, String keyFieldName, String valueFieldName,
			TimeZoneRounding tzRounding, DateHistogramFacet.ComparatorType comparatorType, SearchContext context) {
		super(facetName);
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

		FieldMapper mapper = context.smartNameFieldMapper(valueFieldName);
		if (mapper == null) {
			throw new FacetPhaseExecutionException(facetName, "No mapping found for value_field [" + valueFieldName
					+ "]");
		}
		valueIndexFieldName = mapper.names().indexName();
		valueFieldDataType = mapper.fieldDataType();

		this.histoProc = new DateHistogramProc(tzRounding);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		keyFieldData.forEachValueInDoc(doc, histoProc);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		keyFieldData = (LongFieldData) fieldDataCache.cache(keyFieldDataType, reader, keyIndexFieldName);
		histoProc.valueFieldData = (NumericFieldData) fieldDataCache.cache(valueFieldDataType, reader,
				valueIndexFieldName);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		return new InternalFullDateHistogramFacet(facetName, comparatorType, histoProc.entries, true);
	}

	/**
	 * The Class DateHistogramProc.
	 *
	 * @author l.xue.nong
	 */
	public static class DateHistogramProc implements LongFieldData.LongValueInDocProc {

		/** The entries. */
		final ExtTLongObjectHashMap<InternalFullDateHistogramFacet.FullEntry> entries = CacheRecycler
				.popLongObjectMap();

		/** The tz rounding. */
		private final TimeZoneRounding tzRounding;

		/** The value field data. */
		NumericFieldData valueFieldData;

		/** The value aggregator. */
		final ValueAggregator valueAggregator = new ValueAggregator();

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
			long time = tzRounding.calc(value);

			InternalFullDateHistogramFacet.FullEntry entry = entries.get(time);
			if (entry == null) {
				entry = new InternalFullDateHistogramFacet.FullEntry(time, 0, Double.POSITIVE_INFINITY,
						Double.NEGATIVE_INFINITY, 0, 0);
				entries.put(time, entry);
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
			InternalFullDateHistogramFacet.FullEntry entry;

			/* (non-Javadoc)
			 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData.DoubleValueInDocProc#onValue(int, double)
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