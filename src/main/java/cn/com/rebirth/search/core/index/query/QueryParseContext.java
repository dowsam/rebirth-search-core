/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QueryParseContext.java 2012-7-6 14:29:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.lucene.queryParser.MapperQueryParser;
import org.apache.lucene.queryParser.MultiFieldMapperQueryParser;
import org.apache.lucene.queryParser.MultiFieldQueryParserSettings;
import org.apache.lucene.queryParser.QueryParserSettings;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Similarity;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.analysis.AnalysisService;
import cn.com.rebirth.search.core.index.cache.IndexCache;
import cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter;
import cn.com.rebirth.search.core.index.engine.IndexEngine;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.FieldMappers;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.similarity.SimilarityService;
import cn.com.rebirth.search.core.script.ScriptService;
import cn.com.rebirth.search.core.search.internal.SearchContext;
import cn.com.rebirth.search.core.search.lookup.SearchLookup;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * The Class QueryParseContext.
 *
 * @author l.xue.nong
 */
public class QueryParseContext {

	/** The types context. */
	private static ThreadLocal<String[]> typesContext = new ThreadLocal<String[]>();

	/**
	 * Sets the types.
	 *
	 * @param types the new types
	 */
	public static void setTypes(String[] types) {
		typesContext.set(types);
	}

	/**
	 * Gets the types.
	 *
	 * @return the types
	 */
	public static String[] getTypes() {
		return typesContext.get();
	}

	/**
	 * Sets the types with previous.
	 *
	 * @param types the types
	 * @return the string[]
	 */
	public static String[] setTypesWithPrevious(String[] types) {
		String[] old = typesContext.get();
		setTypes(types);
		return old;
	}

	/**
	 * Removes the types.
	 */
	public static void removeTypes() {
		typesContext.remove();
	}

	/** The index. */
	private final Index index;

	/** The index query parser. */
	IndexQueryParserService indexQueryParser;

	/** The named filters. */
	private final Map<String, Filter> namedFilters = Maps.newHashMap();

	/** The query parser. */
	private final MapperQueryParser queryParser = new MapperQueryParser(this);

	/** The multi field query parser. */
	private final MultiFieldMapperQueryParser multiFieldQueryParser = new MultiFieldMapperQueryParser(this);

	/** The parser. */
	private XContentParser parser;

	/**
	 * Instantiates a new query parse context.
	 *
	 * @param index the index
	 * @param indexQueryParser the index query parser
	 */
	public QueryParseContext(Index index, IndexQueryParserService indexQueryParser) {
		this.index = index;
		this.indexQueryParser = indexQueryParser;
	}

	/**
	 * Reset.
	 *
	 * @param jp the jp
	 */
	public void reset(XContentParser jp) {
		this.lookup = null;
		this.parser = jp;
		this.namedFilters.clear();
	}

	/**
	 * Index.
	 *
	 * @return the index
	 */
	public Index index() {
		return this.index;
	}

	/**
	 * Parser.
	 *
	 * @return the x content parser
	 */
	public XContentParser parser() {
		return parser;
	}

	/**
	 * Analysis service.
	 *
	 * @return the analysis service
	 */
	public AnalysisService analysisService() {
		return indexQueryParser.analysisService;
	}

	/**
	 * Script service.
	 *
	 * @return the script service
	 */
	public ScriptService scriptService() {
		return indexQueryParser.scriptService;
	}

	/**
	 * Mapper service.
	 *
	 * @return the mapper service
	 */
	public MapperService mapperService() {
		return indexQueryParser.mapperService;
	}

	/**
	 * Index engine.
	 *
	 * @return the index engine
	 */
	public IndexEngine indexEngine() {
		return indexQueryParser.indexEngine;
	}

	/**
	 * Similarity service.
	 *
	 * @return the similarity service
	 */
	@Nullable
	public SimilarityService similarityService() {
		return indexQueryParser.similarityService;
	}

	/**
	 * Search similarity.
	 *
	 * @return the similarity
	 */
	public Similarity searchSimilarity() {
		return indexQueryParser.similarityService != null ? indexQueryParser.similarityService
				.defaultSearchSimilarity() : null;
	}

	/**
	 * Index cache.
	 *
	 * @return the index cache
	 */
	public IndexCache indexCache() {
		return indexQueryParser.indexCache;
	}

	/**
	 * Default field.
	 *
	 * @return the string
	 */
	public String defaultField() {
		return indexQueryParser.defaultField();
	}

	/**
	 * Single query parser.
	 *
	 * @param settings the settings
	 * @return the mapper query parser
	 */
	public MapperQueryParser singleQueryParser(QueryParserSettings settings) {
		queryParser.reset(settings);
		return queryParser;
	}

	/**
	 * Multi query parser.
	 *
	 * @param settings the settings
	 * @return the multi field mapper query parser
	 */
	public MultiFieldMapperQueryParser multiQueryParser(MultiFieldQueryParserSettings settings) {
		multiFieldQueryParser.reset(settings);
		return multiFieldQueryParser;
	}

	/**
	 * Cache filter.
	 *
	 * @param filter the filter
	 * @param cacheKey the cache key
	 * @return the filter
	 */
	public Filter cacheFilter(Filter filter, @Nullable CacheKeyFilter.Key cacheKey) {
		if (cacheKey != null) {
			filter = new CacheKeyFilter.Wrapper(filter, cacheKey);
		}
		return indexQueryParser.indexCache.filter().cache(filter);
	}

	/**
	 * Adds the named filter.
	 *
	 * @param name the name
	 * @param filter the filter
	 */
	public void addNamedFilter(String name, Filter filter) {
		namedFilters.put(name, filter);
	}

