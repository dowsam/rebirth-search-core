/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FilterFacetProcessor.java 2012-7-6 14:29:19 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.filter;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.search.Filter;

import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetCollector;
import cn.com.rebirth.search.core.search.facet.FacetProcessor;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class FilterFacetProcessor.
 *
 * @author l.xue.nong
 */
public class FilterFacetProcessor extends AbstractComponent implements FacetProcessor {

	/**
	 * Instantiates a new filter facet processor.
	 *
	 * @param settings the settings
	 */
	@Inject
	public FilterFacetProcessor(Settings settings) {
		super(settings);
		InternalFilterFacet.registerStreams();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#types()
	 */
	@Override
	public String[] types() {
		return new String[] { FilterFacet.TYPE };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#parse(java.lang.String, cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public FacetCollector parse(String facetName, XContentParser parser, SearchContext context) throws IOException {
		Filter facetFilter = context.queryParserService().parseInnerFilter(parser);
		return new FilterFacetCollector(facetName, facetFilter, context.filterCache());
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
			count += ((FilterFacet) facet).count();
		}
		return new InternalFilterFacet(name, count);
	}
}
