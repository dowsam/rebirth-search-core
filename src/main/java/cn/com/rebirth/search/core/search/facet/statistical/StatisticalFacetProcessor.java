/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StatisticalFacetProcessor.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.statistical;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetCollector;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.facet.FacetProcessor;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.Lists;

/**
 * The Class StatisticalFacetProcessor.
 *
 * @author l.xue.nong
 */
public class StatisticalFacetProcessor extends AbstractComponent implements FacetProcessor {

	/**
	 * Instantiates a new statistical facet processor.
	 *
	 * @param settings the settings
	 */
	@Inject
	public StatisticalFacetProcessor(Settings settings) {
		super(settings);
		InternalStatisticalFacet.registerStreams();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#types()
	 */
	@Override
	public String[] types() {
		return new String[] { StatisticalFacet.TYPE };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#parse(java.lang.String, cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public FacetCollector parse(String facetName, XContentParser parser, SearchContext context) throws IOException {
		String field = null;
		String[] fieldsNames = null;

		String script = null;
		String scriptLang = null;
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
			} else if (token == XContentParser.Token.START_ARRAY) {
				if ("fields".equals(currentFieldName)) {
					List<String> fields = Lists.newArrayListWithCapacity(4);
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						fields.add(parser.text());
					}
					fieldsNames = fields.toArray(new String[fields.size()]);
				}
			} else if (token.isValue()) {
				if ("field".equals(currentFieldName)) {
					field = parser.text();
				} else if ("script".equals(currentFieldName)) {
					script = parser.text();
				} else if ("lang".equals(currentFieldName)) {
					scriptLang = parser.text();
				}
			}
		}
		if (fieldsNames != null) {
			return new StatisticalFieldsFacetCollector(facetName, fieldsNames, context);
		}
		if (script == null && field == null) {
			throw new FacetPhaseExecutionException(facetName,
					"statistical facet requires either [script] or [field] to be set");
		}
		if (field != null) {
			return new StatisticalFacetCollector(facetName, field, context);
		} else {
			return new ScriptStatisticalFacetCollector(facetName, scriptLang, script, params, context);
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
		double min = Double.NaN;
		double max = Double.NaN;
		double total = 0;
		double sumOfSquares = 0;
		long count = 0;

		for (Facet facet : facets) {
			if (!facet.name().equals(name)) {
				continue;
			}
			InternalStatisticalFacet statsFacet = (InternalStatisticalFacet) facet;
			if (statsFacet.min() < min || Double.isNaN(min)) {
				min = statsFacet.min();
			}
			if (statsFacet.max() > max || Double.isNaN(max)) {
				max = statsFacet.max();
			}
			total += statsFacet.total();
			sumOfSquares += statsFacet.sumOfSquares();
			count += statsFacet.count();
		}

		return new InternalStatisticalFacet(name, min, max, total, sumOfSquares, count);
	}
}
