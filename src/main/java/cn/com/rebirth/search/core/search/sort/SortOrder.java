/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SortOrder.java 2012-3-29 15:02:53 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.sort;


/**
 * The Enum SortOrder.
 *
 * @author l.xue.nong
 */
public enum SortOrder {

	
	/** The ASC. */
	ASC {
		@Override
		public String toString() {
			return "asc";
		}
	},

	
	/** The DESC. */
	DESC {
		@Override
		public String toString() {
			return "desc";
		}
	}
}
