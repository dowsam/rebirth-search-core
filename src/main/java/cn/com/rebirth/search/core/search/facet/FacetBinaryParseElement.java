/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FacetBinaryParseElement.java 2012-3-29 15:00:57 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class FacetBinaryParseElement.
 *
 * @author l.xue.nong
 */
public class FacetBinaryParseElement extends FacetParseElement {

	
	/**
	 * Instantiates a new facet binary parse element.
	 *
	 * @param facetProcessors the facet processors
	 */
	@Inject
	public FacetBinaryParseElement(FacetProcessors facetProcessors) {
		super(facetProcessors);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetParseElement#parse(cn.com.summall.search.commons.xcontent.XContentParser, cn.com.summall.search.core.search.internal.SearchContext)
	 */
	@Override
	public void parse(XContentParser parser, SearchContext context) throws Exception {
		byte[] facetSource = parser.binaryValue();
		XContentParser fSourceParser = XContentFactory.xContent(facetSource).createParser(facetSource);
		try {
			fSourceParser.nextToken(); 
			super.parse(fSourceParser, context);
		} finally {
			fSourceParser.close();
		}
	}
}