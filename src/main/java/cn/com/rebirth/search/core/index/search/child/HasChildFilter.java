/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core HasChildFilter.java 2012-7-6 14:30:36 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.search.child;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.FixedBitSet;

import cn.com.rebirth.search.core.search.internal.ScopePhase;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class HasChildFilter.
 *
 * @author l.xue.nong
 */
public class HasChildFilter extends Filter implements ScopePhase.CollectorPhase {

	/** The query. */
	private Query query;

	/** The scope. */
	private String scope;

	/** The parent type. */
	private String parentType;

	/** The child type. */
	private String childType;

	/** The search context. */
	private final SearchContext searchContext;

	/** The parent docs. */
	private Map<Object, FixedBitSet> parentDocs;

	/**
	 * Instantiates a new checks for child filter.
	 *
	 * @param query the query
	 * @param scope the scope
	 * @param childType the child type
	 * @param parentType the parent type
	 * @param searchContext the search context
	 */
	public HasChildFilter(Query query, String scope, String childType, String parentType, SearchContext searchContext) {
		this.query = query;
		this.scope = scope;
		this.parentType = parentType;
		this.childType = childType;
		this.searchContext = searchContext;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.internal.ScopePhase#query()
	 */
	@Override
	public Query query() {
		return query;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.internal.ScopePhase.CollectorPhase#requiresProcessing()
	 */
	@Override
	public boolean requiresProcessing() {
		return parentDocs == null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.internal.ScopePhase.CollectorPhase#collector()
	 */
	@Override
	public Collector collector() {
		return new ChildCollector(parentType, searchContext);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.internal.ScopePhase.CollectorPhase#processCollector(org.apache.lucene.search.Collector)
	 */
	@Override
	public void processCollector(Collector collector) {
		this.parentDocs = ((ChildCollector) collector).parentDocs();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.internal.ScopePhase#scope()
	 */
	@Override
	public String scope() {
		return this.scope;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.internal.ScopePhase#clear()
	 */
	@Override
	public void clear() {
		parentDocs = null;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Filter#getDocIdSet(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException {

		return parentDocs.get(reader.getCoreCacheKey());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("child_filter[").append(childType).append("/").append(parentType).append("](").append(query)
				.append(')');
		return sb.toString();
	}
}
