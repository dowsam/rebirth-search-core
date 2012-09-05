/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RangeFacetProcessor.java 2012-7-6 14:29:19 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.range;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetCollector;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.facet.FacetProcessor;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.Lists;

/**
 * The Class RangeFacetProcessor.
 *
 * @author l.xue.nong
 */
public class RangeFacetProcessor extends AbstractComponent implements FacetProcessor {

	/**
	 * Instantiates a new range facet processor.
	 *
	 * @param settings the settings
	 */
	@Inject
	public RangeFacetProcessor(Settings settings) {
		super(settings);
		InternalRangeFacet.registerStreams();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#types()
	 */
	@Override
	public String[] types() {
		return new String[] { RangeFacet.TYPE };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#parse(java.lang.String, cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public FacetCollector parse(String facetName, XContentParser parser, SearchContext context) throws IOException {
		String keyField = null;
		String valueField = null;
		String scriptLang = null;
		String keyScript = null;
		String valueScript = null;
		Map<String, Object> params = null;
		XContentParser.Token token;
		String fieldName = null;
		List<RangeFacet.Entry> entries = Lists.newArrayList();

		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				fieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_ARRAY) {
				if (!"ranges".equals(fieldName)) {

					keyField = fieldName;
				}
				while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
					RangeFacet.Entry entry = new RangeFacet.Entry();
					while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
						if (token == XContentParser.Token.FIELD_NAME) {
							fieldName = parser.currentName();
						} else if (token == XContentParser.Token.VALUE_STRING) {
							if ("from".equals(fieldName)) {
								entry.fromAsString = parser.text();
							} else if ("to".equals(fieldName)) {
								entry.toAsString = parser.text();
							}
						} else if (token.isValue()) {
							if ("from".equals(fieldName)) {
								entry.from = parser.doubleValue();
							} else if ("to".equals(fieldName)) {
								entry.to = parser.doubleValue();
							}
						}
					}
					entries.add(entry);
				}
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
				} else if ("key_script".equals(fieldName) || "keyScript".equals(fieldName)) {
					keyScript = parser.text();
				} else if ("value_script".equals(fieldName) || "valueScript".equals(fieldName)) {
					valueScript = parser.text();
				} else if ("lang".equals(fieldName)) {
					scriptLang = parser.text();
				}
			}
		}

		if (entries.isEmpty()) {
			throw new FacetPhaseExecutionException(facetName, "no ranges defined for range facet");
		}

		RangeFacet.Entry[] rangeEntries = entries.toArray(new RangeFacet.Entry[entries.size()]);

		if (keyField != null) {
			FieldMapper mapper = context.smartNameFieldMapper(keyField);
			if (mapper == null) {
				throw new FacetPhaseExecutionException(facetName, "No mapping found for key_field [" + keyField + "]");
			}
			for (RangeFacet.Entry entry : rangeEntries) {
				if (entry.fromAsString != null) {
					entry.from = ((Number) mapper.valueFromString(entry.fromAsString)).doubleValue();
				}
				if (entry.toAsString != null) {
					entry.to = ((Number) mapper.valueFromString(entry.toAsString)).doubleValue();
				}
			}
		}

		if (keyScript != null && valueScript != null) {
			return new ScriptRangeFacetCollector(facetName, scriptLang, keyScript, valueScript, params, rangeEntries,
					context);
		}

		if (keyField == null) {
			throw new FacetPhaseExecutionException(facetName,
					"key field is required to be set for range facet, either using [field] or using [key_field]");
		}

		if (valueField == null || keyField.equals(valueField)) {
			return new RangeFacetCollector(facetName, keyField, rangeEntries, context);
		} else {

			return new KeyValueRangeFacetCollector(facetName, keyField, valueField, rangeEntries, context);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#reduce(java.lang.String, java.util.List)
	 */
	@Override
	public Facet reduce(String name, List<Facet> facets) {
		if (facets.size() == 1) {
			return facets.get(0);
		}
		InternalRangeFacet agg = null;
		for (Facet facet : facets) {
			InternalRangeFacet geoDistanceFacet = (InternalRangeFacet) facet;
			if (agg == null) {
				agg = geoDistanceFacet;
			} else {
				for (int i = 0; i < geoDistanceFacet.entries.length; i++) {
					RangeFacet.Entry aggEntry = agg.entries[i];
					RangeFacet.Entry currentEntry = geoDistanceFacet.entries[i];
					aggEntry.count += currentEntry.count;
					aggEntry.totalCount += currentEntry.totalCount;
					aggEntry.total += currentEntry.total;
					if (currentEntry.min < aggEntry.min) {
						aggEntry.min = currentEntry.min;
					}
					if (currentEntry.max > aggEntry.max) {
						aggEntry.max = currentEntry.max;
					}
				}
			}
		}
		return agg;
	}
}
