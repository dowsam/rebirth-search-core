/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ObjectMapper.java 2012-7-6 14:30:13 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.object;

import static com.google.common.collect.ImmutableMap.copyOf;
import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Filter;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.commons.joda.FormatDateTimeFormatter;
import cn.com.rebirth.commons.xcontent.ToXContent;
import cn.com.rebirth.commons.xcontent.ToXContent.Params;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.commons.lucene.search.TermFilter;
import cn.com.rebirth.search.commons.lucene.uid.UidField;
import cn.com.rebirth.search.core.index.mapper.ContentPath;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.FieldMapperListener;
import cn.com.rebirth.search.core.index.mapper.InternalMapper;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperBuilders;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ObjectMapperListener;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.mapper.StrictDynamicMappingException;
import cn.com.rebirth.search.core.index.mapper.core.TypeParsers;
import cn.com.rebirth.search.core.index.mapper.internal.AllFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.TypeFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.UidFieldMapper;
import cn.com.rebirth.search.core.index.mapper.multifield.MultiFieldMapper;

import com.google.common.collect.ImmutableMap;

/**
 * The Class ObjectMapper.
 *
 * @author l.xue.nong
 */
public class ObjectMapper implements Mapper, AllFieldMapper.IncludeInAll {

	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "object";

	/** The Constant NESTED_CONTENT_TYPE. */
	public static final String NESTED_CONTENT_TYPE = "nested";

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults {

		/** The Constant ENABLED. */
		public static final boolean ENABLED = true;

		/** The Constant NESTED. */
		public static final Nested NESTED = Nested.NO;

		/** The Constant DYNAMIC. */
		public static final Dynamic DYNAMIC = null;

		/** The Constant PATH_TYPE. */
		public static final ContentPath.Type PATH_TYPE = ContentPath.Type.FULL;
	}

	/**
	 * The Enum Dynamic.
	 *
	 * @author l.xue.nong
	 */
	public static enum Dynamic {

		/** The true. */
		TRUE,

		/** The false. */
		FALSE,

		/** The strict. */
		STRICT
	}

	/**
	 * The Class Nested.
	 *
	 * @author l.xue.nong
	 */
	public static class Nested {

		/** The Constant NO. */
		public static final Nested NO = new Nested(false, false, false);

		/**
		 * New nested.
		 *
		 * @param includeInParent the include in parent
		 * @param includeInRoot the include in root
		 * @return the nested
		 */
		public static Nested newNested(boolean includeInParent, boolean includeInRoot) {
			return new Nested(true, includeInParent, includeInRoot);
		}

		/** The nested. */
		private final boolean nested;

		/** The include in parent. */
		private final boolean includeInParent;

		/** The include in root. */
		private final boolean includeInRoot;

		/**
		 * Instantiates a new nested.
		 *
		 * @param nested the nested
		 * @param includeInParent the include in parent
		 * @param includeInRoot the include in root
		 */
		private Nested(boolean nested, boolean includeInParent, boolean includeInRoot) {
			this.nested = nested;
			this.includeInParent = includeInParent;
			this.includeInRoot = includeInRoot;
		}

		/**
		 * Checks if is nested.
		 *
		 * @return true, if is nested
		 */
		public boolean isNested() {
			return nested;
		}

		/**
		 * Checks if is include in parent.
		 *
		 * @return true, if is include in parent
		 */
		public boolean isIncludeInParent() {
			return includeInParent;
		}

		/**
		 * Checks if is include in root.
		 *
		 * @return true, if is include in root
		 */
		public boolean isIncludeInRoot() {
			return includeInRoot;
		}
	}

	/**
	 * The Class Builder.
	 *
	 * @param <T> the generic type
	 * @param <Y> the generic type
	 * @author l.xue.nong
	 */
	public static class Builder<T extends Builder, Y extends ObjectMapper> extends Mapper.Builder<T, Y> {

		/** The enabled. */
		protected boolean enabled = Defaults.ENABLED;

		/** The nested. */
		protected Nested nested = Defaults.NESTED;

		/** The dynamic. */
		protected Dynamic dynamic = Defaults.DYNAMIC;

		/** The path type. */
		protected ContentPath.Type pathType = Defaults.PATH_TYPE;

