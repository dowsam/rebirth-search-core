/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ContextIndexSearcher.java 2012-3-29 15:02:22 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.internal;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.ExtendedIndexSearcher;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TimeLimitingCollector;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.Weight;

import cn.com.rebirth.search.commons.lucene.MinimumScoreCollector;
import cn.com.rebirth.search.commons.lucene.MultiCollector;
import cn.com.rebirth.search.commons.lucene.search.AndFilter;
import cn.com.rebirth.search.commons.lucene.search.FilteredCollector;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.search.dfs.CachedDfSource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * The Class ContextIndexSearcher.
 *
 * @author l.xue.nong
 */
public class ContextIndexSearcher extends ExtendedIndexSearcher {

    
    /**
     * The Class Scopes.
     *
     * @author l.xue.nong
     */
    public static final class Scopes {
        
        
        /** The Constant MAIN. */
        public static final String MAIN = "_main_";
        
        
        /** The Constant GLOBAL. */
        public static final String GLOBAL = "_global_";
        
        
        /** The Constant NA. */
        public static final String NA = "_na_";
    }

    
    /** The search context. */
    private final SearchContext searchContext;

    
    /** The reader. */
    private final IndexReader reader;

    
    /** The df source. */
    private CachedDfSource dfSource;

    
    /** The scope collectors. */
    private Map<String, List<Collector>> scopeCollectors;

    
    /** The processing scope. */
    private String processingScope;

    
    /**
     * Instantiates a new context index searcher.
     *
     * @param searchContext the search context
     * @param searcher the searcher
     */
    public ContextIndexSearcher(SearchContext searchContext, Engine.Searcher searcher) {
        super(searcher.searcher());
        this.searchContext = searchContext;
        this.reader = searcher.searcher().getIndexReader();
    }

    
    /**
     * Df source.
     *
     * @param dfSource the df source
     */
    public void dfSource(CachedDfSource dfSource) {
        this.dfSource = dfSource;
    }

    
    /**
     * Adds the collector.
     *
     * @param scope the scope
     * @param collector the collector
     */
    public void addCollector(String scope, Collector collector) {
        if (scopeCollectors == null) {
            scopeCollectors = Maps.newHashMap();
        }
        List<Collector> collectors = scopeCollectors.get(scope);
        if (collectors == null) {
            collectors = Lists.newArrayList();
            scopeCollectors.put(scope, collectors);
        }
        collectors.add(collector);
    }

    
    /**
     * Removes the collectors.
     *
     * @param scope the scope
     * @return the list
     */
    public List<Collector> removeCollectors(String scope) {
        if (scopeCollectors == null) {
            return null;
        }
        return scopeCollectors.remove(scope);
    }

    
    /**
     * Checks for collectors.
     *
     * @param scope the scope
     * @return true, if successful
     */
    public boolean hasCollectors(String scope) {
        if (scopeCollectors == null) {
            return false;
        }
        if (!scopeCollectors.containsKey(scope)) {
            return false;
        }
        return !scopeCollectors.get(scope).isEmpty();
    }

    
    /**
     * Processing scope.
     *
     * @param scope the scope
     */
    public void processingScope(String scope) {
        this.processingScope = scope;
    }

    
    /**
     * Processed scope.
     */
    public void processedScope() {
        
        
        if (scopeCollectors != null) {
            scopeCollectors.remove(processingScope);
        }
        this.processingScope = Scopes.NA;
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.IndexSearcher#rewrite(org.apache.lucene.search.Query)
     */
    @Override
    public Query rewrite(Query original) throws IOException {
        if (original == searchContext.query() || original == searchContext.parsedQuery().query()) {
            
            if (searchContext.queryRewritten()) {
                return searchContext.query();
            }
            Query rewriteQuery = super.rewrite(original);
            searchContext.updateRewriteQuery(rewriteQuery);
            return rewriteQuery;
        } else {
            return super.rewrite(original);
        }
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.IndexSearcher#createNormalizedWeight(org.apache.lucene.search.Query)
     */
    @Override
    public Weight createNormalizedWeight(Query query) throws IOException {
        
        if (dfSource != null && (query == searchContext.query() || query == searchContext.parsedQuery().query())) {
            return dfSource.createNormalizedWeight(query);
        }
        return super.createNormalizedWeight(query);
    }

    
    
    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.IndexSearcher#search(org.apache.lucene.search.Weight, org.apache.lucene.search.Filter, int, org.apache.lucene.search.Sort, boolean)
     */
    @Override
    public TopFieldDocs search(Weight weight, Filter filter, int nDocs,
                               Sort sort, boolean fillFields) throws IOException {
        int limit = reader.maxDoc();
        if (limit == 0) {
            limit = 1;
        }
        nDocs = Math.min(nDocs, limit);

        TopFieldCollector collector = TopFieldCollector.create(sort, nDocs,
                fillFields, searchContext.trackScores(), searchContext.trackScores(), !weight.scoresDocsOutOfOrder());
        search(weight, filter, collector);
        return (TopFieldDocs) collector.topDocs();
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.IndexSearcher#search(org.apache.lucene.search.Weight, org.apache.lucene.search.Filter, org.apache.lucene.search.Collector)
     */
    @Override
    public void search(Weight weight, Filter filter, Collector collector) throws IOException {
        if (searchContext.parsedFilter() != null && Scopes.MAIN.equals(processingScope)) {
            
            
            
            collector = new FilteredCollector(collector, searchContext.parsedFilter());
        }
        if (searchContext.timeoutInMillis() != -1) {
            
            collector = new TimeLimitingCollector(collector, TimeLimitingCollector.getGlobalCounter(), searchContext.timeoutInMillis());
        }
        if (scopeCollectors != null) {
            List<Collector> collectors = scopeCollectors.get(processingScope);
            if (collectors != null && !collectors.isEmpty()) {
                collector = new MultiCollector(collector, collectors.toArray(new Collector[collectors.size()]));
            }
        }
        
        if (searchContext.minimumScore() != null) {
            collector = new MinimumScoreCollector(collector, searchContext.minimumScore());
        }

        Filter combinedFilter;
        if (filter == null) {
            combinedFilter = searchContext.aliasFilter();
        } else {
            if (searchContext.aliasFilter() != null) {
                combinedFilter = new AndFilter(ImmutableList.of(filter, searchContext.aliasFilter()));
            } else {
                combinedFilter = filter;
            }
        }

        
        if (searchContext.timeoutInMillis() != -1) {
            try {
                super.search(weight, combinedFilter, collector);
            } catch (TimeLimitingCollector.TimeExceededException e) {
                searchContext.queryResult().searchTimedOut(true);
            }
        } else {
            super.search(weight, combinedFilter, collector);
        }
    }
}