/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RangeFacet.java 2012-3-29 15:01:18 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.range;

import java.util.List;

import cn.com.rebirth.search.core.search.facet.Facet;


/**
 * The Interface RangeFacet.
 *
 * @author l.xue.nong
 */
public interface RangeFacet extends Facet, Iterable<RangeFacet.Entry> {

	
	/** The Constant TYPE. */
	public static final String TYPE = "range";

	
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

		
		/** The from as string. */
		String fromAsString;

		
		/** The to as string. */
		String toAsString;

		
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
		boolean foundInDoc;

		
		/**
		 * Instantiates a new entry.
		 */
		Entry() {
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
		 * From as string.
		 *
		 * @return the string
		 */
		public String fromAsString() {
			if (fromAsString != null) {
				return fromAsString;
			}
			return Double.toString(from);
		}

		
		/**
		 * Gets the from as string.
		 *
		 * @return the from as string
		 */
		public String getFromAsString() {
			return fromAsString();
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
		 * To as string.
		 *
		 * @return the string
		 */
		public String toAsString() {
			if (toAsString != null) {
				return toAsString;
			}
			return Double.toString(to);
		}

		
		/**
		 * Gets the to as string.
		 *
		 * @return the to as string
		 */
		public String getToAsString() {
			return toAsString();
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
