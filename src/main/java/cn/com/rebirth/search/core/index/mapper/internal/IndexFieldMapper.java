/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexFieldMapper.java 2012-7-6 14:28:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.internal;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.core.index.mapper.InternalMapper;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperBuilders;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.mapper.RootMapper;
import cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.TypeParsers;

/**
 * The Class IndexFieldMapper.
 *
 * @author l.xue.nong
 */
public class IndexFieldMapper extends AbstractFieldMapper<String> implements InternalMapper, RootMapper {

	/** The Constant NAME. */
	public static final String NAME = "_index";

	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "_index";

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends AbstractFieldMapper.Defaults {

		/** The Constant NAME. */
		public static final String NAME = IndexFieldMapper.NAME;

		/** The Constant INDEX_NAME. */
		public static final String INDEX_NAME = IndexFieldMapper.NAME;

		/** The Constant INDEX. */
		public static final Field.Index INDEX = Field.Index.NOT_ANALYZED;

		/** The Constant STORE. */
		public static final Field.Store STORE = Field.Store.NO;

		/** The Constant OMIT_NORMS. */
		public static final boolean OMIT_NORMS = true;

		/** The Constant OMIT_TERM_FREQ_AND_POSITIONS. */
		public static final boolean OMIT_TERM_FREQ_AND_POSITIONS = true;

		/** The Constant ENABLED. */
		public static final boolean ENABLED = false;
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends AbstractFieldMapper.Builder<Builder, IndexFieldMapper> {

		/** The enabled. */
		private boolean enabled = Defaults.ENABLED;

		/**
		 * Instantiates a new builder.
		 */
		public Builder() {
			super(Defaults.NAME);
			indexName = Defaults.INDEX_NAME;
			index = Defaults.INDEX;
			store = Defaults.STORE;
			omitNorms = Defaults.OMIT_NORMS;
			omitTermFreqAndPositions = Defaults.OMIT_TERM_FREQ_AND_POSITIONS;
		}

		/**
		 * Enabled.
		 *
		 * @param enabled the enabled
		 * @return the builder
		 */
		public Builder enabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.Builder#build(cn.com.rebirth.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public IndexFieldMapper build(BuilderContext context) {
			return new IndexFieldMapper(name, indexName, store, termVector, boost, omitNorms, omitTermFreqAndPositions,
					enabled);
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
			IndexFieldMapper.Builder builder = MapperBuilders.index();
			TypeParsers.parseField(builder, builder.name, node, parserContext);

			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String fieldName = Strings.toUnderscoreCase(entry.getKey());
				Object fieldNode = entry.getValue();
				if (fieldName.equals("enabled")) {
					builder.enabled(XContentMapValues.nodeBooleanValue(fieldNode));
				}
			}
			return builder;
		}
	}

	/** The enabled. */
	private final boolean enabled;

	/**
	 * Instantiates a new index field mapper.
	 */
	public IndexFieldMapper() {
		this(Defaults.NAME, Defaults.INDEX_NAME);
	}

	/**
	 * Instantiates a new index field mapper.
	 *
	 * @param name the name
	 * @param indexName the index name
	 */
	protected IndexFieldMapper(String name, String indexName) {
		this(name, indexName, Defaults.STORE, Defaults.TERM_VECTOR, Defaults.BOOST, Defaults.OMIT_NORMS,
				Defaults.OMIT_TERM_FREQ_AND_POSITIONS, Defaults.ENABLED);
	}

	/**
	 * Instantiates a new index field mapper.
	 *
	 * @param name the name
	 * @param indexName the index name
	 * @param store the store
	 * @param termVector the term vector
	 * @param boost the boost
	 * @param omitNorms the omit norms
	 * @param omitTermFreqAndPositions the omit term freq and positions
	 * @param enabled the enabled
	 */
	public IndexFieldMapper(String name, String indexName, Field.Store store, Field.TermVector termVector, float boost,
			boolean omitNorms, boolean omitTermFreqAndPositions, boolean enabled) {
		super(new Names(name, indexName, indexName, name), Defaults.INDEX, store, termVector, boost, omitNorms,
				omitTermFreqAndPositions, Lucene.KEYWORD_ANALYZER, Lucene.KEYWORD_ANALYZER);
		this.enabled = enabled;
	}

	/**
	 * Enabled.
	 *
	 * @return true, if successful
	 */
	public boolean enabled() {
		return this.enabled;
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

	/**
	 * Term.
	 *
	 * @param value the value
	 * @return the term
	 */
	public Term term(String value) {
		return names().createIndexNameTerm(value);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.RootMapper#preParse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void preParse(ParseContext context) throws IOException {

		super.parse(context);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.RootMapper#postParse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void postParse(ParseContext context) throws IOException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#parse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void parse(ParseContext context) throws IOException {

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.RootMapper#validate(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void validate(ParseContext context) throws MapperParsingException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.RootMapper#includeInObject()
	 */
	@Override
	public boolean includeInObject() {
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#parseCreateField(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	protected Field parseCreateField(ParseContext context) throws IOException {
		if (!enabled) {
			return null;
		}
		return new Field(names.indexName(), context.index(), store, index);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#contentType()
	 */
	@Override
	protected String contentType() {
		return CONTENT_TYPE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {

		if (store == Defaults.STORE && enabled == Defaults.ENABLED) {
			return builder;
		}
		builder.startObject(CONTENT_TYPE);
		if (store != Defaults.STORE) {
			builder.field("store", store.name().toLowerCase());
		}
		if (enabled != Defaults.ENABLED) {
			builder.field("enabled", enabled);
		}
		builder.endObject();
		return builder;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#merge(cn.com.rebirth.search.core.index.mapper.Mapper, cn.com.rebirth.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {

	}
}
