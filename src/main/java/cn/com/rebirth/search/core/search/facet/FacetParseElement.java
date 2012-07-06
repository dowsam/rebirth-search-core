/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FacetParseElement.java 2012-7-6 14:30:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet;

import java.util.List;

import org.apache.lucene.search.Filter;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.object.ObjectMapper;
import cn.com.rebirth.search.core.index.search.nested.NestedChildrenCollector;
import cn.com.rebirth.search.core.index.search.nested.NonNestedDocsFilter;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.SearchParseException;
import cn.com.rebirth.search.core.search.internal.ContextIndexSearcher;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.Lists;

/**
 * The Class FacetParseElement.
 *
 * @author l.xue.nong
 */
public class FacetParseElement implements SearchParseElement {

	/** The facet processors. */
	private final FacetProcessors facetProcessors;

	/**
	 * Instantiates a new facet parse element.
	 *
	 * @param facetProcessors the facet processors
	 */
	@Inject
	public FacetParseElement(FacetProcessors facetProcessors) {
		this.facetProcessors = facetProcessors;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchParseElement#parse(cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public void parse(XContentParser parser, SearchContext context) throws Exception {
		XContentParser.Token token;

		List<FacetCollector> facetCollectors = null;

		String topLevelFieldName = null;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				topLevelFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				FacetCollector facet = null;
				String scope = ContextIndexSearcher.Scopes.MAIN;
				String facetFieldName = null;
				Filter filter = null;
				boolean cacheFilter = true;
				String nestedPath = null;
				while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
					if (token == XContentParser.Token.FIELD_NAME) {
						facetFieldName = parser.currentName();
					} else if (token == XContentParser.Token.START_OBJECT) {
						if ("facet_filter".equals(facetFieldName) || "facetFilter".equals(facetFieldName)) {
							filter = context.queryParserService().parseInnerFilter(parser);
						} else {
							FacetProcessor facetProcessor = facetProcessors.processor(facetFieldName);
							if (facetProcessor == null) {
								throw new SearchParseException(context, "No facet type found for [" + facetFieldName
										+ "]");
							}
							facet = facetProcessor.parse(topLevelFieldName, parser, context);
						}
					} else if (token.isValue()) {
						if ("global".equals(facetFieldName)) {
							if (parser.booleanValue()) {
								scope = ContextIndexSearcher.Scopes.GLOBAL;
							}
						} else if ("scope".equals(facetFieldName) || "_scope".equals(facetFieldName)) {
							scope = parser.text();
						} else if ("cache_filter".equals(facetFieldName) || "cacheFilter".equals(facetFieldName)) {
							cacheFilter = parser.booleanValue();
						} else if ("nested".equals(facetFieldName)) {
							nestedPath = parser.text();
						}
					}
				}
				if (filter != null) {
					if (cacheFilter) {
						filter = context.filterCache().cache(filter);
					}
					facet.setFilter(filter);
				}

				if (nestedPath != null) {

					MapperService.SmartNameObjectMapper mapper = context.smartNameObjectMapper(nestedPath);
					if (mapper == null) {
						throw new SearchParseException(context, "facet nested path [" + nestedPath + "] not found");
					}
					ObjectMapper objectMapper = mapper.mapper();
					if (objectMapper == null) {
						throw new SearchParseException(context, "facet nested path [" + nestedPath + "] not found");
					}
					if (!objectMapper.nested().isNested()) {
						throw new SearchParseException(context, "facet nested path [" + nestedPath + "] is not nested");
					}
					facet = new NestedChildrenCollector(facet, context.filterCache()
							.cache(NonNestedDocsFilter.INSTANCE), context.filterCache().cache(
							objectMapper.nestedTypeFilter()));
				}

				if (facet == null) {
					throw new SearchParseException(context, "no facet type found for facet named [" + topLevelFieldName
							+ "]");
				}

				if (facetCollectors == null) {
					facetCollectors = Lists.newArrayList();
				}
				facetCollectors.add(facet);
				context.searcher().addCollector(scope, facet);
			}
		}

		context.facets(new SearchContextFacets(facetCollectors));
	}
}
