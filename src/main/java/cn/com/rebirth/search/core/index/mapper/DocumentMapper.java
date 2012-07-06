/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DocumentMapper.java 2012-7-6 14:30:38 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Filter;

import cn.com.rebirth.commons.Booleans;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.compress.CompressedString;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.Preconditions;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentHelper;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.commons.xcontent.XContentType;
import cn.com.rebirth.search.commons.xcontent.ToXContent.Params;
import cn.com.rebirth.search.core.index.mapper.internal.AllFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.AnalyzerMapper;
import cn.com.rebirth.search.core.index.mapper.internal.BoostFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.IdFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.IndexFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.ParentFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.RoutingFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.SizeFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.SourceFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.TTLFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.TimestampFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.TypeFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.UidFieldMapper;
import cn.com.rebirth.search.core.index.mapper.object.ObjectMapper;
import cn.com.rebirth.search.core.index.mapper.object.RootObjectMapper;
import cn.com.rebirth.search.index.analysis.NamedAnalyzer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * The Class DocumentMapper.
 *
 * @author l.xue.nong
 */
public class DocumentMapper implements ToXContent {

	/**
	 * The Class MergeResult.
	 *
	 * @author l.xue.nong
	 */
	public static class MergeResult {

		/** The conflicts. */
		private final String[] conflicts;

		/**
		 * Instantiates a new merge result.
		 *
		 * @param conflicts the conflicts
		 */
		public MergeResult(String[] conflicts) {
			this.conflicts = conflicts;
		}

		/**
		 * Checks for conflicts.
		 *
		 * @return true, if successful
		 */
		public boolean hasConflicts() {
			return conflicts.length > 0;
		}

		/**
		 * Conflicts.
		 *
		 * @return the string[]
		 */
		public String[] conflicts() {
			return this.conflicts;
		}
	}

	/**
	 * The Class MergeFlags.
	 *
	 * @author l.xue.nong
	 */
	public static class MergeFlags {

		/**
		 * Merge flags.
		 *
		 * @return the merge flags
		 */
		public static MergeFlags mergeFlags() {
			return new MergeFlags();
		}

		/** The simulate. */
		private boolean simulate = true;

		/**
		 * Instantiates a new merge flags.
		 */
		public MergeFlags() {
		}

		/**
		 * Simulate.
		 *
		 * @return true, if successful
		 */
		public boolean simulate() {
			return simulate;
		}

		/**
		 * Simulate.
		 *
		 * @param simulate the simulate
		 * @return the merge flags
		 */
		public MergeFlags simulate(boolean simulate) {
			this.simulate = simulate;
			return this;
		}
	}

	/**
	 * The listener interface for receiving parse events.
	 * The class that is interested in processing a parse
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addParseListener<code> method. When
	 * the parse event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @param <ParseContext> the generic type
	 * @see ParseEvent
	 */
	public static interface ParseListener<ParseContext> {

		/** The Constant EMPTY. */
		public static final ParseListener EMPTY = new ParseListenerAdapter();

		/**
		 * Before field added.
		 *
		 * @param fieldMapper the field mapper
		 * @param fieldable the fieldable
		 * @param parseContent the parse content
		 * @return true, if successful
		 */
		boolean beforeFieldAdded(FieldMapper fieldMapper, Fieldable fieldable, ParseContext parseContent);
	}

