/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StringFieldMapper.java 2012-7-6 14:29:10 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.core;

import static cn.com.rebirth.search.core.index.mapper.MapperBuilders.stringField;
import static cn.com.rebirth.search.core.index.mapper.core.TypeParsers.parseField;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.mapper.internal.AllFieldMapper.IncludeInAll;
import cn.com.rebirth.search.index.analysis.NamedAnalyzer;

/**
 * The Class StringFieldMapper.
 *
 * @author l.xue.nong
 */
public class StringFieldMapper extends AbstractFieldMapper<String> implements IncludeInAll {

	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "string";

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends AbstractFieldMapper.Defaults {

		/** The Constant NULL_VALUE. */
		public static final String NULL_VALUE = null;
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends AbstractFieldMapper.OpenBuilder<Builder, StringFieldMapper> {

		/** The null value. */
		protected String nullValue = Defaults.NULL_VALUE;

		/**
		 * Instantiates a new builder.
		 *
		 * @param name the name
		 */
		public Builder(String name) {
			super(name);
			builder = this;
		}

		/**
		 * Null value.
		 *
		 * @param nullValue the null value
		 * @return the builder
		 */
		public Builder nullValue(String nullValue) {
			this.nullValue = nullValue;
			return this;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#includeInAll(java.lang.Boolean)
		 */
		@Override
		public Builder includeInAll(Boolean includeInAll) {
			this.includeInAll = includeInAll;
			return this;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.Builder#build(cn.com.rebirth.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public StringFieldMapper build(BuilderContext context) {
			StringFieldMapper fieldMapper = new StringFieldMapper(buildNames(context), index, store, termVector, boost,
					omitNorms, omitTermFreqAndPositions, nullValue, indexAnalyzer, searchAnalyzer);
			fieldMapper.includeInAll(includeInAll);
			return fieldMapper;
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
			StringFieldMapper.Builder builder = stringField(name);
			parseField(builder, name, node, parserContext);
			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String propName = Strings.toUnderscoreCase(entry.getKey());
				Object propNode = entry.getValue();
				if (propName.equals("null_value")) {
					builder.nullValue(propNode.toString());
				}
			}
			return builder;
		}
	}

	/** The null value. */
	private String nullValue;

	/** The include in all. */
	private Boolean includeInAll;

	/**
	 * Instantiates a new string field mapper.
	 *
	 * @param names the names
	 * @param index the index
	 * @param store the store
	 * @param termVector the term vector
	 * @param boost the boost
	 * @param omitNorms the omit norms
	 * @param omitTermFreqAndPositions the omit term freq and positions
	 * @param nullValue the null value
	 * @param indexAnalyzer the index analyzer
	 * @param searchAnalyzer the search analyzer
	 */
	protected StringFieldMapper(Names names, Field.Index index, Field.Store store, Field.TermVector termVector,
			float boost, boolean omitNorms, boolean omitTermFreqAndPositions, String nullValue,
			NamedAnalyzer indexAnalyzer, NamedAnalyzer searchAnalyzer) {
		super(names, index, store, termVector, boost, omitNorms, omitTermFreqAndPositions, indexAnalyzer,
				searchAnalyzer);
		this.nullValue = nullValue;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.internal.AllFieldMapper.IncludeInAll#includeInAll(java.lang.Boolean)
	 */
	@Override
	public void includeInAll(Boolean includeInAll) {
		if (includeInAll != null) {
			this.includeInAll = includeInAll;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.internal.AllFieldMapper.IncludeInAll#includeInAllIfNotSet(java.lang.Boolean)
	 */
	@Override
	public void includeInAllIfNotSet(Boolean includeInAll) {
		if (includeInAll != null && this.includeInAll == null) {
			this.includeInAll = includeInAll;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#value(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public String value(Fieldable field) {
		return field.stringValue();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#valueFromString(java.lang.String)
	 */
	@Override
	public String valueFromString(String value) {
		return value;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#valueAsString(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public String valueAsString(Fieldable field) {
		return value(field);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#indexedValue(java.lang.String)
	 */
	@Override
	public String indexedValue(String value) {
		return value;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#customBoost()
	 */
	@Override
	protected boolean customBoost() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#parseCreateField(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	protected Field parseCreateField(ParseContext context) throws IOException {
		String value = nullValue;
		float boost = this.boost;
		if (context.externalValueSet()) {
			value = (String) context.externalValue();
		} else {
			XContentParser parser = context.parser();
			if (parser.currentToken() == XContentParser.Token.VALUE_NULL) {
				value = nullValue;
			} else if (parser.currentToken() == XContentParser.Token.START_OBJECT) {
				XContentParser.Token token;
				String currentFieldName = null;
				while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
					if (token == XContentParser.Token.FIELD_NAME) {
						currentFieldName = parser.currentName();
					} else {
						if ("value".equals(currentFieldName) || "_value".equals(currentFieldName)) {
							value = parser.textOrNull();
						} else if ("boost".equals(currentFieldName) || "_boost".equals(currentFieldName)) {
							boost = parser.floatValue();
						}
					}
				}
			} else {
				value = parser.textOrNull();
			}
		}
		if (value == null) {
			return null;
		}
		if (context.includeInAll(includeInAll, this)) {
			context.allEntries().addText(names.fullName(), value, boost);
		}
		if (!indexed() && !stored()) {
			context.ignoredValue(names.indexName(), value);
			return null;
		}
		Field field = new Field(names.indexName(), false, value, store, index, termVector);
		field.setBoost(boost);
		return field;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#contentType()
	 */
	@Override
	protected String contentType() {
		return CONTENT_TYPE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#merge(cn.com.rebirth.search.core.index.mapper.Mapper, cn.com.rebirth.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
		super.merge(mergeWith, mergeContext);
		if (!this.getClass().equals(mergeWith.getClass())) {
			return;
		}
		if (!mergeContext.mergeFlags().simulate()) {
			this.includeInAll = ((StringFieldMapper) mergeWith).includeInAll;
			this.nullValue = ((StringFieldMapper) mergeWith).nullValue;
		}
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
		if (includeInAll != null) {
			builder.field("include_in_all", includeInAll);
		}
	}
}
