/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexQueryParserService.java 2012-7-6 14:29:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.BytesStream;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.analysis.AnalysisService;
import cn.com.rebirth.search.core.index.cache.IndexCache;
import cn.com.rebirth.search.core.index.engine.IndexEngine;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.internal.AllFieldMapper;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.similarity.SimilarityService;
import cn.com.rebirth.search.core.indices.query.IndicesQueriesRegistry;
import cn.com.rebirth.search.core.script.ScriptService;

import com.google.common.collect.ImmutableMap;

/**
 * The Class IndexQueryParserService.
 *
 * @author l.xue.nong
 */
public class IndexQueryParserService extends AbstractIndexComponent {

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static final class Defaults {

		/** The Constant QUERY_PREFIX. */
		public static final String QUERY_PREFIX = "index.queryparser.query";

		/** The Constant FILTER_PREFIX. */
		public static final String FILTER_PREFIX = "index.queryparser.filter";
	}

	/** The cache. */
	private ThreadLocal<QueryParseContext> cache = new ThreadLocal<QueryParseContext>() {
		@Override
		protected QueryParseContext initialValue() {
			return new QueryParseContext(index, IndexQueryParserService.this);
		}
	};

	/** The analysis service. */
	final AnalysisService analysisService;

	/** The script service. */
	final ScriptService scriptService;

	/** The mapper service. */
	final MapperService mapperService;

	/** The similarity service. */
	final SimilarityService similarityService;

	/** The index cache. */
	final IndexCache indexCache;

	/** The index engine. */
	final IndexEngine indexEngine;

	/** The query parsers. */
	private final Map<String, QueryParser> queryParsers;

	/** The filter parsers. */
	private final Map<String, FilterParser> filterParsers;

	/** The default field. */
	private String defaultField;

	/**
	 * Instantiates a new index query parser service.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param indicesQueriesRegistry the indices queries registry
	 * @param scriptService the script service
	 * @param analysisService the analysis service
	 * @param mapperService the mapper service
	 * @param indexCache the index cache
	 * @param indexEngine the index engine
	 * @param similarityService the similarity service
	 * @param namedQueryParsers the named query parsers
	 * @param namedFilterParsers the named filter parsers
	 */
	@Inject
	public IndexQueryParserService(Index index, @IndexSettings Settings indexSettings,
			IndicesQueriesRegistry indicesQueriesRegistry, ScriptService scriptService,
			AnalysisService analysisService, MapperService mapperService, IndexCache indexCache,
			IndexEngine indexEngine, @Nullable SimilarityService similarityService,
			@Nullable Map<String, QueryParserFactory> namedQueryParsers,
			@Nullable Map<String, FilterParserFactory> namedFilterParsers) {
		super(index, indexSettings);
		this.scriptService = scriptService;
		this.analysisService = analysisService;
		this.mapperService = mapperService;
		this.similarityService = similarityService;
		this.indexCache = indexCache;
		this.indexEngine = indexEngine;

		this.defaultField = indexSettings.get("index.query.default_field", AllFieldMapper.NAME);

		List<QueryParser> queryParsers = newArrayList();
		if (namedQueryParsers != null) {
			Map<String, Settings> queryParserGroups = indexSettings
					.getGroups(IndexQueryParserService.Defaults.QUERY_PREFIX);
			for (Map.Entry<String, QueryParserFactory> entry : namedQueryParsers.entrySet()) {
				String queryParserName = entry.getKey();
				QueryParserFactory queryParserFactory = entry.getValue();
				Settings queryParserSettings = queryParserGroups.get(queryParserName);
				if (queryParserSettings == null) {
					queryParserSettings = ImmutableSettings.Builder.EMPTY_SETTINGS;
				}
				queryParsers.add(queryParserFactory.create(queryParserName, queryParserSettings));
			}
		}

		Map<String, QueryParser> queryParsersMap = newHashMap();
		queryParsersMap.putAll(indicesQueriesRegistry.queryParsers());
		if (queryParsers != null) {
			for (QueryParser queryParser : queryParsers) {
				add(queryParsersMap, queryParser);
			}
		}
		this.queryParsers = ImmutableMap.copyOf(queryParsersMap);

		List<FilterParser> filterParsers = newArrayList();
		if (namedFilterParsers != null) {
			Map<String, Settings> filterParserGroups = indexSettings
					.getGroups(IndexQueryParserService.Defaults.FILTER_PREFIX);
			for (Map.Entry<String, FilterParserFactory> entry : namedFilterParsers.entrySet()) {
				String filterParserName = entry.getKey();
				FilterParserFactory filterParserFactory = entry.getValue();
				Settings filterParserSettings = filterParserGroups.get(filterParserName);
				if (filterParserSettings == null) {
					filterParserSettings = ImmutableSettings.Builder.EMPTY_SETTINGS;
				}
				filterParsers.add(filterParserFactory.create(filterParserName, filterParserSettings));
			}
		}

		Map<String, FilterParser> filterParsersMap = newHashMap();
		filterParsersMap.putAll(indicesQueriesRegistry.filterParsers());
		if (filterParsers != null) {
			for (FilterParser filterParser : filterParsers) {
				add(filterParsersMap, filterParser);
			}
		}
		this.filterParsers = ImmutableMap.copyOf(filterParsersMap);
	}

