/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CustomFieldQuery.java 2012-3-29 15:04:16 l.xue.nong$$
 */


package org.apache.lucene.search.vectorhighlight;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MultiTermQueryWrapperFilter;
import org.apache.lucene.search.PublicTermsFilter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

import cn.com.rebirth.search.commons.lucene.search.MultiPhrasePrefixQuery;
import cn.com.rebirth.search.commons.lucene.search.TermFilter;
import cn.com.rebirth.search.commons.lucene.search.XBooleanFilter;
import cn.com.rebirth.search.commons.lucene.search.function.FiltersFunctionScoreQuery;
import cn.com.rebirth.search.commons.lucene.search.function.FunctionScoreQuery;



/**
 * The Class CustomFieldQuery.
 *
 * @author l.xue.nong
 */
public class CustomFieldQuery extends FieldQuery {

    
    /** The multi term query wrapper filter query field. */
    private static Field multiTermQueryWrapperFilterQueryField;

    static {
        try {
            multiTermQueryWrapperFilterQueryField = MultiTermQueryWrapperFilter.class.getDeclaredField("query");
            multiTermQueryWrapperFilterQueryField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            
        }
    }

    
    /** The Constant highlightFilters. */
    public static final ThreadLocal<Boolean> highlightFilters = new ThreadLocal<Boolean>();

    
    /**
     * Instantiates a new custom field query.
     *
     * @param query the query
     * @param reader the reader
     * @param highlighter the highlighter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CustomFieldQuery(Query query, IndexReader reader, FastVectorHighlighter highlighter) throws IOException {
        this(query, reader, highlighter.isPhraseHighlight(), highlighter.isFieldMatch());
    }

    
    /**
     * Instantiates a new custom field query.
     *
     * @param query the query
     * @param reader the reader
     * @param phraseHighlight the phrase highlight
     * @param fieldMatch the field match
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CustomFieldQuery(Query query, IndexReader reader, boolean phraseHighlight, boolean fieldMatch) throws IOException {
        super(query, reader, phraseHighlight, fieldMatch);
        highlightFilters.remove();
    }

    
    /**
     * Flatten.
     *
     * @param sourceQuery the source query
     * @param reader the reader
     * @param flatQueries the flat queries
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    void flatten(Query sourceQuery, IndexReader reader, Collection<Query> flatQueries) throws IOException {
        if (sourceQuery instanceof DisjunctionMaxQuery) {
            DisjunctionMaxQuery dmq = (DisjunctionMaxQuery) sourceQuery;
            for (Query query : dmq) {
                flatten(query, reader, flatQueries);
            }
        } else if (sourceQuery instanceof SpanTermQuery) {
            TermQuery termQuery = new TermQuery(((SpanTermQuery) sourceQuery).getTerm());
            if (!flatQueries.contains(termQuery)) {
                flatQueries.add(termQuery);
            }
        } else if (sourceQuery instanceof ConstantScoreQuery) {
            ConstantScoreQuery constantScoreQuery = (ConstantScoreQuery) sourceQuery;
            if (constantScoreQuery.getFilter() != null) {
                flatten(constantScoreQuery.getFilter(), reader, flatQueries);
            } else {
                flatten(constantScoreQuery.getQuery(), reader, flatQueries);
            }
        } else if (sourceQuery instanceof FunctionScoreQuery) {
            flatten(((FunctionScoreQuery) sourceQuery).getSubQuery(), reader, flatQueries);
        } else if (sourceQuery instanceof FilteredQuery) {
            flatten(((FilteredQuery) sourceQuery).getQuery(), reader, flatQueries);
            flatten(((FilteredQuery) sourceQuery).getFilter(), reader, flatQueries);
        } else if (sourceQuery instanceof MultiPhrasePrefixQuery) {
            try {
                flatten(sourceQuery.rewrite(reader), reader, flatQueries);
            } catch (IOException e) {
                
            }
        } else if (sourceQuery instanceof FiltersFunctionScoreQuery) {
            flatten(((FiltersFunctionScoreQuery) sourceQuery).getSubQuery(), reader, flatQueries);
        } else {
            super.flatten(sourceQuery, reader, flatQueries);
        }
    }

    
    /**
     * Flatten.
     *
     * @param sourceFilter the source filter
     * @param reader the reader
     * @param flatQueries the flat queries
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void flatten(Filter sourceFilter, IndexReader reader, Collection<Query> flatQueries) throws IOException {
        Boolean highlight = highlightFilters.get();
        if (highlight == null || highlight.equals(Boolean.FALSE)) {
            return;
        }
        if (sourceFilter instanceof TermFilter) {
            flatten(new TermQuery(((TermFilter) sourceFilter).getTerm()), reader, flatQueries);
        } else if (sourceFilter instanceof PublicTermsFilter) {
            PublicTermsFilter termsFilter = (PublicTermsFilter) sourceFilter;
            for (Term term : termsFilter.getTerms()) {
                flatten(new TermQuery(term), reader, flatQueries);
            }
        } else if (sourceFilter instanceof MultiTermQueryWrapperFilter) {
            if (multiTermQueryWrapperFilterQueryField != null) {
                try {
                    flatten((Query) multiTermQueryWrapperFilterQueryField.get(sourceFilter), reader, flatQueries);
                } catch (IllegalAccessException e) {
                    
                }
            }
        } else if (sourceFilter instanceof XBooleanFilter) {
            XBooleanFilter booleanFilter = (XBooleanFilter) sourceFilter;
            if (booleanFilter.getMustFilters() != null) {
                for (Filter filter : booleanFilter.getMustFilters()) {
                    flatten(filter, reader, flatQueries);
                }
            }
            if (booleanFilter.getShouldFilters() != null) {
                for (Filter filter : booleanFilter.getShouldFilters()) {
                    flatten(filter, reader, flatQueries);
                }
            }
        }
    }
}
