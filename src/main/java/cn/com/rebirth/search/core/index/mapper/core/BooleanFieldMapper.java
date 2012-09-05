/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BooleanFieldMapper.java 2012-7-6 14:28:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.core;

import static cn.com.rebirth.search.core.index.mapper.MapperBuilders.booleanField;
import static cn.com.rebirth.search.core.index.mapper.core.TypeParsers.parseField;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import cn.com.rebirth.commons.Booleans;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.ParseContext;

/**
 * The Class BooleanFieldMapper.
 *
 * @author l.xue.nong
 */
public class BooleanFieldMapper extends AbstractFieldMapper<Boolean> {

	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "boolean";

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends AbstractFieldMapper.Defaults {

		/** The Constant OMIT_NORMS. */
		public static final boolean OMIT_NORMS = true;

		/** The Constant NULL_VALUE. */
		public static final Boolean NULL_VALUE = null;
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends AbstractFieldMapper.Builder<Builder, BooleanFieldMapper> {

		/** The null value. */
		private Boolean nullValue = Defaults.NULL_VALUE;

		/**
		 * Instantiates a new builder.
		 *
		 * @param name the name
		 */
		public Builder(String name) {
			super(name);
			this.omitNorms = Defaults.OMIT_NORMS;
			this.builder = this;
		}

		/**
		 * Null value.
		 *
		 * @param nullValue the null value
		 * @return the builder
		 */
		public Builder nullValue(boolean nullValue) {
			this.nullValue = nullValue;
			return this;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#index(org.apache.lucene.document.Field.Index)
		 */
		@Override
		public Builder index(Field.Index index) {
			return super.index(index);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#store(org.apache.lucene.document.Field.Store)
		 */
		@Override
		public Builder store(Field.Store store) {
			return super.store(store);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#termVector(org.apache.lucene.document.Field.TermVector)
		 */
		@Override
		public Builder termVector(Field.TermVector termVector) {
			return super.termVector(termVector);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#boost(float)
		 */
		@Override
		public Builder boost(float boost) {
			return super.boost(boost);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#indexName(java.lang.String)
		 */
		@Override
		public Builder indexName(String indexName) {
			return super.indexName(indexName);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#omitTermFreqAndPositions(boolean)
		 */
		@Override
		public Builder omitTermFreqAndPositions(boolean omitTermFreqAndPositions) {
			return super.omitTermFreqAndPositions(omitTermFreqAndPositions);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.Builder#build(cn.com.rebirth.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public BooleanFieldMapper build(BuilderContext context) {
			return new BooleanFieldMapper(buildNames(context), index, store, termVector, boost, omitNorms,
					omitTermFreqAndPositions, nullValue);
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
			BooleanFieldMapper.Builder builder = booleanField(name);
			parseField(builder, name, node, parserContext);
			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String propName = Strings.toUnderscoreCase(entry.getKey());
				Object propNode = entry.getValue();
				if (propName.equals("null_value")) {
					builder.nullValue(XContentMapValues.nodeBooleanValue(propNode));
				}
			}
			return builder;
		}
	}

	/** The null value. */
	private Boolean nullValue;

	/**
	 * Instantiates a new boolean field mapper.
	 *
	 * @param names the names
	 * @param index the index
	 * @param store the store
	 * @param termVector the term vector
	 * @param boost the boost
	 * @param omitNorms the omit norms
	 * @param omitTermFreqAndPositions the omit term freq and positions
	 * @param nullValue the null value
	 */
	protected BooleanFieldMapper(Names names, Field.Index index, Field.Store store, Field.TermVector termVector,
			float boost, boolean omitNorms, boolean omitTermFreqAndPositions, Boolean nullValue) {
		super(names, index, store, termVector, boost, omitNorms, omitTermFreqAndPositions, Lucene.KEYWORD_ANALYZER,
				Lucene.KEYWORD_ANALYZER);
		this.nullValue = nullValue;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#useFieldQueryWithQueryString()
	 */
	@Override
	public boolean useFieldQueryWithQueryString() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#value(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public Boolean value(Fieldable field) {
		return field.stringValue().charAt(0) == 'T' ? Boolean.TRUE : Boolean.FALSE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#valueFromString(java.lang.String)
	 */
	@Override
	public Boolean valueFromString(String value) {
		return value.charAt(0) == 'T' ? Boolean.TRUE : Boolean.FALSE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#valueAsString(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public String valueAsString(Fieldable field) {
		return field.stringValue().charAt(0) == 'T' ? "true" : "false";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#indexedValue(java.lang.String)
	 */
	@Override
	public String indexedValue(String value) {
		if (value == null || value.length() == 0) {
			return "F";
		}
		if (value.length() == 1 && value.charAt(0) == 'F') {
			return "F";
		}
		if (Booleans.parseBoolean(value, false)) {
			return "T";
		}
		return "F";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#parseCreateField(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	protected Field parseCreateField(ParseContext context) throws IOException {
		if (!indexed() && !stored()) {
			return null;
		}
		XContentParser.Token token = context.parser().currentToken();
		String value = null;
		if (token == XContentParser.Token.VALUE_NULL) {
			if (nullValue != null) {
				value = nullValue ? "T" : "F";
			}
		} else {
			value = context.parser().booleanValue() ? "T" : "F";
		}
		if (value == null) {
			return null;
		}
		return new Field(names.indexName(), value, store, index, termVector);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#contentType()
	 */
	@Override
	protected String contentType() {
		return CONTENT_TYPE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#doXContentBody(cn.com.rebirth.search.commons.xcontent.XContentBuilder)
	 */
	@Override
	protected void doXContentBody(XContentBuilder builder) throws IOException {
		super.doXContentBody(builder);
		if (index != Defaults.INDEX) {
			builder.field("index", index.name().toLowerCase());
		}
		if (store != Defaults.STORE) {
			builder.field("store", store.name().toLowerCase());
		}
		if (termVector != Defaults.TERM_VECTOR) {
			builder.field("term_vector", termVector.name().toLowerCase());
		}
		if (omitNorms != Defaults.OMIT_NORMS) {
			builder.field("omit_norms", omitNorms);
		}
		if (omitTermFreqAndPositions != Defaults.OMIT_TERM_FREQ_AND_POSITIONS) {
			builder.field("omit_term_freq_and_positions", omitTermFreqAndPositions);
		}
		if (nullValue != null) {
			builder.field("null_value", nullValue);
		}
	}
}
