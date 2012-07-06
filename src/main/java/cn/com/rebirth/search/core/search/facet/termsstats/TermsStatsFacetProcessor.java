/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TermsStatsFacetProcessor.java 2012-7-6 14:30:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.termsstats;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetCollector;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.facet.FacetProcessor;
import cn.com.rebirth.search.core.search.facet.termsstats.doubles.TermsStatsDoubleFacetCollector;
import cn.com.rebirth.search.core.search.facet.termsstats.longs.TermsStatsLongFacetCollector;
import cn.com.rebirth.search.core.search.facet.termsstats.strings.TermsStatsStringFacetCollector;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class TermsStatsFacetProcessor.
 *
 * @author l.xue.nong
 */
public class TermsStatsFacetProcessor extends AbstractComponent implements FacetProcessor {

	/**
	 * Instantiates a new terms stats facet processor.
	 *
	 * @param settings the settings
	 */
	@Inject
	public TermsStatsFacetProcessor(Settings settings) {
		super(settings);
		InternalTermsStatsFacet.registerStreams();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#types()
	 */
	@Override
	public String[] types() {
		return new String[] { TermsStatsFacet.TYPE, "termsStats" };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#parse(java.lang.String, cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public FacetCollector parse(String facetName, XContentParser parser, SearchContext context) throws IOException {
		String keyField = null;
		String valueField = null;
		int size = 10;
		TermsStatsFacet.ComparatorType comparatorType = TermsStatsFacet.ComparatorType.COUNT;
		String scriptLang = null;
		String script = null;
		Map<String, Object> params = null;

		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("params".equals(currentFieldName)) {
					params = parser.map();
				}
			} else if (token.isValue()) {
				if ("key_field".equals(currentFieldName) || "keyField".equals(currentFieldName)) {
					keyField = parser.text();
				} else if ("value_field".equals(currentFieldName) || "valueField".equals(currentFieldName)) {
					valueField = parser.text();
				} else if ("script_field".equals(currentFieldName)) {
					script = parser.text();
				} else if ("value_script".equals(currentFieldName)) {
					script = parser.text();
				} else if ("size".equals(currentFieldName)) {
					size = parser.intValue();
				} else if ("all_terms".equals(currentFieldName) || "allTerms".equals(currentFieldName)) {
					if (parser.booleanValue()) {
						size = 0;
					}
				} else if ("order".equals(currentFieldName) || "comparator".equals(currentFieldName)) {
					comparatorType = TermsStatsFacet.ComparatorType.fromString(parser.text());
				} else if ("value_script".equals(currentFieldName)) {
					script = parser.text();
				} else if ("lang".equals(currentFieldName)) {
					scriptLang = parser.text();
				}
			}
		}

		if (keyField == null) {
			throw new FacetPhaseExecutionException(facetName, "[key_field] is required to be set for terms stats facet");
		}
		if (valueField == null && script == null) {
			throw new FacetPhaseExecutionException(facetName,
					"either [value_field] or [script] are required to be set for terms stats facet");
		}

		FieldMapper keyFieldMapper = context.smartNameFieldMapper(keyField);
		if (keyFieldMapper != null) {
			if (keyFieldMapper.fieldDataType() == FieldDataType.DefaultTypes.LONG) {
				return new TermsStatsLongFacetCollector(facetName, keyField, valueField, size, comparatorType, context,
						scriptLang, script, params);
			} else if (keyFieldMapper.fieldDataType() == FieldDataType.DefaultTypes.INT) {
				return new TermsStatsLongFacetCollector(facetName, keyField, valueField, size, comparatorType, context,
						scriptLang, script, params);
			} else if (keyFieldMapper.fieldDataType() == FieldDataType.DefaultTypes.SHORT) {
				return new TermsStatsLongFacetCollector(facetName, keyField, valueField, size, comparatorType, context,
						scriptLang, script, params);
			} else if (keyFieldMapper.fieldDataType() == FieldDataType.DefaultTypes.BYTE) {
				return new TermsStatsLongFacetCollector(facetName, keyField, valueField, size, comparatorType, context,
						scriptLang, script, params);
			} else if (keyFieldMapper.fieldDataType() == FieldDataType.DefaultTypes.DOUBLE) {
				return new TermsStatsDoubleFacetCollector(facetName, keyField, valueField, size, comparatorType,
						context, scriptLang, script, params);
			} else if (keyFieldMapper.fieldDataType() == FieldDataType.DefaultTypes.FLOAT) {
				return new TermsStatsDoubleFacetCollector(facetName, keyField, valueField, size, comparatorType,
						context, scriptLang, script, params);
			}
		}

		return new TermsStatsStringFacetCollector(facetName, keyField, valueField, size, comparatorType, context,
				scriptLang, script, params);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#reduce(java.lang.String, java.util.List)
	 */
	@Override
	public Facet reduce(String name, List<Facet> facets) {
		InternalTermsStatsFacet first = (InternalTermsStatsFacet) facets.get(0);
		return first.reduce(name, facets);
	}
}