		/** The include in all. */
		protected Boolean includeInAll;

		/** The mappers builders. */
		protected final List<Mapper.Builder> mappersBuilders = newArrayList();

		/**
		 * Instantiates a new builder.
		 *
		 * @param name the name
		 */
		public Builder(String name) {
			super(name);
			this.builder = (T) this;
		}

		/**
		 * Enabled.
		 *
		 * @param enabled the enabled
		 * @return the t
		 */
		public T enabled(boolean enabled) {
			this.enabled = enabled;
			return builder;
		}

		/**
		 * Dynamic.
		 *
		 * @param dynamic the dynamic
		 * @return the t
		 */
		public T dynamic(Dynamic dynamic) {
			this.dynamic = dynamic;
			return builder;
		}

		/**
		 * Nested.
		 *
		 * @param nested the nested
		 * @return the t
		 */
		public T nested(Nested nested) {
			this.nested = nested;
			return builder;
		}

		/**
		 * Path type.
		 *
		 * @param pathType the path type
		 * @return the t
		 */
		public T pathType(ContentPath.Type pathType) {
			this.pathType = pathType;
			return builder;
		}

		/**
		 * Include in all.
		 *
		 * @param includeInAll the include in all
		 * @return the t
		 */
		public T includeInAll(boolean includeInAll) {
			this.includeInAll = includeInAll;
			return builder;
		}

		/**
		 * Adds the.
		 *
		 * @param builder the builder
		 * @return the t
		 */
		public T add(Mapper.Builder builder) {
			mappersBuilders.add(builder);
			return this.builder;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.Builder#build(cn.com.rebirth.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public Y build(BuilderContext context) {
			ContentPath.Type origPathType = context.path().pathType();
			context.path().pathType(pathType);
			context.path().add(name);

			Map<String, Mapper> mappers = new HashMap<String, Mapper>();
			for (Mapper.Builder builder : mappersBuilders) {
				Mapper mapper = builder.build(context);
				mappers.put(mapper.name(), mapper);
			}
			context.path().pathType(origPathType);
			context.path().remove();

			ObjectMapper objectMapper = createMapper(name, context.path().fullPathAsText(name), enabled, nested,
					dynamic, pathType, mappers);
			objectMapper.includeInAllIfNotSet(includeInAll);

			return (Y) objectMapper;
		}

		/**
		 * Creates the mapper.
		 *
		 * @param name the name
		 * @param fullPath the full path
		 * @param enabled the enabled
		 * @param nested the nested
		 * @param dynamic the dynamic
		 * @param pathType the path type
		 * @param mappers the mappers
		 * @return the object mapper
		 */
		protected ObjectMapper createMapper(String name, String fullPath, boolean enabled, Nested nested,
				Dynamic dynamic, ContentPath.Type pathType, Map<String, Mapper> mappers) {
			return new ObjectMapper(name, fullPath, enabled, nested, dynamic, pathType, mappers);
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
			Map<String, Object> objectNode = node;
			ObjectMapper.Builder builder = createBuilder(name);

			boolean nested = false;
			boolean nestedIncludeInParent = false;
			boolean nestedIncludeInRoot = false;
			for (Map.Entry<String, Object> entry : objectNode.entrySet()) {
				String fieldName = Strings.toUnderscoreCase(entry.getKey());
				Object fieldNode = entry.getValue();

				if (fieldName.equals("dynamic")) {
					String value = fieldNode.toString();
					if (value.equalsIgnoreCase("strict")) {
						builder.dynamic(Dynamic.STRICT);
					} else {
						builder.dynamic(XContentMapValues.nodeBooleanValue(fieldNode) ? Dynamic.TRUE : Dynamic.FALSE);
					}
				} else if (fieldName.equals("type")) {
					String type = fieldNode.toString();
					if (type.equals(CONTENT_TYPE)) {
						builder.nested = Nested.NO;
					} else if (type.equals(NESTED_CONTENT_TYPE)) {
						nested = true;
					} else {
						throw new MapperParsingException("Trying to parse an object but has a different type [" + type
								+ "] for [" + name + "]");
					}
				} else if (fieldName.equals("include_in_parent")) {
					nestedIncludeInParent = XContentMapValues.nodeBooleanValue(fieldNode);
				} else if (fieldName.equals("include_in_root")) {
					nestedIncludeInRoot = XContentMapValues.nodeBooleanValue(fieldNode);
				} else if (fieldName.equals("enabled")) {
					builder.enabled(XContentMapValues.nodeBooleanValue(fieldNode));
				} else if (fieldName.equals("path")) {
					builder.pathType(TypeParsers.parsePathType(name, fieldNode.toString()));
				} else if (fieldName.equals("properties")) {
					parseProperties(builder, (Map<String, Object>) fieldNode, parserContext);
				} else if (fieldName.equals("include_in_all")) {
					builder.includeInAll(XContentMapValues.nodeBooleanValue(fieldNode));
				} else {
					processField(builder, fieldName, fieldNode);
				}
			}

			if (nested) {
				builder.nested = Nested.newNested(nestedIncludeInParent, nestedIncludeInRoot);
			}

			return builder;
		}

