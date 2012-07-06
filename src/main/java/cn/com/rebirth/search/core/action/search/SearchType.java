/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SearchType.java 2012-3-29 15:02:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.search;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;


/**
 * The Enum SearchType.
 *
 * @author l.xue.nong
 */
public enum SearchType {

	/** The DF s_ quer y_ the n_ fetch. */
	DFS_QUERY_THEN_FETCH((byte) 0),

	/** The QUER y_ the n_ fetch. */
	QUERY_THEN_FETCH((byte) 1),

	/** The DF s_ quer y_ an d_ fetch. */
	DFS_QUERY_AND_FETCH((byte) 2),

	/** The QUER y_ an d_ fetch. */
	QUERY_AND_FETCH((byte) 3),

	/** The SCAN. */
	SCAN((byte) 4),

	/** The COUNT. */
	COUNT((byte) 5);

	/** The Constant DEFAULT. */
	public static final SearchType DEFAULT = QUERY_THEN_FETCH;

	/** The id. */
	private byte id;

	/**
	 * Instantiates a new search type.
	 *
	 * @param id the id
	 */
	SearchType(byte id) {
		this.id = id;
	}

	/**
	 * Id.
	 *
	 * @return the byte
	 */
	public byte id() {
		return this.id;
	}

	/**
	 * From id.
	 *
	 * @param id the id
	 * @return the search type
	 */
	public static SearchType fromId(byte id) {
		if (id == 0) {
			return DFS_QUERY_THEN_FETCH;
		} else if (id == 1) {
			return QUERY_THEN_FETCH;
		} else if (id == 2) {
			return DFS_QUERY_AND_FETCH;
		} else if (id == 3) {
			return QUERY_AND_FETCH;
		} else if (id == 4) {
			return SCAN;
		} else if (id == 5) {
			return COUNT;
		} else {
			throw new RestartIllegalArgumentException("No search type for [" + id + "]");
		}
	}

	/**
	 * From string.
	 *
	 * @param searchType the search type
	 * @return the search type
	 * @throws SumMallSearchIllegalArgumentException the sum mall search illegal argument exception
	 */
	public static SearchType fromString(String searchType) throws RestartIllegalArgumentException {
		if (searchType == null) {
			return SearchType.DEFAULT;
		}
		if ("dfs_query_then_fetch".equals(searchType)) {
			return SearchType.DFS_QUERY_THEN_FETCH;
		} else if ("dfs_query_and_fetch".equals(searchType)) {
			return SearchType.DFS_QUERY_AND_FETCH;
		} else if ("query_then_fetch".equals(searchType)) {
			return SearchType.QUERY_THEN_FETCH;
		} else if ("query_and_fetch".equals(searchType)) {
			return SearchType.QUERY_AND_FETCH;
		} else if ("scan".equals(searchType)) {
			return SearchType.SCAN;
		} else if ("count".equals(searchType)) {
			return SearchType.COUNT;
		} else {
			throw new RestartIllegalArgumentException("No search type for [" + searchType + "]");
		}
	}
}