	/**
	 * Copy named filters.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, Filter> copyNamedFilters() {
		if (namedFilters.isEmpty()) {
			return ImmutableMap.of();
		}
		return ImmutableMap.copyOf(namedFilters);
	}

	/**
	 * Parses the inner query.
	 *
	 * @return the query
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws QueryParsingException the query parsing exception
	 */
	public Query parseInnerQuery() throws IOException, QueryParsingException {
		XContentParser.Token token;
		if (parser.currentToken() != XContentParser.Token.START_OBJECT) {
			token = parser.nextToken();
			if (token != XContentParser.Token.START_OBJECT) {
				throw new QueryParsingException(index, "[_na] query malformed, must start with start_object");
			}
		}
		token = parser.nextToken();
		if (token != XContentParser.Token.FIELD_NAME) {
			throw new QueryParsingException(index, "[_na] query malformed, no field after start_object");
		}
		String queryName = parser.currentName();
		token = parser.nextToken();
		if (token != XContentParser.Token.START_OBJECT && token != XContentParser.Token.START_ARRAY) {
			throw new QueryParsingException(index, "[_na] query malformed, no field after start_object");
		}
		QueryParser queryParser = indexQueryParser.queryParser(queryName);
		if (queryParser == null) {
			throw new QueryParsingException(index, "No query registered for [" + queryName + "]");
		}
		Query result = queryParser.parse(this);
		if (parser.currentToken() == XContentParser.Token.END_OBJECT
				|| parser.currentToken() == XContentParser.Token.END_ARRAY) {
			parser.nextToken();
		}
		return result;
	}

	/**
	 * Parses the inner filter.
	 *
	 * @return the filter
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws QueryParsingException the query parsing exception
	 */
	public Filter parseInnerFilter() throws IOException, QueryParsingException {

		XContentParser.Token token;
		if (parser.currentToken() != XContentParser.Token.START_OBJECT) {
			token = parser.nextToken();
			if (token != XContentParser.Token.START_OBJECT) {
				throw new QueryParsingException(index, "[_na] filter malformed, must start with start_object");
			}
		}
		token = parser.nextToken();
		if (token != XContentParser.Token.FIELD_NAME) {
			throw new QueryParsingException(index, "[_na] filter malformed, no field after start_object");
		}
		String filterName = parser.currentName();

		token = parser.nextToken();
		if (token != XContentParser.Token.START_OBJECT && token != XContentParser.Token.START_ARRAY) {
			throw new QueryParsingException(index, "[_na] filter malformed, no field after start_object");
		}

		FilterParser filterParser = indexQueryParser.filterParser(filterName);
		if (filterParser == null) {
			throw new QueryParsingException(index, "No filter registered for [" + filterName + "]");
		}
		Filter result = filterParser.parse(this);
		if (parser.currentToken() == XContentParser.Token.END_OBJECT
				|| parser.currentToken() == XContentParser.Token.END_ARRAY) {

			parser.nextToken();
		}
		return result;
	}

	/**
	 * Parses the inner filter.
	 *
	 * @param filterName the filter name
	 * @return the filter
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws QueryParsingException the query parsing exception
	 */
	public Filter parseInnerFilter(String filterName) throws IOException, QueryParsingException {
		FilterParser filterParser = indexQueryParser.filterParser(filterName);
		if (filterParser == null) {
			throw new QueryParsingException(index, "No filter registered for [" + filterName + "]");
		}
		Filter result = filterParser.parse(this);

		return result;
	}

	/**
	 * Field mapper.
	 *
	 * @param name the name
	 * @return the field mapper
	 */
	public FieldMapper fieldMapper(String name) {
		FieldMappers fieldMappers = indexQueryParser.mapperService.smartNameFieldMappers(name, getTypes());
		if (fieldMappers == null) {
			return null;
		}
		return fieldMappers.mapper();
	}

	/**
	 * Index name.
	 *
	 * @param name the name
	 * @return the string
	 */
	public String indexName(String name) {
		FieldMapper smartMapper = fieldMapper(name);
		if (smartMapper == null) {
			return name;
		}
		return smartMapper.names().indexName();
	}

	/**
	 * Smart field mappers.
	 *
	 * @param name the name
	 * @return the mapper service. smart name field mappers
	 */
	public MapperService.SmartNameFieldMappers smartFieldMappers(String name) {
		return indexQueryParser.mapperService.smartName(name, getTypes());
	}

	/**
	 * Smart object mapper.
	 *
	 * @param name the name
	 * @return the mapper service. smart name object mapper
	 */
	public MapperService.SmartNameObjectMapper smartObjectMapper(String name) {
		return indexQueryParser.mapperService.smartNameObjectMapper(name, getTypes());
	}

	/**
	 * Query types.
	 *
	 * @return the collection
	 */
	public Collection<String> queryTypes() {
		String[] types = getTypes();
		if (types == null || types.length == 0) {
			return mapperService().types();
		}
		if (types.length == 1 && types[0].equals("_all")) {
			return mapperService().types();
		}
		return Arrays.asList(types);
	}

	/** The lookup. */
	private SearchLookup lookup = null;

	/**
	 * Lookup.
	 *
	 * @return the search lookup
	 */
	public SearchLookup lookup() {
		SearchContext current = SearchContext.current();
		if (current != null) {
			return current.lookup();
		}
		if (lookup == null) {
			lookup = new SearchLookup(mapperService(), indexCache().fieldData());
		}
		return lookup;
	}

	/**
	 * Now in millis.
	 *
	 * @return the long
	 */
	public long nowInMillis() {
		SearchContext current = SearchContext.current();
		if (current != null) {
			return current.nowInMillis();
		}
		return System.currentTimeMillis();
	}
}
