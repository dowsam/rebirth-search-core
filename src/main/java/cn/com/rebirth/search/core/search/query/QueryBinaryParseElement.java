/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core QueryBinaryParseElement.java 2012-3-29 15:02:06 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.query;

import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class QueryBinaryParseElement.
 *
 * @author l.xue.nong
 */
public class QueryBinaryParseElement implements SearchParseElement {

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.search.SearchParseElement#parse(cn.com.summall.search.commons.xcontent.XContentParser, cn.com.summall.search.core.search.internal.SearchContext)
     */
    @Override
    public void parse(XContentParser parser, SearchContext context) throws Exception {
        byte[] querySource = parser.binaryValue();
        XContentParser qSourceParser = XContentFactory.xContent(querySource).createParser(querySource);
        try {
            context.parsedQuery(context.queryParserService().parse(qSourceParser));
        } finally {
            qSourceParser.close();
        }
    }
}