/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchHits.java 2012-7-6 14:29:09 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search;

import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.xcontent.ToXContent;

/**
 * The Interface SearchHits.
 *
 * @author l.xue.nong
 */
public interface SearchHits extends Streamable, ToXContent, Iterable<SearchHit> {

	/**
	 * Total hits.
	 *
	 * @return the long
	 */
	long totalHits();

	/**
	 * Gets the total hits.
	 *
	 * @return the total hits
	 */
	long getTotalHits();

	/**
	 * Max score.
	 *
	 * @return the float
	 */
	float maxScore();

	/**
	 * Gets the max score.
	 *
	 * @return the max score
	 */
	float getMaxScore();

	/**
	 * Hits.
	 *
	 * @return the search hit[]
	 */
	SearchHit[] hits();

	/**
	 * Gets the at.
	 *
	 * @param position the position
	 * @return the at
	 */
	SearchHit getAt(int position);

	/**
	 * Gets the hits.
	 *
	 * @return the hits
	 */
	public SearchHit[] getHits();
}
