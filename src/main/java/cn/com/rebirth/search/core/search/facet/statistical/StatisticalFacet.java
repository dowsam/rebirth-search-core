/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core StatisticalFacet.java 2012-3-29 15:01:24 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.statistical;

import cn.com.rebirth.search.core.search.facet.Facet;


/**
 * The Interface StatisticalFacet.
 *
 * @author l.xue.nong
 */
public interface StatisticalFacet extends Facet {

	
	/** The Constant TYPE. */
	public static final String TYPE = "statistical";

	
	/**
	 * Count.
	 *
	 * @return the long
	 */
	long count();

	
	/**
	 * Gets the count.
	 *
	 * @return the count
	 */
	long getCount();

	
	/**
	 * Total.
	 *
	 * @return the double
	 */
	double total();

	
	/**
	 * Gets the total.
	 *
	 * @return the total
	 */
	double getTotal();

	
	/**
	 * Sum of squares.
	 *
	 * @return the double
	 */
	double sumOfSquares();

	
	/**
	 * Gets the sum of squares.
	 *
	 * @return the sum of squares
	 */
	double getSumOfSquares();

	
	/**
	 * Mean.
	 *
	 * @return the double
	 */
	double mean();

	
	/**
	 * Gets the mean.
	 *
	 * @return the mean
	 */
	double getMean();

	
	/**
	 * Min.
	 *
	 * @return the double
	 */
	double min();

	
	/**
	 * Gets the min.
	 *
	 * @return the min
	 */
	double getMin();

	
	/**
	 * Max.
	 *
	 * @return the double
	 */
	double max();

	
	/**
	 * Gets the max.
	 *
	 * @return the max
	 */
	double getMax();

	
	/**
	 * Variance.
	 *
	 * @return the double
	 */
	double variance();

	
	/**
	 * Gets the variance.
	 *
	 * @return the variance
	 */
	double getVariance();

	
	/**
	 * Std deviation.
	 *
	 * @return the double
	 */
	double stdDeviation();

	
	/**
	 * Gets the std deviation.
	 *
	 * @return the std deviation
	 */
	double getStdDeviation();
}
