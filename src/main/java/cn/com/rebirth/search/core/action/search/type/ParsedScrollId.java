/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ParsedScrollId.java 2012-3-29 15:02:09 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.search.type;

import java.util.Map;

import cn.com.rebirth.commons.collect.Tuple;


/**
 * The Class ParsedScrollId.
 *
 * @author l.xue.nong
 */
public class ParsedScrollId {

	
	/** The Constant QUERY_THEN_FETCH_TYPE. */
	public static final String QUERY_THEN_FETCH_TYPE = "queryThenFetch";

	
	/** The Constant QUERY_AND_FETCH_TYPE. */
	public static final String QUERY_AND_FETCH_TYPE = "queryAndFetch";

	
	/** The Constant SCAN. */
	public static final String SCAN = "scan";

	
	/** The source. */
	private final String source;

	
	/** The type. */
	private final String type;

	
	/** The context. */
	private final Tuple<String, Long>[] context;

	
	/** The attributes. */
	private final Map<String, String> attributes;

	
	/**
	 * Instantiates a new parsed scroll id.
	 *
	 * @param source the source
	 * @param type the type
	 * @param context the context
	 * @param attributes the attributes
	 */
	public ParsedScrollId(String source, String type, Tuple<String, Long>[] context, Map<String, String> attributes) {
		this.source = source;
		this.type = type;
		this.context = context;
		this.attributes = attributes;
	}

	
	/**
	 * Source.
	 *
	 * @return the string
	 */
	public String source() {
		return source;
	}

	
	/**
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		return type;
	}

	
	/**
	 * Context.
	 *
	 * @return the tuple[]
	 */
	public Tuple<String, Long>[] context() {
		return context;
	}

	
	/**
	 * Attributes.
	 *
	 * @return the map
	 */
	public Map<String, String> attributes() {
		return this.attributes;
	}
}
