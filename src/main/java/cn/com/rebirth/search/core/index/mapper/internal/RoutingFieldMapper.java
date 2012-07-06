/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RoutingFieldMapper.java 2012-3-29 15:01:38 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper.internal;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.core.index.mapper.InternalMapper;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperBuilders;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.mapper.RootMapper;
import cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.TypeParsers;


/**
 * The Class RoutingFieldMapper.
 *
 * @author l.xue.nong
 */
public class RoutingFieldMapper extends AbstractFieldMapper<String> implements InternalMapper, RootMapper {

	
	/** The Constant NAME. */
	public static final String NAME = "_routing";

	
	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "_routing";

	
	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends AbstractFieldMapper.Defaults {

		
		/** The Constant NAME. */
		public static final String NAME = "_routing";

		
		/** The Constant INDEX. */
		public static final Field.Index INDEX = Field.Index.NOT_ANALYZED;

		
		/** The Constant STORE. */
		public static final Field.Store STORE = Field.Store.YES;

		
		/** The Constant OMIT_NORMS. */
		public static final boolean OMIT_NORMS = true;

		
		/** The Constant OMIT_TERM_FREQ_AND_POSITIONS. */
		public static final boolean OMIT_TERM_FREQ_AND_POSITIONS = true;

		
		/** The Constant REQUIRED. */
		public static final boolean REQUIRED = false;

		
		/** The Constant PATH. */
		public static final String PATH = null;
	}

	
	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends AbstractFieldMapper.Builder<Builder, RoutingFieldMapper> {

		
		/** The required. */
		private boolean required = Defaults.REQUIRED;

		
		/** The path. */
		private String path = Defaults.PATH;

		
		/**
		 * Instantiates a new builder.
		 */
		public Builder() {
			super(Defaults.NAME);
			store = Defaults.STORE;
			index = Defaults.INDEX;
		}

		
		/**
		 * Required.
		 *
		 * @param required the required
		 * @return the builder
		 */
		public Builder required(boolean required) {
			this.required = required;
			return builder;
		}

		
		/**
		 * Path.
		 *
		 * @param path the path
		 * @return the builder
		 */
		public Builder path(String path) {
			this.path = path;
			return builder;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.Mapper.Builder#build(cn.com.summall.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public RoutingFieldMapper build(BuilderContext context) {
			return new RoutingFieldMapper(store, index, required, path);
		}
	}

	
	/**
	 * The Class TypeParser.
	 *
	 * @author l.xue.nong
	 */
	public static class TypeParser implements Mapper.TypeParser {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.Mapper.TypeParser#parse(java.lang.String, java.util.Map, cn.com.summall.search.core.index.mapper.Mapper.TypeParser.ParserContext)
		 */
		@Override
		public Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext)
				throws MapperParsingException {
			RoutingFieldMapper.Builder builder = MapperBuilders.routing();
			TypeParsers.parseField(builder, builder.name, node, parserContext);
			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String fieldName = Strings.toUnderscoreCase(entry.getKey());
				Object fieldNode = entry.getValue();
				if (fieldName.equals("required")) {
					builder.required(XContentMapValues.nodeBooleanValue(fieldNode));
				} else if (fieldName.equals("path")) {
					builder.path(fieldNode.toString());
				}
			}
			return builder;
		}
	}

	
	/** The required. */
	private boolean required;

	
	/** The path. */
	private final String path;

	
	/**
	 * Instantiates a new routing field mapper.
	 */
	public RoutingFieldMapper() {
		this(Defaults.STORE, Defaults.INDEX, Defaults.REQUIRED, Defaults.PATH);
	}

	
	/**
	 * Instantiates a new routing field mapper.
	 *
	 * @param store the store
	 * @param index the index
	 * @param required the required
	 * @param path the path
	 */
	protected RoutingFieldMapper(Field.Store store, Field.Index index, boolean required, String path) {
		super(new Names(Defaults.NAME, Defaults.NAME, Defaults.NAME, Defaults.NAME), index, store,
				Defaults.TERM_VECTOR, 1.0f, Defaults.OMIT_NORMS, Defaults.OMIT_TERM_FREQ_AND_POSITIONS,
				Lucene.KEYWORD_ANALYZER, Lucene.KEYWORD_ANALYZER);
		this.required = required;
		this.path = path;
	}

	
	/**
	 * Mark as required.
	 */
	public void markAsRequired() {
		this.required = true;
	}

	
	/**
	 * Required.
	 *
	 * @return true, if successful
	 */
	public boolean required() {
		return this.required;
	}

	
	/**
	 * Path.
	 *
	 * @return the string
	 */
	public String path() {
		return this.path;
	}

	
	/**
	 * Value.
	 *
	 * @param document the document
	 * @return the string
	 */
	public String value(Document document) {
		Fieldable field = document.getFieldable(names.indexName());
		return field == null ? null : value(field);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.FieldMapper#value(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public String value(Fieldable field) {
		return field.stringValue();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.FieldMapper#valueFromString(java.lang.String)
	 */
	@Override
	public String valueFromString(String value) {
		return value;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.FieldMapper#valueAsString(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public String valueAsString(Fieldable field) {
		return value(field);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#indexedValue(java.lang.String)
	 */
	@Override
	public String indexedValue(String value) {
		return value;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.RootMapper#validate(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void validate(ParseContext context) throws MapperParsingException {
		String routing = context.sourceToParse().routing();
		if (path != null && routing != null) {
			
			String value = null;
			Fieldable field = context.doc().getFieldable(path);
			if (field != null) {
				value = field.stringValue();
				if (value == null) {
					
					if (field instanceof NumberFieldMapper.CustomNumericField) {
						value = ((NumberFieldMapper.CustomNumericField) field).numericAsString();
					}
				}
			}
			if (value == null) {
				value = context.ignoredValue(path);
			}
			if (!routing.equals(value)) {
				throw new MapperParsingException("External routing [" + routing + "] and document path routing ["
						+ value + "] mismatch");
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.RootMapper#preParse(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void preParse(ParseContext context) throws IOException {
		super.parse(context);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.RootMapper#postParse(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void postParse(ParseContext context) throws IOException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#parse(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void parse(ParseContext context) throws IOException {
		
		
		
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.RootMapper#includeInObject()
	 */
	@Override
	public boolean includeInObject() {
		return true;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#parseCreateField(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	protected Field parseCreateField(ParseContext context) throws IOException {
		if (context.sourceToParse().routing() != null) {
			String routing = context.sourceToParse().routing();
			if (routing != null) {
				if (!indexed() && !stored()) {
					context.ignoredValue(names.indexName(), routing);
					return null;
				}
				return new Field(names.indexName(), routing, store, index);
			}
		}
		return null;

	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#contentType()
	 */
	@Override
	protected String contentType() {
		return CONTENT_TYPE;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		
		if (index == Defaults.INDEX && store == Defaults.STORE && required == Defaults.REQUIRED
				&& path == Defaults.PATH) {
			return builder;
		}
		builder.startObject(CONTENT_TYPE);
		if (index != Defaults.INDEX) {
			builder.field("index", index.name().toLowerCase());
		}
		if (store != Defaults.STORE) {
			builder.field("store", store.name().toLowerCase());
		}
		if (required != Defaults.REQUIRED) {
			builder.field("required", required);
		}
		if (path != Defaults.PATH) {
			builder.field("path", path);
		}
		builder.endObject();
		return builder;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#merge(cn.com.summall.search.core.index.mapper.Mapper, cn.com.summall.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
		
	}
}
