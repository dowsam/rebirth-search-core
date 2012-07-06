/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndicesQueriesRegistry.java 2012-3-29 15:02:33 l.xue.nong$$
 */


package cn.com.rebirth.search.core.indices.query;

import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.index.query.AndFilterParser;
import cn.com.rebirth.search.core.index.query.BoolFilterParser;
import cn.com.rebirth.search.core.index.query.BoolQueryParser;
import cn.com.rebirth.search.core.index.query.BoostingQueryParser;
import cn.com.rebirth.search.core.index.query.ConstantScoreQueryParser;
import cn.com.rebirth.search.core.index.query.CustomBoostFactorQueryParser;
import cn.com.rebirth.search.core.index.query.CustomFiltersScoreQueryParser;
import cn.com.rebirth.search.core.index.query.CustomScoreQueryParser;
import cn.com.rebirth.search.core.index.query.DisMaxQueryParser;
import cn.com.rebirth.search.core.index.query.ExistsFilterParser;
import cn.com.rebirth.search.core.index.query.FQueryFilterParser;
import cn.com.rebirth.search.core.index.query.FieldQueryParser;
import cn.com.rebirth.search.core.index.query.FilterParser;
import cn.com.rebirth.search.core.index.query.FilteredQueryParser;
import cn.com.rebirth.search.core.index.query.FuzzyLikeThisFieldQueryParser;
import cn.com.rebirth.search.core.index.query.FuzzyLikeThisQueryParser;
import cn.com.rebirth.search.core.index.query.FuzzyQueryParser;
import cn.com.rebirth.search.core.index.query.GeoBoundingBoxFilterParser;
import cn.com.rebirth.search.core.index.query.GeoDistanceFilterParser;
import cn.com.rebirth.search.core.index.query.GeoDistanceRangeFilterParser;
import cn.com.rebirth.search.core.index.query.GeoPolygonFilterParser;
import cn.com.rebirth.search.core.index.query.HasChildFilterParser;
import cn.com.rebirth.search.core.index.query.HasChildQueryParser;
import cn.com.rebirth.search.core.index.query.IdsFilterParser;
import cn.com.rebirth.search.core.index.query.IdsQueryParser;
import cn.com.rebirth.search.core.index.query.IndicesFilterParser;
import cn.com.rebirth.search.core.index.query.IndicesQueryParser;
import cn.com.rebirth.search.core.index.query.LimitFilterParser;
import cn.com.rebirth.search.core.index.query.MatchAllFilterParser;
import cn.com.rebirth.search.core.index.query.MatchAllQueryParser;
import cn.com.rebirth.search.core.index.query.MissingFilterParser;
import cn.com.rebirth.search.core.index.query.MoreLikeThisFieldQueryParser;
import cn.com.rebirth.search.core.index.query.MoreLikeThisQueryParser;
import cn.com.rebirth.search.core.index.query.NestedFilterParser;
import cn.com.rebirth.search.core.index.query.NestedQueryParser;
import cn.com.rebirth.search.core.index.query.NotFilterParser;
import cn.com.rebirth.search.core.index.query.NumericRangeFilterParser;
import cn.com.rebirth.search.core.index.query.OrFilterParser;
import cn.com.rebirth.search.core.index.query.PrefixFilterParser;
import cn.com.rebirth.search.core.index.query.PrefixQueryParser;
import cn.com.rebirth.search.core.index.query.QueryFilterParser;
import cn.com.rebirth.search.core.index.query.QueryParser;
import cn.com.rebirth.search.core.index.query.QueryStringQueryParser;
import cn.com.rebirth.search.core.index.query.RangeFilterParser;
import cn.com.rebirth.search.core.index.query.RangeQueryParser;
import cn.com.rebirth.search.core.index.query.ScriptFilterParser;
import cn.com.rebirth.search.core.index.query.SpanFirstQueryParser;
import cn.com.rebirth.search.core.index.query.SpanNearQueryParser;
import cn.com.rebirth.search.core.index.query.SpanNotQueryParser;
import cn.com.rebirth.search.core.index.query.SpanOrQueryParser;
import cn.com.rebirth.search.core.index.query.SpanTermQueryParser;
import cn.com.rebirth.search.core.index.query.TermFilterParser;
import cn.com.rebirth.search.core.index.query.TermQueryParser;
import cn.com.rebirth.search.core.index.query.TermsFilterParser;
import cn.com.rebirth.search.core.index.query.TermsQueryParser;
import cn.com.rebirth.search.core.index.query.TextQueryParser;
import cn.com.rebirth.search.core.index.query.TopChildrenQueryParser;
import cn.com.rebirth.search.core.index.query.TypeFilterParser;
import cn.com.rebirth.search.core.index.query.WildcardQueryParser;
import cn.com.rebirth.search.core.index.query.WrapperQueryParser;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;


/**
 * The Class IndicesQueriesRegistry.
 *
 * @author l.xue.nong
 */
