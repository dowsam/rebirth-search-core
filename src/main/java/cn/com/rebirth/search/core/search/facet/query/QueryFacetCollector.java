/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core QueryFacetCollector.java 2012-7-6 14:30:02 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.query;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TotalHitCountCollector;

import cn.com.rebirth.search.commons.lucene.docset.DocSet;
import cn.com.rebirth.search.commons.lucene.docset.DocSets;
import cn.com.rebirth.search.commons.lucene.search.DeletionAwareConstantScoreQuery;
import cn.com.rebirth.search.commons.lucene.search.Queries;
import cn.com.rebirth.search.core.index.cache.filter.FilterCache;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.OptimizeGlobalFacetCollector;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class QueryFacetCollector.
 *
 * @author l.xue.nong
 */
public class QueryFacetCollector extends AbstractFacetCollector implements OptimizeGlobalFacetCollector {

	/** The query. */
	private final Query query;

	/** The filter. */
	private final Filter filter;

	/** The doc set. */
	private DocSet docSet;

	/** The count. */
	private int count = 0;

	/**
	 * Instantiates a new query facet collector.
	 *
	 * @param facetName the facet name
	 * @param query the query
	 * @param filterCache the filter cache
	 */
	public QueryFacetCollector(String facetName, Query query, FilterCache filterCache) {
		super(facetName);
		this.query = query;
		Filter possibleFilter = extractFilterIfApplicable(query);
		if (possibleFilter != null) {
			this.filter = possibleFilter;
		} else {
			this.filter = new QueryWrapperFilter(query);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		docSet = DocSets.convert(reader, filter.getDocIdSet(reader));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		if (docSet.get(doc)) {
			count++;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.OptimizeGlobalFacetCollector#optimizedGlobalExecution(cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public void optimizedGlobalExecution(SearchContext searchContext) throws IOException {
		Query query = this.query;
		if (super.filter != null) {
			query = new FilteredQuery(query, super.filter);
		}
		Filter searchFilter = searchContext.mapperService().searchFilter(searchContext.types());
		if (searchFilter != null) {
			query = new FilteredQuery(query, searchContext.filterCache().cache(searchFilter));
		}
		TotalHitCountCollector collector = new TotalHitCountCollector();
		searchContext.searcher().search(query, collector);
		count = collector.getTotalHits();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		return new InternalQueryFacet(facetName, count);
	}

	/**
	 * Extract filter if applicable.
	 *
	 * @param query the query
	 * @return the filter
	 */
	private Filter extractFilterIfApplicable(Query query) {
		if (query instanceof FilteredQuery) {
			FilteredQuery fQuery = (FilteredQuery) query;
			if (Queries.isMatchAllQuery(fQuery.getQuery())) {
				return fQuery.getFilter();
			}
		} else if (query instanceof DeletionAwareConstantScoreQuery) {
			return ((DeletionAwareConstantScoreQuery) query).getFilter();
		} else if (query instanceof ConstantScoreQuery) {
			ConstantScoreQuery constantScoreQuery = (ConstantScoreQuery) query;
			if (constantScoreQuery.getFilter() != null) {
				return constantScoreQuery.getFilter();
			}
		}
		return null;
	}
}