	/**
	 * The Class ParseListenerAdapter.
	 *
	 * @author l.xue.nong
	 */
	public static class ParseListenerAdapter implements ParseListener {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.DocumentMapper.ParseListener#beforeFieldAdded(cn.com.rebirth.search.core.index.mapper.FieldMapper, org.apache.lucene.document.Fieldable, java.lang.Object)
		 */
		@Override
		public boolean beforeFieldAdded(FieldMapper fieldMapper, Fieldable fieldable, Object parseContext) {
			return true;
		}
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder {

		/** The root mappers. */
		private Map<Class<? extends RootMapper>, RootMapper> rootMappers = new LinkedHashMap<Class<? extends RootMapper>, RootMapper>();

		/** The index analyzer. */
		private NamedAnalyzer indexAnalyzer;

		/** The search analyzer. */
		private NamedAnalyzer searchAnalyzer;

		/** The index. */
		private final String index;

		/** The index settings. */
		@Nullable
		private final Settings indexSettings;

		/** The root object mapper. */
		private final RootObjectMapper rootObjectMapper;

		/** The meta. */
		private ImmutableMap<String, Object> meta = ImmutableMap.of();

		/** The builder context. */
		private final Mapper.BuilderContext builderContext;

		/**
		 * Instantiates a new builder.
		 *
		 * @param index the index
		 * @param indexSettings the index settings
		 * @param builder the builder
		 */
		public Builder(String index, @Nullable Settings indexSettings, RootObjectMapper.Builder builder) {
			this.index = index;
			this.indexSettings = indexSettings;
			this.builderContext = new Mapper.BuilderContext(indexSettings, new ContentPath(1));
			this.rootObjectMapper = builder.build(builderContext);
			IdFieldMapper idFieldMapper = new IdFieldMapper();
			if (indexSettings != null) {
				String idIndexed = indexSettings.get("index.mapping._id.indexed");
				if (idIndexed != null && Booleans.parseBoolean(idIndexed, false)) {
					idFieldMapper = new IdFieldMapper(Field.Index.NOT_ANALYZED);
				}
			}
			this.rootMappers.put(IdFieldMapper.class, idFieldMapper);

			this.rootMappers.put(SizeFieldMapper.class, new SizeFieldMapper());
			this.rootMappers.put(IndexFieldMapper.class, new IndexFieldMapper());
			this.rootMappers.put(SourceFieldMapper.class, new SourceFieldMapper());
			this.rootMappers.put(TypeFieldMapper.class, new TypeFieldMapper());
			this.rootMappers.put(AnalyzerMapper.class, new AnalyzerMapper());
			this.rootMappers.put(AllFieldMapper.class, new AllFieldMapper());
			this.rootMappers.put(BoostFieldMapper.class, new BoostFieldMapper());
			this.rootMappers.put(RoutingFieldMapper.class, new RoutingFieldMapper());
			this.rootMappers.put(TimestampFieldMapper.class, new TimestampFieldMapper());
			this.rootMappers.put(TTLFieldMapper.class, new TTLFieldMapper());
			this.rootMappers.put(UidFieldMapper.class, new UidFieldMapper());

		}

		/**
		 * Meta.
		 *
		 * @param meta the meta
		 * @return the builder
		 */
		public Builder meta(ImmutableMap<String, Object> meta) {
			this.meta = meta;
			return this;
		}

		/**
		 * Put.
		 *
		 * @param mapper the mapper
		 * @return the builder
		 */
		public Builder put(RootMapper.Builder mapper) {
			RootMapper rootMapper = (RootMapper) mapper.build(builderContext);
			rootMappers.put(rootMapper.getClass(), rootMapper);
			return this;
		}

		/**
		 * Index analyzer.
		 *
		 * @param indexAnalyzer the index analyzer
		 * @return the builder
		 */
		public Builder indexAnalyzer(NamedAnalyzer indexAnalyzer) {
			this.indexAnalyzer = indexAnalyzer;
			return this;
		}

		/**
		 * Checks for index analyzer.
		 *
		 * @return true, if successful
		 */
		public boolean hasIndexAnalyzer() {
			return indexAnalyzer != null;
		}

		/**
		 * Search analyzer.
		 *
		 * @param searchAnalyzer the search analyzer
		 * @return the builder
		 */
		public Builder searchAnalyzer(NamedAnalyzer searchAnalyzer) {
			this.searchAnalyzer = searchAnalyzer;
			return this;
		}

		/**
		 * Checks for search analyzer.
		 *
		 * @return true, if successful
		 */
		public boolean hasSearchAnalyzer() {
			return searchAnalyzer != null;
		}

		/**
		 * Builds the.
		 *
		 * @param docMapperParser the doc mapper parser
		 * @return the document mapper
		 */
		public DocumentMapper build(DocumentMapperParser docMapperParser) {
			Preconditions.checkNotNull(rootObjectMapper, "Mapper builder must have the root object mapper set");
			return new DocumentMapper(index, indexSettings, docMapperParser, rootObjectMapper, meta, indexAnalyzer,
					searchAnalyzer, rootMappers);
		}
	}