		/**
		 * Parses the properties.
		 *
		 * @param objBuilder the obj builder
		 * @param propsNode the props node
		 * @param parserContext the parser context
		 */
		private void parseProperties(ObjectMapper.Builder objBuilder, Map<String, Object> propsNode,
				ParserContext parserContext) {
			for (Map.Entry<String, Object> entry : propsNode.entrySet()) {
				String propName = entry.getKey();
				Map<String, Object> propNode = (Map<String, Object>) entry.getValue();

				String type;
				Object typeNode = propNode.get("type");
				if (typeNode != null) {
					type = typeNode.toString();
				} else {

					if (propNode.get("properties") != null) {
						type = ObjectMapper.CONTENT_TYPE;
					} else if (propNode.get("fields") != null) {
						type = MultiFieldMapper.CONTENT_TYPE;
					} else {
						throw new MapperParsingException("No type specified for property [" + propName + "]");
					}
				}

				Mapper.TypeParser typeParser = parserContext.typeParser(type);
				if (typeParser == null) {
					throw new MapperParsingException("No handler for type [" + type + "] declared on field ["
							+ propName + "]");
				}
				objBuilder.add(typeParser.parse(propName, propNode, parserContext));
			}
		}

		/**
		 * Creates the builder.
		 *
		 * @param name the name
		 * @return the builder
		 */
		protected Builder createBuilder(String name) {
			return MapperBuilders.object(name);
		}

		/**
		 * Process field.
		 *
		 * @param builder the builder
		 * @param fieldName the field name
		 * @param fieldNode the field node
		 */
		protected void processField(Builder builder, String fieldName, Object fieldNode) {

		}
	}

	/** The name. */
	private final String name;

	/** The full path. */
	private final String fullPath;

	/** The enabled. */
	private final boolean enabled;

	/** The nested. */
	private final Nested nested;

	/** The nested type path. */
	private final String nestedTypePath;

	/** The nested type filter. */
	private final Filter nestedTypeFilter;

	/** The dynamic. */
	private final Dynamic dynamic;

	/** The path type. */
	private final ContentPath.Type pathType;

	/** The include in all. */
	private Boolean includeInAll;

	/** The mappers. */
	private volatile ImmutableMap<String, Mapper> mappers = ImmutableMap.of();

	/** The mutex. */
	private final Object mutex = new Object();

