/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ValueScriptHistogramFacetCollector.java 2012-7-6 14:30:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.histogram.unbounded;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.commons.trove.ExtTLongObjectHashMap;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.NumericFieldData;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class ValueScriptHistogramFacetCollector.
 *
 * @author l.xue.nong
 */
public class ValueScriptHistogramFacetCollector extends AbstractFacetCollector {

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

	/** The value script. */
	private final SearchScript valueScript;

	/** The histo proc. */
	private final HistogramProc histoProc;

	/**
	 * Instantiates a new value script histogram facet collector.
	 *
	 * @param facetName the facet name
	 * @param fieldName the field name
	 * @param scriptLang the script lang
	 * @param valueScript the value script
	 * @param params the params
	 * @param interval the interval
	 * @param comparatorType the comparator type
	 * @param context the context
	 */
	public ValueScriptHistogramFacetCollector(String facetName, String fieldName, String scriptLang,
			String valueScript, Map<String, Object> params, long interval,
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

		this.valueScript = context.scriptService().search(context.lookup(), scriptLang, valueScript, params);

		FieldMapper mapper = smartMappers.mapper();

		indexFieldName = mapper.names().indexName();
		fieldDataType = mapper.fieldDataType();

		histoProc = new HistogramProc(interval, this.valueScript);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		fieldData.forEachValueInDoc(doc, histoProc);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#setScorer(org.apache.lucene.search.Scorer)
	 */
	@Override
	public void setScorer(Scorer scorer) throws IOException {
		valueScript.setScorer(scorer);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		fieldData = (NumericFieldData) fieldDataCache.cache(fieldDataType, reader, indexFieldName);
		valueScript.setNextReader(reader);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetCollector#facet()
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
		private final long interval;

		/** The value script. */
		private final SearchScript valueScript;

		/** The entries. */
		final ExtTLongObjectHashMap<InternalFullHistogramFacet.FullEntry> entries = CacheRecycler.popLongObjectMap();

		/**
		 * Instantiates a new histogram proc.
		 *
		 * @param interval the interval
		 * @param valueScript the value script
		 */
		public HistogramProc(long interval, SearchScript valueScript) {
			this.interval = interval;
			this.valueScript = valueScript;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData.DoubleValueInDocProc#onValue(int, double)
		 */
		@Override
		public void onValue(int docId, double value) {
			valueScript.setNextDocId(docId);
			long bucket = bucket(value, interval);
			double scriptValue = valueScript.runAsDouble();

			InternalFullHistogramFacet.FullEntry entry = entries.get(bucket);
			if (entry == null) {
				entry = new InternalFullHistogramFacet.FullEntry(bucket, 1, scriptValue, scriptValue, 1, scriptValue);
				entries.put(bucket, entry);
			} else {
				entry.count++;
				entry.totalCount++;
				entry.total += scriptValue;
				if (scriptValue < entry.min) {
					entry.min = scriptValue;
				}
				if (scriptValue > entry.max) {
					entry.max = scriptValue;
				}
			}
		}
	}
}