/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ParentFieldMapper.java 2012-7-6 14:30:36 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.internal;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.PublicTermsFilter;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.core.index.mapper.InternalMapper;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.mapper.RootMapper;
import cn.com.rebirth.search.core.index.mapper.Uid;
import cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper;
import cn.com.rebirth.search.core.index.query.QueryParseContext;

/**
 * The Class ParentFieldMapper.
 *
 * @author l.xue.nong
 */
public class ParentFieldMapper extends AbstractFieldMapper<Uid> implements InternalMapper, RootMapper {

	/** The Constant NAME. */
	public static final String NAME = "_parent";

	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "_parent";

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends AbstractFieldMapper.Defaults {

		/** The Constant NAME. */
		public static final String NAME = ParentFieldMapper.NAME;

		/** The Constant INDEX. */
		public static final Field.Index INDEX = Field.Index.NOT_ANALYZED;

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
	public static class Builder extends Mapper.Builder<Builder, ParentFieldMapper> {

		/** The index name. */
		protected String indexName;

		/** The type. */
		private String type;

		/**
		 * Instantiates a new builder.
		 */
		public Builder() {
			super(Defaults.NAME);
			this.indexName = name;
		}

		/**
		 * Type.
		 *
		 * @param type the type
		 * @return the builder
		 */
		public Builder type(String type) {
			this.type = type;
			return builder;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.Builder#build(cn.com.rebirth.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public ParentFieldMapper build(BuilderContext context) {
			if (type == null) {
				throw new MapperParsingException("Parent mapping must contain the parent type");
			}
			return new ParentFieldMapper(name, indexName, type);
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
			ParentFieldMapper.Builder builder = new ParentFieldMapper.Builder();
			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String fieldName = Strings.toUnderscoreCase(entry.getKey());
				Object fieldNode = entry.getValue();
				if (fieldName.equals("type")) {
					builder.type(fieldNode.toString());
				}
			}
			return builder;
		}
	}

	/** The type. */
	private final String type;

	/**
	 * Instantiates a new parent field mapper.
	 *
	 * @param name the name
	 * @param indexName the index name
	 * @param type the type
	 */
	protected ParentFieldMapper(String name, String indexName, String type) {
		super(new Names(name, indexName, indexName, name), Defaults.INDEX, Field.Store.YES, Defaults.TERM_VECTOR,
				Defaults.BOOST, Defaults.OMIT_NORMS, Defaults.OMIT_TERM_FREQ_AND_POSITIONS, Lucene.KEYWORD_ANALYZER,
				Lucene.KEYWORD_ANALYZER);
		this.type = type;
	}

	/**
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		return type;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.RootMapper#preParse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void preParse(ParseContext context) throws IOException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.RootMapper#postParse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void postParse(ParseContext context) throws IOException {
		parse(context);
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
		if (context.parser().currentName() != null && context.parser().currentName().equals(Defaults.NAME)) {

			String parentId = context.parser().text();
			context.sourceToParse().parent(parentId);
			return new Field(names.indexName(), Uid.createUid(context.stringBuilder(), type, parentId), store, index);
		}

		String parsedParentId = context.doc().get(Defaults.NAME);
		if (context.sourceToParse().parent() != null) {
			String parentId = context.sourceToParse().parent();
			if (parsedParentId == null) {
				if (parentId == null) {
					throw new MapperParsingException(
							"No parent id provided, not within the document, and not externally");
				}

				return new Field(names.indexName(), Uid.createUid(context.stringBuilder(), type, parentId), store,
						index);
			} else if (parentId != null
					&& !parsedParentId.equals(Uid.createUid(context.stringBuilder(), type, parentId))) {
				throw new MapperParsingException("Parent id mismatch, document value is ["
						+ Uid.createUid(parsedParentId).id() + "], while external value is [" + parentId + "]");
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#value(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public Uid value(Fieldable field) {
		return Uid.createUid(field.stringValue());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#valueFromString(java.lang.String)
	 */
	@Override
	public Uid valueFromString(String value) {
		return Uid.createUid(value);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#valueAsString(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public String valueAsString(Fieldable field) {
		return field.stringValue();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#valueForSearch(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public Object valueForSearch(Fieldable field) {
		String fieldValue = field.stringValue();
		if (fieldValue == null) {
			return null;
		}
		int index = fieldValue.indexOf(Uid.DELIMITER);
		if (index == -1) {
			return fieldValue;
		}
		return fieldValue.substring(index + 1);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#indexedValue(java.lang.String)
	 */
	@Override
	public String indexedValue(String value) {
		if (value.indexOf(Uid.DELIMITER) == -1) {
			return Uid.createUid(type, value);
		}
		return value;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#fieldQuery(java.lang.String, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query fieldQuery(String value, @Nullable QueryParseContext context) {
		if (context == null) {
			return super.fieldQuery(value, context);
		}
		return new ConstantScoreQuery(fieldFilter(value, context));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#fieldFilter(java.lang.String, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter fieldFilter(String value, @Nullable QueryParseContext context) {
		if (context == null) {
			return super.fieldFilter(value, context);
		}

		PublicTermsFilter filter = new PublicTermsFilter();
		for (String type : context.mapperService().types()) {
			filter.addTerm(names.createIndexNameTerm(Uid.createUid(type, value)));
		}
		return filter;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#useFieldQueryWithQueryString()
	 */
	@Override
	public boolean useFieldQueryWithQueryString() {
		return true;
	}

	/**
	 * Term.
	 *
	 * @param type the type
	 * @param id the id
	 * @return the term
	 */
	public Term term(String type, String id) {
		return term(Uid.createUid(type, id));
	}

	/**
	 * Term.
	 *
	 * @param uid the uid
	 * @return the term
	 */
	public Term term(String uid) {
		return names().createIndexNameTerm(uid);
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
		builder.startObject(CONTENT_TYPE);
		builder.field("type", type);
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
