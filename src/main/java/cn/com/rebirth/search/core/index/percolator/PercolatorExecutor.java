/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PercolatorExecutor.java 2012-7-6 14:30:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.percolator;

import static cn.com.rebirth.search.core.index.mapper.SourceToParse.source;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.memory.CustomMemoryIndex;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.Preconditions;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.BytesStream;
import cn.com.rebirth.search.commons.io.FastStringReader;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentHelper;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.cache.IndexCache;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.field.data.FieldData;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.ParsedDocument;
import cn.com.rebirth.search.core.index.mapper.Uid;
import cn.com.rebirth.search.core.index.mapper.internal.UidFieldMapper;
import cn.com.rebirth.search.core.index.query.IndexQueryParserService;
import cn.com.rebirth.search.core.index.query.QueryBuilder;
import cn.com.rebirth.search.core.index.query.QueryBuilders;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.indices.IndicesService;

import com.google.common.collect.ImmutableMap;

/**
 * The Class PercolatorExecutor.
 *
 * @author l.xue.nong
 */
public class PercolatorExecutor extends AbstractIndexComponent {

	/**
	 * The Class SourceRequest.
	 *
	 * @author l.xue.nong
	 */
	public static class SourceRequest {

		/** The type. */
		private final String type;

		/** The source. */
		private final byte[] source;

		/** The offset. */
		private final int offset;

		/** The length. */
		private final int length;

		/**
		 * Instantiates a new source request.
		 *
		 * @param type the type
		 * @param source the source
		 */
		public SourceRequest(String type, byte[] source) {
			this(type, source, 0, source.length);
		}

		/**
		 * Instantiates a new source request.
		 *
		 * @param type the type
		 * @param source the source
		 * @param offset the offset
		 * @param length the length
		 */
		public SourceRequest(String type, byte[] source, int offset, int length) {
			this.type = type;
			this.source = source;
			this.offset = offset;
			this.length = length;
		}

		/**
		 * Type.
		 *
		 * @return the string
		 */
		public String type() {
			return this.type;
		}

		/**
		 * Source.
		 *
		 * @return the byte[]
		 */
		public byte[] source() {
			return source;
		}

		/**
		 * Offset.
		 *
		 * @return the int
		 */
		public int offset() {
			return this.offset;
		}

		/**
		 * Length.
		 *
		 * @return the int
		 */
		public int length() {
			return this.length;
		}
	}

	/**
	 * The Class DocAndSourceQueryRequest.
	 *
	 * @author l.xue.nong
	 */
	public static class DocAndSourceQueryRequest {

		/** The doc. */
		private final ParsedDocument doc;

		/** The query. */
		@Nullable
		private final String query;

		/**
		 * Instantiates a new doc and source query request.
		 *
		 * @param doc the doc
		 * @param query the query
		 */
		public DocAndSourceQueryRequest(ParsedDocument doc, @Nullable String query) {
			this.doc = doc;
			this.query = query;
		}

		/**
		 * Doc.
		 *
		 * @return the parsed document
		 */
		public ParsedDocument doc() {
			return this.doc;
		}

		/**
		 * Query.
		 *
		 * @return the string
		 */
		@Nullable
		String query() {
			return this.query;
		}
	}

	/**
	 * The Class DocAndQueryRequest.
	 *
	 * @author l.xue.nong
	 */
	public static class DocAndQueryRequest {

		/** The doc. */
		private final ParsedDocument doc;

		/** The query. */
		@Nullable
		private final Query query;

		/**
		 * Instantiates a new doc and query request.
		 *
		 * @param doc the doc
		 * @param query the query
		 */
		public DocAndQueryRequest(ParsedDocument doc, @Nullable Query query) {
			this.doc = doc;
			this.query = query;
		}

		/**
		 * Doc.
		 *
		 * @return the parsed document
		 */
		public ParsedDocument doc() {
			return this.doc;
		}

		/**
		 * Query.
		 *
		 * @return the query
		 */
		@Nullable
		Query query() {
			return this.query;
		}
	}

	/**
	 * The Class Response.
	 *
	 * @author l.xue.nong
	 */
	public static final class Response {

		/** The matches. */
		private final List<String> matches;

