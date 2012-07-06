/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GeoDistanceFacet.java 2012-7-6 14:30:26 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.geodistance;

import java.util.List;

import cn.com.rebirth.search.core.search.facet.Facet;

/**
 * The Interface GeoDistanceFacet.
 *
 * @author l.xue.nong
 */
public interface GeoDistanceFacet extends Facet, Iterable<GeoDistanceFacet.Entry> {

	/** The Constant TYPE. */
	public static final String TYPE = "geo_distance";

	/**
	 * Entries.
	 *
	 * @return the list
	 */
	List<Entry> entries();

	/**
	 * Gets the entries.
	 *
	 * @return the entries
	 */
	List<Entry> getEntries();

	/**
	 * The Class Entry.
	 *
	 * @author l.xue.nong
	 */
	public class Entry {

		/** The from. */
		double from = Double.NEGATIVE_INFINITY;

		/** The to. */
		double to = Double.POSITIVE_INFINITY;

		/** The count. */
		long count;

		/** The total count. */
		long totalCount;

		/** The total. */
		double total;

		/** The min. */
		double min = Double.POSITIVE_INFINITY;

		/** The max. */
		double max = Double.NEGATIVE_INFINITY;

		/** The found in doc. */
		boolean foundInDoc = false;

		/**
		 * Instantiates a new entry.
		 */
		Entry() {
		}

		/**
		 * Instantiates a new entry.
		 *
		 * @param from the from
		 * @param to the to
		 * @param count the count
		 * @param totalCount the total count
		 * @param total the total
		 * @param min the min
		 * @param max the max
		 */
		public Entry(double from, double to, long count, long totalCount, double total, double min, double max) {
			this.from = from;
			this.to = to;
			this.count = count;
			this.totalCount = totalCount;
			this.total = total;
			this.min = min;
			this.max = max;
		}

		/**
		 * From.
		 *
		 * @return the double
		 */
		public double from() {
			return this.from;
		}

		/**
		 * Gets the from.
		 *
		 * @return the from
		 */
		public double getFrom() {
			return from();
		}

		/**
		 * To.
		 *
		 * @return the double
		 */
		public double to() {
			return this.to;
		}

		/**
		 * Gets the to.
		 *
		 * @return the to
		 */
		public double getTo() {
			return to();
		}

		/**
		 * Count.
		 *
		 * @return the long
		 */
		public long count() {
			return this.count;
		}

		/**
		 * Gets the count.
		 *
		 * @return the count
		 */
		public long getCount() {
			return count();
		}

		/**
		 * Total count.
		 *
		 * @return the long
		 */
		public long totalCount() {
			return this.totalCount;
		}

		/**
		 * Gets the total count.
		 *
		 * @return the total count
		 */
		public long getTotalCount() {
			return this.totalCount;
		}

		/**
		 * Total.
		 *
		 * @return the double
		 */
		public double total() {
			return this.total;
		}

		/**
		 * Gets the total.
		 *
		 * @return the total
		 */
		public double getTotal() {
			return total();
		}

		/**
		 * Mean.
		 *
		 * @return the double
		 */
		public double mean() {
			if (totalCount == 0) {
				return 0;
			}
			return total / totalCount;
		}

		/**
		 * Gets the mean.
		 *
		 * @return the mean
		 */
		public double getMean() {
			return mean();
		}

		/**
		 * Min.
		 *
		 * @return the double
		 */
		public double min() {
			return this.min;
		}

		/**
		 * Gets the min.
		 *
		 * @return the min
		 */
		public double getMin() {
			return this.min;
		}

		/**
		 * Max.
		 *
		 * @return the double
		 */
		public double max() {
			return this.max;
		}

		/**
		 * Gets the max.
		 *
		 * @return the max
		 */
		public double getMax() {
			return this.max;
		}
	}
}
