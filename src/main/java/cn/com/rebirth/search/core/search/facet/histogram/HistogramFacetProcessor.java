/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core HistogramFacetProcessor.java 2012-7-6 14:29:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.histogram;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetCollector;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.facet.FacetProcessor;
import cn.com.rebirth.search.core.search.facet.histogram.bounded.BoundedCountHistogramFacetCollector;
import cn.com.rebirth.search.core.search.facet.histogram.bounded.BoundedValueHistogramFacetCollector;
import cn.com.rebirth.search.core.search.facet.histogram.bounded.BoundedValueScriptHistogramFacetCollector;
import cn.com.rebirth.search.core.search.facet.histogram.unbounded.CountHistogramFacetCollector;
import cn.com.rebirth.search.core.search.facet.histogram.unbounded.FullHistogramFacetCollector;
import cn.com.rebirth.search.core.search.facet.histogram.unbounded.ScriptHistogramFacetCollector;
import cn.com.rebirth.search.core.search.facet.histogram.unbounded.ValueHistogramFacetCollector;
import cn.com.rebirth.search.core.search.facet.histogram.unbounded.ValueScriptHistogramFacetCollector;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class HistogramFacetProcessor.
 *
 * @author l.xue.nong
 */
public class HistogramFacetProcessor extends AbstractComponent implements FacetProcessor {

	/**
	 * Instantiates a new histogram facet processor.
	 *
	 * @param settings the settings
	 */
	@Inject
	public HistogramFacetProcessor(Settings settings) {
		super(settings);
		InternalHistogramFacet.registerStreams();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#types()
	 */
	@Override
	public String[] types() {
		return new String[] { HistogramFacet.TYPE };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#parse(java.lang.String, cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public FacetCollector parse(String facetName, XContentParser parser, SearchContext context) throws IOException {
		String keyField = null;
		String valueField = null;
		String keyScript = null;
		String valueScript = null;
		String scriptLang = null;
		Map<String, Object> params = null;
		long interval = 0;
		HistogramFacet.ComparatorType comparatorType = HistogramFacet.ComparatorType.KEY;
		XContentParser.Token token;
		String fieldName = null;
		String sFrom = null;
		String sTo = null;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				fieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("params".equals(fieldName)) {
					params = parser.map();
				}
			} else if (token.isValue()) {
				if ("field".equals(fieldName)) {
					keyField = parser.text();
				} else if ("key_field".equals(fieldName) || "keyField".equals(fieldName)) {
					keyField = parser.text();
				} else if ("value_field".equals(fieldName) || "valueField".equals(fieldName)) {
					valueField = parser.text();
				} else if ("interval".equals(fieldName)) {
					interval = parser.longValue();
				} else if ("from".equals(fieldName)) {
					sFrom = parser.text();
				} else if ("to".equals(fieldName)) {
					sTo = parser.text();
				} else if ("time_interval".equals(fieldName)) {
					interval = TimeValue.parseTimeValue(parser.text(), null).millis();
				} else if ("key_script".equals(fieldName) || "keyScript".equals(fieldName)) {
					keyScript = parser.text();
				} else if ("value_script".equals(fieldName) || "valueScript".equals(fieldName)) {
					valueScript = parser.text();
				} else if ("order".equals(fieldName) || "comparator".equals(fieldName)) {
					comparatorType = HistogramFacet.ComparatorType.fromString(parser.text());
				} else if ("lang".equals(fieldName)) {
					scriptLang = parser.text();
				}
			}
		}

		if (keyScript != null && valueScript != null) {
			return new ScriptHistogramFacetCollector(facetName, scriptLang, keyScript, valueScript, params, interval,
					comparatorType, context);
		}

		if (keyField == null) {
			throw new FacetPhaseExecutionException(facetName,
					"key field is required to be set for histogram facet, either using [field] or using [key_field]");
		}

		if (interval <= 0) {
			throw new FacetPhaseExecutionException(facetName, "[interval] is required to be set for histogram facet");
		}

		if (sFrom != null && sTo != null && keyField != null) {
			FieldMapper mapper = context.smartNameFieldMapper(keyField);
			if (mapper == null) {
				throw new FacetPhaseExecutionException(facetName, "No mapping found for key_field [" + keyField + "]");
			}
			long from = ((Number) mapper.valueFromString(sFrom)).longValue();
			long to = ((Number) mapper.valueFromString(sTo)).longValue();

			if (valueField != null) {
				return new BoundedValueHistogramFacetCollector(facetName, keyField, valueField, interval, from, to,
						comparatorType, context);
			} else if (valueScript != null) {
				return new BoundedValueScriptHistogramFacetCollector(facetName, keyField, scriptLang, valueScript,
						params, interval, from, to, comparatorType, context);
			} else {
				return new BoundedCountHistogramFacetCollector(facetName, keyField, interval, from, to, comparatorType,
						context);
			}
		}

		if (valueScript != null) {
			return new ValueScriptHistogramFacetCollector(facetName, keyField, scriptLang, valueScript, params,
					interval, comparatorType, context);
		} else if (valueField == null) {
			return new CountHistogramFacetCollector(facetName, keyField, interval, comparatorType, context);
		} else if (keyField.equals(valueField)) {
			return new FullHistogramFacetCollector(facetName, keyField, interval, comparatorType, context);
		} else {

			return new ValueHistogramFacetCollector(facetName, keyField, valueField, interval, comparatorType, context);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#reduce(java.lang.String, java.util.List)
	 */
	@Override
	public Facet reduce(String name, List<Facet> facets) {
		InternalHistogramFacet first = (InternalHistogramFacet) facets.get(0);
		return first.reduce(name, facets);
	}
}