		/** The mappers added. */
		private final boolean mappersAdded;

		/**
		 * Instantiates a new response.
		 *
		 * @param matches the matches
		 * @param mappersAdded the mappers added
		 */
		public Response(List<String> matches, boolean mappersAdded) {
			this.matches = matches;
			this.mappersAdded = mappersAdded;
		}

		/**
		 * Mappers added.
		 *
		 * @return true, if successful
		 */
		public boolean mappersAdded() {
			return this.mappersAdded;
		}

		/**
		 * Matches.
		 *
		 * @return the list
		 */
		public List<String> matches() {
			return matches;
		}
	}

	/** The mapper service. */
	private final MapperService mapperService;

	/** The query parser service. */
	private final IndexQueryParserService queryParserService;

	/** The index cache. */
	private final IndexCache indexCache;

	/** The queries. */
	private volatile ImmutableMap<String, Query> queries = ImmutableMap.of();

	/** The indices service. */
	private IndicesService indicesService;

	/**
	 * Instantiates a new percolator executor.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param mapperService the mapper service
	 * @param queryParserService the query parser service
	 * @param indexCache the index cache
	 */
	@Inject
	public PercolatorExecutor(Index index, @IndexSettings Settings indexSettings, MapperService mapperService,
			IndexQueryParserService queryParserService, IndexCache indexCache) {
		super(index, indexSettings);
		this.mapperService = mapperService;
		this.queryParserService = queryParserService;
		this.indexCache = indexCache;
	}

	/**
	 * Sets the indices service.
	 *
	 * @param indicesService the new indices service
	 */
	public void setIndicesService(IndicesService indicesService) {
		this.indicesService = indicesService;
	}

	/**
	 * Close.
	 */
	public synchronized void close() {
		ImmutableMap<String, Query> old = queries;
		queries = ImmutableMap.of();
		old.clear();
	}

	/**
	 * Adds the query.
	 *
	 * @param name the name
	 * @param queryBuilder the query builder
	 * @throws RebirthException the rebirth exception
	 */
	public void addQuery(String name, QueryBuilder queryBuilder) throws RebirthException {
		try {
			XContentBuilder builder = XContentFactory.smileBuilder().startObject().field("query", queryBuilder)
					.endObject();
			BytesStream unsafeBytes = builder.underlyingStream();
			addQuery(name, unsafeBytes.underlyingBytes(), 0, unsafeBytes.size());
		} catch (IOException e) {
			throw new RebirthException("Failed to add query [" + name + "]", e);
		}
	}

	/**
	 * Adds the query.
	 *
	 * @param name the name
	 * @param source the source
	 * @throws RebirthException the rebirth exception
	 */
	public void addQuery(String name, byte[] source) throws RebirthException {
		addQuery(name, source, 0, source.length);
	}

	/**
	 * Adds the query.
	 *
	 * @param name the name
	 * @param source the source
	 * @param sourceOffset the source offset
	 * @param sourceLength the source length
	 * @throws RebirthException the rebirth exception
	 */
	public void addQuery(String name, byte[] source, int sourceOffset, int sourceLength) throws RebirthException {
		addQuery(name, parseQuery(name, source, sourceOffset, sourceLength));
	}

