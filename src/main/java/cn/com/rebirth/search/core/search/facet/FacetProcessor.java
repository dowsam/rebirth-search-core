/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FacetProcessor.java 2012-3-29 15:01:41 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet;

import java.io.IOException;
import java.util.List;

import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Interface FacetProcessor.
 *
 * @author l.xue.nong
 */
public interface FacetProcessor {

	
	/**
	 * Types.
	 *
	 * @return the string[]
	 */
	String[] types();

	
	/**
	 * Parses the.
	 *
	 * @param facetName the facet name
	 * @param parser the parser
	 * @param context the context
	 * @return the facet collector
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	FacetCollector parse(String facetName, XContentParser parser, SearchContext context) throws IOException;

	
	/**
	 * Reduce.
	 *
	 * @param name the name
	 * @param facets the facets
	 * @return the facet
	 */
	Facet reduce(String name, List<Facet> facets);
}
