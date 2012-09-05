/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TermsFilterParser.java 2012-7-6 14:30:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.PublicTermsFilter;

import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.AndFilter;
import cn.com.rebirth.search.commons.lucene.search.TermFilter;
import cn.com.rebirth.search.commons.lucene.search.XBooleanFilter;
import cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;

import com.google.common.collect.Lists;

/**
 * The Class TermsFilterParser.
 *
 * @author l.xue.nong
 */
public class TermsFilterParser implements FilterParser {

	/** The Constant NAME. */
	public static final String NAME = "terms";

	/**
	 * Instantiates a new terms filter parser.
	 */
	@Inject
	public TermsFilterParser() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.FilterParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, "in" };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.FilterParser#parse(cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		MapperService.SmartNameFieldMappers smartNameFieldMappers = null;
		Boolean cache = null;
		String filterName = null;
		String currentFieldName = null;
		CacheKeyFilter.Key cacheKey = null;
		XContentParser.Token token;
		String execution = "plain";
		List<String> terms = Lists.newArrayList();
		String fieldName = null;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_ARRAY) {
				fieldName = currentFieldName;

				while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
					String value = parser.text();
					if (value == null) {
						throw new QueryParsingException(parseContext.index(), "No value specified for term filter");
					}
					terms.add(value);
				}
			} else if (token.isValue()) {
				if ("execution".equals(currentFieldName)) {
					execution = parser.text();
				} else if ("_name".equals(currentFieldName)) {
					filterName = parser.text();
				} else if ("_cache".equals(currentFieldName)) {
					cache = parser.booleanValue();
				} else if ("_cache_key".equals(currentFieldName) || "_cacheKey".equals(currentFieldName)) {
					cacheKey = new CacheKeyFilter.Key(parser.text());
				} else {
					throw new QueryParsingException(parseContext.index(), "[terms] filter does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		if (fieldName == null) {
			throw new QueryParsingException(parseContext.index(),
					"bool filter requires a field name, followed by array of terms");
		}

		FieldMapper fieldMapper = null;
		smartNameFieldMappers = parseContext.smartFieldMappers(fieldName);
		String[] previousTypes = null;
		if (smartNameFieldMappers != null) {
			if (smartNameFieldMappers.hasMapper()) {
				fieldMapper = smartNameFieldMappers.mapper();
				fieldName = fieldMapper.names().indexName();
			}

			if (smartNameFieldMappers.explicitTypeInNameWithDocMapper()) {
				previousTypes = QueryParseContext.setTypesWithPrevious(new String[] { smartNameFieldMappers.docMapper()
						.type() });
			}
		}

		try {
			Filter filter;
			if ("plain".equals(execution)) {
				PublicTermsFilter termsFilter = new PublicTermsFilter();
				if (fieldMapper != null) {
					for (String term : terms) {
						termsFilter.addTerm(fieldMapper.names().createIndexNameTerm(fieldMapper.indexedValue(term)));
					}
				} else {
					for (String term : terms) {
						termsFilter.addTerm(new Term(fieldName, term));
					}
				}
				filter = termsFilter;

				if (cache == null || cache) {
					filter = parseContext.cacheFilter(filter, cacheKey);
				}
			} else if ("bool".equals(execution)) {
				XBooleanFilter boolFiler = new XBooleanFilter();
				if (fieldMapper != null) {
					for (String term : terms) {
						boolFiler
								.addShould(parseContext.cacheFilter(fieldMapper.fieldFilter(term, parseContext), null));
					}
				} else {
					for (String term : terms) {
						boolFiler.addShould(parseContext.cacheFilter(new TermFilter(new Term(fieldName, term)), null));
					}
				}
				filter = boolFiler;

				if (cache != null && cache) {
					filter = parseContext.cacheFilter(filter, cacheKey);
				}
			} else if ("bool_nocache".equals(execution)) {
				XBooleanFilter boolFiler = new XBooleanFilter();
				if (fieldMapper != null) {
					for (String term : terms) {
						boolFiler.addShould(fieldMapper.fieldFilter(term, parseContext));
					}
				} else {
					for (String term : terms) {
						boolFiler.addShould(new TermFilter(new Term(fieldName, term)));
					}
				}
				filter = boolFiler;

				if (cache == null || cache) {
					filter = parseContext.cacheFilter(filter, cacheKey);
				}
			} else if ("and".equals(execution)) {
				List<Filter> filters = Lists.newArrayList();
				if (fieldMapper != null) {
					for (String term : terms) {
						filters.add(parseContext.cacheFilter(fieldMapper.fieldFilter(term, parseContext), null));
					}
				} else {
					for (String term : terms) {
						filters.add(parseContext.cacheFilter(new TermFilter(new Term(fieldName, term)), null));
					}
				}
				filter = new AndFilter(filters);

				if (cache != null && cache) {
					filter = parseContext.cacheFilter(filter, cacheKey);
				}
			} else if ("and_nocache".equals(execution)) {
				List<Filter> filters = Lists.newArrayList();
				if (fieldMapper != null) {
					for (String term : terms) {
						filters.add(fieldMapper.fieldFilter(term, parseContext));
					}
				} else {
					for (String term : terms) {
						filters.add(new TermFilter(new Term(fieldName, term)));
					}
				}
				filter = new AndFilter(filters);

				if (cache == null || cache) {
					filter = parseContext.cacheFilter(filter, cacheKey);
				}
			} else {
				throw new QueryParsingException(parseContext.index(), "bool filter execution value [" + execution
						+ "] not supported");
			}

			filter = QueryParsers.wrapSmartNameFilter(filter, smartNameFieldMappers, parseContext);
			if (filterName != null) {
				parseContext.addNamedFilter(filterName, filter);
			}
			return filter;
		} finally {
			if (smartNameFieldMappers != null && smartNameFieldMappers.explicitTypeInNameWithDocMapper()) {
				QueryParseContext.setTypes(previousTypes);
			}
		}
	}
}
