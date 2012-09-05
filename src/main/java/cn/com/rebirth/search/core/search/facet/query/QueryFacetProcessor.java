/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QueryFacetProcessor.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.query;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetCollector;
import cn.com.rebirth.search.core.search.facet.FacetProcessor;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class QueryFacetProcessor.
 *
 * @author l.xue.nong
 */
public class QueryFacetProcessor extends AbstractComponent implements FacetProcessor {

	/**
	 * Instantiates a new query facet processor.
	 *
	 * @param settings the settings
	 */
	@Inject
	public QueryFacetProcessor(Settings settings) {
		super(settings);
		InternalQueryFacet.registerStreams();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#types()
	 */
	@Override
	public String[] types() {
		return new String[] { QueryFacet.TYPE };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#parse(java.lang.String, cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public FacetCollector parse(String facetName, XContentParser parser, SearchContext context) throws IOException {
		Query facetQuery = context.queryParserService().parse(parser).query();
		return new QueryFacetCollector(facetName, facetQuery, context.filterCache());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#reduce(java.lang.String, java.util.List)
	 */
	@Override
	public Facet reduce(String name, List<Facet> facets) {
		if (facets.size() == 1) {
			return facets.get(0);
		}
		int count = 0;
		for (Facet facet : facets) {
			if (facet.name().equals(name)) {
				count += ((QueryFacet) facet).count();
			}
		}
		return new InternalQueryFacet(name, count);
	}
}
