/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SortBuilder.java 2012-7-6 14:30:43 l.xue.nong$$
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
