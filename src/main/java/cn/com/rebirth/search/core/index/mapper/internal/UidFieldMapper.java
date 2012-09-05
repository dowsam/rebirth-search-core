/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core UidFieldMapper.java 2012-7-6 14:30:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.internal;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;

import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.commons.lucene.uid.UidField;
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

/**
 * The Class UidFieldMapper.
 *
 * @author l.xue.nong
 */
public class UidFieldMapper extends AbstractFieldMapper<Uid> implements InternalMapper, RootMapper {

	/** The Constant NAME. */
	public static final String NAME = "_uid";

	/** The Constant TERM_FACTORY. */
	public static final Term TERM_FACTORY = new Term(NAME, "");

	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "_uid";

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends AbstractFieldMapper.Defaults {

		/** The Constant NAME. */
		public static final String NAME = UidFieldMapper.NAME;

		/** The Constant INDEX. */
		public static final Field.Index INDEX = Field.Index.NOT_ANALYZED;

		/** The Constant OMIT_NORMS. */
		public static final boolean OMIT_NORMS = true;

		/** The Constant OMIT_TERM_FREQ_AND_POSITIONS. */
		public static final boolean OMIT_TERM_FREQ_AND_POSITIONS = false;
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends Mapper.Builder<Builder, UidFieldMapper> {

		/** The index name. */
		protected String indexName;

		/**
		 * Instantiates a new builder.
		 */
		public Builder() {
			super(Defaults.NAME);
			this.indexName = name;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.Builder#build(cn.com.rebirth.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public UidFieldMapper build(BuilderContext context) {
			return new UidFieldMapper(name, indexName);
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
			return MapperBuilders.uid();
		}
	}

	/** The field cache. */
	private ThreadLocal<UidField> fieldCache = new ThreadLocal<UidField>() {
		@Override
		protected UidField initialValue() {
			return new UidField(names().indexName(), "", 0);
		}
	};

	/**
	 * Instantiates a new uid field mapper.
	 */
	public UidFieldMapper() {
		this(Defaults.NAME);
	}

	/**
	 * Instantiates a new uid field mapper.
	 *
	 * @param name the name
	 */
	protected UidFieldMapper(String name) {
		this(name, name);
	}

	/**
	 * Instantiates a new uid field mapper.
	 *
	 * @param name the name
	 * @param indexName the index name
	 */
	protected UidFieldMapper(String name, String indexName) {
		super(new Names(name, indexName, indexName, name), Defaults.INDEX, Field.Store.YES, Defaults.TERM_VECTOR,
				Defaults.BOOST, Defaults.OMIT_NORMS, Defaults.OMIT_TERM_FREQ_AND_POSITIONS, Lucene.KEYWORD_ANALYZER,
				Lucene.KEYWORD_ANALYZER);
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

		if (context.sourceToParse().id() == null) {
			super.parse(context);

			if (context.docs().size() > 1) {
				UidField uidField = (UidField) context.rootDoc().getFieldable(UidFieldMapper.NAME);
				assert uidField != null;

				for (int i = 1; i < context.docs().size(); i++) {

					context.docs()
							.get(i)
							.add(new Field(UidFieldMapper.NAME, uidField.uid(), Field.Store.NO,
									Field.Index.NOT_ANALYZED));
				}
			}
		}
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
	protected Fieldable parseCreateField(ParseContext context) throws IOException {
		context.uid(Uid.createUid(context.stringBuilder(), context.type(), context.id()));

		UidField field = fieldCache.get();
		field.setUid(context.uid());
		return field;
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
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#indexedValue(java.lang.String)
	 */
	@Override
	public String indexedValue(String value) {
		return value;
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
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#close()
	 */
	@Override
	public void close() {
		fieldCache.remove();
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

		return builder;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#merge(cn.com.rebirth.search.core.index.mapper.Mapper, cn.com.rebirth.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {

	}
}
