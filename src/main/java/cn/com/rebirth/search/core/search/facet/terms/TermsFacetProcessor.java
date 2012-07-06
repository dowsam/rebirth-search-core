/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TermsFacetProcessor.java 2012-7-6 14:29:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.terms;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import cn.com.rebirth.commons.regex.Regex;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.ip.IpFieldMapper;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetCollector;
import cn.com.rebirth.search.core.search.facet.FacetProcessor;
import cn.com.rebirth.search.core.search.facet.terms.bytes.TermsByteFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.bytes.TermsByteOrdinalsFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.doubles.TermsDoubleFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.doubles.TermsDoubleOrdinalsFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.floats.TermsFloatFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.floats.TermsFloatOrdinalsFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.index.IndexNameFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.ints.TermsIntFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.ints.TermsIntOrdinalsFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.ip.TermsIpFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.ip.TermsIpOrdinalsFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.longs.TermsLongFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.longs.TermsLongOrdinalsFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.shorts.TermsShortFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.shorts.TermsShortOrdinalsFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.strings.FieldsTermsStringFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.strings.ScriptTermsStringFieldFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.strings.TermsStringFacetCollector;
import cn.com.rebirth.search.core.search.facet.terms.strings.TermsStringOrdinalsFacetCollector;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * The Class TermsFacetProcessor.
 *
 * @author l.xue.nong
 */
public class TermsFacetProcessor extends AbstractComponent implements FacetProcessor {

