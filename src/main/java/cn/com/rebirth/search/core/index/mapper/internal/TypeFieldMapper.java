/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TypeFieldMapper.java 2012-7-6 14:29:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.internal;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.PrefixFilter;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.commons.lucene.search.DeletionAwareConstantScoreQuery;
import cn.com.rebirth.search.commons.lucene.search.TermFilter;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.index.mapper.InternalMapper;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperBuilders;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.mapper.RootMapper;
import cn.com.rebirth.search.core.index.mapper.Uid;
import cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.TypeParsers;
import cn.com.rebirth.search.core.index.query.QueryParseContext;

/**
 * The Class TypeFieldMapper.
 *
 * @author l.xue.nong
 */
public class TypeFieldMapper extends AbstractFieldMapper<String> implements InternalMapper, RootMapper {

	/** The Constant NAME. */
	public static final String NAME = "_type";

	/** The Constant TERM_FACTORY. */
	public static final Term TERM_FACTORY = new Term(NAME, "");

	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "_type";

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends AbstractFieldMapper.Defaults {

		/** The Constant NAME. */
		public static final String NAME = TypeFieldMapper.NAME;

		/** The Constant INDEX_NAME. */
		public static final String INDEX_NAME = TypeFieldMapper.NAME;

		/** The Constant INDEX. */
		public static final Field.Index INDEX = Field.Index.NOT_ANALYZED;

		/** The Constant STORE. */
		public static final Field.Store STORE = Field.Store.NO;

		/** The Constant OMIT_NORMS. */
		public static final boolean OMIT_NORMS = true;

		/** The Constant OMIT_TERM_FREQ_AND_POSITIONS. */
		public static final boolean OMIT_TERM_FREQ_AND_POSITIONS = true;
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends AbstractFieldMapper.Builder<Builder, TypeFieldMapper> {

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

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.Builder#build(cn.com.rebirth.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public TypeFieldMapper build(BuilderContext context) {
			return new TypeFieldMapper(name, indexName, index, store, termVector, boost, omitNorms,
					omitTermFreqAndPositions);
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
			TypeFieldMapper.Builder builder = MapperBuilders.type();
			TypeParsers.parseField(builder, builder.name, node, parserContext);
			return builder;
		}
	}

	/**
	 * Instantiates a new type field mapper.
	 */
	public TypeFieldMapper() {
		this(Defaults.NAME, Defaults.INDEX_NAME);
	}

	/**
	 * Instantiates a new type field mapper.
	 *
	 * @param name the name
	 * @param indexName the index name
	 */
	protected TypeFieldMapper(String name, String indexName) {
		this(name, indexName, Defaults.INDEX, Defaults.STORE, Defaults.TERM_VECTOR, Defaults.BOOST,
				Defaults.OMIT_NORMS, Defaults.OMIT_TERM_FREQ_AND_POSITIONS);
	}

	/**
	 * Instantiates a new type field mapper.
	 *
	 * @param name the name
	 * @param indexName the index name
	 * @param index the index
	 * @param store the store
	 * @param termVector the term vector
	 * @param boost the boost
	 * @param omitNorms the omit norms
	 * @param omitTermFreqAndPositions the omit term freq and positions
	 */
	public TypeFieldMapper(String name, String indexName, Field.Index index, Field.Store store,
			Field.TermVector termVector, float boost, boolean omitNorms, boolean omitTermFreqAndPositions) {
		super(new Names(name, indexName, indexName, name), index, store, termVector, boost, omitNorms,
				omitTermFreqAndPositions, Lucene.KEYWORD_ANALYZER, Lucene.KEYWORD_ANALYZER);
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
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#fieldFilter(java.lang.String, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter fieldFilter(String value, @Nullable QueryParseContext context) {
		if (index == Field.Index.NO) {
			return new PrefixFilter(UidFieldMapper.TERM_FACTORY.createTerm(Uid.typePrefix(value)));
		}
		return new TermFilter(names().createIndexNameTerm(value));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#fieldQuery(java.lang.String, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query fieldQuery(String value, @Nullable QueryParseContext context) {
		return new DeletionAwareConstantScoreQuery(context.cacheFilter(fieldFilter(value, context), null));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#useFieldQueryWithQueryString()
	 */
	@Override
	public boolean useFieldQueryWithQueryString() {
		return true;
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
		if (index == Field.Index.NO && store == Field.Store.NO) {
			return null;
		}
		return new Field(names.indexName(), false, context.type(), store, index, termVector);
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

		if (store == Defaults.STORE && index == Defaults.INDEX) {
			return builder;
		}
		builder.startObject(CONTENT_TYPE);
		if (store != Defaults.STORE) {
			builder.field("store", store.name().toLowerCase());
		}
		if (index != Defaults.INDEX) {
			builder.field("index", index.name().toLowerCase());
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
