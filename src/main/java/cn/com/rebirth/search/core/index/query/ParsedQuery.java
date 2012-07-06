/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ParsedQuery.java 2012-3-29 15:02:38 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;

import cn.com.rebirth.search.commons.lucene.search.Queries;

import com.google.common.collect.ImmutableMap;


/**
 * The Class ParsedQuery.
 *
 * @author l.xue.nong
 */
public class ParsedQuery {

	
	/** The MATC h_ al l_ parse d_ query. */
	public static ParsedQuery MATCH_ALL_PARSED_QUERY = new ParsedQuery(Queries.MATCH_ALL_QUERY,
			ImmutableMap.<String, Filter> of());

	
	/** The query. */
	private final Query query;

	
	/** The named filters. */
	private final ImmutableMap<String, Filter> namedFilters;

	
	/**
	 * Instantiates a new parsed query.
	 *
	 * @param query the query
	 * @param namedFilters the named filters
	 */
	public ParsedQuery(Query query, ImmutableMap<String, Filter> namedFilters) {
		this.query = query;
		this.namedFilters = namedFilters;
	}

	
	/**
	 * Instantiates a new parsed query.
	 *
	 * @param query the query
	 * @param parsedQuery the parsed query
	 */
	public ParsedQuery(Query query, ParsedQuery parsedQuery) {
		this.query = query;
		this.namedFilters = parsedQuery.namedFilters;
	}

	
	/**
	 * Query.
	 *
	 * @return the query
	 */
	public Query query() {
		return this.query;
	}

	
	/**
	 * Named filters.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, Filter> namedFilters() {
		return this.namedFilters;
	}
}
