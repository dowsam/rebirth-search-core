/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MapperService.java 2012-3-29 15:01:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilterClause;
import org.apache.lucene.search.PublicTermsFilter;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.regex.Regex;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.Streams;
import cn.com.rebirth.search.commons.lucene.search.TermFilter;
import cn.com.rebirth.search.commons.lucene.search.XBooleanFilter;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.env.FailedToResolveConfigException;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.analysis.AnalysisService;
import cn.com.rebirth.search.core.index.mapper.internal.TypeFieldMapper;
import cn.com.rebirth.search.core.index.mapper.object.ObjectMapper;
import cn.com.rebirth.search.core.index.search.nested.NonNestedDocsFilter;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.indices.InvalidTypeNameException;
import cn.com.rebirth.search.core.indices.TypeMissingException;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

/**
 * The Class MapperService.
 *
 * @author l.xue.nong
 */
public class MapperService extends AbstractIndexComponent implements Iterable<DocumentMapper> {

	/** The Constant DEFAULT_MAPPING. */
	public static final String DEFAULT_MAPPING = "_default_";

	/** The analysis service. */
	private final AnalysisService analysisService;

	/** The dynamic. */
	private final boolean dynamic;

	/** The default mapping source. */
	private volatile String defaultMappingSource;

	/** The mappers. */
	private volatile Map<String, DocumentMapper> mappers = ImmutableMap.of();

	/** The mutex. */
	private final Object mutex = new Object();

	/** The name field mappers. */
	private volatile Map<String, FieldMappers> nameFieldMappers = ImmutableMap.of();

	/** The index name field mappers. */
	private volatile Map<String, FieldMappers> indexNameFieldMappers = ImmutableMap.of();

	/** The full name field mappers. */
	private volatile Map<String, FieldMappers> fullNameFieldMappers = ImmutableMap.of();

	/** The object mappers. */
	private volatile Map<String, ObjectMappers> objectMappers = ImmutableMap.of();

	/** The has nested. */
	private boolean hasNested = false;

	/** The document parser. */
	private final DocumentMapperParser documentParser;

	/** The field mapper listener. */
	private final InternalFieldMapperListener fieldMapperListener = new InternalFieldMapperListener();

	/** The object mapper listener. */
	private final InternalObjectMapperListener objectMapperListener = new InternalObjectMapperListener();

	/** The search analyzer. */
	private final SmartIndexNameSearchAnalyzer searchAnalyzer;

	/**
	 * Instantiates a new mapper service.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param environment the environment
	 * @param analysisService the analysis service
	 */
	@Inject
	public MapperService(Index index, @IndexSettings Settings indexSettings, Environment environment,
			AnalysisService analysisService) {
		super(index, indexSettings);
		this.analysisService = analysisService;
		this.documentParser = new DocumentMapperParser(index, indexSettings, analysisService);
		this.searchAnalyzer = new SmartIndexNameSearchAnalyzer(analysisService.defaultSearchAnalyzer());

		this.dynamic = componentSettings.getAsBoolean("dynamic", true);
		String defaultMappingLocation = componentSettings.get("default_mapping_location");
		URL defaultMappingUrl;
		if (defaultMappingLocation == null) {
			try {
				defaultMappingUrl = environment.resolveConfig("default-mapping.json");
			} catch (FailedToResolveConfigException e) {
				defaultMappingUrl = indexSettings.getClassLoader().getResource(
						"cn/com/summall/search/core/index/mapper/default-mapping.json");
				if (defaultMappingUrl == null) {
					defaultMappingUrl = MapperService.class
							.getResource("cn/com/summall/search/core/index/mapper/default-mapping.json");
				}
			}
		} else {
			try {
				defaultMappingUrl = environment.resolveConfig(defaultMappingLocation);
			} catch (FailedToResolveConfigException e) {

				try {
					defaultMappingUrl = new File(defaultMappingLocation).toURI().toURL();
				} catch (MalformedURLException e1) {
					throw new FailedToResolveConfigException("Failed to resolve dynamic mapping location ["
							+ defaultMappingLocation + "]");
				}
			}
		}

		try {
			defaultMappingSource = Streams.copyToString(new InputStreamReader(defaultMappingUrl.openStream(),
					Charsets.UTF_8));
		} catch (IOException e) {
			throw new MapperException("Failed to load default mapping source from [" + defaultMappingLocation + "]", e);
		}

	}

