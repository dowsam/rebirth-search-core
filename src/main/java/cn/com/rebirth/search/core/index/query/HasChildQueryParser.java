/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core HasChildQueryParser.java 2012-3-29 15:01:18 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.search.child.HasChildFilter;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class HasChildQueryParser.
 *
 * @author l.xue.nong
 */
public class HasChildQueryParser implements QueryParser {

    
    /** The Constant NAME. */
    public static final String NAME = "has_child";

    
    /**
     * Instantiates a new checks for child query parser.
     */
    @Inject
    public HasChildQueryParser() {
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.query.QueryParser#names()
     */
    @Override
    public String[] names() {
        return new String[]{NAME, Strings.toCamelCase(NAME)};
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.query.QueryParser#parse(cn.com.summall.search.core.index.query.QueryParseContext)
     */
    @Override
    public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
        XContentParser parser = parseContext.parser();

        Query query = null;
        float boost = 1.0f;
        String childType = null;
        String scope = null;

        String currentFieldName = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_OBJECT) {
                if ("query".equals(currentFieldName)) {
                    
                    
                    String[] origTypes = QueryParseContext.setTypesWithPrevious(childType == null ? null : new String[]{childType});
                    try {
                        query = parseContext.parseInnerQuery();
                    } finally {
                        QueryParseContext.setTypes(origTypes);
                    }
                } else {
                    throw new QueryParsingException(parseContext.index(), "[has_child] query does not support [" + currentFieldName + "]");
                }
            } else if (token.isValue()) {
                if ("type".equals(currentFieldName)) {
                    childType = parser.text();
                } else if ("_scope".equals(currentFieldName)) {
                    scope = parser.text();
                } else if ("boost".equals(currentFieldName)) {
                    boost = parser.floatValue();
                } else {
                    throw new QueryParsingException(parseContext.index(), "[has_child] query does not support [" + currentFieldName + "]");
                }
            }
        }
        if (query == null) {
            throw new QueryParsingException(parseContext.index(), "[has_child] requires 'query' field");
        }
        if (childType == null) {
            throw new QueryParsingException(parseContext.index(), "[has_child] requires 'type' field");
        }

        DocumentMapper childDocMapper = parseContext.mapperService().documentMapper(childType);
        if (childDocMapper == null) {
            throw new QueryParsingException(parseContext.index(), "[has_child] No mapping for for type [" + childType + "]");
        }
        if (childDocMapper.parentFieldMapper() == null) {
            throw new QueryParsingException(parseContext.index(), "[has_child]  Type [" + childType + "] does not have parent mapping");
        }
        String parentType = childDocMapper.parentFieldMapper().type();

        query.setBoost(boost);
        
        query = new FilteredQuery(query, parseContext.cacheFilter(childDocMapper.typeFilter(), null));

        SearchContext searchContext = SearchContext.current();
        HasChildFilter childFilter = new HasChildFilter(query, scope, childType, parentType, searchContext);
        
        ConstantScoreQuery childQuery = new ConstantScoreQuery(childFilter);
        childQuery.setBoost(boost);
        searchContext.addScopePhase(childFilter);
        return childQuery;
    }
}
