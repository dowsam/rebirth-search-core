/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MultiFieldMapper.java 2012-7-6 14:30:13 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.multifield;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.index.mapper.ContentPath;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.FieldMapperListener;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperBuilders;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ObjectMapperListener;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.TypeParsers;
import cn.com.rebirth.search.core.index.mapper.internal.AllFieldMapper;

import com.google.common.collect.ImmutableMap;

/**
 * The Class MultiFieldMapper.
 *
 * @author l.xue.nong
 */
public class MultiFieldMapper implements Mapper, AllFieldMapper.IncludeInAll {

	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "multi_field";

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults {

		/** The Constant PATH_TYPE. */
		public static final ContentPath.Type PATH_TYPE = ContentPath.Type.FULL;
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends Mapper.Builder<Builder, MultiFieldMapper> {

		/** The path type. */
		private ContentPath.Type pathType = Defaults.PATH_TYPE;

		/** The mappers builders. */
		private final List<Mapper.Builder> mappersBuilders = newArrayList();

		/** The default mapper builder. */
		private Mapper.Builder defaultMapperBuilder;

		/**
		 * Instantiates a new builder.
		 *
		 * @param name the name
		 */
		public Builder(String name) {
			super(name);
			this.builder = this;
		}

		/**
		 * Path type.
		 *
		 * @param pathType the path type
		 * @return the builder
		 */
		public Builder pathType(ContentPath.Type pathType) {
			this.pathType = pathType;
			return this;
		}

		/**
		 * Adds the.
		 *
		 * @param builder the builder
		 * @return the builder
		 */
		public Builder add(Mapper.Builder builder) {
			if (builder.name().equals(name)) {
				defaultMapperBuilder = builder;
			} else {
				mappersBuilders.add(builder);
			}
			return this;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.Builder#build(cn.com.rebirth.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public MultiFieldMapper build(BuilderContext context) {
			ContentPath.Type origPathType = context.path().pathType();
			context.path().pathType(pathType);

			Mapper defaultMapper = null;
			if (defaultMapperBuilder != null) {
				defaultMapper = defaultMapperBuilder.build(context);
			}

			String origSourcePath = context.path().sourcePath(context.path().fullPathAsText(name));
			context.path().add(name);
			Map<String, Mapper> mappers = new HashMap<String, Mapper>();
			for (Mapper.Builder builder : mappersBuilders) {
				Mapper mapper = builder.build(context);
				mappers.put(mapper.name(), mapper);
			}
			context.path().remove();
			context.path().sourcePath(origSourcePath);

			context.path().pathType(origPathType);

			return new MultiFieldMapper(name, pathType, mappers, defaultMapper);
		}
	}

	/**
	 * The Class TypeParser.
	 *
	 * @author l.xue.nong
	 */
	public static class TypeParser implements Mapper.TypeParser {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.TypeParser#parse(java.lang.String, java.util.Map, cn.com.rebirth.search.core.index.mapper.Mapper.TypeParser.ParserContext)
		 */
		@Override
		public Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext)
				throws MapperParsingException {
			MultiFieldMapper.Builder builder = MapperBuilders.multiField(name);

			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String fieldName = Strings.toUnderscoreCase(entry.getKey());
				Object fieldNode = entry.getValue();
				if (fieldName.equals("path")) {
					builder.pathType(TypeParsers.parsePathType(name, fieldNode.toString()));
				} else if (fieldName.equals("fields")) {
					Map<String, Object> fieldsNode = (Map<String, Object>) fieldNode;
					for (Map.Entry<String, Object> entry1 : fieldsNode.entrySet()) {
						String propName = entry1.getKey();
						Map<String, Object> propNode = (Map<String, Object>) entry1.getValue();

						String type;
						Object typeNode = propNode.get("type");
						if (typeNode != null) {
							type = typeNode.toString();
						} else {
							throw new MapperParsingException("No type specified for property [" + propName + "]");
						}

						Mapper.TypeParser typeParser = parserContext.typeParser(type);
						if (typeParser == null) {
							throw new MapperParsingException("No handler for type [" + type + "] declared on field ["
									+ fieldName + "]");
						}
						builder.add(typeParser.parse(propName, propNode, parserContext));
					}
				}
			}
			return builder;
		}
	}

	/** The name. */
	private final String name;

	/** The path type. */
	private final ContentPath.Type pathType;

	/** The mutex. */
	private final Object mutex = new Object();

	/** The mappers. */
	private volatile ImmutableMap<String, Mapper> mappers = ImmutableMap.of();

	/** The default mapper. */
	private volatile Mapper defaultMapper;

	/**
	 * Instantiates a new multi field mapper.
	 *
	 * @param name the name
	 * @param pathType the path type
	 * @param defaultMapper the default mapper
	 */
	public MultiFieldMapper(String name, ContentPath.Type pathType, Mapper defaultMapper) {
		this(name, pathType, new HashMap<String, Mapper>(), defaultMapper);
	}

	/**
	 * Instantiates a new multi field mapper.
	 *
	 * @param name the name
	 * @param pathType the path type
	 * @param mappers the mappers
	 * @param defaultMapper the default mapper
	 */
	public MultiFieldMapper(String name, ContentPath.Type pathType, Map<String, Mapper> mappers, Mapper defaultMapper) {
		this.name = name;
		this.pathType = pathType;
		this.mappers = ImmutableMap.copyOf(mappers);
		this.defaultMapper = defaultMapper;

		for (Mapper mapper : mappers.values()) {
			if (mapper instanceof AllFieldMapper.IncludeInAll) {
				((AllFieldMapper.IncludeInAll) mapper).includeInAll(false);
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#name()
	 */
	@Override
	public String name() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.internal.AllFieldMapper.IncludeInAll#includeInAll(java.lang.Boolean)
	 */
	@Override
	public void includeInAll(Boolean includeInAll) {
		if (includeInAll != null && defaultMapper != null && (defaultMapper instanceof AllFieldMapper.IncludeInAll)) {
			((AllFieldMapper.IncludeInAll) defaultMapper).includeInAll(includeInAll);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.internal.AllFieldMapper.IncludeInAll#includeInAllIfNotSet(java.lang.Boolean)
	 */
	@Override
	public void includeInAllIfNotSet(Boolean includeInAll) {
		if (includeInAll != null && defaultMapper != null && (defaultMapper instanceof AllFieldMapper.IncludeInAll)) {
			((AllFieldMapper.IncludeInAll) defaultMapper).includeInAllIfNotSet(includeInAll);
		}
	}

	/**
	 * Path type.
	 *
	 * @return the content path. type
	 */
	public ContentPath.Type pathType() {
		return pathType;
	}

	/**
	 * Default mapper.
	 *
	 * @return the mapper
	 */
	public Mapper defaultMapper() {
		return this.defaultMapper;
	}

	/**
	 * Mappers.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, Mapper> mappers() {
		return this.mappers;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#parse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void parse(ParseContext context) throws IOException {
		ContentPath.Type origPathType = context.path().pathType();
		context.path().pathType(pathType);

		if (defaultMapper != null) {
			defaultMapper.parse(context);
		}

		context.path().add(name);
		for (Mapper mapper : mappers.values()) {
			mapper.parse(context);
		}
		context.path().remove();

		context.path().pathType(origPathType);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#merge(cn.com.rebirth.search.core.index.mapper.Mapper, cn.com.rebirth.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
		if (!(mergeWith instanceof MultiFieldMapper) && !(mergeWith instanceof AbstractFieldMapper)) {
			mergeContext.addConflict("Can't merge a non multi_field / non simple mapping [" + mergeWith.name()
					+ "] with a multi_field mapping [" + name() + "]");
			return;
		}
		List<FieldMapper> mappersToAddToDocMapper = new ArrayList<FieldMapper>();
		synchronized (mutex) {
			if (mergeWith instanceof AbstractFieldMapper) {

				if (defaultMapper == null) {
					if (!mergeContext.mergeFlags().simulate()) {
						defaultMapper = mergeWith;
						mappersToAddToDocMapper.add((FieldMapper) defaultMapper);
					}
				}
			} else {
				MultiFieldMapper mergeWithMultiField = (MultiFieldMapper) mergeWith;

				if (defaultMapper == null) {
					if (mergeWithMultiField.defaultMapper != null) {
						if (!mergeContext.mergeFlags().simulate()) {
							defaultMapper = mergeWithMultiField.defaultMapper;
							mappersToAddToDocMapper.add((FieldMapper) defaultMapper);
						}
					}
				} else {
					if (mergeWithMultiField.defaultMapper != null) {
						defaultMapper.merge(mergeWithMultiField.defaultMapper, mergeContext);
					}
				}

				for (Mapper mergeWithMapper : mergeWithMultiField.mappers.values()) {
					Mapper mergeIntoMapper = mappers.get(mergeWithMapper.name());
					if (mergeIntoMapper == null) {

						if (!mergeContext.mergeFlags().simulate()) {

							if (mergeWithMapper instanceof AllFieldMapper.IncludeInAll) {
								((AllFieldMapper.IncludeInAll) mergeWithMapper).includeInAll(false);
							}
							mappers = MapBuilder.newMapBuilder(mappers).put(mergeWithMapper.name(), mergeWithMapper)
									.immutableMap();
							if (mergeWithMapper instanceof AbstractFieldMapper) {
								mappersToAddToDocMapper.add((FieldMapper) mergeWithMapper);
							}
						}
					} else {
						mergeIntoMapper.merge(mergeWithMapper, mergeContext);
					}
				}
			}
		}

		for (FieldMapper fieldMapper : mappersToAddToDocMapper) {
			mergeContext.docMapper().addFieldMapper(fieldMapper);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#close()
	 */
	@Override
	public void close() {
		if (defaultMapper != null) {
			defaultMapper.close();
		}
		for (Mapper mapper : mappers.values()) {
			mapper.close();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#traverse(cn.com.rebirth.search.core.index.mapper.FieldMapperListener)
	 */
	@Override
	public void traverse(FieldMapperListener fieldMapperListener) {
		if (defaultMapper != null) {
			defaultMapper.traverse(fieldMapperListener);
		}
		for (Mapper mapper : mappers.values()) {
			mapper.traverse(fieldMapperListener);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#traverse(cn.com.rebirth.search.core.index.mapper.ObjectMapperListener)
	 */
	@Override
	public void traverse(ObjectMapperListener objectMapperListener) {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(name);
		builder.field("type", CONTENT_TYPE);
		if (pathType != Defaults.PATH_TYPE) {
			builder.field("path", pathType.name().toLowerCase());
		}

		builder.startObject("fields");
		if (defaultMapper != null) {
			defaultMapper.toXContent(builder, params);
		}
		if (mappers.size() <= 1) {
			for (Mapper mapper : mappers.values()) {
				mapper.toXContent(builder, params);
			}
		} else {

			TreeMap<String, Mapper> sortedMappers = new TreeMap<String, Mapper>(mappers);
			for (Mapper mapper : sortedMappers.values()) {
				mapper.toXContent(builder, params);
			}
		}
		builder.endObject();

		builder.endObject();
		return builder;
	}
}
