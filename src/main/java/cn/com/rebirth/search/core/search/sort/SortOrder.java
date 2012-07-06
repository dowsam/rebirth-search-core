/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SortOrder.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.sort;

/**
 * The Enum SortOrder.
 *
 * @author l.xue.nong
 */
public enum SortOrder {

	/** The asc. */
	ASC {
		@Override
		public String toString() {
			return "asc";
		}
	},

	/** The desc. */
	DESC {
		@Override
		public String toString() {
			return "desc";
		}
	}
}