	/** The cache. */
	private ThreadLocal<ParseContext> cache = new ThreadLocal<ParseContext>() {
		@Override
		protected ParseContext initialValue() {
			return new ParseContext(index, indexSettings, docMapperParser, DocumentMapper.this, new ContentPath(0));
		}
	};

	/** The index. */
	private final String index;

	/** The index settings. */
	private final Settings indexSettings;

	/** The type. */
	private final String type;

	/** The doc mapper parser. */
	private final DocumentMapperParser docMapperParser;

	/** The meta. */
	private volatile ImmutableMap<String, Object> meta;

	/** The mapping source. */
	private volatile CompressedString mappingSource;

	/** The root object mapper. */
	private final RootObjectMapper rootObjectMapper;

	/** The root mappers. */
	private final ImmutableMap<Class<? extends RootMapper>, RootMapper> rootMappers;

	/** The root mappers ordered. */
	private final RootMapper[] rootMappersOrdered;

	/** The root mappers not included in object. */
	private final RootMapper[] rootMappersNotIncludedInObject;

	/** The index analyzer. */
	private final NamedAnalyzer indexAnalyzer;

	/** The search analyzer. */
	private final NamedAnalyzer searchAnalyzer;

	/** The field mappers. */
	private volatile DocumentFieldMappers fieldMappers;

	/** The object mappers. */
	private volatile ImmutableMap<String, ObjectMapper> objectMappers = ImmutableMap.of();

	/** The field mapper listeners. */
	private final List<FieldMapperListener> fieldMapperListeners = new CopyOnWriteArrayList<FieldMapperListener>();

	/** The object mapper listeners. */
	private final List<ObjectMapperListener> objectMapperListeners = new CopyOnWriteArrayList<ObjectMapperListener>();

	/** The has nested objects. */
	private boolean hasNestedObjects = false;

	/** The type filter. */
	private final Filter typeFilter;

	/** The mutex. */
	private final Object mutex = new Object();

	/** The init mappers added. */
	private boolean initMappersAdded = true;

