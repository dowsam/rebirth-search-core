/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DocumentMapperParser.java 2012-3-29 15:02:40 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.collect.Tuple;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentHelper;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.analysis.AnalysisService;
import cn.com.rebirth.search.core.index.mapper.core.BinaryFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.BooleanFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.ByteFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.DateFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.DoubleFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.FloatFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.IntegerFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.LongFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.ShortFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.StringFieldMapper;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldMapper;
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
import cn.com.rebirth.search.core.index.mapper.ip.IpFieldMapper;
import cn.com.rebirth.search.core.index.mapper.multifield.MultiFieldMapper;
import cn.com.rebirth.search.core.index.mapper.object.ObjectMapper;
import cn.com.rebirth.search.core.index.mapper.object.RootObjectMapper;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.index.analysis.NamedAnalyzer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;


/**
 * The Class DocumentMapperParser.
 *
 * @author l.xue.nong
 */
public class DocumentMapperParser extends AbstractIndexComponent {

	
	/** The analysis service. */
	final AnalysisService analysisService;

	
	/** The root object type parser. */
	private final RootObjectMapper.TypeParser rootObjectTypeParser = new RootObjectMapper.TypeParser();

	
	/** The type parsers mutex. */
	private final Object typeParsersMutex = new Object();

	
	/** The type parsers. */
	private volatile ImmutableMap<String, Mapper.TypeParser> typeParsers;

	
	/** The root type parsers. */
	private volatile ImmutableMap<String, Mapper.TypeParser> rootTypeParsers;

	
	/**
	 * Instantiates a new document mapper parser.
	 *
	 * @param index the index
	 * @param analysisService the analysis service
	 */
	public DocumentMapperParser(Index index, AnalysisService analysisService) {
		this(index, ImmutableSettings.Builder.EMPTY_SETTINGS, analysisService);
	}

	
	/**
	 * Instantiates a new document mapper parser.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param analysisService the analysis service
	 */
	public DocumentMapperParser(Index index, @IndexSettings Settings indexSettings, AnalysisService analysisService) {
		super(index, indexSettings);
		this.analysisService = analysisService;
		typeParsers = new MapBuilder<String, Mapper.TypeParser>()
				.put(ByteFieldMapper.CONTENT_TYPE, new ByteFieldMapper.TypeParser())
				.put(ShortFieldMapper.CONTENT_TYPE, new ShortFieldMapper.TypeParser())
				.put(IntegerFieldMapper.CONTENT_TYPE, new IntegerFieldMapper.TypeParser())
				.put(LongFieldMapper.CONTENT_TYPE, new LongFieldMapper.TypeParser())
				.put(FloatFieldMapper.CONTENT_TYPE, new FloatFieldMapper.TypeParser())
				.put(DoubleFieldMapper.CONTENT_TYPE, new DoubleFieldMapper.TypeParser())
				.put(BooleanFieldMapper.CONTENT_TYPE, new BooleanFieldMapper.TypeParser())
				.put(BinaryFieldMapper.CONTENT_TYPE, new BinaryFieldMapper.TypeParser())
				.put(DateFieldMapper.CONTENT_TYPE, new DateFieldMapper.TypeParser())
				.put(IpFieldMapper.CONTENT_TYPE, new IpFieldMapper.TypeParser())
				.put(StringFieldMapper.CONTENT_TYPE, new StringFieldMapper.TypeParser())
				.put(ObjectMapper.CONTENT_TYPE, new ObjectMapper.TypeParser())
				.put(ObjectMapper.NESTED_CONTENT_TYPE, new ObjectMapper.TypeParser())
				.put(MultiFieldMapper.CONTENT_TYPE, new MultiFieldMapper.TypeParser())
				.put(GeoPointFieldMapper.CONTENT_TYPE, new GeoPointFieldMapper.TypeParser()).immutableMap();

		rootTypeParsers = new MapBuilder<String, Mapper.TypeParser>()
				.put(SizeFieldMapper.NAME, new SizeFieldMapper.TypeParser())
				.put(IndexFieldMapper.NAME, new IndexFieldMapper.TypeParser())
				.put(SourceFieldMapper.NAME, new SourceFieldMapper.TypeParser())
				.put(TypeFieldMapper.NAME, new TypeFieldMapper.TypeParser())
				.put(AllFieldMapper.NAME, new AllFieldMapper.TypeParser())
				.put(AnalyzerMapper.NAME, new AnalyzerMapper.TypeParser())
				.put(BoostFieldMapper.NAME, new BoostFieldMapper.TypeParser())
				.put(ParentFieldMapper.NAME, new ParentFieldMapper.TypeParser())
				.put(RoutingFieldMapper.NAME, new RoutingFieldMapper.TypeParser())
				.put(TimestampFieldMapper.NAME, new TimestampFieldMapper.TypeParser())
				.put(TTLFieldMapper.NAME, new TTLFieldMapper.TypeParser())
				.put(UidFieldMapper.NAME, new UidFieldMapper.TypeParser())
				.put(IdFieldMapper.NAME, new IdFieldMapper.TypeParser()).immutableMap();
	}

	
	/**
	 * Put type parser.
	 *
	 * @param type the type
	 * @param typeParser the type parser
	 */
	public void putTypeParser(String type, Mapper.TypeParser typeParser) {
		synchronized (typeParsersMutex) {
			typeParsers = new MapBuilder<String, Mapper.TypeParser>().putAll(typeParsers).put(type, typeParser)
					.immutableMap();
		}
	}

	
	/**
	 * Put root type parser.
	 *
	 * @param type the type
	 * @param typeParser the type parser
	 */
	public void putRootTypeParser(String type, Mapper.TypeParser typeParser) {
		synchronized (typeParsersMutex) {
			rootTypeParsers = new MapBuilder<String, Mapper.TypeParser>().putAll(typeParsers).put(type, typeParser)
					.immutableMap();
		}
	}

	
	/**
	 * Parser context.
	 *
	 * @return the mapper. type parser. parser context
	 */
	public Mapper.TypeParser.ParserContext parserContext() {
		return new Mapper.TypeParser.ParserContext(analysisService, typeParsers);
	}

	
	/**
	 * Parses the.
	 *
	 * @param source the source
	 * @return the document mapper
	 * @throws MapperParsingException the mapper parsing exception
	 */
	public DocumentMapper parse(String source) throws MapperParsingException {
		return parse(null, source);
	}

	
	/**
	 * Parses the.
	 *
	 * @param type the type
	 * @param source the source
	 * @return the document mapper
	 * @throws MapperParsingException the mapper parsing exception
	 */
	public DocumentMapper parse(@Nullable String type, String source) throws MapperParsingException {
		return parse(type, source, null);
	}

	
	/**
	 * Parses the.
	 *
	 * @param type the type
	 * @param source the source
	 * @param defaultSource the default source
	 * @return the document mapper
	 * @throws MapperParsingException the mapper parsing exception
	 */
	@SuppressWarnings({ "unchecked" })
	public DocumentMapper parse(@Nullable String type, String source, String defaultSource)
			throws MapperParsingException {
		Map<String, Object> mapping = null;
		if (source != null) {
			Tuple<String, Map<String, Object>> t = extractMapping(type, source);
			type = t.v1();
			mapping = t.v2();
		}
		if (mapping == null) {
			mapping = Maps.newHashMap();
		}

		if (type == null) {
			throw new MapperParsingException("Failed to derive type");
		}

		if (defaultSource != null) {
			Tuple<String, Map<String, Object>> t = extractMapping(MapperService.DEFAULT_MAPPING, defaultSource);
			if (t.v2() != null) {
				XContentHelper.mergeDefaults(mapping, t.v2());
			}
		}

		Mapper.TypeParser.ParserContext parserContext = new Mapper.TypeParser.ParserContext(analysisService,
				typeParsers);

		DocumentMapper.Builder docBuilder = MapperBuilders.doc(index.name(), indexSettings,
				(RootObjectMapper.Builder) rootObjectTypeParser.parse(type, mapping, parserContext));

		for (Map.Entry<String, Object> entry : mapping.entrySet()) {
			String fieldName = Strings.toUnderscoreCase(entry.getKey());
			Object fieldNode = entry.getValue();

			if ("index_analyzer".equals(fieldName)) {
				NamedAnalyzer analyzer = analysisService.analyzer(fieldNode.toString());
				if (analyzer == null) {
					throw new MapperParsingException("Analyzer [" + fieldNode.toString()
							+ "] not found for index_analyzer setting on root type [" + type + "]");
				}
				docBuilder.indexAnalyzer(analyzer);
			} else if ("search_analyzer".equals(fieldName)) {
				NamedAnalyzer analyzer = analysisService.analyzer(fieldNode.toString());
				if (analyzer == null) {
					throw new MapperParsingException("Analyzer [" + fieldNode.toString()
							+ "] not found for search_analyzer setting on root type [" + type + "]");
				}
				docBuilder.searchAnalyzer(analyzer);
			} else if ("analyzer".equals(fieldName)) {
				NamedAnalyzer analyzer = analysisService.analyzer(fieldNode.toString());
				if (analyzer == null) {
					throw new MapperParsingException("Analyzer [" + fieldNode.toString()
							+ "] not found for analyzer setting on root type [" + type + "]");
				}
				docBuilder.indexAnalyzer(analyzer);
				docBuilder.searchAnalyzer(analyzer);
			} else {
				Mapper.TypeParser typeParser = rootTypeParsers.get(fieldName);
				if (typeParser != null) {
					docBuilder.put(typeParser.parse(fieldName, (Map<String, Object>) fieldNode, parserContext));
				}
			}
		}

		if (!docBuilder.hasIndexAnalyzer()) {
			docBuilder.indexAnalyzer(analysisService.defaultIndexAnalyzer());
		}
		if (!docBuilder.hasSearchAnalyzer()) {
			docBuilder.searchAnalyzer(analysisService.defaultSearchAnalyzer());
		}

		ImmutableMap<String, Object> attributes = ImmutableMap.of();
		if (mapping.containsKey("_meta")) {
			attributes = ImmutableMap.copyOf((Map<String, Object>) mapping.get("_meta"));
		}
		docBuilder.meta(attributes);

		DocumentMapper documentMapper = docBuilder.build(this);
		
		documentMapper.refreshSource();
		return documentMapper;
	}

	
	/**
	 * Extract mapping.
	 *
	 * @param type the type
	 * @param source the source
	 * @return the tuple
	 * @throws MapperParsingException the mapper parsing exception
	 */
	@SuppressWarnings({ "unchecked" })
	private Tuple<String, Map<String, Object>> extractMapping(String type, String source) throws MapperParsingException {
		Map<String, Object> root;
		XContentParser xContentParser = null;
		try {
			xContentParser = XContentFactory.xContent(source).createParser(source);
			root = xContentParser.mapOrdered();
		} catch (IOException e) {
			throw new MapperParsingException("Failed to parse mapping definition", e);
		} finally {
			if (xContentParser != null) {
				xContentParser.close();
			}
		}

		
		if (root.keySet().size() != 1) {
			throw new MapperParsingException("Mapping must have the `type` as the root object");
		}

		String rootName = root.keySet().iterator().next();
		if (type == null) {
			type = rootName;
		}

		return new Tuple<String, Map<String, Object>>(type, (Map<String, Object>) root.get(rootName));
	}
}
