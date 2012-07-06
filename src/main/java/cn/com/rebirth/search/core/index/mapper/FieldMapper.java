/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FieldMapper.java 2012-7-6 14:29:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.query.QueryParseContext;

/**
 * The Interface FieldMapper.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public interface FieldMapper<T> {

	/**
	 * The Class Names.
	 *
	 * @author l.xue.nong
	 */
	public static class Names {

		/** The name. */
		private final String name;

		/** The index name. */
		private final String indexName;

		/** The index name clean. */
		private final String indexNameClean;

		/** The full name. */
		private final String fullName;

		/** The source path. */
		private final String sourcePath;

		/** The index name term factory. */
		private final Term indexNameTermFactory;

		/**
		 * Instantiates a new names.
		 *
		 * @param name the name
		 */
		public Names(String name) {
			this(name, name, name, name);
		}

		/**
		 * Instantiates a new names.
		 *
		 * @param name the name
		 * @param indexName the index name
		 * @param indexNameClean the index name clean
		 * @param fullName the full name
		 */
		public Names(String name, String indexName, String indexNameClean, String fullName) {
			this(name, indexName, indexNameClean, fullName, fullName);
		}

		/**
		 * Instantiates a new names.
		 *
		 * @param name the name
		 * @param indexName the index name
		 * @param indexNameClean the index name clean
		 * @param fullName the full name
		 * @param sourcePath the source path
		 */
		public Names(String name, String indexName, String indexNameClean, String fullName, @Nullable String sourcePath) {
			this.name = name.intern();
			this.indexName = indexName.intern();
			this.indexNameClean = indexNameClean.intern();
			this.fullName = fullName.intern();
			this.sourcePath = sourcePath == null ? this.fullName : sourcePath.intern();
			this.indexNameTermFactory = new Term(indexName, "");
		}

		/**
		 * Name.
		 *
		 * @return the string
		 */
		public String name() {
			return name;
		}

		/**
		 * Index name.
		 *
		 * @return the string
		 */
		public String indexName() {
			return indexName;
		}

		/**
		 * Index name clean.
		 *
		 * @return the string
		 */
		public String indexNameClean() {
			return indexNameClean;
		}

		/**
		 * Full name.
		 *
		 * @return the string
		 */
		public String fullName() {
			return fullName;
		}

		/**
		 * Source path.
		 *
		 * @return the string
		 */
		public String sourcePath() {
			return sourcePath;
		}

		/**
		 * Index name term.
		 *
		 * @return the term
		 */
		public Term indexNameTerm() {
			return this.indexNameTermFactory;
		}

		/**
		 * Creates the index name term.
		 *
		 * @param value the value
		 * @return the term
		 */
		public Term createIndexNameTerm(String value) {
			return indexNameTermFactory.createTerm(value);
		}
	}

	/**
	 * Names.
	 *
	 * @return the names
	 */
	Names names();

	/**
	 * Index.
	 *
	 * @return the field. index
	 */
	Field.Index index();

	/**
	 * Indexed.
	 *
	 * @return true, if successful
	 */
	boolean indexed();

	/**
	 * Analyzed.
	 *
	 * @return true, if successful
	 */
	boolean analyzed();

	/**
	 * Store.
	 *
	 * @return the field. store
	 */
	Field.Store store();

	/**
	 * Stored.
	 *
	 * @return true, if successful
	 */
	boolean stored();

	/**
	 * Term vector.
	 *
	 * @return the field. term vector
	 */
	Field.TermVector termVector();

	/**
	 * Boost.
	 *
	 * @return the float
	 */
	float boost();

	/**
	 * Omit norms.
	 *
	 * @return true, if successful
	 */
	boolean omitNorms();

	/**
	 * Omit term freq and positions.
	 *
	 * @return true, if successful
	 */
	boolean omitTermFreqAndPositions();

	/**
	 * Index analyzer.
	 *
	 * @return the analyzer
	 */
	Analyzer indexAnalyzer();

	/**
	 * Search analyzer.
	 *
	 * @return the analyzer
	 */
	Analyzer searchAnalyzer();

	/**
	 * Value for search.
	 *
	 * @param field the field
	 * @return the object
	 */
	Object valueForSearch(Fieldable field);

	/**
	 * Value.
	 *
	 * @param field the field
	 * @return the t
	 */
	T value(Fieldable field);

	/**
	 * Value from string.
	 *
	 * @param value the value
	 * @return the t
	 */
	T valueFromString(String value);

	/**
	 * Value as string.
	 *
	 * @param field the field
	 * @return the string
	 */
	String valueAsString(Fieldable field);

	/**
	 * Indexed value.
	 *
	 * @param value the value
	 * @return the string
	 */
	String indexedValue(String value);

	/**
	 * Use field query with query string.
	 *
	 * @return true, if successful
	 */
	boolean useFieldQueryWithQueryString();

	/**
	 * Field query.
	 *
	 * @param value the value
	 * @param context the context
	 * @return the query
	 */
	Query fieldQuery(String value, @Nullable QueryParseContext context);

	/**
	 * Fuzzy query.
	 *
	 * @param value the value
	 * @param minSim the min sim
	 * @param prefixLength the prefix length
	 * @param maxExpansions the max expansions
	 * @return the query
	 */
	Query fuzzyQuery(String value, String minSim, int prefixLength, int maxExpansions);

	/**
	 * Fuzzy query.
	 *
	 * @param value the value
	 * @param minSim the min sim
	 * @param prefixLength the prefix length
	 * @param maxExpansions the max expansions
	 * @return the query
	 */
	Query fuzzyQuery(String value, double minSim, int prefixLength, int maxExpansions);

	/**
	 * Prefix query.
	 *
	 * @param value the value
	 * @param method the method
	 * @param context the context
	 * @return the query
	 */
	Query prefixQuery(String value, @Nullable MultiTermQuery.RewriteMethod method, @Nullable QueryParseContext context);

	/**
	 * Prefix filter.
	 *
	 * @param value the value
	 * @param context the context
	 * @return the filter
	 */
	Filter prefixFilter(String value, @Nullable QueryParseContext context);

	/**
	 * Query string term query.
	 *
	 * @param term the term
	 * @return the query
	 */
	Query queryStringTermQuery(Term term);

	/**
	 * Field filter.
	 *
	 * @param value the value
	 * @param context the context
	 * @return the filter
	 */
	Filter fieldFilter(String value, @Nullable QueryParseContext context);

	/**
	 * Range query.
	 *
	 * @param lowerTerm the lower term
	 * @param upperTerm the upper term
	 * @param includeLower the include lower
	 * @param includeUpper the include upper
	 * @param context the context
	 * @return the query
	 */
	Query rangeQuery(String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper,
			@Nullable QueryParseContext context);

	/**
	 * Range filter.
	 *
	 * @param lowerTerm the lower term
	 * @param upperTerm the upper term
	 * @param includeLower the include lower
	 * @param includeUpper the include upper
	 * @param context the context
	 * @return the filter
	 */
	Filter rangeFilter(String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper,
			@Nullable QueryParseContext context);

	/**
	 * Field data type.
	 *
	 * @return the field data type
	 */
	@SuppressWarnings("rawtypes")
	FieldDataType fieldDataType();
}