	/**
	 * Parses the query.
	 *
	 * @param name the name
	 * @param source the source
	 * @param sourceOffset the source offset
	 * @param sourceLength the source length
	 * @return the query
	 * @throws RebirthException the rebirth exception
	 */
	public Query parseQuery(String name, byte[] source, int sourceOffset, int sourceLength) throws RebirthException {
		XContentParser parser = null;
		try {
			parser = XContentHelper.createParser(source, sourceOffset, sourceLength);
			Query query = null;
			String currentFieldName = null;
			XContentParser.Token token = parser.nextToken();
			if (token != XContentParser.Token.START_OBJECT) {
				throw new RebirthException("Failed to add query [" + name + "], not starting with OBJECT");
			}
			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.FIELD_NAME) {
					currentFieldName = parser.currentName();
				} else if (token == XContentParser.Token.START_OBJECT) {
					if ("query".equals(currentFieldName)) {
						query = queryParserService.parse(parser).query();
						break;
					} else {
						parser.skipChildren();
					}
				} else if (token == XContentParser.Token.START_ARRAY) {
					parser.skipChildren();
				}
			}
			return query;
		} catch (IOException e) {
			throw new RebirthException("Failed to add query [" + name + "]", e);
		} finally {
			if (parser != null) {
				parser.close();
			}
		}
	}

	/**
	 * Adds the query.
	 *
	 * @param name the name
	 * @param query the query
	 */
	public synchronized void addQuery(String name, Query query) {
		Preconditions.checkArgument(query != null, "query must be provided for percolate request");
		this.queries = MapBuilder.newMapBuilder(queries).put(name, query).immutableMap();
	}

	/**
	 * Removes the query.
	 *
	 * @param name the name
	 */
	public synchronized void removeQuery(String name) {
		this.queries = MapBuilder.newMapBuilder(queries).remove(name).immutableMap();
	}

	/**
	 * Adds the queries.
	 *
	 * @param queries the queries
	 */
	public synchronized void addQueries(Map<String, Query> queries) {
		this.queries = MapBuilder.newMapBuilder(this.queries).putAll(queries).immutableMap();
	}

	/**
	 * Percolate.
	 *
	 * @param request the request
	 * @return the response
	 * @throws RebirthException the rebirth exception
	 */
	public Response percolate(final SourceRequest request) throws RebirthException {
		Query query = null;
		ParsedDocument doc = null;
		XContentParser parser = null;
		try {

			parser = XContentFactory.xContent(request.source(), request.offset(), request.length()).createParser(
					request.source(), request.offset(), request.length());
			String currentFieldName = null;
			XContentParser.Token token;
			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.FIELD_NAME) {
					currentFieldName = parser.currentName();

					if ("doc".equals(currentFieldName)) {
						DocumentMapper docMapper = mapperService.documentMapperWithAutoCreate(request.type());
						doc = docMapper.parse(source(parser).type(request.type()).flyweight(true));
					}
				} else if (token == XContentParser.Token.START_OBJECT) {
					if ("query".equals(currentFieldName)) {
						query = queryParserService.parse(parser).query();
					}
				} else if (token == null) {
					break;
				}
			}
		} catch (IOException e) {
			throw new PercolatorException(index, "failed to parse request", e);
		} finally {
			if (parser != null) {
				parser.close();
			}
		}

		if (doc == null) {
			throw new PercolatorException(index, "No doc to percolate in the request");
		}

		return percolate(new DocAndQueryRequest(doc, query));
	}

	/**
	 * Percolate.
	 *
	 * @param request the request
	 * @return the response
	 * @throws RebirthException the rebirth exception
	 */
	public Response percolate(DocAndSourceQueryRequest request) throws RebirthException {
		Query query = null;
		if (Strings.hasLength(request.query()) && !request.query().equals("*")) {
			query = queryParserService.parse(QueryBuilders.queryString(request.query())).query();
		}
		return percolate(new DocAndQueryRequest(request.doc(), query));
	}

	/**
	 * Percolate.
	 *
	 * @param request the request
	 * @return the response
	 * @throws RebirthException the rebirth exception
	 */
	public Response percolate(DocAndQueryRequest request) throws RebirthException {

		final CustomMemoryIndex memoryIndex = new CustomMemoryIndex();

		for (Fieldable field : request.doc().rootDoc().getFields()) {
			if (!field.isIndexed()) {
				continue;
			}

			if (field.name().equals(UidFieldMapper.NAME)) {
				continue;
			}
			TokenStream tokenStream = field.tokenStreamValue();
			if (tokenStream != null) {
				memoryIndex.addField(field.name(), tokenStream, field.getBoost());
			} else {
				Reader reader = field.readerValue();
				if (reader != null) {
					try {
						memoryIndex.addField(field.name(),
								request.doc().analyzer().reusableTokenStream(field.name(), reader), field.getBoost()
										* request.doc().rootDoc().getBoost());
					} catch (IOException e) {
						throw new MapperParsingException("Failed to analyze field [" + field.name() + "]", e);
					}
				} else {
					String value = field.stringValue();
					if (value != null) {
						try {
							memoryIndex.addField(
									field.name(),
									request.doc().analyzer()
											.reusableTokenStream(field.name(), new FastStringReader(value)),
									field.getBoost() * request.doc().rootDoc().getBoost());
						} catch (IOException e) {
							throw new MapperParsingException("Failed to analyze field [" + field.name() + "]", e);
						}
					}
				}
			}
		}

		final IndexSearcher searcher = memoryIndex.createSearcher();

		List<String> matches = new ArrayList<String>();
		if (request.query() == null) {
			Lucene.ExistsCollector collector = new Lucene.ExistsCollector();
			for (Map.Entry<String, Query> entry : queries.entrySet()) {
				collector.reset();
				try {
					searcher.search(entry.getValue(), collector);
				} catch (IOException e) {
					logger.warn("[" + entry.getKey() + "] failed to execute query", e);
				}

				if (collector.exists()) {
					matches.add(entry.getKey());
				}
			}
		} else {
			IndexService percolatorIndex = indicesService.indexService(PercolatorService.INDEX_NAME);
			if (percolatorIndex == null) {
				throw new PercolateIndexUnavailable(new Index(PercolatorService.INDEX_NAME));
			}
			if (percolatorIndex.numberOfShards() == 0) {
				throw new PercolateIndexUnavailable(new Index(PercolatorService.INDEX_NAME));
			}
			IndexShard percolatorShard = percolatorIndex.shard(0);
			Engine.Searcher percolatorSearcher = percolatorShard.searcher();
			try {
				percolatorSearcher.searcher().search(request.query(),
						new QueryCollector(queries, searcher, percolatorIndex, matches));
			} catch (IOException e) {
				logger.warn("failed to execute", e);
			} finally {
				percolatorSearcher.release();
			}
		}

		indexCache.clear(searcher.getIndexReader());

		return new Response(matches, request.doc().mappersAdded());
	}

	/**
	 * The Class QueryCollector.
	 *
	 * @author l.xue.nong
	 */
	static class QueryCollector extends Collector {

		/** The searcher. */
		private final IndexSearcher searcher;

		/** The percolator index. */
		private final IndexService percolatorIndex;

		/** The matches. */
		private final List<String> matches;

		/** The queries. */
		private final ImmutableMap<String, Query> queries;

		/** The logger. */
		private final Logger logger = LoggerFactory.getLogger(getClass());

		/** The collector. */
		private final Lucene.ExistsCollector collector = new Lucene.ExistsCollector();

		/**
		 * Instantiates a new query collector.
		 *
		 * @param queries the queries
		 * @param searcher the searcher
		 * @param percolatorIndex the percolator index
		 * @param matches the matches
		 */
		QueryCollector(ImmutableMap<String, Query> queries, IndexSearcher searcher, IndexService percolatorIndex,
				List<String> matches) {
			this.queries = queries;
			this.searcher = searcher;
			this.percolatorIndex = percolatorIndex;
			this.matches = matches;
		}

		/** The field data. */
		private FieldData fieldData;

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Collector#setScorer(org.apache.lucene.search.Scorer)
		 */
		@Override
		public void setScorer(Scorer scorer) throws IOException {
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Collector#collect(int)
		 */
		@Override
		public void collect(int doc) throws IOException {
			String uid = fieldData.stringValue(doc);
			if (uid == null) {
				return;
			}
			String id = Uid.idFromUid(uid);
			Query query = queries.get(id);
			if (query == null) {

				return;
			}

			try {
				searcher.search(query, collector);
				if (collector.exists()) {
					matches.add(id);
				}
			} catch (IOException e) {
				logger.warn("[" + id + "] failed to execute query", e);
			}
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Collector#setNextReader(org.apache.lucene.index.IndexReader, int)
		 */
		@Override
		public void setNextReader(IndexReader reader, int docBase) throws IOException {

			fieldData = percolatorIndex.cache().fieldData()
					.cache(FieldDataType.DefaultTypes.STRING, reader, UidFieldMapper.NAME);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Collector#acceptsDocsOutOfOrder()
		 */
		@Override
		public boolean acceptsDocsOutOfOrder() {
			return true;
		}
	}
}