	/**
	 * Instantiates a new terms facet processor.
	 *
	 * @param settings the settings
	 */
	@Inject
	public TermsFacetProcessor(Settings settings) {
		super(settings);
		InternalTermsFacet.registerStreams();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#types()
	 */
	@Override
	public String[] types() {
		return new String[] { TermsFacet.TYPE };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#parse(java.lang.String, cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public FacetCollector parse(String facetName, XContentParser parser, SearchContext context) throws IOException {
		String field = null;
		int size = 10;

		String[] fieldsNames = null;
		ImmutableSet<String> excluded = ImmutableSet.of();
		String regex = null;
		String regexFlags = null;
		TermsFacet.ComparatorType comparatorType = TermsFacet.ComparatorType.COUNT;
		String scriptLang = null;
		String script = null;
		Map<String, Object> params = null;
		boolean allTerms = false;
		String executionHint = null;

		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("params".equals(currentFieldName)) {
					params = parser.map();
				}
			} else if (token == XContentParser.Token.START_ARRAY) {
				if ("exclude".equals(currentFieldName)) {
					ImmutableSet.Builder<String> builder = ImmutableSet.builder();
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						builder.add(parser.text());
					}
					excluded = builder.build();
				} else if ("fields".equals(currentFieldName)) {
					List<String> fields = Lists.newArrayListWithCapacity(4);
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						fields.add(parser.text());
					}
					fieldsNames = fields.toArray(new String[fields.size()]);
				}
			} else if (token.isValue()) {
				if ("field".equals(currentFieldName)) {
					field = parser.text();
				} else if ("script_field".equals(currentFieldName)) {
					script = parser.text();
				} else if ("size".equals(currentFieldName)) {
					size = parser.intValue();
				} else if ("all_terms".equals(currentFieldName) || "allTerms".equals(currentFieldName)) {
					allTerms = parser.booleanValue();
				} else if ("regex".equals(currentFieldName)) {
					regex = parser.text();
				} else if ("regex_flags".equals(currentFieldName) || "regexFlags".equals(currentFieldName)) {
					regexFlags = parser.text();
				} else if ("order".equals(currentFieldName) || "comparator".equals(currentFieldName)) {
					comparatorType = TermsFacet.ComparatorType.fromString(parser.text());
				} else if ("script".equals(currentFieldName)) {
					script = parser.text();
				} else if ("lang".equals(currentFieldName)) {
					scriptLang = parser.text();
				} else if ("execution_hint".equals(currentFieldName) || "executionHint".equals(currentFieldName)) {
					executionHint = parser.textOrNull();
				}
			}
		}

		if ("_index".equals(field)) {
			return new IndexNameFacetCollector(facetName, context.shardTarget().index(), comparatorType, size);
		}

		Pattern pattern = null;
		if (regex != null) {
			pattern = Regex.compile(regex, regexFlags);
		}
		if (fieldsNames != null) {
			return new FieldsTermsStringFacetCollector(facetName, fieldsNames, size, comparatorType, allTerms, context,
					excluded, pattern, scriptLang, script, params);
		}
		if (field == null && fieldsNames == null && script != null) {
			return new ScriptTermsStringFieldFacetCollector(facetName, size, comparatorType, context, excluded,
					pattern, scriptLang, script, params);
		}

		FieldMapper fieldMapper = context.smartNameFieldMapper(field);
		if (fieldMapper != null) {
			if (fieldMapper instanceof IpFieldMapper) {
				if (script != null || "map".equals(executionHint)) {
					return new TermsIpFacetCollector(facetName, field, size, comparatorType, allTerms, context,
							scriptLang, script, params);
				} else {
					return new TermsIpOrdinalsFacetCollector(facetName, field, size, comparatorType, allTerms, context,
							null);
				}
			} else if (fieldMapper.fieldDataType() == FieldDataType.DefaultTypes.LONG) {
				if (script != null || "map".equals(executionHint)) {
					return new TermsLongFacetCollector(facetName, field, size, comparatorType, allTerms, context,
							excluded, scriptLang, script, params);
				} else {
					return new TermsLongOrdinalsFacetCollector(facetName, field, size, comparatorType, allTerms,
							context, excluded);
				}
			} else if (fieldMapper.fieldDataType() == FieldDataType.DefaultTypes.DOUBLE) {
				if (script != null) {
					return new TermsDoubleFacetCollector(facetName, field, size, comparatorType, allTerms, context,
							excluded, scriptLang, script, params);
				} else {
					return new TermsDoubleOrdinalsFacetCollector(facetName, field, size, comparatorType, allTerms,
							context, excluded);
				}
			} else if (fieldMapper.fieldDataType() == FieldDataType.DefaultTypes.INT) {
				if (script != null || "map".equals(executionHint)) {
					return new TermsIntFacetCollector(facetName, field, size, comparatorType, allTerms, context,
							excluded, scriptLang, script, params);
				} else {
					return new TermsIntOrdinalsFacetCollector(facetName, field, size, comparatorType, allTerms,
							context, excluded);
				}
			} else if (fieldMapper.fieldDataType() == FieldDataType.DefaultTypes.FLOAT) {
				if (script != null || "map".equals(executionHint)) {
					return new TermsFloatFacetCollector(facetName, field, size, comparatorType, allTerms, context,
							excluded, scriptLang, script, params);
				} else {
					return new TermsFloatOrdinalsFacetCollector(facetName, field, size, comparatorType, allTerms,
							context, excluded);
				}
			} else if (fieldMapper.fieldDataType() == FieldDataType.DefaultTypes.SHORT) {
				if (script != null || "map".equals(executionHint)) {
					return new TermsShortFacetCollector(facetName, field, size, comparatorType, allTerms, context,
							excluded, scriptLang, script, params);
				} else {
					return new TermsShortOrdinalsFacetCollector(facetName, field, size, comparatorType, allTerms,
							context, excluded);
				}
			} else if (fieldMapper.fieldDataType() == FieldDataType.DefaultTypes.BYTE) {
				if (script != null || "map".equals(executionHint)) {
					return new TermsByteFacetCollector(facetName, field, size, comparatorType, allTerms, context,
							excluded, scriptLang, script, params);
				} else {
					return new TermsByteOrdinalsFacetCollector(facetName, field, size, comparatorType, allTerms,
							context, excluded);
				}
			} else if (fieldMapper.fieldDataType() == FieldDataType.DefaultTypes.STRING) {
				if (script == null && !"map".equals(executionHint)) {
					return new TermsStringOrdinalsFacetCollector(facetName, field, size, comparatorType, allTerms,
							context, excluded, pattern);
				}
			}
		}
		return new TermsStringFacetCollector(facetName, field, size, comparatorType, allTerms, context, excluded,
				pattern, scriptLang, script, params);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#reduce(java.lang.String, java.util.List)
	 */
	@Override
	public Facet reduce(String name, List<Facet> facets) {
		InternalTermsFacet first = (InternalTermsFacet) facets.get(0);
		return first.reduce(name, facets);
	}
}
