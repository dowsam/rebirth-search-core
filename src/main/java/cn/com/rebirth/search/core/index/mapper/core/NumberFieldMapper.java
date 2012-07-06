/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NumberFieldMapper.java 2012-3-29 15:02:45 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper.core;

import java.io.Reader;

import org.apache.lucene.analysis.NumericTokenStream;
import org.apache.lucene.document.AbstractField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.NumericUtils;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.internal.AllFieldMapper;
import cn.com.rebirth.search.core.index.query.QueryParseContext;
import cn.com.rebirth.search.index.analysis.NamedAnalyzer;


/**
 * The Class NumberFieldMapper.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public abstract class NumberFieldMapper<T extends Number> extends AbstractFieldMapper<T> implements
		AllFieldMapper.IncludeInAll {
	
	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends AbstractFieldMapper.Defaults {

		
		/** The Constant PRECISION_STEP. */
		public static final int PRECISION_STEP = NumericUtils.PRECISION_STEP_DEFAULT;

		
		/** The Constant INDEX. */
		public static final Field.Index INDEX = Field.Index.NOT_ANALYZED;

		
		/** The Constant OMIT_NORMS. */
		public static final boolean OMIT_NORMS = true;

		
		/** The Constant OMIT_TERM_FREQ_AND_POSITIONS. */
		public static final boolean OMIT_TERM_FREQ_AND_POSITIONS = true;

		
		/** The Constant FUZZY_FACTOR. */
		public static final String FUZZY_FACTOR = null;
	}

	
	/**
	 * The Class Builder.
	 *
	 * @param <T> the generic type
	 * @param <Y> the generic type
	 * @author l.xue.nong
	 */
	public abstract static class Builder<T extends Builder, Y extends NumberFieldMapper> extends
			AbstractFieldMapper.Builder<T, Y> {

		
		/** The precision step. */
		protected int precisionStep = Defaults.PRECISION_STEP;

		
		/** The fuzzy factor. */
		protected String fuzzyFactor = Defaults.FUZZY_FACTOR;

		
		/**
		 * Instantiates a new builder.
		 *
		 * @param name the name
		 */
		public Builder(String name) {
			super(name);
			this.index = Defaults.INDEX;
			this.omitNorms = Defaults.OMIT_NORMS;
			this.omitTermFreqAndPositions = Defaults.OMIT_TERM_FREQ_AND_POSITIONS;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper.Builder#store(org.apache.lucene.document.Field.Store)
		 */
		@Override
		public T store(Field.Store store) {
			return super.store(store);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper.Builder#boost(float)
		 */
		@Override
		public T boost(float boost) {
			return super.boost(boost);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper.Builder#indexName(java.lang.String)
		 */
		@Override
		public T indexName(String indexName) {
			return super.indexName(indexName);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper.Builder#includeInAll(java.lang.Boolean)
		 */
		@Override
		public T includeInAll(Boolean includeInAll) {
			return super.includeInAll(includeInAll);
		}

		
		/**
		 * Precision step.
		 *
		 * @param precisionStep the precision step
		 * @return the t
		 */
		public T precisionStep(int precisionStep) {
			this.precisionStep = precisionStep;
			return builder;
		}

		
		/**
		 * Fuzzy factor.
		 *
		 * @param fuzzyFactor the fuzzy factor
		 * @return the t
		 */
		public T fuzzyFactor(String fuzzyFactor) {
			this.fuzzyFactor = fuzzyFactor;
			return builder;
		}
	}

	
	/** The precision step. */
	protected int precisionStep;

	
	/** The fuzzy factor. */
	protected String fuzzyFactor;

	
	/** The d fuzzy factor. */
	protected double dFuzzyFactor;

	
	/** The include in all. */
	protected Boolean includeInAll;

	
	/** The token stream. */
	private ThreadLocal<NumericTokenStream> tokenStream = new ThreadLocal<NumericTokenStream>() {
		@Override
		protected NumericTokenStream initialValue() {
			return new NumericTokenStream(precisionStep);
		}
	};

	
	/**
	 * Instantiates a new number field mapper.
	 *
	 * @param names the names
	 * @param precisionStep the precision step
	 * @param fuzzyFactor the fuzzy factor
	 * @param index the index
	 * @param store the store
	 * @param boost the boost
	 * @param omitNorms the omit norms
	 * @param omitTermFreqAndPositions the omit term freq and positions
	 * @param indexAnalyzer the index analyzer
	 * @param searchAnalyzer the search analyzer
	 */
	protected NumberFieldMapper(Names names, int precisionStep, @Nullable String fuzzyFactor, Field.Index index,
			Field.Store store, float boost, boolean omitNorms, boolean omitTermFreqAndPositions,
			NamedAnalyzer indexAnalyzer, NamedAnalyzer searchAnalyzer) {
		super(names, index, store, Field.TermVector.NO, boost, boost != 1.0f || omitNorms, omitTermFreqAndPositions,
				indexAnalyzer, searchAnalyzer);
		if (precisionStep <= 0 || precisionStep >= maxPrecisionStep()) {
			this.precisionStep = Integer.MAX_VALUE;
		} else {
			this.precisionStep = precisionStep;
		}
		this.fuzzyFactor = fuzzyFactor;
		this.dFuzzyFactor = parseFuzzyFactor(fuzzyFactor);
	}

	
	/**
	 * Parses the fuzzy factor.
	 *
	 * @param fuzzyFactor the fuzzy factor
	 * @return the double
	 */
	protected double parseFuzzyFactor(String fuzzyFactor) {
		if (fuzzyFactor == null) {
			return 1.0d;
		}
		return Double.parseDouble(fuzzyFactor);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.internal.AllFieldMapper.IncludeInAll#includeInAll(java.lang.Boolean)
	 */
	@Override
	public void includeInAll(Boolean includeInAll) {
		if (includeInAll != null) {
			this.includeInAll = includeInAll;
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.internal.AllFieldMapper.IncludeInAll#includeInAllIfNotSet(java.lang.Boolean)
	 */
	@Override
	public void includeInAllIfNotSet(Boolean includeInAll) {
		if (includeInAll != null && this.includeInAll == null) {
			this.includeInAll = includeInAll;
		}
	}

	
	/**
	 * Max precision step.
	 *
	 * @return the int
	 */
	protected abstract int maxPrecisionStep();

	
	/**
	 * Precision step.
	 *
	 * @return the int
	 */
	public int precisionStep() {
		return this.precisionStep;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#useFieldQueryWithQueryString()
	 */
	@Override
	public boolean useFieldQueryWithQueryString() {
		return true;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#fieldQuery(java.lang.String, cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query fieldQuery(String value, @Nullable QueryParseContext context) {
		return rangeQuery(value, value, true, true, context);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#fuzzyQuery(java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public abstract Query fuzzyQuery(String value, String minSim, int prefixLength, int maxExpansions);

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#fuzzyQuery(java.lang.String, double, int, int)
	 */
	@Override
	public abstract Query fuzzyQuery(String value, double minSim, int prefixLength, int maxExpansions);

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#fieldFilter(java.lang.String, cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter fieldFilter(String value, @Nullable QueryParseContext context) {
		return rangeFilter(value, value, true, true, context);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#rangeQuery(java.lang.String, java.lang.String, boolean, boolean, cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public abstract Query rangeQuery(String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper,
			@Nullable QueryParseContext context);

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#rangeFilter(java.lang.String, java.lang.String, boolean, boolean, cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public abstract Filter rangeFilter(String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper,
			@Nullable QueryParseContext context);

	
	/**
	 * Range filter.
	 *
	 * @param fieldDataCache the field data cache
	 * @param lowerTerm the lower term
	 * @param upperTerm the upper term
	 * @param includeLower the include lower
	 * @param includeUpper the include upper
	 * @param context the context
	 * @return the filter
	 */
	public abstract Filter rangeFilter(FieldDataCache fieldDataCache, String lowerTerm, String upperTerm,
			boolean includeLower, boolean includeUpper, @Nullable QueryParseContext context);

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#valueForSearch(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public Object valueForSearch(Fieldable field) {
		return value(field);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.FieldMapper#valueAsString(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public String valueAsString(Fieldable field) {
		Number num = value(field);
		return num == null ? null : num.toString();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#merge(cn.com.summall.search.core.index.mapper.Mapper, cn.com.summall.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
		super.merge(mergeWith, mergeContext);
		if (!this.getClass().equals(mergeWith.getClass())) {
			return;
		}
		if (!mergeContext.mergeFlags().simulate()) {
			this.precisionStep = ((NumberFieldMapper) mergeWith).precisionStep;
			this.includeInAll = ((NumberFieldMapper) mergeWith).includeInAll;
			this.fuzzyFactor = ((NumberFieldMapper) mergeWith).fuzzyFactor;
			this.dFuzzyFactor = parseFuzzyFactor(this.fuzzyFactor);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#close()
	 */
	@Override
	public void close() {
		tokenStream.remove();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#fieldDataType()
	 */
	@Override
	public abstract FieldDataType fieldDataType();

	
	/**
	 * Pop cached stream.
	 *
	 * @return the numeric token stream
	 */
	protected NumericTokenStream popCachedStream() {
		return tokenStream.get();
	}

	
	
	/**
	 * The Class CustomNumericField.
	 *
	 * @author l.xue.nong
	 */
	public abstract static class CustomNumericField extends AbstractField {

		
		/** The mapper. */
		protected final NumberFieldMapper mapper;

		
		/**
		 * Instantiates a new custom numeric field.
		 *
		 * @param mapper the mapper
		 * @param value the value
		 */
		public CustomNumericField(NumberFieldMapper mapper, byte[] value) {
			this.mapper = mapper;
			this.name = mapper.names().indexName();
			fieldsData = value;

			isIndexed = mapper.indexed();
			isTokenized = mapper.indexed();
			indexOptions = FieldInfo.IndexOptions.DOCS_ONLY;
			omitNorms = mapper.omitNorms();

			if (value != null) {
				isStored = true;
				isBinary = true;
				binaryLength = value.length;
				binaryOffset = 0;
			}

			setStoreTermVector(Field.TermVector.NO);
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.document.Fieldable#stringValue()
		 */
		@Override
		public String stringValue() {
			return null;
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.document.Fieldable#readerValue()
		 */
		@Override
		public Reader readerValue() {
			return null;
		}

		
		/**
		 * Numeric as string.
		 *
		 * @return the string
		 */
		public abstract String numericAsString();
	}
}