	/**
	 * Close.
	 */
	public void close() {
		cache.remove();
	}

	/**
	 * Default field.
	 *
	 * @return the string
	 */
	public String defaultField() {
		return this.defaultField;
	}

	/**
	 * Query parser.
	 *
	 * @param name the name
	 * @return the query parser
	 */
	public QueryParser queryParser(String name) {
		return queryParsers.get(name);
	}

	/**
	 * Filter parser.
	 *
	 * @param name the name
	 * @return the filter parser
	 */
	public FilterParser filterParser(String name) {
		return filterParsers.get(name);
	}

	/**
	 * Parses the.
	 *
	 * @param queryBuilder the query builder
	 * @return the parsed query
	 * @throws RebirthException the rebirth exception
	 */
	public ParsedQuery parse(QueryBuilder queryBuilder) throws RebirthException {
		XContentParser parser = null;
		try {
			BytesStream bytes = queryBuilder.buildAsBytes();
			parser = XContentFactory.xContent(bytes.underlyingBytes(), 0, bytes.size()).createParser(
					bytes.underlyingBytes(), 0, bytes.size());
			return parse(cache.get(), parser);
		} catch (QueryParsingException e) {
			throw e;
		} catch (Exception e) {
			throw new QueryParsingException(index, "Failed to parse", e);
		} finally {
			if (parser != null) {
				parser.close();
			}
		}
	}

	/**
	 * Parses the.
	 *
	 * @param source the source
	 * @return the parsed query
	 * @throws RebirthException the rebirth exception
	 */
	public ParsedQuery parse(byte[] source) throws RebirthException {
		return parse(source, 0, source.length);
	}

	/**
	 * Parses the.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @return the parsed query
	 * @throws RebirthException the rebirth exception
	 */
	public ParsedQuery parse(byte[] source, int offset, int length) throws RebirthException {
		XContentParser parser = null;
		try {
			parser = XContentFactory.xContent(source, offset, length).createParser(source, offset, length);
			return parse(cache.get(), parser);
		} catch (QueryParsingException e) {
			throw e;
		} catch (Exception e) {
			throw new QueryParsingException(index, "Failed to parse", e);
		} finally {
			if (parser != null) {
				parser.close();
			}
		}
	}

	/**
	 * Parses the.
	 *
	 * @param source the source
	 * @return the parsed query
	 * @throws QueryParsingException the query parsing exception
	 */
	public ParsedQuery parse(String source) throws QueryParsingException {
		XContentParser parser = null;
		try {
			parser = XContentFactory.xContent(source).createParser(source);
			return parse(cache.get(), parser);
		} catch (QueryParsingException e) {
			throw e;
		} catch (Exception e) {
			throw new QueryParsingException(index, "Failed to parse [" + source + "]", e);
		} finally {
			if (parser != null) {
				parser.close();
			}
		}
	}

	/**
	 * Parses the.
	 *
	 * @param parser the parser
	 * @return the parsed query
	 */
	public ParsedQuery parse(XContentParser parser) {
		try {
			return parse(cache.get(), parser);
		} catch (IOException e) {
			throw new QueryParsingException(index, "Failed to parse", e);
		}
	}

	/**
	 * Parses the inner filter.
	 *
	 * @param parser the parser
	 * @return the filter
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Filter parseInnerFilter(XContentParser parser) throws IOException {
		QueryParseContext context = cache.get();
		context.reset(parser);
		return context.parseInnerFilter();
	}

	/**
	 * Parses the inner query.
	 *
	 * @param parser the parser
	 * @return the query
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Query parseInnerQuery(XContentParser parser) throws IOException {
		QueryParseContext context = cache.get();
		context.reset(parser);
		return context.parseInnerQuery();
	}

	/**
	 * Parses the.
	 *
	 * @param parseContext the parse context
	 * @param parser the parser
	 * @return the parsed query
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws QueryParsingException the query parsing exception
	 */
	private ParsedQuery parse(QueryParseContext parseContext, XContentParser parser) throws IOException,
			QueryParsingException {
		parseContext.reset(parser);
		Query query = parseContext.parseInnerQuery();
		return new ParsedQuery(query, parseContext.copyNamedFilters());
	}

	/**
	 * Adds the.
	 *
	 * @param map the map
	 * @param filterParser the filter parser
	 */
	private void add(Map<String, FilterParser> map, FilterParser filterParser) {
		for (String name : filterParser.names()) {
			map.put(name.intern(), filterParser);
		}
	}

	/**
	 * Adds the.
	 *
	 * @param map the map
	 * @param queryParser the query parser
	 */
	private void add(Map<String, QueryParser> map, QueryParser queryParser) {
		for (String name : queryParser.names()) {
			map.put(name.intern(), queryParser);
		}
	}
}