	/**
	 * Close.
	 */
	public void close() {
		for (DocumentMapper documentMapper : mappers.values()) {
			documentMapper.close();
		}
	}

	/**
	 * Checks for nested.
	 *
	 * @return true, if successful
	 */
	public boolean hasNested() {
		return this.hasNested;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public UnmodifiableIterator<DocumentMapper> iterator() {
		return Iterators.unmodifiableIterator(mappers.values().iterator());
	}

	/**
	 * Analysis service.
	 *
	 * @return the analysis service
	 */
	public AnalysisService analysisService() {
		return this.analysisService;
	}

	/**
	 * Document mapper parser.
	 *
	 * @return the document mapper parser
	 */
	public DocumentMapperParser documentMapperParser() {
		return this.documentParser;
	}

	/**
	 * Adds the.
	 *
	 * @param type the type
	 * @param mappingSource the mapping source
	 */
	public void add(String type, String mappingSource) {
		if (DEFAULT_MAPPING.equals(type)) {

			DocumentMapper mapper = documentParser.parse(type, mappingSource);

			synchronized (mutex) {
				mappers = MapBuilder.newMapBuilder(mappers).put(type, mapper).map();
			}
			defaultMappingSource = mappingSource;
		} else {
			add(parse(type, mappingSource));
		}
	}

	/**
	 * Adds the.
	 *
	 * @param mapper the mapper
	 */
	private void add(DocumentMapper mapper) {
		synchronized (mutex) {
			if (mapper.type().charAt(0) == '_') {
				throw new InvalidTypeNameException("mapping type name [" + mapper.type() + "] can't start with '_'");
			}
			if (mapper.type().contains("#")) {
				throw new InvalidTypeNameException("mapping type name [" + mapper.type()
						+ "] should not include '#' in it");
			}
			if (mapper.type().contains(",")) {
				throw new InvalidTypeNameException("mapping type name [" + mapper.type()
						+ "] should not include ',' in it");
			}
			if (mapper.type().contains(".")) {
				logger.warn("Type [{}] contains a '.', it is recommended not to include it within a type name",
						mapper.type());
			}

			DocumentMapper oldMapper = mappers.get(mapper.type());
			mapper.addFieldMapperListener(fieldMapperListener, true);
			mapper.addObjectMapperListener(objectMapperListener, true);
			mappers = MapBuilder.newMapBuilder(mappers).put(mapper.type(), mapper).map();
			if (oldMapper != null) {
				removeObjectFieldMappers(oldMapper);
				oldMapper.close();
			}
		}
	}

	/**
	 * Removes the.
	 *
	 * @param type the type
	 */
	public void remove(String type) {
		synchronized (mutex) {
			DocumentMapper docMapper = mappers.get(type);
			if (docMapper == null) {
				return;
			}
			docMapper.close();
			mappers = MapBuilder.newMapBuilder(mappers).remove(type).map();
			removeObjectFieldMappers(docMapper);
		}
	}

	/**
	 * Removes the object field mappers.
	 *
	 * @param docMapper the doc mapper
	 */
	@SuppressWarnings("rawtypes")
	private void removeObjectFieldMappers(DocumentMapper docMapper) {

		for (FieldMapper mapper : docMapper.mappers()) {
			FieldMappers mappers = nameFieldMappers.get(mapper.names().name());
			if (mappers != null) {
				mappers = mappers.remove(mapper);
				if (mappers.isEmpty()) {
					nameFieldMappers = MapBuilder.newMapBuilder(nameFieldMappers).remove(mapper.names().name()).map();
				} else {
					nameFieldMappers = MapBuilder.newMapBuilder(nameFieldMappers).put(mapper.names().name(), mappers)
							.map();
				}
			}

			mappers = indexNameFieldMappers.get(mapper.names().indexName());
			if (mappers != null) {
				mappers = mappers.remove(mapper);
				if (mappers.isEmpty()) {
					indexNameFieldMappers = MapBuilder.newMapBuilder(indexNameFieldMappers)
							.remove(mapper.names().indexName()).map();
				} else {
					indexNameFieldMappers = MapBuilder.newMapBuilder(indexNameFieldMappers)
							.put(mapper.names().indexName(), mappers).map();
				}
			}

			mappers = fullNameFieldMappers.get(mapper.names().fullName());
			if (mappers != null) {
				mappers = mappers.remove(mapper);
				if (mappers.isEmpty()) {
					fullNameFieldMappers = MapBuilder.newMapBuilder(fullNameFieldMappers)
							.remove(mapper.names().fullName()).map();
				} else {
					fullNameFieldMappers = MapBuilder.newMapBuilder(fullNameFieldMappers)
							.put(mapper.names().fullName(), mappers).map();
				}
			}
		}

		for (ObjectMapper mapper : docMapper.objectMappers().values()) {
			ObjectMappers mappers = objectMappers.get(mapper.fullPath());
			if (mappers != null) {
				mappers = mappers.remove(mapper);
				if (mappers.isEmpty()) {
					objectMappers = MapBuilder.newMapBuilder(objectMappers).remove(mapper.fullPath()).map();
				} else {
					objectMappers = MapBuilder.newMapBuilder(objectMappers).put(mapper.fullPath(), mappers).map();
				}
			}
		}
	}

	/**
	 * Parses the.
	 *
	 * @param mappingType the mapping type
	 * @param mappingSource the mapping source
	 * @return the document mapper
	 * @throws MapperParsingException the mapper parsing exception
	 */
	public DocumentMapper parse(String mappingType, String mappingSource) throws MapperParsingException {
		return documentParser.parse(mappingType, mappingSource, defaultMappingSource);
	}

	/**
	 * Checks for mapping.
	 *
	 * @param mappingType the mapping type
	 * @return true, if successful
	 */
	public boolean hasMapping(String mappingType) {
		return mappers.containsKey(mappingType);
	}

	/**
	 * Types.
	 *
	 * @return the collection
	 */
	public Collection<String> types() {
		return mappers.keySet();
	}

	/**
	 * Document mapper.
	 *
	 * @param type the type
	 * @return the document mapper
	 */
	public DocumentMapper documentMapper(String type) {
		return mappers.get(type);
	}

	/**
	 * Document mapper with auto create.
	 *
	 * @param type the type
	 * @return the document mapper
	 */
	public DocumentMapper documentMapperWithAutoCreate(String type) {
		DocumentMapper mapper = mappers.get(type);
		if (mapper != null) {
			return mapper;
		}
		if (!dynamic) {
			throw new TypeMissingException(index, type,
					"trying to auto create mapping, but dynamic mapping is disabled");
		}

		synchronized (mutex) {
			mapper = mappers.get(type);
			if (mapper != null) {
				return mapper;
			}
			add(type, null);
			return mappers.get(type);
		}
	}

	/**
	 * Search filter.
	 *
	 * @param types the types
	 * @return the filter
	 */
	public Filter searchFilter(String... types) {
		if (types == null || types.length == 0) {
			if (hasNested) {
				return NonNestedDocsFilter.INSTANCE;
			} else {
				return null;
			}
		}

		if (types.length == 1) {
			DocumentMapper docMapper = documentMapper(types[0]);
			if (docMapper == null) {
				return new TermFilter(new Term(types[0]));
			}
			return docMapper.typeFilter();
		}

		boolean useTermsFilter = true;
		for (String type : types) {
			DocumentMapper docMapper = documentMapper(type);
			if (docMapper == null) {
				useTermsFilter = false;
				break;
			}
			if (!docMapper.typeMapper().indexed()) {
				useTermsFilter = false;
				break;
			}
		}
		if (useTermsFilter) {
			PublicTermsFilter termsFilter = new PublicTermsFilter();
			for (String type : types) {
				termsFilter.addTerm(TypeFieldMapper.TERM_FACTORY.createTerm(type));
			}
			return termsFilter;
		} else {
			XBooleanFilter bool = new XBooleanFilter();
			for (String type : types) {
				DocumentMapper docMapper = documentMapper(type);
				if (docMapper == null) {
					bool.add(new FilterClause(new TermFilter(TypeFieldMapper.TERM_FACTORY.createTerm(type)),
							BooleanClause.Occur.SHOULD));
				} else {
					bool.add(new FilterClause(docMapper.typeFilter(), BooleanClause.Occur.SHOULD));
				}
			}
			return bool;
		}
	}

	/**
	 * Types filter fail on missing.
	 *
	 * @param types the types
	 * @return the filter
	 * @throws TypeMissingException the type missing exception
	 */
	public Filter typesFilterFailOnMissing(String... types) throws TypeMissingException {
		if (types.length == 1) {
			DocumentMapper docMapper = documentMapper(types[0]);
			if (docMapper == null) {
				throw new TypeMissingException(index, types[0]);
			}
			return docMapper.typeFilter();
		}
		PublicTermsFilter termsFilter = new PublicTermsFilter();
		for (String type : types) {
			if (!hasMapping(type)) {
				throw new TypeMissingException(index, type);
			}
			termsFilter.addTerm(TypeFieldMapper.TERM_FACTORY.createTerm(type));
		}
		return termsFilter;
	}

	/**
	 * Name.
	 *
	 * @param name the name
	 * @return the field mappers
	 */
	public FieldMappers name(String name) {
		return nameFieldMappers.get(name);
	}

	/**
	 * Index name.
	 *
	 * @param indexName the index name
	 * @return the field mappers
	 */
	public FieldMappers indexName(String indexName) {
		return indexNameFieldMappers.get(indexName);
	}

	/**
	 * Full name.
	 *
	 * @param fullName the full name
	 * @return the field mappers
	 */
	public FieldMappers fullName(String fullName) {
		return fullNameFieldMappers.get(fullName);
	}

	/**
	 * Object mapper.
	 *
	 * @param path the path
	 * @return the object mappers
	 */
	public ObjectMappers objectMapper(String path) {
		return objectMappers.get(path);
	}

	/**
	 * Simple match to index names.
	 *
	 * @param pattern the pattern
	 * @return the sets the
	 */
	public Set<String> simpleMatchToIndexNames(String pattern) {
		int dotIndex = pattern.indexOf('.');
		if (dotIndex != -1) {
			String possibleType = pattern.substring(0, dotIndex);
			DocumentMapper possibleDocMapper = mappers.get(possibleType);
			if (possibleDocMapper != null) {
				Set<String> typedFields = Sets.newHashSet();
				for (String indexName : possibleDocMapper.mappers().simpleMatchToIndexNames(pattern)) {
					typedFields.add(possibleType + "." + indexName);
				}
				return typedFields;
			}
		}
		Set<String> fields = Sets.newHashSet();
		for (Map.Entry<String, FieldMappers> entry : fullNameFieldMappers.entrySet()) {
			if (Regex.simpleMatch(pattern, entry.getKey())) {
				for (FieldMapper mapper : entry.getValue()) {
					fields.add(mapper.names().indexName());
				}
			}
		}
		for (Map.Entry<String, FieldMappers> entry : indexNameFieldMappers.entrySet()) {
			if (Regex.simpleMatch(pattern, entry.getKey())) {
				for (FieldMapper mapper : entry.getValue()) {
					fields.add(mapper.names().indexName());
				}
			}
		}
		for (Map.Entry<String, FieldMappers> entry : nameFieldMappers.entrySet()) {
			if (Regex.simpleMatch(pattern, entry.getKey())) {
				for (FieldMapper mapper : entry.getValue()) {
					fields.add(mapper.names().indexName());
				}
			}
		}
		return fields;
	}

	/**
	 * Smart name object mapper.
	 *
	 * @param smartName the smart name
	 * @param types the types
	 * @return the smart name object mapper
	 */
	public SmartNameObjectMapper smartNameObjectMapper(String smartName, @Nullable String[] types) {
		if (types == null || types.length == 0) {
			return smartNameObjectMapper(smartName);
		}
		if (types.length == 1 && types[0].equals("_all")) {
			return smartNameObjectMapper(smartName);
		}
		for (String type : types) {
			DocumentMapper possibleDocMapper = mappers.get(type);
			if (possibleDocMapper != null) {
				ObjectMapper mapper = possibleDocMapper.objectMappers().get(smartName);
				if (mapper != null) {
					return new SmartNameObjectMapper(mapper, possibleDocMapper);
				}
			}
		}

		int dotIndex = smartName.indexOf('.');
		if (dotIndex != -1) {
			String possibleType = smartName.substring(0, dotIndex);
			DocumentMapper possibleDocMapper = mappers.get(possibleType);
			if (possibleDocMapper != null) {
				String possiblePath = smartName.substring(dotIndex + 1);
				ObjectMapper mapper = possibleDocMapper.objectMappers().get(possiblePath);
				if (mapper != null) {
					return new SmartNameObjectMapper(mapper, possibleDocMapper);
				}
			}
		}

		return null;
	}

	/**
	 * Smart name object mapper.
	 *
	 * @param smartName the smart name
	 * @return the smart name object mapper
	 */
	public SmartNameObjectMapper smartNameObjectMapper(String smartName) {
		int dotIndex = smartName.indexOf('.');
		if (dotIndex != -1) {
			String possibleType = smartName.substring(0, dotIndex);
			DocumentMapper possibleDocMapper = mappers.get(possibleType);
			if (possibleDocMapper != null) {
				String possiblePath = smartName.substring(dotIndex + 1);
				ObjectMapper mapper = possibleDocMapper.objectMappers().get(possiblePath);
				if (mapper != null) {
					return new SmartNameObjectMapper(mapper, possibleDocMapper);
				}
			}
		}
		ObjectMappers mappers = objectMapper(smartName);
		if (mappers != null) {
			return new SmartNameObjectMapper(mappers.mapper(), null);
		}
		return null;
	}

	/**
	 * Smart name field mapper.
	 *
	 * @param smartName the smart name
	 * @return the field mapper
	 */
	public FieldMapper smartNameFieldMapper(String smartName) {
		FieldMappers fieldMappers = smartNameFieldMappers(smartName);
		if (fieldMappers != null) {
			return fieldMappers.mapper();
		}
		return null;
	}

	/**
	 * Smart name field mapper.
	 *
	 * @param smartName the smart name
	 * @param types the types
	 * @return the field mapper
	 */
	public FieldMapper smartNameFieldMapper(String smartName, @Nullable String[] types) {
		FieldMappers fieldMappers = smartNameFieldMappers(smartName, types);
		if (fieldMappers != null) {
			return fieldMappers.mapper();
		}
		return null;
	}

	/**
	 * Smart name field mappers.
	 *
	 * @param smartName the smart name
	 * @param types the types
	 * @return the field mappers
	 */
	public FieldMappers smartNameFieldMappers(String smartName, @Nullable String[] types) {
		if (types == null || types.length == 0) {
			return smartNameFieldMappers(smartName);
		}
		for (String type : types) {
			DocumentMapper documentMapper = mappers.get(type);

			if (documentMapper != null) {

				FieldMappers mappers = documentMapper.mappers().smartName(smartName);
				if (mappers != null) {
					return mappers;
				}
			}
		}

		int dotIndex = smartName.indexOf('.');
		if (dotIndex != -1) {
			String possibleType = smartName.substring(0, dotIndex);
			DocumentMapper possibleDocMapper = mappers.get(possibleType);
			if (possibleDocMapper != null) {
				String possibleName = smartName.substring(dotIndex + 1);
				FieldMappers mappers = possibleDocMapper.mappers().smartName(possibleName);
				if (mappers != null) {
					return mappers;
				}
			}
		}

		return null;
	}

	/**
	 * Smart name field mappers.
	 *
	 * @param smartName the smart name
	 * @return the field mappers
	 */
	public FieldMappers smartNameFieldMappers(String smartName) {
		int dotIndex = smartName.indexOf('.');
		if (dotIndex != -1) {
			String possibleType = smartName.substring(0, dotIndex);
			DocumentMapper possibleDocMapper = mappers.get(possibleType);
			if (possibleDocMapper != null) {
				String possibleName = smartName.substring(dotIndex + 1);
				FieldMappers mappers = possibleDocMapper.mappers().smartName(possibleName);
				if (mappers != null) {
					return mappers;
				}
			}
		}
		FieldMappers mappers = fullName(smartName);
		if (mappers != null) {
			return mappers;
		}
		mappers = indexName(smartName);
		if (mappers != null) {
			return mappers;
		}
		return name(smartName);
	}

	/**
	 * Smart name.
	 *
	 * @param smartName the smart name
	 * @param types the types
	 * @return the smart name field mappers
	 */
	public SmartNameFieldMappers smartName(String smartName, @Nullable String[] types) {
		if (types == null || types.length == 0) {
			return smartName(smartName);
		}
		if (types.length == 1 && types[0].equals("_all")) {
			return smartName(smartName);
		}
		for (String type : types) {
			DocumentMapper documentMapper = mappers.get(type);

			if (documentMapper != null) {

				FieldMappers mappers = documentMapper.mappers().smartName(smartName);
				if (mappers != null) {
					return new SmartNameFieldMappers(this, mappers, documentMapper, false);
				}
			}
		}

		int dotIndex = smartName.indexOf('.');
		if (dotIndex != -1) {
			String possibleType = smartName.substring(0, dotIndex);
			DocumentMapper possibleDocMapper = mappers.get(possibleType);
			if (possibleDocMapper != null) {
				String possibleName = smartName.substring(dotIndex + 1);
				FieldMappers mappers = possibleDocMapper.mappers().smartName(possibleName);
				if (mappers != null) {
					return new SmartNameFieldMappers(this, mappers, possibleDocMapper, true);
				}
			}
		}

		return null;
	}

	/**
	 * Smart name.
	 *
	 * @param smartName the smart name
	 * @return the smart name field mappers
	 */
	public SmartNameFieldMappers smartName(String smartName) {
		int dotIndex = smartName.indexOf('.');
		if (dotIndex != -1) {
			String possibleType = smartName.substring(0, dotIndex);
			DocumentMapper possibleDocMapper = mappers.get(possibleType);
			if (possibleDocMapper != null) {
				String possibleName = smartName.substring(dotIndex + 1);
				FieldMappers mappers = possibleDocMapper.mappers().smartName(possibleName);
				if (mappers != null) {
					return new SmartNameFieldMappers(this, mappers, possibleDocMapper, true);
				}
			}
		}
		FieldMappers fieldMappers = fullName(smartName);
		if (fieldMappers != null) {
			return new SmartNameFieldMappers(this, fieldMappers, null, false);
		}
		fieldMappers = indexName(smartName);
		if (fieldMappers != null) {
			return new SmartNameFieldMappers(this, fieldMappers, null, false);
		}
		fieldMappers = name(smartName);
		if (fieldMappers != null) {
			return new SmartNameFieldMappers(this, fieldMappers, null, false);
		}
		return null;
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
	 * The Class SmartNameObjectMapper.
	 *
	 * @author l.xue.nong
	 */
	public static class SmartNameObjectMapper {

		/** The mapper. */
		private final ObjectMapper mapper;

		/** The doc mapper. */
		private final DocumentMapper docMapper;

		/**
		 * Instantiates a new smart name object mapper.
		 *
		 * @param mapper the mapper
		 * @param docMapper the doc mapper
		 */
		public SmartNameObjectMapper(ObjectMapper mapper, @Nullable DocumentMapper docMapper) {
			this.mapper = mapper;
			this.docMapper = docMapper;
		}

		/**
		 * Checks for mapper.
		 *
		 * @return true, if successful
		 */
		public boolean hasMapper() {
			return mapper != null;
		}

		/**
		 * Mapper.
		 *
		 * @return the object mapper
		 */
		public ObjectMapper mapper() {
			return mapper;
		}

		/**
		 * Checks for doc mapper.
		 *
		 * @return true, if successful
		 */
		public boolean hasDocMapper() {
			return docMapper != null;
		}

		/**
		 * Doc mapper.
		 *
		 * @return the document mapper
		 */
		public DocumentMapper docMapper() {
			return docMapper;
		}
	}

	/**
	 * The Class SmartNameFieldMappers.
	 *
	 * @author l.xue.nong
	 */
	public static class SmartNameFieldMappers {

		/** The mapper service. */
		private final MapperService mapperService;

		/** The field mappers. */
		private final FieldMappers fieldMappers;

		/** The doc mapper. */
		private final DocumentMapper docMapper;

		/** The explicit type in name. */
		private final boolean explicitTypeInName;

		/**
		 * Instantiates a new smart name field mappers.
		 *
		 * @param mapperService the mapper service
		 * @param fieldMappers the field mappers
		 * @param docMapper the doc mapper
		 * @param explicitTypeInName the explicit type in name
		 */
		public SmartNameFieldMappers(MapperService mapperService, FieldMappers fieldMappers,
				@Nullable DocumentMapper docMapper, boolean explicitTypeInName) {
			this.mapperService = mapperService;
			this.fieldMappers = fieldMappers;
			this.docMapper = docMapper;
			this.explicitTypeInName = explicitTypeInName;
		}

		/**
		 * Checks for mapper.
		 *
		 * @return true, if successful
		 */
		public boolean hasMapper() {
			return !fieldMappers.isEmpty();
		}

		/**
		 * Mapper.
		 *
		 * @return the field mapper
		 */
		public FieldMapper mapper() {
			return fieldMappers.mapper();
		}

		/**
		 * Field mappers.
		 *
		 * @return the field mappers
		 */
		public FieldMappers fieldMappers() {
			return fieldMappers;
		}

		/**
		 * Checks for doc mapper.
		 *
		 * @return true, if successful
		 */
		public boolean hasDocMapper() {
			return docMapper != null;
		}

		/**
		 * Doc mapper.
		 *
		 * @return the document mapper
		 */
		public DocumentMapper docMapper() {
			return docMapper;
		}

		/**
		 * Explicit type in name.
		 *
		 * @return true, if successful
		 */
		public boolean explicitTypeInName() {
			return this.explicitTypeInName;
		}

		/**
		 * Explicit type in name with doc mapper.
		 *
		 * @return true, if successful
		 */
		public boolean explicitTypeInNameWithDocMapper() {
			return explicitTypeInName && docMapper != null;
		}

		/**
		 * Search analyzer.
		 *
		 * @return the analyzer
		 */
		public Analyzer searchAnalyzer() {
			if (hasMapper()) {
				Analyzer analyzer = mapper().searchAnalyzer();
				if (analyzer != null) {
					return analyzer;
				}
			}
			if (docMapper != null && docMapper.searchAnalyzer() != null) {
				return docMapper.searchAnalyzer();
			}
			return mapperService.searchAnalyzer();
		}
	}

	/**
	 * The Class SmartIndexNameSearchAnalyzer.
	 *
	 * @author l.xue.nong
	 */
	final class SmartIndexNameSearchAnalyzer extends Analyzer {

		/** The default analyzer. */
		private final Analyzer defaultAnalyzer;

		/**
		 * Instantiates a new smart index name search analyzer.
		 *
		 * @param defaultAnalyzer the default analyzer
		 */
		SmartIndexNameSearchAnalyzer(Analyzer defaultAnalyzer) {
			this.defaultAnalyzer = defaultAnalyzer;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.analysis.Analyzer#getPositionIncrementGap(java.lang.String)
		 */
		@Override
		public int getPositionIncrementGap(String fieldName) {
			int dotIndex = fieldName.indexOf('.');
			if (dotIndex != -1) {
				String possibleType = fieldName.substring(0, dotIndex);
				DocumentMapper possibleDocMapper = mappers.get(possibleType);
				if (possibleDocMapper != null) {
					return possibleDocMapper.mappers().searchAnalyzer().getPositionIncrementGap(fieldName);
				}
			}
			FieldMappers mappers = fullNameFieldMappers.get(fieldName);
			if (mappers != null && mappers.mapper() != null && mappers.mapper().searchAnalyzer() != null) {
				return mappers.mapper().searchAnalyzer().getPositionIncrementGap(fieldName);
			}

			mappers = indexNameFieldMappers.get(fieldName);
			if (mappers != null && mappers.mapper() != null && mappers.mapper().searchAnalyzer() != null) {
				return mappers.mapper().searchAnalyzer().getPositionIncrementGap(fieldName);
			}
			return defaultAnalyzer.getPositionIncrementGap(fieldName);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.analysis.Analyzer#getOffsetGap(org.apache.lucene.document.Fieldable)
		 */
		@Override
		public int getOffsetGap(Fieldable field) {
			String fieldName = field.name();
			int dotIndex = fieldName.indexOf('.');
			if (dotIndex != -1) {
				String possibleType = fieldName.substring(0, dotIndex);
				DocumentMapper possibleDocMapper = mappers.get(possibleType);
				if (possibleDocMapper != null) {
					return possibleDocMapper.mappers().searchAnalyzer().getOffsetGap(field);
				}
			}
			FieldMappers mappers = fullNameFieldMappers.get(fieldName);
			if (mappers != null && mappers.mapper() != null && mappers.mapper().searchAnalyzer() != null) {
				return mappers.mapper().searchAnalyzer().getOffsetGap(field);
			}

			mappers = indexNameFieldMappers.get(fieldName);
			if (mappers != null && mappers.mapper() != null && mappers.mapper().searchAnalyzer() != null) {
				return mappers.mapper().searchAnalyzer().getOffsetGap(field);
			}
			return defaultAnalyzer.getOffsetGap(field);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
		 */
		@Override
		public final TokenStream tokenStream(String fieldName, Reader reader) {
			int dotIndex = fieldName.indexOf('.');
			if (dotIndex != -1) {
				String possibleType = fieldName.substring(0, dotIndex);
				DocumentMapper possibleDocMapper = mappers.get(possibleType);
				if (possibleDocMapper != null) {
					return possibleDocMapper.mappers().searchAnalyzer().tokenStream(fieldName, reader);
				}
			}
			FieldMappers mappers = fullNameFieldMappers.get(fieldName);
			if (mappers != null && mappers.mapper() != null && mappers.mapper().searchAnalyzer() != null) {
				return mappers.mapper().searchAnalyzer().tokenStream(fieldName, reader);
			}

			mappers = indexNameFieldMappers.get(fieldName);
			if (mappers != null && mappers.mapper() != null && mappers.mapper().searchAnalyzer() != null) {
				return mappers.mapper().searchAnalyzer().tokenStream(fieldName, reader);
			}
			return defaultAnalyzer.tokenStream(fieldName, reader);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.analysis.Analyzer#reusableTokenStream(java.lang.String, java.io.Reader)
		 */
		@Override
		public final TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
			int dotIndex = fieldName.indexOf('.');
			if (dotIndex != -1) {
				String possibleType = fieldName.substring(0, dotIndex);
				DocumentMapper possibleDocMapper = mappers.get(possibleType);
				if (possibleDocMapper != null) {
					return possibleDocMapper.mappers().searchAnalyzer().reusableTokenStream(fieldName, reader);
				}
			}
			FieldMappers mappers = fullNameFieldMappers.get(fieldName);
			if (mappers != null && mappers.mapper() != null && mappers.mapper().searchAnalyzer() != null) {
				return mappers.mapper().searchAnalyzer().reusableTokenStream(fieldName, reader);
			}

			mappers = indexNameFieldMappers.get(fieldName);
			if (mappers != null && mappers.mapper() != null && mappers.mapper().searchAnalyzer() != null) {
				return mappers.mapper().searchAnalyzer().reusableTokenStream(fieldName, reader);
			}
			return defaultAnalyzer.reusableTokenStream(fieldName, reader);
		}
	}

	/**
	 * The listener interface for receiving internalFieldMapper events.
	 * The class that is interested in processing a internalFieldMapper
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addInternalFieldMapperListener<code> method. When
	 * the internalFieldMapper event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see InternalFieldMapperEvent
	 */
	class InternalFieldMapperListener implements FieldMapperListener {

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.FieldMapperListener#fieldMapper(cn.com.summall.search.core.index.mapper.FieldMapper)
		 */
		@Override
		public void fieldMapper(FieldMapper fieldMapper) {
			synchronized (mutex) {
				FieldMappers mappers = nameFieldMappers.get(fieldMapper.names().name());
				if (mappers == null) {
					mappers = new FieldMappers(fieldMapper);
				} else {
					mappers = mappers.concat(fieldMapper);
				}

				nameFieldMappers = MapBuilder.newMapBuilder(nameFieldMappers).put(fieldMapper.names().name(), mappers)
						.map();

				mappers = indexNameFieldMappers.get(fieldMapper.names().indexName());
				if (mappers == null) {
					mappers = new FieldMappers(fieldMapper);
				} else {
					mappers = mappers.concat(fieldMapper);
				}
				indexNameFieldMappers = MapBuilder.newMapBuilder(indexNameFieldMappers)
						.put(fieldMapper.names().indexName(), mappers).map();

				mappers = fullNameFieldMappers.get(fieldMapper.names().fullName());
				if (mappers == null) {
					mappers = new FieldMappers(fieldMapper);
				} else {
					mappers = mappers.concat(fieldMapper);
				}
				fullNameFieldMappers = MapBuilder.newMapBuilder(fullNameFieldMappers)
						.put(fieldMapper.names().fullName(), mappers).map();
			}
		}
	}

	/**
	 * The listener interface for receiving internalObjectMapper events.
	 * The class that is interested in processing a internalObjectMapper
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addInternalObjectMapperListener<code> method. When
	 * the internalObjectMapper event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see InternalObjectMapperEvent
	 */
	class InternalObjectMapperListener implements ObjectMapperListener {

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.ObjectMapperListener#objectMapper(cn.com.summall.search.core.index.mapper.object.ObjectMapper)
		 */
		@Override
		public void objectMapper(ObjectMapper objectMapper) {
			ObjectMappers mappers = objectMappers.get(objectMapper.fullPath());
			if (mappers == null) {
				mappers = new ObjectMappers(objectMapper);
			} else {
				mappers = mappers.concat(objectMapper);
			}
			objectMappers = MapBuilder.newMapBuilder(objectMappers).put(objectMapper.fullPath(), mappers).map();

			if (objectMapper.nested().isNested()) {
				hasNested = true;
			}
		}
	}
}
