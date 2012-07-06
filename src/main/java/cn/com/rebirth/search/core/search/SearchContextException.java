/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchContextException.java 2012-7-6 14:30:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search;

import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class SearchContextException.
 *
 * @author l.xue.nong
 */
public class SearchContextException extends SearchException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5601882923732760207L;

	/**
	 * Instantiates a new search context exception.
	 *
	 * @param context the context
	 * @param msg the msg
	 */
	public SearchContextException(SearchContext context, String msg) {
		super(context.shardTarget(), buildMessage(context, msg));
	}

	/**
	 * Instantiates a new search context exception.
	 *
	 * @param context the context
	 * @param msg the msg
	 * @param t the t
	 */
	public SearchContextException(SearchContext context, String msg, Throwable t) {
		super(context.shardTarget(), buildMessage(context, msg), t);
	}

	/**
	 * Builds the message.
	 *
	 * @param context the context
	 * @param msg the msg
	 * @return the string
	 */
	private static String buildMessage(SearchContext context, String msg) {
		StringBuilder sb = new StringBuilder();
		sb.append('[').append(context.shardTarget().index()).append("][").append(context.shardTarget().shardId())
				.append("]: ");
		if (context.parsedQuery() != null) {
			try {
				sb.append("query[").append(context.parsedQuery().query()).append("],");
			} catch (Exception e) {
				sb.append("query[_failed_to_string_],");
			}
		}
		sb.append("from[").append(context.from()).append("],size[").append(context.size()).append("]");
		if (context.sort() != null) {
			sb.append(",sort[").append(context.sort()).append("]");
		}
		return sb.append(": ").append(msg).toString();
	}
}
