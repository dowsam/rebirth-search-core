/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core QueryParser.java 2012-3-29 15:02:11 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import org.apache.lucene.search.Query;

import java.io.IOException;


/**
 * The Interface QueryParser.
 *
 * @author l.xue.nong
 */
public interface QueryParser {

    
    /**
     * Names.
     *
     * @return the string[]
     */
    String[] names();

    
    /**
     * Parses the.
     *
     * @param parseContext the parse context
     * @return the query
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws QueryParsingException the query parsing exception
     */
    Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException;
}
