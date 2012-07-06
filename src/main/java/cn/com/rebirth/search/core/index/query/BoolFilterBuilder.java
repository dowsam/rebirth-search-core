/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BoolFilterBuilder.java 2012-7-6 14:30:35 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.search.BooleanClause;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class BoolFilterBuilder.
 *
 * @author l.xue.nong
 */
public class BoolFilterBuilder extends BaseFilterBuilder {

	/** The clauses. */
	private ArrayList<Clause> clauses = new ArrayList<Clause>();

	/** The cache. */
	private Boolean cache;

	/** The cache key. */
	private String cacheKey;

	/** The filter name. */
	private String filterName;

	/**
	 * Must.
	 *
	 * @param filterBuilder the filter builder
	 * @return the bool filter builder
	 */
	public BoolFilterBuilder must(FilterBuilder filterBuilder) {
		clauses.add(new Clause(filterBuilder, BooleanClause.Occur.MUST));
		return this;
	}

	/**
	 * Must not.
	 *
	 * @param filterBuilder the filter builder
	 * @return the bool filter builder
	 */
	public BoolFilterBuilder mustNot(FilterBuilder filterBuilder) {
		clauses.add(new Clause(filterBuilder, BooleanClause.Occur.MUST_NOT));
		return this;
	}

	/**
	 * Should.
	 *
	 * @param filterBuilder the filter builder
	 * @return the bool filter builder
	 */
	public BoolFilterBuilder should(FilterBuilder filterBuilder) {
		clauses.add(new Clause(filterBuilder, BooleanClause.Occur.SHOULD));
		return this;
	}

	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the bool filter builder
	 */
	public BoolFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	/**
	 * Cache.
	 *
	 * @param cache the cache
	 * @return the bool filter builder
	 */
	public BoolFilterBuilder cache(boolean cache) {
		this.cache = cache;
		return this;
	}

	/**
	 * Cache key.
	 *
	 * @param cacheKey the cache key
	 * @return the bool filter builder
	 */
	public BoolFilterBuilder cacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject("bool");
		for (Clause clause : clauses) {
			if (clause.occur == BooleanClause.Occur.MUST) {
				builder.field("must");
				clause.filterBuilder.toXContent(builder, params);
			} else if (clause.occur == BooleanClause.Occur.MUST_NOT) {
				builder.field("must_not");
				clause.filterBuilder.toXContent(builder, params);
			} else if (clause.occur == BooleanClause.Occur.SHOULD) {
				builder.field("should");
				clause.filterBuilder.toXContent(builder, params);
			}
		}
		if (filterName != null) {
			builder.field("_name", filterName);
		}
		if (cache != null) {
			builder.field("_cache", cache);
		}
		if (cacheKey != null) {
			builder.field("_cache_key", cacheKey);
		}
		builder.endObject();
	}

	/**
	 * The Class Clause.
	 *
	 * @author l.xue.nong
	 */
	private static class Clause {

		/** The filter builder. */
		final FilterBuilder filterBuilder;

		/** The occur. */
		final BooleanClause.Occur occur;

		/**
		 * Instantiates a new clause.
		 *
		 * @param filterBuilder the filter builder
		 * @param occur the occur
		 */
		private Clause(FilterBuilder filterBuilder, BooleanClause.Occur occur) {
			this.filterBuilder = filterBuilder;
			this.occur = occur;
		}
	}
}