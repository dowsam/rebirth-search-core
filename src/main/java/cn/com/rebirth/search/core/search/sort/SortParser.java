/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SortParser.java 2012-3-29 15:02:52 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.sort;

import org.apache.lucene.search.SortField;

import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Interface SortParser.
 *
 * @author l.xue.nong
 */
public interface SortParser {

	
	/**
	 * Names.
	 *
	 * @return the string[]
	 */
	String[] names();

	
	/**
	 * Parses the.
	 *
	 * @param parser the parser
	 * @param context the context
	 * @return the sort field
	 * @throws Exception the exception
	 */
	SortField parse(XContentParser parser, SearchContext context) throws Exception;
}
