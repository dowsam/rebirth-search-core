/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IdFieldMapper.java 2012-7-6 14:30:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.internal;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PrefixFilter;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.commons.lucene.search.XBooleanFilter;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
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
import cn.com.rebirth.search.core.index.search.UidFilter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * The Class IdFieldMapper.
 *
 * @author l.xue.nong
 */
public class IdFieldMapper extends AbstractFieldMapper<String> implements InternalMapper, RootMapper {

	/** The Constant NAME. */
	public static final String NAME = "_id";

	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "_id";

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends AbstractFieldMapper.Defaults {

		/** The Constant NAME. */
		public static final String NAME = IdFieldMapper.NAME;

		/** The Constant INDEX_NAME. */
		public static final String INDEX_NAME = IdFieldMapper.NAME;

		/** The Constant INDEX. */
		public static final Field.Index INDEX = Field.Index.NO;

		/** The Constant STORE. */
		public static final Field.Store STORE = Field.Store.NO;

		/** The Constant OMIT_NORMS. */
		public static final boolean OMIT_NORMS = true;

		/** The Constant OMIT_TERM_FREQ_AND_POSITIONS. */
		public static final boolean OMIT_TERM_FREQ_AND_POSITIONS = true;

		/** The Constant PATH. */
		public static final String PATH = null;
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends AbstractFieldMapper.Builder<Builder, IdFieldMapper> {

		/** The path. */
		private String path = Defaults.PATH;

		/**
		 * Instantiates a new builder.
		 */
		public Builder() {
			super(Defaults.NAME);
			indexName = Defaults.INDEX_NAME;
			store = Defaults.STORE;
			index = Defaults.INDEX;
			omitNorms = Defaults.OMIT_NORMS;
			omitTermFreqAndPositions = Defaults.OMIT_TERM_FREQ_AND_POSITIONS;
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
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.Builder#build(cn.com.rebirth.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public IdFieldMapper build(BuilderContext context) {
			return new IdFieldMapper(name, indexName, index, store, termVector, boost, omitNorms,
					omitTermFreqAndPositions, path);
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
			IdFieldMapper.Builder builder = MapperBuilders.id();
			TypeParsers.parseField(builder, builder.name, node, parserContext);
			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String fieldName = Strings.toUnderscoreCase(entry.getKey());
				Object fieldNode = entry.getValue();
				if (fieldName.equals("path")) {
					builder.path(fieldNode.toString());
				}
			}
			return builder;
		}
	}

	/** The path. */
	private final String path;

	/**
	 * Instantiates a new id field mapper.
	 */
	public IdFieldMapper() {
		this(Defaults.NAME, Defaults.INDEX_NAME, Defaults.INDEX);
	}

	/**
	 * Instantiates a new id field mapper.
	 *
	 * @param index the index
	 */
	public IdFieldMapper(Field.Index index) {
		this(Defaults.NAME, Defaults.INDEX_NAME, index);
	}

	/**
	 * Instantiates a new id field mapper.
	 *
	 * @param name the name
	 * @param indexName the index name
	 * @param index the index
	 */
	protected IdFieldMapper(String name, String indexName, Field.Index index) {
		this(name, indexName, index, Defaults.STORE, Defaults.TERM_VECTOR, Defaults.BOOST, Defaults.OMIT_NORMS,
				Defaults.OMIT_TERM_FREQ_AND_POSITIONS, Defaults.PATH);
	}

