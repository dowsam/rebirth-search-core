/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MultiFieldMapperQueryParser.java 2012-3-29 15:04:16 l.xue.nong$$
 */


package org.apache.lucene.queryParser;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.search.core.index.query.QueryParseContext;


/**
 * The Class MultiFieldMapperQueryParser.
 *
 * @author l.xue.nong
 */
public class MultiFieldMapperQueryParser extends MapperQueryParser {

    
    /** The settings. */
    private MultiFieldQueryParserSettings settings;

    
    /**
     * Instantiates a new multi field mapper query parser.
     *
     * @param parseContext the parse context
     */
    public MultiFieldMapperQueryParser(QueryParseContext parseContext) {
        super(parseContext);
    }

    
    /**
     * Instantiates a new multi field mapper query parser.
     *
     * @param settings the settings
     * @param parseContext the parse context
     */
    public MultiFieldMapperQueryParser(MultiFieldQueryParserSettings settings, QueryParseContext parseContext) {
        super(settings, parseContext);
        this.settings = settings;
    }

    
    /**
     * Reset.
     *
     * @param settings the settings
     */
    public void reset(MultiFieldQueryParserSettings settings) {
        super.reset(settings);
        this.settings = settings;
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.queryParser.MapperQueryParser#getFieldQuery(java.lang.String, java.lang.String, boolean)
     */
    @Override
    public Query getFieldQuery(String xField, String queryText, boolean quoted) throws ParseException {
        if (xField != null) {
            return super.getFieldQuery(xField, queryText, quoted);
        }
        if (settings.useDisMax()) {
            DisjunctionMaxQuery disMaxQuery = new DisjunctionMaxQuery(settings.tieBreaker());
            boolean added = false;
            for (String field : settings.fields()) {
                Query q = super.getFieldQuery(field, queryText, quoted);
                if (q != null) {
                    added = true;
                    applyBoost(field, q);
                    disMaxQuery.add(q);
                }
            }
            if (!added) {
                return null;
            }
            return disMaxQuery;
        } else {
            List<BooleanClause> clauses = new ArrayList<BooleanClause>();
            for (String field : settings.fields()) {
                Query q = super.getFieldQuery(field, queryText, true);
                if (q != null) {
                    applyBoost(field, q);
                    clauses.add(new BooleanClause(q, BooleanClause.Occur.SHOULD));
                }
            }
            if (clauses.size() == 0)  
                return null;
            return getBooleanQuery(clauses, true);
        }
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.queryParser.QueryParser#getFieldQuery(java.lang.String, java.lang.String, int)
     */
    @Override
    public Query getFieldQuery(String xField, String queryText, int slop) throws ParseException {
        if (xField != null) {
            Query q = super.getFieldQuery(xField, queryText, true);
            applySlop(q, slop);
            return q;
        }
        if (settings.useDisMax()) {
            DisjunctionMaxQuery disMaxQuery = new DisjunctionMaxQuery(settings.tieBreaker());
            boolean added = false;
            for (String field : settings.fields()) {
                Query q = super.getFieldQuery(field, queryText, true);
                if (q != null) {
                    added = true;
                    applyBoost(field, q);
                    applySlop(q, slop);
                    disMaxQuery.add(q);
                }
            }
            if (!added) {
                return null;
            }
            return disMaxQuery;
        } else {
            List<BooleanClause> clauses = new ArrayList<BooleanClause>();
            for (String field : settings.fields()) {
                Query q = super.getFieldQuery(field, queryText, true);
                if (q != null) {
                    applyBoost(field, q);
                    applySlop(q, slop);
                    clauses.add(new BooleanClause(q, BooleanClause.Occur.SHOULD));
                }
            }
            if (clauses.size() == 0)  
                return null;
            return getBooleanQuery(clauses, true);
        }
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.queryParser.MapperQueryParser#getRangeQuery(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    protected Query getRangeQuery(String xField, String part1, String part2, boolean inclusive) throws ParseException {
        if (xField != null) {
            return super.getRangeQuery(xField, part1, part2, inclusive);
        }
        if (settings.useDisMax()) {
            DisjunctionMaxQuery disMaxQuery = new DisjunctionMaxQuery(settings.tieBreaker());
            boolean added = false;
            for (String field : settings.fields()) {
                Query q = super.getRangeQuery(field, part1, part2, inclusive);
                if (q != null) {
                    added = true;
                    applyBoost(field, q);
                    disMaxQuery.add(q);
                }
            }
            if (!added) {
                return null;
            }
            return disMaxQuery;
        } else {
            List<BooleanClause> clauses = new ArrayList<BooleanClause>();
            for (String field : settings.fields()) {
                Query q = super.getRangeQuery(field, part1, part2, inclusive);
                if (q != null) {
                    applyBoost(field, q);
                    clauses.add(new BooleanClause(q, BooleanClause.Occur.SHOULD));
                }
            }
            if (clauses.size() == 0)  
                return null;
            return getBooleanQuery(clauses, true);
        }
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.queryParser.MapperQueryParser#getPrefixQuery(java.lang.String, java.lang.String)
     */
    @Override
    protected Query getPrefixQuery(String xField, String termStr) throws ParseException {
        if (xField != null) {
            return super.getPrefixQuery(xField, termStr);
        }
        if (settings.useDisMax()) {
            DisjunctionMaxQuery disMaxQuery = new DisjunctionMaxQuery(settings.tieBreaker());
            boolean added = false;
            for (String field : settings.fields()) {
                Query q = super.getPrefixQuery(field, termStr);
                if (q != null) {
                    added = true;
                    applyBoost(field, q);
                    disMaxQuery.add(q);
                }
            }
            if (!added) {
                return null;
            }
            return disMaxQuery;
        } else {
            List<BooleanClause> clauses = new ArrayList<BooleanClause>();
            for (String field : settings.fields()) {
                Query q = super.getPrefixQuery(field, termStr);
                if (q != null) {
                    applyBoost(field, q);
                    clauses.add(new BooleanClause(q, BooleanClause.Occur.SHOULD));
                }
            }
            if (clauses.size() == 0)  
                return null;
            return getBooleanQuery(clauses, true);
        }
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.queryParser.MapperQueryParser#getWildcardQuery(java.lang.String, java.lang.String)
     */
    @Override
    protected Query getWildcardQuery(String xField, String termStr) throws ParseException {
        if (xField != null) {
            return super.getWildcardQuery(xField, termStr);
        }
        if (settings.useDisMax()) {
            DisjunctionMaxQuery disMaxQuery = new DisjunctionMaxQuery(settings.tieBreaker());
            boolean added = false;
            for (String field : settings.fields()) {
                Query q = super.getWildcardQuery(field, termStr);
                if (q != null) {
                    added = true;
                    applyBoost(field, q);
                    disMaxQuery.add(q);
                }
            }
            if (!added) {
                return null;
            }
            return disMaxQuery;
        } else {
            List<BooleanClause> clauses = new ArrayList<BooleanClause>();
            for (String field : settings.fields()) {
                Query q = super.getWildcardQuery(field, termStr);
                if (q != null) {
                    applyBoost(field, q);
                    clauses.add(new BooleanClause(q, BooleanClause.Occur.SHOULD));
                }
            }
            if (clauses.size() == 0)  
                return null;
            return getBooleanQuery(clauses, true);
        }
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.queryParser.MapperQueryParser#getFuzzyQuery(java.lang.String, java.lang.String, float)
     */
    @Override
    protected Query getFuzzyQuery(String xField, String termStr, float minSimilarity) throws ParseException {
        if (xField != null) {
            return super.getFuzzyQuery(xField, termStr, minSimilarity);
        }
        if (settings.useDisMax()) {
            DisjunctionMaxQuery disMaxQuery = new DisjunctionMaxQuery(settings.tieBreaker());
            boolean added = false;
            for (String field : settings.fields()) {
                Query q = super.getFuzzyQuery(field, termStr, minSimilarity);
                if (q != null) {
                    added = true;
                    applyBoost(field, q);
                    disMaxQuery.add(q);
                }
            }
            if (!added) {
                return null;
            }
            return disMaxQuery;
        } else {
            List<BooleanClause> clauses = new ArrayList<BooleanClause>();
            for (String field : settings.fields()) {
                Query q = super.getFuzzyQuery(field, termStr, minSimilarity);
                applyBoost(field, q);
                clauses.add(new BooleanClause(q, BooleanClause.Occur.SHOULD));
            }
            return getBooleanQuery(clauses, true);
        }
    }

    
    /**
     * Apply boost.
     *
     * @param field the field
     * @param q the q
     */
    private void applyBoost(String field, Query q) {
        if (settings.boosts() != null) {
            float boost = settings.boosts().get(field);
            q.setBoost(boost);
        }
    }

    
    /**
     * Apply slop.
     *
     * @param q the q
     * @param slop the slop
     */
    private void applySlop(Query q, int slop) {
        if (q instanceof PhraseQuery) {
            ((PhraseQuery) q).setSlop(slop);
        } else if (q instanceof MultiPhraseQuery) {
            ((MultiPhraseQuery) q).setSlop(slop);
        }
    }
}