	/**
	 * Instantiates a new document mapper.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param docMapperParser the doc mapper parser
	 * @param rootObjectMapper the root object mapper
	 * @param meta the meta
	 * @param indexAnalyzer the index analyzer
	 * @param searchAnalyzer the search analyzer
	 * @param rootMappers the root mappers
	 */
	public DocumentMapper(String index, @Nullable Settings indexSettings, DocumentMapperParser docMapperParser,
			RootObjectMapper rootObjectMapper, ImmutableMap<String, Object> meta, NamedAnalyzer indexAnalyzer,
			NamedAnalyzer searchAnalyzer, Map<Class<? extends RootMapper>, RootMapper> rootMappers) {
		this.index = index;
		this.indexSettings = indexSettings;
		this.type = rootObjectMapper.name();
		this.docMapperParser = docMapperParser;
		this.meta = meta;
		this.rootObjectMapper = rootObjectMapper;

		this.rootMappers = ImmutableMap.copyOf(rootMappers);
		this.rootMappersOrdered = rootMappers.values().toArray(new RootMapper[rootMappers.values().size()]);
		List<RootMapper> rootMappersNotIncludedInObjectLst = newArrayList();
		for (RootMapper rootMapper : rootMappersOrdered) {
			if (!rootMapper.includeInObject()) {
				rootMappersNotIncludedInObjectLst.add(rootMapper);
			}
		}
		this.rootMappersNotIncludedInObject = rootMappersNotIncludedInObjectLst
				.toArray(new RootMapper[rootMappersNotIncludedInObjectLst.size()]);

		this.indexAnalyzer = indexAnalyzer;
		this.searchAnalyzer = searchAnalyzer;

		this.typeFilter = typeMapper().fieldFilter(type, null);

		if (rootMapper(ParentFieldMapper.class) != null) {

			rootMapper(RoutingFieldMapper.class).markAsRequired();
		}

		final List<FieldMapper> tempFieldMappers = newArrayList();
		for (RootMapper rootMapper : rootMappersOrdered) {
			if (rootMapper.includeInObject()) {
				rootObjectMapper.putMapper(rootMapper);
			} else {
				if (rootMapper instanceof FieldMapper) {
					tempFieldMappers.add((FieldMapper) rootMapper);
				}
			}
		}

		rootObjectMapper.traverse(new FieldMapperListener() {
			@Override
			public void fieldMapper(FieldMapper fieldMapper) {
				tempFieldMappers.add(fieldMapper);
			}
		});

		this.fieldMappers = new DocumentFieldMappers(this, tempFieldMappers);

		final Map<String, ObjectMapper> objectMappers = Maps.newHashMap();
		rootObjectMapper.traverse(new ObjectMapperListener() {
			@Override
			public void objectMapper(ObjectMapper objectMapper) {
				objectMappers.put(objectMapper.fullPath(), objectMapper);
			}
		});
		this.objectMappers = ImmutableMap.copyOf(objectMappers);
		for (ObjectMapper objectMapper : objectMappers.values()) {
			if (objectMapper.nested().isNested()) {
				hasNestedObjects = true;
			}
		}

		refreshSource();
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
	 * Meta.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, Object> meta() {
		return this.meta;
	}

	/**
	 * Mapping source.
	 *
	 * @return the compressed string
	 */
	public CompressedString mappingSource() {
		return this.mappingSource;
	}

	/**
	 * Root.
	 *
	 * @return the root object mapper
	 */
	public RootObjectMapper root() {
		return this.rootObjectMapper;
	}

	/**
	 * Uid mapper.
	 *
	 * @return the uid field mapper
	 */
	public UidFieldMapper uidMapper() {
		return rootMapper(UidFieldMapper.class);
	}

	/**
	 * Root mapper.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @return the t
	 */
	@SuppressWarnings({ "unchecked" })
	public <T extends RootMapper> T rootMapper(Class<T> type) {
		return (T) rootMappers.get(type);
	}

	/**
	 * Type mapper.
	 *
	 * @return the type field mapper
	 */
	public TypeFieldMapper typeMapper() {
		return rootMapper(TypeFieldMapper.class);
	}

	/**
	 * Source mapper.
	 *
	 * @return the source field mapper
	 */
	public SourceFieldMapper sourceMapper() {
		return rootMapper(SourceFieldMapper.class);
	}

	/**
	 * All field mapper.
	 *
	 * @return the all field mapper
	 */
	public AllFieldMapper allFieldMapper() {
		return rootMapper(AllFieldMapper.class);
	}

	/**
	 * Id field mapper.
	 *
	 * @return the id field mapper
	 */
	public IdFieldMapper idFieldMapper() {
		return rootMapper(IdFieldMapper.class);
	}

	/**
	 * Routing field mapper.
	 *
	 * @return the routing field mapper
	 */
	public RoutingFieldMapper routingFieldMapper() {
		return rootMapper(RoutingFieldMapper.class);
	}

	/**
	 * Parent field mapper.
	 *
	 * @return the parent field mapper
	 */
	public ParentFieldMapper parentFieldMapper() {
		return rootMapper(ParentFieldMapper.class);
	}

	/**
	 * Timestamp field mapper.
	 *
	 * @return the timestamp field mapper
	 */
	public TimestampFieldMapper timestampFieldMapper() {
		return rootMapper(TimestampFieldMapper.class);
	}

	/**
	 * TTL field mapper.
	 *
	 * @return the tTL field mapper
	 */
	public TTLFieldMapper TTLFieldMapper() {
		return rootMapper(TTLFieldMapper.class);
	}

	/**
	 * Index analyzer.
	 *
	 * @return the analyzer
	 */
	public Analyzer indexAnalyzer() {
		return this.indexAnalyzer;
	}

	/**
	 * Search analyzer.
	 *
	 * @return the analyzer
	 */
	public Analyzer searchAnalyzer() {
		return this.searchAnalyzer;
	}

	/**
	 * Type filter.
	 *
	 * @return the filter
	 */
	public Filter typeFilter() {
		return this.typeFilter;
	}

	/**
	 * Checks for nested objects.
	 *
	 * @return true, if successful
	 */
	public boolean hasNestedObjects() {
		return hasNestedObjects;
	}

	/**
	 * Mappers.
	 *
	 * @return the document field mappers
	 */
	public DocumentFieldMappers mappers() {
		return this.fieldMappers;
	}

	/**
	 * Object mappers.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, ObjectMapper> objectMappers() {
		return this.objectMappers;
	}

	/**
	 * Parses the.
	 *
	 * @param source the source
	 * @return the parsed document
	 * @throws MapperParsingException the mapper parsing exception
	 */
	public ParsedDocument parse(byte[] source) throws MapperParsingException {
		return parse(SourceToParse.source(source));
	}

	/**
	 * Parses the.
	 *
	 * @param type the type
	 * @param id the id
	 * @param source the source
	 * @return the parsed document
	 * @throws MapperParsingException the mapper parsing exception
	 */
	public ParsedDocument parse(String type, String id, byte[] source) throws MapperParsingException {
		return parse(SourceToParse.source(source).type(type).id(id));
	}

	/**
	 * Parses the.
	 *
	 * @param source the source
	 * @return the parsed document
	 * @throws MapperParsingException the mapper parsing exception
	 */
	public ParsedDocument parse(SourceToParse source) throws MapperParsingException {
		return parse(source, null);
	}

	/**
	 * Parses the.
	 *
	 * @param source the source
	 * @param listener the listener
	 * @return the parsed document
	 * @throws MapperParsingException the mapper parsing exception
	 */
	public ParsedDocument parse(SourceToParse source, @Nullable ParseListener listener) throws MapperParsingException {
		ParseContext context = cache.get();

		if (source.type() != null && !source.type().equals(this.type)) {
			throw new MapperParsingException("Type mismatch, provide type [" + source.type()
					+ "] but mapper is of type [" + this.type + "]");
		}
		source.type(this.type);

		XContentParser parser = source.parser();
		try {
			if (parser == null) {
				parser = XContentHelper.createParser(source.source(), source.sourceOffset(), source.sourceLength());
			}
			context.reset(parser, new Document(), source, listener);

			if (initMappersAdded) {
				context.addedMapper();
				initMappersAdded = false;
			}

			int countDownTokens = 0;
			XContentParser.Token token = parser.nextToken();
			if (token != XContentParser.Token.START_OBJECT) {
				throw new MapperParsingException("Malformed content, must start with an object");
			}
			boolean emptyDoc = false;
			token = parser.nextToken();
			if (token == XContentParser.Token.END_OBJECT) {

				emptyDoc = true;
			} else if (token != XContentParser.Token.FIELD_NAME) {
				throw new MapperParsingException(
						"Malformed content, after first object, either the type field or the actual properties should exist");
			}
			if (type.equals(parser.currentName())) {

				token = parser.nextToken();
				countDownTokens++;

			}

			for (RootMapper rootMapper : rootMappersOrdered) {
				rootMapper.preParse(context);
			}

			if (!emptyDoc) {
				rootObjectMapper.parse(context);
			}

			for (int i = 0; i < countDownTokens; i++) {
				parser.nextToken();
			}

			for (RootMapper rootMapper : rootMappersOrdered) {
				rootMapper.postParse(context);
			}

			for (RootMapper rootMapper : rootMappersOrdered) {
				rootMapper.validate(context);
			}
		} catch (IOException e) {
			throw new MapperParsingException("Failed to parse", e);
		} finally {

			if (source.parser() == null && parser != null) {
				parser.close();
			}
		}

		if (context.docs().size() > 1) {
			Collections.reverse(context.docs());
		}
		ParsedDocument doc = new ParsedDocument(context.uid(), context.id(), context.type(), source.routing(),
				source.timestamp(), source.ttl(), context.docs(), context.analyzer(), context.source(),
				context.sourceOffset(), context.sourceLength(), context.mappersAdded()).parent(source.parent());

		context.reset(null, null, null, null);
		return doc;
	}

	/**
	 * Adds the field mapper.
	 *
	 * @param fieldMapper the field mapper
	 */
	public void addFieldMapper(FieldMapper fieldMapper) {
		synchronized (mutex) {
			fieldMappers = fieldMappers.concat(this, fieldMapper);
		}
		for (FieldMapperListener listener : fieldMapperListeners) {
			listener.fieldMapper(fieldMapper);
		}
	}

	/**
	 * Adds the field mapper listener.
	 *
	 * @param fieldMapperListener the field mapper listener
	 * @param includeExisting the include existing
	 */
	public void addFieldMapperListener(FieldMapperListener fieldMapperListener, boolean includeExisting) {
		fieldMapperListeners.add(fieldMapperListener);
		if (includeExisting) {
			for (RootMapper rootMapper : rootMappersOrdered) {
				if (!rootMapper.includeInObject() && rootMapper instanceof FieldMapper) {
					fieldMapperListener.fieldMapper((FieldMapper) rootMapper);
				}
			}
			rootObjectMapper.traverse(fieldMapperListener);
		}
	}

	/**
	 * Adds the object mapper.
	 *
	 * @param objectMapper the object mapper
	 */
	public void addObjectMapper(ObjectMapper objectMapper) {
		synchronized (mutex) {
			objectMappers = MapBuilder.newMapBuilder(objectMappers).put(objectMapper.fullPath(), objectMapper)
					.immutableMap();
			if (objectMapper.nested().isNested()) {
				hasNestedObjects = true;
			}
		}
		for (ObjectMapperListener objectMapperListener : objectMapperListeners) {
			objectMapperListener.objectMapper(objectMapper);
		}
	}

	/**
	 * Adds the object mapper listener.
	 *
	 * @param objectMapperListener the object mapper listener
	 * @param includeExisting the include existing
	 */
	public void addObjectMapperListener(ObjectMapperListener objectMapperListener, boolean includeExisting) {
		objectMapperListeners.add(objectMapperListener);
		if (includeExisting) {
			rootObjectMapper.traverse(objectMapperListener);
		}
	}

	/**
	 * Merge.
	 *
	 * @param mergeWith the merge with
	 * @param mergeFlags the merge flags
	 * @return the merge result
	 */
	public synchronized MergeResult merge(DocumentMapper mergeWith, MergeFlags mergeFlags) {
		MergeContext mergeContext = new MergeContext(this, mergeFlags);
		rootObjectMapper.merge(mergeWith.rootObjectMapper, mergeContext);

		for (Map.Entry<Class<? extends RootMapper>, RootMapper> entry : rootMappers.entrySet()) {

			if (entry.getValue().includeInObject()) {
				continue;
			}
			RootMapper mergeWithRootMapper = mergeWith.rootMappers.get(entry.getKey());
			if (mergeWithRootMapper != null) {
				entry.getValue().merge(mergeWithRootMapper, mergeContext);
			}
		}

		if (!mergeFlags.simulate()) {

			meta = mergeWith.meta();

			refreshSource();
		}
		return new MergeResult(mergeContext.buildConflicts());
	}

	/**
	 * Refresh source.
	 *
	 * @throws FailedToGenerateSourceMapperException the failed to generate source mapper exception
	 */
	public void refreshSource() throws FailedToGenerateSourceMapperException {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
			builder.startObject();
			toXContent(builder, ToXContent.EMPTY_PARAMS);
			builder.endObject();
			this.mappingSource = new CompressedString(builder.underlyingBytes(), 0, builder.underlyingBytesLength());
		} catch (Exception e) {
			throw new FailedToGenerateSourceMapperException(e.getMessage(), e);
		}
	}

	/**
	 * Close.
	 */
	public void close() {
		cache.remove();
		rootObjectMapper.close();
		for (RootMapper rootMapper : rootMappersOrdered) {
			rootMapper.close();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		rootObjectMapper.toXContent(builder, params, new ToXContent() {
			@Override
			public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
				if (indexAnalyzer != null && searchAnalyzer != null
						&& indexAnalyzer.name().equals(searchAnalyzer.name()) && !indexAnalyzer.name().startsWith("_")) {
					if (!indexAnalyzer.name().equals("default")) {

						builder.field("analyzer", indexAnalyzer.name());
					}
				} else {
					if (indexAnalyzer != null && !indexAnalyzer.name().startsWith("_")) {
						if (!indexAnalyzer.name().equals("default")) {
							builder.field("index_analyzer", indexAnalyzer.name());
						}
					}
					if (searchAnalyzer != null && !searchAnalyzer.name().startsWith("_")) {
						if (!searchAnalyzer.name().equals("default")) {
							builder.field("search_analyzer", searchAnalyzer.name());
						}
					}
				}

				if (meta != null && !meta.isEmpty()) {
					builder.field("_meta", meta());
				}
				return builder;
			}

		}, rootMappersNotIncludedInObject);
		return builder;
	}
}
