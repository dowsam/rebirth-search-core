/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchContext.java 2012-7-6 14:30:22 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.search.commons.lease.Releasable;
import cn.com.rebirth.search.core.action.search.SearchType;
import cn.com.rebirth.search.core.index.analysis.AnalysisService;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.cache.filter.FilterCache;
import cn.com.rebirth.search.core.index.cache.id.IdCache;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.FieldMappers;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.query.IndexQueryParserService;
import cn.com.rebirth.search.core.index.query.ParsedQuery;
import cn.com.rebirth.search.core.index.query.QueryParseContext;
import cn.com.rebirth.search.core.index.search.nested.BlockJoinQuery;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.index.similarity.SimilarityService;
import cn.com.rebirth.search.core.script.ScriptService;
import cn.com.rebirth.search.core.search.Scroll;
import cn.com.rebirth.search.core.search.SearchShardTarget;
import cn.com.rebirth.search.core.search.dfs.DfsSearchResult;
import cn.com.rebirth.search.core.search.facet.SearchContextFacets;
import cn.com.rebirth.search.core.search.fetch.FetchSearchResult;
import cn.com.rebirth.search.core.search.fetch.partial.PartialFieldsContext;
import cn.com.rebirth.search.core.search.fetch.script.ScriptFieldsContext;
import cn.com.rebirth.search.core.search.highlight.SearchContextHighlight;
import cn.com.rebirth.search.core.search.lookup.SearchLookup;
import cn.com.rebirth.search.core.search.query.QuerySearchResult;
import cn.com.rebirth.search.core.search.scan.ScanContext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * The Class SearchContext.
 *
 * @author l.xue.nong
 */
public class SearchContext implements Releasable {

	/** The current. */
	private static ThreadLocal<SearchContext> current = new ThreadLocal<SearchContext>();

	/**
	 * Sets the current.
	 *
	 * @param value the new current
	 */
	public static void setCurrent(SearchContext value) {
		current.set(value);
		QueryParseContext.setTypes(value.types());
	}

	/**
	 * Removes the current.
	 */
	public static void removeCurrent() {
		current.remove();
		QueryParseContext.removeTypes();
	}

	/**
	 * Current.
	 *
	 * @return the search context
	 */
	public static SearchContext current() {
		return current.get();
	}

	/** The id. */
	private final long id;

	/** The request. */
	private final InternalSearchRequest request;

	/** The shard target. */
	private final SearchShardTarget shardTarget;

	/** The search type. */
	private SearchType searchType;

	/** The engine searcher. */
	private final Engine.Searcher engineSearcher;

	/** The script service. */
	private final ScriptService scriptService;

	/** The index shard. */
	private final IndexShard indexShard;

	/** The index service. */
	private final IndexService indexService;

	/** The searcher. */
	private final ContextIndexSearcher searcher;

	/** The dfs result. */
	private final DfsSearchResult dfsResult;

	/** The query result. */
	private final QuerySearchResult queryResult;

	/** The fetch result. */
	private final FetchSearchResult fetchResult;

	/** The scan context. */
	private ScanContext scanContext;

	/** The query boost. */
	private float queryBoost = 1.0f;

	/** The timeout in millis. */
	private long timeoutInMillis = -1;

	/** The group stats. */
	private List<String> groupStats;

	/** The scroll. */
	private Scroll scroll;

	/** The explain. */
	private boolean explain;

	/** The version. */
	private boolean version = false;

	/** The field names. */
	private List<String> fieldNames;

	/** The script fields. */
	private ScriptFieldsContext scriptFields;

	/** The partial fields. */
	private PartialFieldsContext partialFields;

	/** The from. */
	private int from = -1;

	/** The size. */
	private int size = -1;

	/** The sort. */
	private Sort sort;

	/** The minimum score. */
	private Float minimumScore;

	/** The track scores. */
	private boolean trackScores = false;

	/** The original query. */
	private ParsedQuery originalQuery;

	/** The query. */
	private Query query;

	/** The filter. */
	private Filter filter;

	/** The alias filter. */
	private Filter aliasFilter;

	/** The doc ids to load. */
	private int[] docIdsToLoad;

	/** The docs ids to load from. */
	private int docsIdsToLoadFrom;