	/**
	 * Instantiates a new object mapper.
	 *
	 * @param name the name
	 * @param fullPath the full path
	 * @param enabled the enabled
	 * @param nested the nested
	 * @param dynamic the dynamic
	 * @param pathType the path type
	 * @param mappers the mappers
	 */
	ObjectMapper(String name, String fullPath, boolean enabled, Nested nested, Dynamic dynamic,
			ContentPath.Type pathType, Map<String, Mapper> mappers) {
		this.name = name;
		this.fullPath = fullPath;
		this.enabled = enabled;
		this.nested = nested;
		this.dynamic = dynamic;
		this.pathType = pathType;
		if (mappers != null) {
			this.mappers = copyOf(mappers);
		}
		this.nestedTypePath = "__" + fullPath;
		this.nestedTypeFilter = new TermFilter(TypeFieldMapper.TERM_FACTORY.createTerm(nestedTypePath));
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
		if (includeInAll == null) {
			return;
		}
		this.includeInAll = includeInAll;

		for (Mapper mapper : mappers.values()) {
			if (mapper instanceof AllFieldMapper.IncludeInAll) {
				((AllFieldMapper.IncludeInAll) mapper).includeInAll(includeInAll);
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.internal.AllFieldMapper.IncludeInAll#includeInAllIfNotSet(java.lang.Boolean)
	 */
	@Override
	public void includeInAllIfNotSet(Boolean includeInAll) {
		if (this.includeInAll == null) {
			this.includeInAll = includeInAll;
		}

		for (Mapper mapper : mappers.values()) {
			if (mapper instanceof AllFieldMapper.IncludeInAll) {
				((AllFieldMapper.IncludeInAll) mapper).includeInAllIfNotSet(includeInAll);
			}
		}
	}

	/**
	 * Nested.
	 *
	 * @return the nested
	 */
	public Nested nested() {
		return this.nested;
	}

	/**
	 * Nested type filter.
	 *
	 * @return the filter
	 */
	public Filter nestedTypeFilter() {
		return this.nestedTypeFilter;
	}

	/**
	 * Put mapper.
	 *
	 * @param mapper the mapper
	 * @return the object mapper
	 */
	public ObjectMapper putMapper(Mapper mapper) {
		if (mapper instanceof AllFieldMapper.IncludeInAll) {
			((AllFieldMapper.IncludeInAll) mapper).includeInAllIfNotSet(includeInAll);
		}
		synchronized (mutex) {
			mappers = MapBuilder.newMapBuilder(mappers).put(mapper.name(), mapper).immutableMap();
		}
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#traverse(cn.com.rebirth.search.core.index.mapper.FieldMapperListener)
	 */
	@Override
	public void traverse(FieldMapperListener fieldMapperListener) {
		for (Mapper mapper : mappers.values()) {
			mapper.traverse(fieldMapperListener);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#traverse(cn.com.rebirth.search.core.index.mapper.ObjectMapperListener)
	 */
	@Override
	public void traverse(ObjectMapperListener objectMapperListener) {
		objectMapperListener.objectMapper(this);
		for (Mapper mapper : mappers.values()) {
			mapper.traverse(objectMapperListener);
		}
	}

	/**
	 * Full path.
	 *
	 * @return the string
	 */
	public String fullPath() {
		return this.fullPath;
	}

	/**
	 * Nested type path.
	 *
	 * @return the string
	 */
	public String nestedTypePath() {
		return nestedTypePath;
	}

	/**
	 * Dynamic.
	 *
	 * @return the dynamic
	 */
	public final Dynamic dynamic() {
		return this.dynamic;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#parse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	public void parse(ParseContext context) throws IOException {
		if (!enabled) {
			context.parser().skipChildren();
			return;
		}
		XContentParser parser = context.parser();

		String currentFieldName = parser.currentName();
		XContentParser.Token token = parser.currentToken();
		if (token == XContentParser.Token.VALUE_NULL) {

			return;
		}

		Document restoreDoc = null;
		if (nested.isNested()) {
			Document nestedDoc = new Document();

			Fieldable uidField = context.doc().getFieldable(UidFieldMapper.NAME);
			if (uidField != null) {

				if (uidField.stringValue() != null) {
					nestedDoc.add(new Field(UidFieldMapper.NAME, uidField.stringValue(), Field.Store.NO,
							Field.Index.NOT_ANALYZED));
				} else {
					nestedDoc.add(new Field(UidFieldMapper.NAME, ((UidField) uidField).uid(), Field.Store.NO,
							Field.Index.NOT_ANALYZED));
				}
			}

			nestedDoc.add(new Field(TypeFieldMapper.NAME, nestedTypePath, Field.Store.NO, Field.Index.NOT_ANALYZED));
			restoreDoc = context.switchDoc(nestedDoc);
			context.addDoc(nestedDoc);
		}

		ContentPath.Type origPathType = context.path().pathType();
		context.path().pathType(pathType);

		if (token == XContentParser.Token.END_OBJECT) {
			token = parser.nextToken();
		}
		if (token == XContentParser.Token.START_OBJECT) {

			token = parser.nextToken();
		}

		while (token != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.START_OBJECT) {
				serializeObject(context, currentFieldName);
			} else if (token == XContentParser.Token.START_ARRAY) {
				serializeArray(context, currentFieldName);
			} else if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.VALUE_NULL) {
				serializeNullValue(context, currentFieldName);
			} else if (token == null) {
				throw new MapperParsingException("object mapping for [" + name
						+ "] tried to parse as object, but got EOF, has a concrete value been provided to it?");
			} else if (token.isValue()) {
				serializeValue(context, currentFieldName, token);
			}
			token = parser.nextToken();
		}

		context.path().pathType(origPathType);
		if (nested.isNested()) {
			Document nestedDoc = context.switchDoc(restoreDoc);
			if (nested.isIncludeInParent()) {
				for (Fieldable field : nestedDoc.getFields()) {
					if (field.name().equals(UidFieldMapper.NAME) || field.name().equals(TypeFieldMapper.NAME)) {
						continue;
					} else {
						context.doc().add(field);
					}
				}
			}
			if (nested.isIncludeInRoot()) {

				if (!(nested.isIncludeInParent() && context.doc() == context.rootDoc())) {
					for (Fieldable field : nestedDoc.getFields()) {
						if (field.name().equals(UidFieldMapper.NAME) || field.name().equals(TypeFieldMapper.NAME)) {
							continue;
						} else {
							context.rootDoc().add(field);
						}
					}
				}
			}
		}
	}

	/**
	 * Serialize null value.
	 *
	 * @param context the context
	 * @param lastFieldName the last field name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void serializeNullValue(ParseContext context, String lastFieldName) throws IOException {

		Mapper mapper = mappers.get(lastFieldName);
		if (mapper != null) {
			mapper.parse(context);
		}
	}

	/**
	 * Serialize object.
	 *
	 * @param context the context
	 * @param currentFieldName the current field name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void serializeObject(final ParseContext context, String currentFieldName) throws IOException {
		if (currentFieldName == null) {
			throw new MapperParsingException("object mapping [" + name
					+ "] trying to serialize an object with no field associated with it, current value ["
					+ context.parser().textOrNull() + "]");
		}
		context.path().add(currentFieldName);

		Mapper objectMapper = mappers.get(currentFieldName);
		if (objectMapper != null) {
			objectMapper.parse(context);
		} else {
			Dynamic dynamic = this.dynamic;
			if (dynamic == null) {
				dynamic = context.root().dynamic();
			}
			if (dynamic == Dynamic.STRICT) {
				throw new StrictDynamicMappingException(fullPath, currentFieldName);
			} else if (dynamic == Dynamic.TRUE) {

				boolean newMapper = false;
				synchronized (mutex) {
					objectMapper = mappers.get(currentFieldName);
					if (objectMapper == null) {
						newMapper = true;
						Mapper.Builder builder = context.root()
								.findTemplateBuilder(context, currentFieldName, "object");
						if (builder == null) {
							builder = MapperBuilders.object(currentFieldName).enabled(true).dynamic(dynamic)
									.pathType(pathType);
						}

						context.path().remove();
						BuilderContext builderContext = new BuilderContext(context.indexSettings(), context.path());
						objectMapper = builder.build(builderContext);
						putMapper(objectMapper);

						context.path().add(currentFieldName);
						context.addedMapper();
					}
				}

				if (newMapper) {

					objectMapper.traverse(new FieldMapperListener() {
						@Override
						public void fieldMapper(FieldMapper fieldMapper) {
							context.docMapper().addFieldMapper(fieldMapper);
						}
					});
					objectMapper.traverse(new ObjectMapperListener() {
						@Override
						public void objectMapper(ObjectMapper objectMapper) {
							context.docMapper().addObjectMapper(objectMapper);
						}
					});

				}

				objectMapper.parse(context);
			} else {

				context.parser().skipChildren();
			}
		}

		context.path().remove();
	}

	/**
	 * Serialize array.
	 *
	 * @param context the context
	 * @param lastFieldName the last field name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void serializeArray(ParseContext context, String lastFieldName) throws IOException {
		Mapper mapper = mappers.get(lastFieldName);
		if (mapper != null && mapper instanceof ArrayValueMapperParser) {
			mapper.parse(context);
		} else {
			XContentParser parser = context.parser();
			XContentParser.Token token;
			while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
				if (token == XContentParser.Token.START_OBJECT) {
					serializeObject(context, lastFieldName);
				} else if (token == XContentParser.Token.START_ARRAY) {
					serializeArray(context, lastFieldName);
				} else if (token == XContentParser.Token.FIELD_NAME) {
					lastFieldName = parser.currentName();
				} else if (token == XContentParser.Token.VALUE_NULL) {
					serializeNullValue(context, lastFieldName);
				} else {
					serializeValue(context, lastFieldName, token);
				}
			}
		}
	}

	/**
	 * Serialize value.
	 *
	 * @param context the context
	 * @param currentFieldName the current field name
	 * @param token the token
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void serializeValue(final ParseContext context, String currentFieldName, XContentParser.Token token)
			throws IOException {
		if (currentFieldName == null) {
			throw new MapperParsingException("object mapping [" + name
					+ "] trying to serialize a value with no field associated with it, current value ["
					+ context.parser().textOrNull() + "]");
		}
		Mapper mapper = mappers.get(currentFieldName);
		if (mapper != null) {
			mapper.parse(context);
			return;
		}
		Dynamic dynamic = this.dynamic;
		if (dynamic == null) {
			dynamic = context.root().dynamic();
		}
		if (dynamic == Dynamic.STRICT) {
			throw new StrictDynamicMappingException(fullPath, currentFieldName);
		}
		if (dynamic == Dynamic.FALSE) {
			return;
		}

		boolean newMapper = false;
		synchronized (mutex) {
			mapper = mappers.get(currentFieldName);
			if (mapper == null) {
				newMapper = true;
				BuilderContext builderContext = new BuilderContext(context.indexSettings(), context.path());
				if (token == XContentParser.Token.VALUE_STRING) {
					boolean resolved = false;

					if (!resolved) {
						Mapper.Builder builder = context.root().findTemplateBuilder(context, currentFieldName,
								"string", null);
						if (builder != null) {
							mapper = builder.build(builderContext);
							resolved = true;
						}
					}

					if (!resolved && context.parser().textLength() == 0) {

						return;
					}

					if (!resolved && context.root().dateDetection()) {
						String text = context.parser().text();

						if (text.contains(":") || text.contains("-") || text.contains("/")) {
							for (FormatDateTimeFormatter dateTimeFormatter : context.root().dynamicDateTimeFormatters()) {
								try {
									dateTimeFormatter.parser().parseMillis(text);
									Mapper.Builder builder = context.root().findTemplateBuilder(context,
											currentFieldName, "date");
									if (builder == null) {
										builder = MapperBuilders.dateField(currentFieldName).dateTimeFormatter(
												dateTimeFormatter);
									}
									mapper = builder.build(builderContext);
									resolved = true;
									break;
								} catch (Exception e) {

								}
							}
						}
					}
					if (!resolved && context.root().numericDetection()) {
						String text = context.parser().text();
						try {
							Long.parseLong(text);
							Mapper.Builder builder = context.root().findTemplateBuilder(context, currentFieldName,
									"long");
							if (builder == null) {
								builder = MapperBuilders.longField(currentFieldName);
							}
							mapper = builder.build(builderContext);
							resolved = true;
						} catch (Exception e) {

						}
						if (!resolved) {
							try {
								Double.parseDouble(text);
								Mapper.Builder builder = context.root().findTemplateBuilder(context, currentFieldName,
										"double");
								if (builder == null) {
									builder = MapperBuilders.doubleField(currentFieldName);
								}
								mapper = builder.build(builderContext);
								resolved = true;
							} catch (Exception e) {

							}
						}
					}

					if (!resolved) {
						Mapper.Builder builder = context.root()
								.findTemplateBuilder(context, currentFieldName, "string");
						if (builder == null) {
							builder = MapperBuilders.stringField(currentFieldName);
						}
						mapper = builder.build(builderContext);
					}
				} else if (token == XContentParser.Token.VALUE_NUMBER) {
					XContentParser.NumberType numberType = context.parser().numberType();
					if (numberType == XContentParser.NumberType.INT) {
						if (context.parser().estimatedNumberType()) {
							Mapper.Builder builder = context.root().findTemplateBuilder(context, currentFieldName,
									"long");
							if (builder == null) {
								builder = MapperBuilders.longField(currentFieldName);
							}
							mapper = builder.build(builderContext);
						} else {
							Mapper.Builder builder = context.root().findTemplateBuilder(context, currentFieldName,
									"integer");
							if (builder == null) {
								builder = MapperBuilders.integerField(currentFieldName);
							}
							mapper = builder.build(builderContext);
						}
					} else if (numberType == XContentParser.NumberType.LONG) {
						Mapper.Builder builder = context.root().findTemplateBuilder(context, currentFieldName, "long");
						if (builder == null) {
							builder = MapperBuilders.longField(currentFieldName);
						}
						mapper = builder.build(builderContext);
					} else if (numberType == XContentParser.NumberType.FLOAT) {
						if (context.parser().estimatedNumberType()) {
							Mapper.Builder builder = context.root().findTemplateBuilder(context, currentFieldName,
									"double");
							if (builder == null) {
								builder = MapperBuilders.doubleField(currentFieldName);
							}
							mapper = builder.build(builderContext);
						} else {
							Mapper.Builder builder = context.root().findTemplateBuilder(context, currentFieldName,
									"float");
							if (builder == null) {
								builder = MapperBuilders.floatField(currentFieldName);
							}
							mapper = builder.build(builderContext);
						}
					} else if (numberType == XContentParser.NumberType.DOUBLE) {
						Mapper.Builder builder = context.root()
								.findTemplateBuilder(context, currentFieldName, "double");
						if (builder == null) {
							builder = MapperBuilders.doubleField(currentFieldName);
						}
						mapper = builder.build(builderContext);
					}
				} else if (token == XContentParser.Token.VALUE_BOOLEAN) {
					Mapper.Builder builder = context.root().findTemplateBuilder(context, currentFieldName, "boolean");
					if (builder == null) {
						builder = MapperBuilders.booleanField(currentFieldName);
					}
					mapper = builder.build(builderContext);
				} else if (token == XContentParser.Token.VALUE_EMBEDDED_OBJECT) {
					Mapper.Builder builder = context.root().findTemplateBuilder(context, currentFieldName, "binary");
					if (builder == null) {
						builder = MapperBuilders.binaryField(currentFieldName);
					}
					mapper = builder.build(builderContext);
				} else {
					Mapper.Builder builder = context.root().findTemplateBuilder(context, currentFieldName, null);
					if (builder != null) {
						mapper = builder.build(builderContext);
					} else {

						throw new RebirthIllegalStateException(
								"Can't handle serializing a dynamic type with content token [" + token
										+ "] and field name [" + currentFieldName + "]");
					}
				}
				putMapper(mapper);
				context.addedMapper();
			}
		}
		if (newMapper) {
			mapper.traverse(new FieldMapperListener() {
				@Override
				public void fieldMapper(FieldMapper fieldMapper) {
					context.docMapper().addFieldMapper(fieldMapper);
				}
			});
		}
		mapper.parse(context);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#merge(cn.com.rebirth.search.core.index.mapper.Mapper, cn.com.rebirth.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(final Mapper mergeWith, final MergeContext mergeContext) throws MergeMappingException {
		if (!(mergeWith instanceof ObjectMapper)) {
			mergeContext.addConflict("Can't merge a non object mapping [" + mergeWith.name()
					+ "] with an object mapping [" + name() + "]");
			return;
		}
		ObjectMapper mergeWithObject = (ObjectMapper) mergeWith;

		doMerge(mergeWithObject, mergeContext);

		List<Mapper> mappersToTraverse = new ArrayList<Mapper>();
		synchronized (mutex) {
			for (Mapper mergeWithMapper : mergeWithObject.mappers.values()) {
				Mapper mergeIntoMapper = mappers.get(mergeWithMapper.name());
				if (mergeIntoMapper == null) {

					if (!mergeContext.mergeFlags().simulate()) {
						putMapper(mergeWithMapper);
						mappersToTraverse.add(mergeWithMapper);
					}
				} else {
					if ((mergeWithMapper instanceof MultiFieldMapper) && !(mergeIntoMapper instanceof MultiFieldMapper)) {
						MultiFieldMapper mergeWithMultiField = (MultiFieldMapper) mergeWithMapper;
						mergeWithMultiField.merge(mergeIntoMapper, mergeContext);
						if (!mergeContext.mergeFlags().simulate()) {
							putMapper(mergeWithMultiField);

							for (Mapper mapper : mergeWithMultiField.mappers().values()) {
								mappersToTraverse.add(mapper);
							}
						}
					} else {
						mergeIntoMapper.merge(mergeWithMapper, mergeContext);
					}
				}
			}
		}

		for (Mapper mapper : mappersToTraverse) {
			mapper.traverse(new FieldMapperListener() {
				@Override
				public void fieldMapper(FieldMapper fieldMapper) {
					mergeContext.docMapper().addFieldMapper(fieldMapper);
				}
			});
			mapper.traverse(new ObjectMapperListener() {
				@Override
				public void objectMapper(ObjectMapper objectMapper) {
					mergeContext.docMapper().addObjectMapper(objectMapper);
				}
			});
		}
	}

	/**
	 * Do merge.
	 *
	 * @param mergeWith the merge with
	 * @param mergeContext the merge context
	 */
	protected void doMerge(ObjectMapper mergeWith, MergeContext mergeContext) {

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#close()
	 */
	@Override
	public void close() {
		for (Mapper mapper : mappers.values()) {
			mapper.close();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		toXContent(builder, params, null, Mapper.EMPTY_ARRAY);
		return builder;
	}

	/**
	 * To x content.
	 *
	 * @param builder the builder
	 * @param params the params
	 * @param custom the custom
	 * @param additionalMappers the additional mappers
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void toXContent(XContentBuilder builder, Params params, ToXContent custom, Mapper... additionalMappers)
			throws IOException {
		builder.startObject(name);
		if (nested.isNested()) {
			builder.field("type", NESTED_CONTENT_TYPE);
			if (nested.isIncludeInParent()) {
				builder.field("include_in_parent", true);
			}
			if (nested.isIncludeInRoot()) {
				builder.field("include_in_root", true);
			}
		} else if (mappers.isEmpty()) {
			builder.field("type", CONTENT_TYPE);
		}

		if (this instanceof RootObjectMapper) {
			if (dynamic != Dynamic.TRUE) {
				builder.field("dynamic", dynamic.name().toLowerCase());
			}
		} else {
			if (dynamic != Defaults.DYNAMIC) {
				builder.field("dynamic", dynamic.name().toLowerCase());
			}
		}
		if (enabled != Defaults.ENABLED) {
			builder.field("enabled", enabled);
		}
		if (pathType != Defaults.PATH_TYPE) {
			builder.field("path", pathType.name().toLowerCase());
		}
		if (includeInAll != null) {
			builder.field("include_in_all", includeInAll);
		}

		if (custom != null) {
			custom.toXContent(builder, params);
		}

		doXContent(builder, params);

		TreeMap<String, Mapper> sortedMappers = new TreeMap<String, Mapper>(mappers);

		for (Mapper mapper : sortedMappers.values()) {
			if (mapper instanceof InternalMapper) {
				mapper.toXContent(builder, params);
			}
		}
		if (additionalMappers != null && additionalMappers.length > 0) {
			TreeMap<String, Mapper> additionalSortedMappers = new TreeMap<String, Mapper>();
			for (Mapper mapper : additionalMappers) {
				additionalSortedMappers.put(mapper.name(), mapper);
			}

			for (Mapper mapper : additionalSortedMappers.values()) {
				mapper.toXContent(builder, params);
			}
		}

		if (!mappers.isEmpty()) {
			builder.startObject("properties");
			for (Mapper mapper : sortedMappers.values()) {
				if (!(mapper instanceof InternalMapper)) {
					mapper.toXContent(builder, params);
				}
			}
			builder.endObject();
		}
		builder.endObject();
	}

	/**
	 * Do x content.
	 *
	 * @param builder the builder
	 * @param params the params
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {

	}
}