public class IndicesQueriesRegistry {

    
    /** The query parsers. */
    private ImmutableMap<String, QueryParser> queryParsers;
    
    
    /** The filter parsers. */
    private ImmutableMap<String, FilterParser> filterParsers;

    
    /**
     * Instantiates a new indices queries registry.
     *
     * @param settings the settings
     * @param clusterService the cluster service
     */
    @Inject
    public IndicesQueriesRegistry(Settings settings, @Nullable ClusterService clusterService) {
        Map<String, QueryParser> queryParsers = Maps.newHashMap();
        addQueryParser(queryParsers, new TextQueryParser());
        addQueryParser(queryParsers, new NestedQueryParser());
        addQueryParser(queryParsers, new HasChildQueryParser());
        addQueryParser(queryParsers, new TopChildrenQueryParser());
        addQueryParser(queryParsers, new DisMaxQueryParser());
        addQueryParser(queryParsers, new IdsQueryParser());
        addQueryParser(queryParsers, new MatchAllQueryParser());
        addQueryParser(queryParsers, new QueryStringQueryParser(settings));
        addQueryParser(queryParsers, new BoostingQueryParser());
        addQueryParser(queryParsers, new BoolQueryParser(settings));
        addQueryParser(queryParsers, new TermQueryParser());
        addQueryParser(queryParsers, new TermsQueryParser());
        addQueryParser(queryParsers, new FuzzyQueryParser());
        addQueryParser(queryParsers, new FieldQueryParser(settings));
        addQueryParser(queryParsers, new RangeQueryParser());
        addQueryParser(queryParsers, new PrefixQueryParser());
        addQueryParser(queryParsers, new WildcardQueryParser());
        addQueryParser(queryParsers, new FilteredQueryParser());
        addQueryParser(queryParsers, new ConstantScoreQueryParser());
        addQueryParser(queryParsers, new CustomBoostFactorQueryParser());
        addQueryParser(queryParsers, new CustomScoreQueryParser());
        addQueryParser(queryParsers, new CustomFiltersScoreQueryParser());
        addQueryParser(queryParsers, new SpanTermQueryParser());
        addQueryParser(queryParsers, new SpanNotQueryParser());
        addQueryParser(queryParsers, new SpanFirstQueryParser());
        addQueryParser(queryParsers, new SpanNearQueryParser());
        addQueryParser(queryParsers, new SpanOrQueryParser());
        addQueryParser(queryParsers, new MoreLikeThisQueryParser());
        addQueryParser(queryParsers, new MoreLikeThisFieldQueryParser());
        addQueryParser(queryParsers, new FuzzyLikeThisQueryParser());
        addQueryParser(queryParsers, new FuzzyLikeThisFieldQueryParser());
        addQueryParser(queryParsers, new WrapperQueryParser());
        addQueryParser(queryParsers, new IndicesQueryParser(clusterService));
        this.queryParsers = ImmutableMap.copyOf(queryParsers);

        Map<String, FilterParser> filterParsers = Maps.newHashMap();
        addFilterParser(filterParsers, new HasChildFilterParser());
        addFilterParser(filterParsers, new NestedFilterParser());
        addFilterParser(filterParsers, new TypeFilterParser());
        addFilterParser(filterParsers, new IdsFilterParser());
        addFilterParser(filterParsers, new LimitFilterParser());
        addFilterParser(filterParsers, new TermFilterParser());
        addFilterParser(filterParsers, new TermsFilterParser());
        addFilterParser(filterParsers, new RangeFilterParser());
        addFilterParser(filterParsers, new NumericRangeFilterParser());
        addFilterParser(filterParsers, new PrefixFilterParser());
        addFilterParser(filterParsers, new ScriptFilterParser());
        addFilterParser(filterParsers, new GeoDistanceFilterParser());
        addFilterParser(filterParsers, new GeoDistanceRangeFilterParser());
        addFilterParser(filterParsers, new GeoBoundingBoxFilterParser());
        addFilterParser(filterParsers, new GeoPolygonFilterParser());
        addFilterParser(filterParsers, new QueryFilterParser());
        addFilterParser(filterParsers, new FQueryFilterParser());
        addFilterParser(filterParsers, new BoolFilterParser());
        addFilterParser(filterParsers, new AndFilterParser());
        addFilterParser(filterParsers, new OrFilterParser());
        addFilterParser(filterParsers, new NotFilterParser());
        addFilterParser(filterParsers, new MatchAllFilterParser());
        addFilterParser(filterParsers, new ExistsFilterParser());
        addFilterParser(filterParsers, new MissingFilterParser());
        addFilterParser(filterParsers, new IndicesFilterParser(clusterService));
        this.filterParsers = ImmutableMap.copyOf(filterParsers);
    }

    
    /**
     * Adds the query parser.
     *
     * @param queryParser the query parser
     */
    public void addQueryParser(QueryParser queryParser) {
        Map<String, QueryParser> queryParsers = Maps.newHashMap(this.queryParsers);
        addQueryParser(queryParsers, queryParser);
        this.queryParsers = ImmutableMap.copyOf(queryParsers);
    }

    
    /**
     * Adds the filter parser.
     *
     * @param filterParser the filter parser
     */
    public void addFilterParser(FilterParser filterParser) {
        Map<String, FilterParser> filterParsers = Maps.newHashMap(this.filterParsers);
        addFilterParser(filterParsers, filterParser);
        this.filterParsers = ImmutableMap.copyOf(filterParsers);
    }

    
    /**
     * Query parsers.
     *
     * @return the immutable map
     */
    public ImmutableMap<String, QueryParser> queryParsers() {
        return queryParsers;
    }

    
    /**
     * Filter parsers.
     *
     * @return the immutable map
     */
    public ImmutableMap<String, FilterParser> filterParsers() {
        return filterParsers;
    }

    
    /**
     * Adds the query parser.
     *
     * @param queryParsers the query parsers
     * @param queryParser the query parser
     */
    private void addQueryParser(Map<String, QueryParser> queryParsers, QueryParser queryParser) {
        for (String name : queryParser.names()) {
            queryParsers.put(name, queryParser);
        }
    }

    
    /**
     * Adds the filter parser.
     *
     * @param filterParsers the filter parsers
     * @param filterParser the filter parser
     */
    private void addFilterParser(Map<String, FilterParser> filterParsers, FilterParser filterParser) {
        for (String name : filterParser.names()) {
            filterParsers.put(name, filterParser);
        }
    }
}