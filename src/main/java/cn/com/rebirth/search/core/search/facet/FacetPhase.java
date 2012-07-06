/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FacetPhase.java 2012-7-6 14:29:00 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.DeletionAwareConstantScoreQuery;
import cn.com.rebirth.search.commons.lucene.search.Queries;
import cn.com.rebirth.search.core.index.search.nested.BlockJoinQuery;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.SearchPhase;
import cn.com.rebirth.search.core.search.internal.ContextIndexSearcher;
import cn.com.rebirth.search.core.search.internal.SearchContext;
import cn.com.rebirth.search.core.search.query.QueryPhaseExecutionException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * The Class FacetPhase.
 *
 * @author l.xue.nong
 */
public class FacetPhase implements SearchPhase {

	/** The facet parse element. */
	private final FacetParseElement facetParseElement;

	/** The facet binary parse element. */
	private final FacetBinaryParseElement facetBinaryParseElement;

	/**
	 * Instantiates a new facet phase.
	 *
	 * @param facetParseElement the facet parse element
	 * @param facetBinaryParseElement the facet binary parse element
	 */
	@Inject
	public FacetPhase(FacetParseElement facetParseElement, FacetBinaryParseElement facetBinaryParseElement) {
		this.facetParseElement = facetParseElement;
		this.facetBinaryParseElement = facetBinaryParseElement;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhase#parseElements()
	 */
	@Override
	public Map<String, ? extends SearchParseElement> parseElements() {
		return ImmutableMap.of("facets", facetParseElement, "facets_binary", facetBinaryParseElement, "facetsBinary",
				facetBinaryParseElement);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhase#preProcess(cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public void preProcess(SearchContext context) {

		if (context.nestedQueries() != null) {
			for (Map.Entry<String, BlockJoinQuery> entry : context.nestedQueries().entrySet()) {
				List<Collector> collectors = context.searcher().removeCollectors(entry.getKey());
				if (collectors != null && !collectors.isEmpty()) {
					if (collectors.size() == 1) {
						entry.getValue().setCollector(collectors.get(0));
					} else {
						entry.getValue().setCollector(
								MultiCollector.wrap(collectors.toArray(new Collector[collectors.size()])));
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhase#execute(cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public void execute(SearchContext context) throws RebirthException {
		if (context.facets() == null || context.facets().facetCollectors() == null) {
			return;
		}
		if (context.queryResult().facets() != null) {

			return;
		}

		List<Collector> collectors = context.searcher().removeCollectors(ContextIndexSearcher.Scopes.GLOBAL);

		if (collectors != null && !collectors.isEmpty()) {
			Map<Filter, List<Collector>> filtersByCollector = Maps.newHashMap();
			for (Collector collector : collectors) {
				if (collector instanceof OptimizeGlobalFacetCollector) {
					try {
						((OptimizeGlobalFacetCollector) collector).optimizedGlobalExecution(context);
					} catch (IOException e) {
						throw new QueryPhaseExecutionException(context, "Failed to execute global facets", e);
					}
				} else {
					Filter filter = Queries.MATCH_ALL_FILTER;
					if (collector instanceof AbstractFacetCollector) {
						AbstractFacetCollector facetCollector = (AbstractFacetCollector) collector;
						if (facetCollector.getFilter() != null) {
							filter = facetCollector.getFilter();
						}
					}
					List<Collector> list = filtersByCollector.get(filter);
					if (list == null) {
						list = new ArrayList<Collector>();
						filtersByCollector.put(filter, list);
					}
					list.add(collector);
				}
			}

			for (Map.Entry<Filter, List<Collector>> entry : filtersByCollector.entrySet()) {
				Filter filter = entry.getKey();
				Query query = new DeletionAwareConstantScoreQuery(filter);
				Filter searchFilter = context.mapperService().searchFilter(context.types());
				if (searchFilter != null) {
					query = new FilteredQuery(query, context.filterCache().cache(searchFilter));
				}
				try {
					context.searcher().search(query,
							MultiCollector.wrap(entry.getValue().toArray(new Collector[entry.getValue().size()])));
				} catch (IOException e) {
					throw new QueryPhaseExecutionException(context, "Failed to execute global facets", e);
				}
			}
		}

		SearchContextFacets contextFacets = context.facets();

		List<Facet> facets = Lists.newArrayListWithCapacity(2);
		if (contextFacets.facetCollectors() != null) {
			for (FacetCollector facetCollector : contextFacets.facetCollectors()) {
				facets.add(facetCollector.facet());
			}
		}
		context.queryResult().facets(new InternalFacets(facets));
	}
}
