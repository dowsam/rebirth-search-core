/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FilterFacetCollector.java 2012-3-29 15:00:53 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.filter;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TotalHitCountCollector;

import cn.com.rebirth.search.commons.lucene.docset.DocSet;
import cn.com.rebirth.search.commons.lucene.docset.DocSets;
import cn.com.rebirth.search.commons.lucene.search.DeletionAwareConstantScoreQuery;
import cn.com.rebirth.search.core.index.cache.filter.FilterCache;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.OptimizeGlobalFacetCollector;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class FilterFacetCollector.
 *
 * @author l.xue.nong
 */
public class FilterFacetCollector extends AbstractFacetCollector implements OptimizeGlobalFacetCollector {

	
	/** The filter. */
	private final Filter filter;

	
	/** The doc set. */
	private DocSet docSet;

	
	/** The count. */
	private int count = 0;

	
	/**
	 * Instantiates a new filter facet collector.
	 *
	 * @param facetName the facet name
	 * @param filter the filter
	 * @param filterCache the filter cache
	 */
	public FilterFacetCollector(String facetName, Filter filter, FilterCache filterCache) {
		super(facetName);
		this.filter = filter;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.OptimizeGlobalFacetCollector#optimizedGlobalExecution(cn.com.summall.search.core.search.internal.SearchContext)
	 */
	@Override
	public void optimizedGlobalExecution(SearchContext searchContext) throws IOException {
		Query query = new DeletionAwareConstantScoreQuery(filter);
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
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		docSet = DocSets.convert(reader, filter.getDocIdSet(reader));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		if (docSet.get(doc)) {
			count++;
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		return new InternalFilterFacet(facetName, count);
	}
}
