/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SortBuilder.java 2012-3-29 15:02:40 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.sort;

import cn.com.rebirth.search.commons.xcontent.ToXContent;


/**
 * The Class SortBuilder.
 *
 * @author l.xue.nong
 */
public abstract class SortBuilder implements ToXContent {

	
	/**
	 * Order.
	 *
	 * @param order the order
	 * @return the sort builder
	 */
	public abstract SortBuilder order(SortOrder order);

	
	/**
	 * Missing.
	 *
	 * @param missing the missing
	 * @return the sort builder
	 */
	public abstract SortBuilder missing(Object missing);
}