	/**
	 * Instantiates a new id field mapper.
	 *
	 * @param name the name
	 * @param indexName the index name
	 * @param index the index
	 * @param store the store
	 * @param termVector the term vector
	 * @param boost the boost
	 * @param omitNorms the omit norms
	 * @param omitTermFreqAndPositions the omit term freq and positions
	 * @param path the path
	 */
	protected IdFieldMapper(String name, String indexName, Field.Index index, Field.Store store,
			Field.TermVector termVector, float boost, boolean omitNorms, boolean omitTermFreqAndPositions, String path) {
		super(new Names(name, indexName, indexName, name), index, store, termVector, boost, omitNorms,
				omitTermFreqAndPositions, Lucene.KEYWORD_ANALYZER, Lucene.KEYWORD_ANALYZER);
		this.path = path;
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
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#useFieldQueryWithQueryString()
	 */
	@Override
	public boolean useFieldQueryWithQueryString() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#fieldQuery(java.lang.String, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query fieldQuery(String value, @Nullable QueryParseContext context) {
		if (indexed() || context == null) {
			return super.fieldQuery(value, context);
		}
		UidFilter filter = new UidFilter(context.queryTypes(), ImmutableList.of(value), context.indexCache()
				.bloomCache());

		return new ConstantScoreQuery(filter);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#fieldFilter(java.lang.String, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter fieldFilter(String value, @Nullable QueryParseContext context) {
		if (indexed() || context == null) {
			return super.fieldFilter(value, context);
		}
		return new UidFilter(context.queryTypes(), ImmutableList.of(value), context.indexCache().bloomCache());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#prefixQuery(java.lang.String, org.apache.lucene.search.MultiTermQuery.RewriteMethod, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query prefixQuery(String value, @Nullable MultiTermQuery.RewriteMethod method,
			@Nullable QueryParseContext context) {
		if (indexed() || context == null) {
			return super.prefixQuery(value, method, context);
		}
		Collection<String> queryTypes = context.queryTypes();
		if (queryTypes.size() == 1) {
			PrefixQuery prefixQuery = new PrefixQuery(UidFieldMapper.TERM_FACTORY.createTerm(Uid.createUid(
					Iterables.getFirst(queryTypes, null), value)));
			if (method != null) {
				prefixQuery.setRewriteMethod(method);
			}
		}
		BooleanQuery query = new BooleanQuery();
		for (String queryType : queryTypes) {
			PrefixQuery prefixQuery = new PrefixQuery(UidFieldMapper.TERM_FACTORY.createTerm(Uid.createUid(queryType,
					value)));
			if (method != null) {
				prefixQuery.setRewriteMethod(method);
			}
			query.add(prefixQuery, BooleanClause.Occur.SHOULD);
		}
		return query;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#prefixFilter(java.lang.String, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter prefixFilter(String value, @Nullable QueryParseContext context) {
		if (indexed() || context == null) {
			return super.prefixFilter(value, context);
		}
		Collection<String> queryTypes = context.queryTypes();
		if (queryTypes.size() == 1) {
			return new PrefixFilter(UidFieldMapper.TERM_FACTORY.createTerm(Uid.createUid(
					Iterables.getFirst(queryTypes, null), value)));
		}
		XBooleanFilter filter = new XBooleanFilter();
		for (String queryType : queryTypes) {
			filter.addShould(new PrefixFilter(UidFieldMapper.TERM_FACTORY.createTerm(Uid.createUid(queryType, value))));
		}
		return filter;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.RootMapper#preParse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void preParse(ParseContext context) throws IOException {
		if (context.sourceToParse().id() != null) {
			context.id(context.sourceToParse().id());
			super.parse(context);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.RootMapper#postParse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void postParse(ParseContext context) throws IOException {
		if (context.id() == null && !context.sourceToParse().flyweight()) {
			throw new MapperParsingException("No id found while parsing the content source");
		}

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#parse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void parse(ParseContext context) throws IOException {
		super.parse(context);
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
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#parseCreateField(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	protected Field parseCreateField(ParseContext context) throws IOException {
		XContentParser parser = context.parser();
		if (parser.currentName() != null && parser.currentName().equals(Defaults.NAME)
				&& parser.currentToken().isValue()) {

			String id = parser.text();
			if (context.id() != null && !context.id().equals(id)) {
				throw new MapperParsingException("Provided id [" + context.id() + "] does not match the content one ["
						+ id + "]");
			}
			context.id(id);
			if (index == Field.Index.NO && store == Field.Store.NO) {
				return null;
			}
			return new Field(names.indexName(), false, context.id(), store, index, termVector);
		} else {

			if (index == Field.Index.NO && store == Field.Store.NO) {
				return null;
			}
			return new Field(names.indexName(), false, context.id(), store, index, termVector);
		}
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

		if (store == Defaults.STORE && index == Defaults.INDEX && path == Defaults.PATH) {
			return builder;
		}
		builder.startObject(CONTENT_TYPE);
		if (store != Defaults.STORE) {
			builder.field("store", store.name().toLowerCase());
		}
		if (index != Defaults.INDEX) {
			builder.field("index", index.name().toLowerCase());
		}
		if (path != Defaults.PATH) {
			builder.field("path", path);
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