	/** The docs ids to load size. */
	private int docsIdsToLoadSize;

	/** The facets. */
	private SearchContextFacets facets;

	/** The highlight. */
	private SearchContextHighlight highlight;

	/** The search lookup. */
	private SearchLookup searchLookup;

	/** The query rewritten. */
	private boolean queryRewritten;

	/** The keep alive. */
	private volatile long keepAlive;

	/** The last access time. */
	private volatile long lastAccessTime;

	/** The scope phases. */
	private List<ScopePhase> scopePhases = null;

	/** The nested queries. */
	private Map<String, BlockJoinQuery> nestedQueries;

	/**
	 * Instantiates a new search context.
	 *
	 * @param id the id
	 * @param request the request
	 * @param shardTarget the shard target
	 * @param engineSearcher the engine searcher
	 * @param indexService the index service
	 * @param indexShard the index shard
	 * @param scriptService the script service
	 */
	public SearchContext(long id, InternalSearchRequest request, SearchShardTarget shardTarget,
			Engine.Searcher engineSearcher, IndexService indexService, IndexShard indexShard,
			ScriptService scriptService) {
		this.id = id;
		this.request = request;
		this.searchType = request.searchType();
		this.shardTarget = shardTarget;
		this.engineSearcher = engineSearcher;
		this.scriptService = scriptService;
		this.dfsResult = new DfsSearchResult(id, shardTarget);
		this.queryResult = new QuerySearchResult(id, shardTarget);
		this.fetchResult = new FetchSearchResult(id, shardTarget);
		this.indexShard = indexShard;
		this.indexService = indexService;

		this.searcher = new ContextIndexSearcher(this, engineSearcher);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.lease.Releasable#release()
	 */
	@Override
	public boolean release() throws RebirthException {
		if (scanContext != null) {
			scanContext.clear();
		}

		if (scopePhases != null) {
			for (ScopePhase scopePhase : scopePhases) {
				scopePhase.clear();
			}
		}

		try {
			searcher.close();
		} catch (Exception e) {

		}
		engineSearcher.release();
		return true;
	}

	/**
	 * Id.
	 *
	 * @return the long
	 */
	public long id() {
		return this.id;
	}

	/**
	 * Request.
	 *
	 * @return the internal search request
	 */
	public InternalSearchRequest request() {
		return this.request;
	}

	/**
	 * Search type.
	 *
	 * @return the search type
	 */
	public SearchType searchType() {
		return this.searchType;
	}

	/**
	 * Search type.
	 *
	 * @param searchType the search type
	 * @return the search context
	 */
	public SearchContext searchType(SearchType searchType) {
		this.searchType = searchType;
		return this;
	}

	/**
	 * Shard target.
	 *
	 * @return the search shard target
	 */
	public SearchShardTarget shardTarget() {
		return this.shardTarget;
	}

	/**
	 * Number of shards.
	 *
	 * @return the int
	 */
	public int numberOfShards() {
		return request.numberOfShards();
	}

	/**
	 * Checks for types.
	 *
	 * @return true, if successful
	 */
	public boolean hasTypes() {
		return request.types() != null && request.types().length > 0;
	}

	/**
	 * Types.
	 *
	 * @return the string[]
	 */
	public String[] types() {
		return request.types();
	}

	/**
	 * Query boost.
	 *
	 * @return the float
	 */
	public float queryBoost() {
		return queryBoost;
	}

	/**
	 * Query boost.
	 *
	 * @param queryBoost the query boost
	 * @return the search context
	 */
	public SearchContext queryBoost(float queryBoost) {
		this.queryBoost = queryBoost;
		return this;
	}

	/**
	 * Now in millis.
	 *
	 * @return the long
	 */
	public long nowInMillis() {
		return request.nowInMillis();
	}

	/**
	 * Scroll.
	 *
	 * @return the scroll
	 */
	public Scroll scroll() {
		return this.scroll;
	}

	/**
	 * Scroll.
	 *
	 * @param scroll the scroll
	 * @return the search context
	 */
	public SearchContext scroll(Scroll scroll) {
		this.scroll = scroll;
		return this;
	}

	/**
	 * Facets.
	 *
	 * @return the search context facets
	 */
	public SearchContextFacets facets() {
		return facets;
	}

	/**
	 * Facets.
	 *
	 * @param facets the facets
	 * @return the search context
	 */
	public SearchContext facets(SearchContextFacets facets) {
		this.facets = facets;
		return this;
	}

	/**
	 * Highlight.
	 *
	 * @return the search context highlight
	 */
	public SearchContextHighlight highlight() {
		return highlight;
	}

	/**
	 * Highlight.
	 *
	 * @param highlight the highlight
	 */
	public void highlight(SearchContextHighlight highlight) {
		this.highlight = highlight;
	}

	/**
	 * Checks for script fields.
	 *
	 * @return true, if successful
	 */
	public boolean hasScriptFields() {
		return scriptFields != null;
	}

	/**
	 * Script fields.
	 *
	 * @return the script fields context
	 */
	public ScriptFieldsContext scriptFields() {
		if (scriptFields == null) {
			scriptFields = new ScriptFieldsContext();
		}
		return this.scriptFields;
	}

	/**
	 * Checks for partial fields.
	 *
	 * @return true, if successful
	 */
	public boolean hasPartialFields() {
		return partialFields != null;
	}

	/**
	 * Partial fields.
	 *
	 * @return the partial fields context
	 */
	public PartialFieldsContext partialFields() {
		if (partialFields == null) {
			partialFields = new PartialFieldsContext();
		}
		return this.partialFields;
	}

	/**
	 * Searcher.
	 *
	 * @return the context index searcher
	 */
	public ContextIndexSearcher searcher() {
		return this.searcher;
	}

	/**
	 * Index shard.
	 *
	 * @return the index shard
	 */
	public IndexShard indexShard() {
		return this.indexShard;
	}

	/**
	 * Mapper service.
	 *
	 * @return the mapper service
	 */
	public MapperService mapperService() {
		return indexService.mapperService();
	}

	/**
	 * Analysis service.
	 *
	 * @return the analysis service
	 */
	public AnalysisService analysisService() {
		return indexService.analysisService();
	}

	/**
	 * Query parser service.
	 *
	 * @return the index query parser service
	 */
	public IndexQueryParserService queryParserService() {
		return indexService.queryParserService();
	}

	/**
	 * Similarity service.
	 *
	 * @return the similarity service
	 */
	public SimilarityService similarityService() {
		return indexService.similarityService();
	}

	/**
	 * Script service.
	 *
	 * @return the script service
	 */
	public ScriptService scriptService() {
		return scriptService;
	}

	/**
	 * Filter cache.
	 *
	 * @return the filter cache
	 */
	public FilterCache filterCache() {
		return indexService.cache().filter();
	}

	/**
	 * Field data cache.
	 *
	 * @return the field data cache
	 */
	public FieldDataCache fieldDataCache() {
		return indexService.cache().fieldData();
	}

	/**
	 * Id cache.
	 *
	 * @return the id cache
	 */
	public IdCache idCache() {
		return indexService.cache().idCache();
	}

	/**
	 * Timeout in millis.
	 *
	 * @return the long
	 */
	public long timeoutInMillis() {
		return timeoutInMillis;
	}

	/**
	 * Timeout in millis.
	 *
	 * @param timeoutInMillis the timeout in millis
	 */
	public void timeoutInMillis(long timeoutInMillis) {
		this.timeoutInMillis = timeoutInMillis;
	}

	/**
	 * Minimum score.
	 *
	 * @param minimumScore the minimum score
	 * @return the search context
	 */
	public SearchContext minimumScore(float minimumScore) {
		this.minimumScore = minimumScore;
		return this;
	}

	/**
	 * Minimum score.
	 *
	 * @return the float
	 */
	public Float minimumScore() {
		return this.minimumScore;
	}

	/**
	 * Sort.
	 *
	 * @param sort the sort
	 * @return the search context
	 */
	public SearchContext sort(Sort sort) {
		this.sort = sort;
		return this;
	}

	/**
	 * Sort.
	 *
	 * @return the sort
	 */
	public Sort sort() {
		return this.sort;
	}

	/**
	 * Track scores.
	 *
	 * @param trackScores the track scores
	 * @return the search context
	 */
	public SearchContext trackScores(boolean trackScores) {
		this.trackScores = trackScores;
		return this;
	}

	/**
	 * Track scores.
	 *
	 * @return true, if successful
	 */
	public boolean trackScores() {
		return this.trackScores;
	}

	/**
	 * Parsed filter.
	 *
	 * @param filter the filter
	 * @return the search context
	 */
	public SearchContext parsedFilter(Filter filter) {
		this.filter = filter;
		return this;
	}

	/**
	 * Parsed filter.
	 *
	 * @return the filter
	 */
	public Filter parsedFilter() {
		return this.filter;
	}

	/**
	 * Alias filter.
	 *
	 * @param aliasFilter the alias filter
	 * @return the search context
	 */
	public SearchContext aliasFilter(Filter aliasFilter) {
		this.aliasFilter = aliasFilter;
		return this;
	}

	/**
	 * Alias filter.
	 *
	 * @return the filter
	 */
	public Filter aliasFilter() {
		return aliasFilter;
	}

	/**
	 * Parsed query.
	 *
	 * @param query the query
	 * @return the search context
	 */
	public SearchContext parsedQuery(ParsedQuery query) {
		queryRewritten = false;
		this.originalQuery = query;
		this.query = query.query();
		return this;
	}

	/**
	 * Parsed query.
	 *
	 * @return the parsed query
	 */
	public ParsedQuery parsedQuery() {
		return this.originalQuery;
	}

	/**
	 * Query.
	 *
	 * @return the query
	 */
	public Query query() {
		return this.query;
	}

	/**
	 * Query rewritten.
	 *
	 * @return true, if successful
	 */
	public boolean queryRewritten() {
		return queryRewritten;
	}

	/**
	 * Update rewrite query.
	 *
	 * @param rewriteQuery the rewrite query
	 * @return the search context
	 */
	public SearchContext updateRewriteQuery(Query rewriteQuery) {
		query = rewriteQuery;
		queryRewritten = true;
		return this;
	}

	/**
	 * From.
	 *
	 * @return the int
	 */
	public int from() {
		return from;
	}

	/**
	 * From.
	 *
	 * @param from the from
	 * @return the search context
	 */
	public SearchContext from(int from) {
		this.from = from;
		return this;
	}

	/**
	 * Size.
	 *
	 * @return the int
	 */
	public int size() {
		return size;
	}

	/**
	 * Size.
	 *
	 * @param size the size
	 * @return the search context
	 */
	public SearchContext size(int size) {
		this.size = size;
		return this;
	}

	/**
	 * Checks for field names.
	 *
	 * @return true, if successful
	 */
	public boolean hasFieldNames() {
		return fieldNames != null;
	}

	/**
	 * Field names.
	 *
	 * @return the list
	 */
	public List<String> fieldNames() {
		if (fieldNames == null) {
			fieldNames = Lists.newArrayList();
		}
		return fieldNames;
	}

	/**
	 * Empty field names.
	 */
	public void emptyFieldNames() {
		this.fieldNames = ImmutableList.of();
	}

	/**
	 * Explain.
	 *
	 * @return true, if successful
	 */
	public boolean explain() {
		return explain;
	}

	/**
	 * Explain.
	 *
	 * @param explain the explain
	 */
	public void explain(boolean explain) {
		this.explain = explain;
	}

	/**
	 * Group stats.
	 *
	 * @return the list
	 */
	@Nullable
	public List<String> groupStats() {
		return this.groupStats;
	}

	/**
	 * Group stats.
	 *
	 * @param groupStats the group stats
	 */
	public void groupStats(List<String> groupStats) {
		this.groupStats = groupStats;
	}

	/**
	 * Version.
	 *
	 * @return true, if successful
	 */
	public boolean version() {
		return version;
	}

	/**
	 * Version.
	 *
	 * @param version the version
	 */
	public void version(boolean version) {
		this.version = version;
	}

	/**
	 * Doc ids to load.
	 *
	 * @return the int[]
	 */
	public int[] docIdsToLoad() {
		return docIdsToLoad;
	}

	/**
	 * Doc ids to load from.
	 *
	 * @return the int
	 */
	public int docIdsToLoadFrom() {
		return docsIdsToLoadFrom;
	}

	/**
	 * Doc ids to load size.
	 *
	 * @return the int
	 */
	public int docIdsToLoadSize() {
		return docsIdsToLoadSize;
	}

	/**
	 * Doc ids to load.
	 *
	 * @param docIdsToLoad the doc ids to load
	 * @param docsIdsToLoadFrom the docs ids to load from
	 * @param docsIdsToLoadSize the docs ids to load size
	 * @return the search context
	 */
	public SearchContext docIdsToLoad(int[] docIdsToLoad, int docsIdsToLoadFrom, int docsIdsToLoadSize) {
		this.docIdsToLoad = docIdsToLoad;
		this.docsIdsToLoadFrom = docsIdsToLoadFrom;
		this.docsIdsToLoadSize = docsIdsToLoadSize;
		return this;
	}

	/**
	 * Accessed.
	 *
	 * @param accessTime the access time
	 */
	public void accessed(long accessTime) {
		this.lastAccessTime = accessTime;
	}

	/**
	 * Last access time.
	 *
	 * @return the long
	 */
	public long lastAccessTime() {
		return this.lastAccessTime;
	}

	/**
	 * Keep alive.
	 *
	 * @return the long
	 */
	public long keepAlive() {
		return this.keepAlive;
	}

	/**
	 * Keep alive.
	 *
	 * @param keepAlive the keep alive
	 */
	public void keepAlive(long keepAlive) {
		this.keepAlive = keepAlive;
	}

	/**
	 * Lookup.
	 *
	 * @return the search lookup
	 */
	public SearchLookup lookup() {
		if (searchLookup == null) {
			searchLookup = new SearchLookup(mapperService(), fieldDataCache());
		}
		return searchLookup;
	}

	/**
	 * Dfs result.
	 *
	 * @return the dfs search result
	 */
	public DfsSearchResult dfsResult() {
		return dfsResult;
	}

	/**
	 * Query result.
	 *
	 * @return the query search result
	 */
	public QuerySearchResult queryResult() {
		return queryResult;
	}

	/**
	 * Fetch result.
	 *
	 * @return the fetch search result
	 */
	public FetchSearchResult fetchResult() {
		return fetchResult;
	}

	/**
	 * Scope phases.
	 *
	 * @return the list
	 */
	public List<ScopePhase> scopePhases() {
		return this.scopePhases;
	}

	/**
	 * Adds the scope phase.
	 *
	 * @param scopePhase the scope phase
	 */
	public void addScopePhase(ScopePhase scopePhase) {
		if (this.scopePhases == null) {
			this.scopePhases = new ArrayList<ScopePhase>();
		}
		this.scopePhases.add(scopePhase);
	}

	/**
	 * Nested queries.
	 *
	 * @return the map
	 */
	public Map<String, BlockJoinQuery> nestedQueries() {
		return this.nestedQueries;
	}

	/**
	 * Adds the nested query.
	 *
	 * @param scope the scope
	 * @param query the query
	 */
	public void addNestedQuery(String scope, BlockJoinQuery query) {
		if (nestedQueries == null) {
			nestedQueries = new HashMap<String, BlockJoinQuery>();
		}
		nestedQueries.put(scope, query);
	}

	/**
	 * Scan context.
	 *
	 * @return the scan context
	 */
	public ScanContext scanContext() {
		if (scanContext == null) {
			scanContext = new ScanContext();
		}
		return scanContext;
	}

	/**
	 * Smart field mappers.
	 *
	 * @param name the name
	 * @return the mapper service. smart name field mappers
	 */
	public MapperService.SmartNameFieldMappers smartFieldMappers(String name) {
		return mapperService().smartName(name, request.types());
	}

	/**
	 * Smart name field mappers.
	 *
	 * @param name the name
	 * @return the field mappers
	 */
	public FieldMappers smartNameFieldMappers(String name) {
		return mapperService().smartNameFieldMappers(name, request.types());
	}

	/**
	 * Smart name field mapper.
	 *
	 * @param name the name
	 * @return the field mapper
	 */
	public FieldMapper smartNameFieldMapper(String name) {
		return mapperService().smartNameFieldMapper(name, request.types());
	}

	/**
	 * Smart name object mapper.
	 *
	 * @param name the name
	 * @return the mapper service. smart name object mapper
	 */
	public MapperService.SmartNameObjectMapper smartNameObjectMapper(String name) {
		return mapperService().smartNameObjectMapper(name, request.types());
	}
}
