/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AbstractFieldMapper.java 2012-7-6 14:29:01 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.core;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PrefixFilter;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeFilter;
import org.apache.lucene.search.TermRangeQuery;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.commons.lucene.search.TermFilter;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.FieldMapperListener;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ObjectMapperListener;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.query.QueryParseContext;
import cn.com.rebirth.search.index.analysis.NamedAnalyzer;

/**
 * The Class AbstractFieldMapper.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public abstract class AbstractFieldMapper<T> implements FieldMapper<T>, Mapper {

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults {

		/** The Constant INDEX. */
		public static final Field.Index INDEX = Field.Index.ANALYZED;

		/** The Constant STORE. */
		public static final Field.Store STORE = Field.Store.NO;

		/** The Constant TERM_VECTOR. */
		public static final Field.TermVector TERM_VECTOR = Field.TermVector.NO;

		/** The Constant BOOST. */
		public static final float BOOST = 1.0f;

		/** The Constant OMIT_NORMS. */
		public static final boolean OMIT_NORMS = false;

		/** The Constant OMIT_TERM_FREQ_AND_POSITIONS. */
		public static final boolean OMIT_TERM_FREQ_AND_POSITIONS = false;
	}

	/**
	 * The Class OpenBuilder.
	 *
	 * @param <T> the generic type
	 * @param <Y> the generic type
	 * @author l.xue.nong
	 */
	public abstract static class OpenBuilder<T extends Builder, Y extends AbstractFieldMapper> extends
			AbstractFieldMapper.Builder<T, Y> {

		/**
		 * Instantiates a new open builder.
		 *
		 * @param name the name
		 */
		protected OpenBuilder(String name) {
			super(name);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#index(org.apache.lucene.document.Field.Index)
		 */
		@Override
		public T index(Field.Index index) {
			return super.index(index);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#store(org.apache.lucene.document.Field.Store)
		 */
		@Override
		public T store(Field.Store store) {
			return super.store(store);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#termVector(org.apache.lucene.document.Field.TermVector)
		 */
		@Override
		public T termVector(Field.TermVector termVector) {
			return super.termVector(termVector);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#boost(float)
		 */
		@Override
		public T boost(float boost) {
			return super.boost(boost);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#omitNorms(boolean)
		 */
		@Override
		public T omitNorms(boolean omitNorms) {
			return super.omitNorms(omitNorms);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#omitTermFreqAndPositions(boolean)
		 */
		@Override
		public T omitTermFreqAndPositions(boolean omitTermFreqAndPositions) {
			return super.omitTermFreqAndPositions(omitTermFreqAndPositions);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#indexName(java.lang.String)
		 */
		@Override
		public T indexName(String indexName) {
			return super.indexName(indexName);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#indexAnalyzer(cn.com.rebirth.search.index.analysis.NamedAnalyzer)
		 */
		@Override
		public T indexAnalyzer(NamedAnalyzer indexAnalyzer) {
			return super.indexAnalyzer(indexAnalyzer);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#searchAnalyzer(cn.com.rebirth.search.index.analysis.NamedAnalyzer)
		 */
		@Override
		public T searchAnalyzer(NamedAnalyzer searchAnalyzer) {
			return super.searchAnalyzer(searchAnalyzer);
		}
	}

	/**
	 * The Class Builder.
	 *
	 * @param <T> the generic type
	 * @param <Y> the generic type
	 * @author l.xue.nong
	 */
	public abstract static class Builder<T extends Builder, Y extends AbstractFieldMapper> extends Mapper.Builder<T, Y> {

		/** The index. */
		protected Field.Index index = Defaults.INDEX;

		/** The store. */
		protected Field.Store store = Defaults.STORE;

		/** The term vector. */
		protected Field.TermVector termVector = Defaults.TERM_VECTOR;

		/** The boost. */
		protected float boost = Defaults.BOOST;

		/** The omit norms. */
		protected boolean omitNorms = Defaults.OMIT_NORMS;

		/** The omit term freq and positions. */
		protected boolean omitTermFreqAndPositions = Defaults.OMIT_TERM_FREQ_AND_POSITIONS;

		/** The index name. */
		protected String indexName;

		/** The index analyzer. */
		protected NamedAnalyzer indexAnalyzer;

		/** The search analyzer. */
		protected NamedAnalyzer searchAnalyzer;

		/** The include in all. */
		protected Boolean includeInAll;

		/**
		 * Instantiates a new builder.
		 *
		 * @param name the name
		 */
		protected Builder(String name) {
			super(name);
		}

		/**
		 * Index.
		 *
		 * @param index the index
		 * @return the t
		 */
		protected T index(Field.Index index) {
			this.index = index;
			return builder;
		}

		/**
		 * Store.
		 *
		 * @param store the store
		 * @return the t
		 */
		protected T store(Field.Store store) {
			this.store = store;
			return builder;
		}

		/**
		 * Term vector.
		 *
		 * @param termVector the term vector
		 * @return the t
		 */
		protected T termVector(Field.TermVector termVector) {
			this.termVector = termVector;
			return builder;
		}

		/**
		 * Boost.
		 *
		 * @param boost the boost
		 * @return the t
		 */
		protected T boost(float boost) {
			this.boost = boost;
			return builder;
		}

		/**
		 * Omit norms.
		 *
		 * @param omitNorms the omit norms
		 * @return the t
		 */
		protected T omitNorms(boolean omitNorms) {
			this.omitNorms = omitNorms;
			return builder;
		}

		/**
		 * Omit term freq and positions.
		 *
		 * @param omitTermFreqAndPositions the omit term freq and positions
		 * @return the t
		 */
		protected T omitTermFreqAndPositions(boolean omitTermFreqAndPositions) {
			this.omitTermFreqAndPositions = omitTermFreqAndPositions;
			return builder;
		}

		/**
		 * Index name.
		 *
		 * @param indexName the index name
		 * @return the t
		 */
		protected T indexName(String indexName) {
			this.indexName = indexName;
			return builder;
		}

		/**
		 * Index analyzer.
		 *
		 * @param indexAnalyzer the index analyzer
		 * @return the t
		 */
		protected T indexAnalyzer(NamedAnalyzer indexAnalyzer) {
			this.indexAnalyzer = indexAnalyzer;
			if (this.searchAnalyzer == null) {
				this.searchAnalyzer = indexAnalyzer;
			}
			return builder;
		}

		/**
		 * Search analyzer.
		 *
		 * @param searchAnalyzer the search analyzer
		 * @return the t
		 */
		protected T searchAnalyzer(NamedAnalyzer searchAnalyzer) {
			this.searchAnalyzer = searchAnalyzer;
			return builder;
		}

		/**
		 * Include in all.
		 *
		 * @param includeInAll the include in all
		 * @return the t
		 */
		protected T includeInAll(Boolean includeInAll) {
			this.includeInAll = includeInAll;
			return builder;
		}

		/**
		 * Builds the names.
		 *
		 * @param context the context
		 * @return the names
		 */
		protected Names buildNames(BuilderContext context) {
			return new Names(name, buildIndexName(context), indexName == null ? name : indexName,
					buildFullName(context), context.path().sourcePath());
		}

		/**
		 * Builds the index name.
		 *
		 * @param context the context
		 * @return the string
		 */
		protected String buildIndexName(BuilderContext context) {
			String actualIndexName = indexName == null ? name : indexName;
			return context.path().pathAsText(actualIndexName);
		}

		/**
		 * Builds the full name.
		 *
		 * @param context the context
		 * @return the string
		 */
		protected String buildFullName(BuilderContext context) {
			return context.path().fullPathAsText(name);
		}
	}

	/** The names. */
	protected final Names names;

	/** The index. */
	protected final Field.Index index;

	/** The store. */
	protected final Field.Store store;

	/** The term vector. */
	protected final Field.TermVector termVector;

	/** The boost. */
	protected float boost;

	/** The omit norms. */
	protected final boolean omitNorms;

	/** The omit term freq and positions. */
	protected final boolean omitTermFreqAndPositions;

	/** The index options. */
	protected final FieldInfo.IndexOptions indexOptions;

	/** The index analyzer. */
	protected final NamedAnalyzer indexAnalyzer;

	/** The search analyzer. */
	protected final NamedAnalyzer searchAnalyzer;

	/**
	 * Instantiates a new abstract field mapper.
	 *
	 * @param names the names
	 * @param index the index
	 * @param store the store
	 * @param termVector the term vector
	 * @param boost the boost
	 * @param omitNorms the omit norms
	 * @param omitTermFreqAndPositions the omit term freq and positions
	 * @param indexAnalyzer the index analyzer
	 * @param searchAnalyzer the search analyzer
	 */
	protected AbstractFieldMapper(Names names, Field.Index index, Field.Store store, Field.TermVector termVector,
			float boost, boolean omitNorms, boolean omitTermFreqAndPositions, NamedAnalyzer indexAnalyzer,
			NamedAnalyzer searchAnalyzer) {
		this.names = names;
		this.index = index;
		this.store = store;
		this.termVector = termVector;
		this.boost = boost;
		this.omitNorms = omitNorms;
		this.omitTermFreqAndPositions = omitTermFreqAndPositions;
		this.indexOptions = omitTermFreqAndPositions ? FieldInfo.IndexOptions.DOCS_ONLY
				: FieldInfo.IndexOptions.DOCS_AND_FREQS_AND_POSITIONS;
		if (indexAnalyzer == null && !index.isAnalyzed()) {
			this.indexAnalyzer = Lucene.KEYWORD_ANALYZER;
		} else {
			this.indexAnalyzer = indexAnalyzer;
		}
		if (searchAnalyzer == null && !index.isAnalyzed()) {
			this.searchAnalyzer = Lucene.KEYWORD_ANALYZER;
		} else {
			this.searchAnalyzer = searchAnalyzer;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#name()
	 */
	@Override
	public String name() {
		return names.name();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#names()
	 */
	@Override
	public Names names() {
		return this.names;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#index()
	 */
	@Override
	public Field.Index index() {
		return this.index;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#store()
	 */
	@Override
	public Field.Store store() {
		return this.store;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#stored()
	 */
	@Override
	public boolean stored() {
		return store == Field.Store.YES;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#indexed()
	 */
	@Override
	public boolean indexed() {
		return index != Field.Index.NO;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#analyzed()
	 */
	@Override
	public boolean analyzed() {
		return index == Field.Index.ANALYZED;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#termVector()
	 */
	@Override
	public Field.TermVector termVector() {
		return this.termVector;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#boost()
	 */
	@Override
	public float boost() {
		return this.boost;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#omitNorms()
	 */
	@Override
	public boolean omitNorms() {
		return this.omitNorms;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#omitTermFreqAndPositions()
	 */
	@Override
	public boolean omitTermFreqAndPositions() {
		return this.omitTermFreqAndPositions;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#indexAnalyzer()
	 */
	@Override
	public Analyzer indexAnalyzer() {
		return this.indexAnalyzer;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#searchAnalyzer()
	 */
	@Override
	public Analyzer searchAnalyzer() {
		return this.searchAnalyzer;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#parse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void parse(ParseContext context) throws IOException {
		try {
			Fieldable field = parseCreateField(context);
			if (field == null) {
				return;
			}
			field.setOmitNorms(omitNorms);
			field.setIndexOptions(indexOptions);
			if (!customBoost()) {
				field.setBoost(boost);
			}
			if (context.listener().beforeFieldAdded(this, field, context)) {
				context.doc().add(field);
			}
		} catch (Exception e) {
			throw new MapperParsingException("Failed to parse [" + names.fullName() + "]", e);
		}
	}

	/**
	 * Parses the create field.
	 *
	 * @param context the context
	 * @return the fieldable
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected abstract Fieldable parseCreateField(ParseContext context) throws IOException;

	/**
	 * Custom boost.
	 *
	 * @return true, if successful
	 */
	protected boolean customBoost() {
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#traverse(cn.com.rebirth.search.core.index.mapper.FieldMapperListener)
	 */
	@Override
	public void traverse(FieldMapperListener fieldMapperListener) {
		fieldMapperListener.fieldMapper(this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#traverse(cn.com.rebirth.search.core.index.mapper.ObjectMapperListener)
	 */
	@Override
	public void traverse(ObjectMapperListener objectMapperListener) {

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#valueForSearch(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public Object valueForSearch(Fieldable field) {
		return valueAsString(field);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#indexedValue(java.lang.String)
	 */
	@Override
	public String indexedValue(String value) {
		return value;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#queryStringTermQuery(org.apache.lucene.index.Term)
	 */
	@Override
	public Query queryStringTermQuery(Term term) {
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#useFieldQueryWithQueryString()
	 */
	@Override
	public boolean useFieldQueryWithQueryString() {
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#fieldQuery(java.lang.String, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query fieldQuery(String value, @Nullable QueryParseContext context) {
		return new TermQuery(names().createIndexNameTerm(indexedValue(value)));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#fieldFilter(java.lang.String, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter fieldFilter(String value, @Nullable QueryParseContext context) {
		return new TermFilter(names().createIndexNameTerm(indexedValue(value)));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#fuzzyQuery(java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public Query fuzzyQuery(String value, String minSim, int prefixLength, int maxExpansions) {
		return new FuzzyQuery(names().createIndexNameTerm(indexedValue(value)), Float.parseFloat(minSim), prefixLength,
				maxExpansions);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#fuzzyQuery(java.lang.String, double, int, int)
	 */
	@Override
	public Query fuzzyQuery(String value, double minSim, int prefixLength, int maxExpansions) {
		return new FuzzyQuery(names().createIndexNameTerm(value), (float) minSim, prefixLength, maxExpansions);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#prefixQuery(java.lang.String, org.apache.lucene.search.MultiTermQuery.RewriteMethod, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query prefixQuery(String value, @Nullable MultiTermQuery.RewriteMethod method,
			@Nullable QueryParseContext context) {
		PrefixQuery query = new PrefixQuery(names().createIndexNameTerm(indexedValue(value)));
		if (method != null) {
			query.setRewriteMethod(method);
		}
		return query;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#prefixFilter(java.lang.String, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter prefixFilter(String value, @Nullable QueryParseContext context) {
		return new PrefixFilter(names().createIndexNameTerm(indexedValue(value)));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#rangeQuery(java.lang.String, java.lang.String, boolean, boolean, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query rangeQuery(String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper,
			@Nullable QueryParseContext context) {
		return new TermRangeQuery(names.indexName(), lowerTerm == null ? null : indexedValue(lowerTerm),
				upperTerm == null ? null : indexedValue(upperTerm), includeLower, includeUpper);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#rangeFilter(java.lang.String, java.lang.String, boolean, boolean, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter rangeFilter(String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper,
			@Nullable QueryParseContext context) {
		return new TermRangeFilter(names.indexName(), lowerTerm == null ? null : indexedValue(lowerTerm),
				upperTerm == null ? null : indexedValue(upperTerm), includeLower, includeUpper);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#merge(cn.com.rebirth.search.core.index.mapper.Mapper, cn.com.rebirth.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
		if (!this.getClass().equals(mergeWith.getClass())) {
			String mergedType = mergeWith.getClass().getSimpleName();
			if (mergeWith instanceof AbstractFieldMapper) {
				mergedType = ((AbstractFieldMapper) mergeWith).contentType();
			}
			mergeContext.addConflict("mapper [" + names.fullName() + "] of different type, current_type ["
					+ contentType() + "], merged_type [" + mergedType + "]");

			return;
		}
		AbstractFieldMapper fieldMergeWith = (AbstractFieldMapper) mergeWith;
		if (!this.index.equals(fieldMergeWith.index)) {
			mergeContext.addConflict("mapper [" + names.fullName() + "] has different index values");
		}
		if (!this.store.equals(fieldMergeWith.store)) {
			mergeContext.addConflict("mapper [" + names.fullName() + "] has different store values");
		}
		if (!this.termVector.equals(fieldMergeWith.termVector)) {
			mergeContext.addConflict("mapper [" + names.fullName() + "] has different term_vector values");
		}
		if (this.indexAnalyzer == null) {
			if (fieldMergeWith.indexAnalyzer != null) {
				mergeContext.addConflict("mapper [" + names.fullName() + "] has different index_analyzer");
			}
		} else if (fieldMergeWith.indexAnalyzer == null) {
			mergeContext.addConflict("mapper [" + names.fullName() + "] has different index_analyzer");
		} else if (!this.indexAnalyzer.name().equals(fieldMergeWith.indexAnalyzer.name())) {
			mergeContext.addConflict("mapper [" + names.fullName() + "] has different index_analyzer");
		}
		if (this.searchAnalyzer == null) {
			if (fieldMergeWith.searchAnalyzer != null) {
				mergeContext.addConflict("mapper [" + names.fullName() + "] has different search_analyzer");
			}
		} else if (fieldMergeWith.searchAnalyzer == null) {
			mergeContext.addConflict("mapper [" + names.fullName() + "] has different search_analyzer");
		} else if (!this.searchAnalyzer.name().equals(fieldMergeWith.searchAnalyzer.name())) {
			mergeContext.addConflict("mapper [" + names.fullName() + "] has different search_analyzer");
		}
		if (!mergeContext.mergeFlags().simulate()) {

			this.boost = fieldMergeWith.boost;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#fieldDataType()
	 */
	@Override
	public FieldDataType fieldDataType() {
		return FieldDataType.DefaultTypes.STRING;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(names.name());
		doXContentBody(builder);
		builder.endObject();
		return builder;
	}

	/**
	 * Do x content body.
	 *
	 * @param builder the builder
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void doXContentBody(XContentBuilder builder) throws IOException {
		builder.field("type", contentType());
		if (!names.name().equals(names.indexNameClean())) {
			builder.field("index_name", names.indexNameClean());
		}
		if (boost != 1.0f) {
			builder.field("boost", boost);
		}
		if (indexAnalyzer != null && searchAnalyzer != null && indexAnalyzer.name().equals(searchAnalyzer.name())
				&& !indexAnalyzer.name().startsWith("_")) {

			builder.field("analyzer", indexAnalyzer.name());
		} else {
			if (indexAnalyzer != null && !indexAnalyzer.name().startsWith("_")) {
				builder.field("index_analyzer", indexAnalyzer.name());
			}
			if (searchAnalyzer != null && !searchAnalyzer.name().startsWith("_")) {
				builder.field("search_analyzer", searchAnalyzer.name());
			}
		}
	}

	/**
	 * Content type.
	 *
	 * @return the string
	 */
	protected abstract String contentType();

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#close()
	 */
	@Override
	public void close() {

	}

}
