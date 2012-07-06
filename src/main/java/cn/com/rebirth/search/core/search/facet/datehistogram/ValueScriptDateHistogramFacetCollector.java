/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ValueScriptDateHistogramFacetCollector.java 2012-7-6 14:29:15 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.datehistogram;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.commons.joda.TimeZoneRounding;
import cn.com.rebirth.commons.trove.ExtTLongObjectHashMap;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.longs.LongFieldData;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class ValueScriptDateHistogramFacetCollector.
 *
 * @author l.xue.nong
 */
public class ValueScriptDateHistogramFacetCollector extends AbstractFacetCollector {

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

	/** The value script. */
	private final SearchScript valueScript;

	/** The histo proc. */
	private final DateHistogramProc histoProc;

	/**
	 * Instantiates a new value script date histogram facet collector.
	 *
	 * @param facetName the facet name
	 * @param fieldName the field name
	 * @param scriptLang the script lang
	 * @param valueScript the value script
	 * @param params the params
	 * @param tzRounding the tz rounding
	 * @param comparatorType the comparator type
	 * @param context the context
	 */
	public ValueScriptDateHistogramFacetCollector(String facetName, String fieldName, String scriptLang,
			String valueScript, Map<String, Object> params, TimeZoneRounding tzRounding,
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

		this.valueScript = context.scriptService().search(context.lookup(), scriptLang, valueScript, params);

		FieldMapper mapper = smartMappers.mapper();

		indexFieldName = mapper.names().indexName();
		fieldDataType = mapper.fieldDataType();

		histoProc = new DateHistogramProc(tzRounding, this.valueScript);
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
		fieldData = (LongFieldData) fieldDataCache.cache(fieldDataType, reader, indexFieldName);
		valueScript.setNextReader(reader);
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

		/** The tz rounding. */
		private final TimeZoneRounding tzRounding;

		/** The value script. */
		protected final SearchScript valueScript;

		/** The entries. */
		final ExtTLongObjectHashMap<InternalFullDateHistogramFacet.FullEntry> entries = CacheRecycler
				.popLongObjectMap();

		/**
		 * Instantiates a new date histogram proc.
		 *
		 * @param tzRounding the tz rounding
		 * @param valueScript the value script
		 */
		public DateHistogramProc(TimeZoneRounding tzRounding, SearchScript valueScript) {
			this.tzRounding = tzRounding;
			this.valueScript = valueScript;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData.LongValueInDocProc#onValue(int, long)
		 */
		@Override
		public void onValue(int docId, long value) {
			valueScript.setNextDocId(docId);
			long time = tzRounding.calc(value);
			double scriptValue = valueScript.runAsDouble();

			InternalFullDateHistogramFacet.FullEntry entry = entries.get(time);
			if (entry == null) {
				entry = new InternalFullDateHistogramFacet.FullEntry(time, 1, scriptValue, scriptValue, 1, scriptValue);
				entries.put(time, entry);
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