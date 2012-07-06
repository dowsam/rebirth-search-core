/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DateHistogramFacet.java 2012-7-6 14:29:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.datehistogram;

import java.util.Comparator;
import java.util.List;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.search.core.search.facet.Facet;

/**
 * The Interface DateHistogramFacet.
 *
 * @author l.xue.nong
 */
public interface DateHistogramFacet extends Facet, Iterable<DateHistogramFacet.Entry> {

	/** The Constant TYPE. */
	public static final String TYPE = "date_histogram";

	/**
	 * Entries.
	 *
	 * @return the list<? extends entry>
	 */
	List<? extends Entry> entries();

	/**
	 * Gets the entries.
	 *
	 * @return the entries
	 */
	List<? extends Entry> getEntries();

	/**
	 * The Enum ComparatorType.
	 *
	 * @author l.xue.nong
	 */
	public static enum ComparatorType {

		/** The time. */
		TIME((byte) 0, "time", new Comparator<Entry>() {

			@Override
			public int compare(Entry o1, Entry o2) {

				if (o1 == null) {
					if (o2 == null) {
						return 0;
					}
					return 1;
				}
				if (o2 == null) {
					return -1;
				}
				return (o1.time() < o2.time() ? -1 : (o1.time() == o2.time() ? 0 : 1));
			}
		}),

		/** The count. */
		COUNT((byte) 1, "count", new Comparator<Entry>() {

			@Override
			public int compare(Entry o1, Entry o2) {

				if (o1 == null) {
					if (o2 == null) {
						return 0;
					}
					return 1;
				}
				if (o2 == null) {
					return -1;
				}
				return (o1.count() < o2.count() ? -1 : (o1.count() == o2.count() ? 0 : 1));
			}
		}),

		/** The total. */
		TOTAL((byte) 2, "total", new Comparator<Entry>() {

			@Override
			public int compare(Entry o1, Entry o2) {

				if (o1 == null) {
					if (o2 == null) {
						return 0;
					}
					return 1;
				}
				if (o2 == null) {
					return -1;
				}
				return (o1.total() < o2.total() ? -1 : (o1.total() == o2.total() ? 0 : 1));
			}
		});

		/** The id. */
		private final byte id;

		/** The description. */
		private final String description;

		/** The comparator. */
		private final Comparator<Entry> comparator;

		/**
		 * Instantiates a new comparator type.
		 *
		 * @param id the id
		 * @param description the description
		 * @param comparator the comparator
		 */
		ComparatorType(byte id, String description, Comparator<Entry> comparator) {
			this.id = id;
			this.description = description;
			this.comparator = comparator;
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
		 * Description.
		 *
		 * @return the string
		 */
		public String description() {
			return this.description;
		}

		/**
		 * Comparator.
		 *
		 * @return the comparator
		 */
		public Comparator<Entry> comparator() {
			return comparator;
		}

		/**
		 * From id.
		 *
		 * @param id the id
		 * @return the comparator type
		 */
		public static ComparatorType fromId(byte id) {
			if (id == 0) {
				return TIME;
			} else if (id == 1) {
				return COUNT;
			} else if (id == 2) {
				return TOTAL;
			}
			throw new RebirthIllegalArgumentException("No type argument match for histogram comparator [" + id + "]");
		}

		/**
		 * From string.
		 *
		 * @param type the type
		 * @return the comparator type
		 */
		public static ComparatorType fromString(String type) {
			if ("time".equals(type)) {
				return TIME;
			} else if ("count".equals(type)) {
				return COUNT;
			} else if ("total".equals(type)) {
				return TOTAL;
			}
			throw new RebirthIllegalArgumentException("No type argument match for histogram comparator [" + type + "]");
		}
	}

	/**
	 * The Interface Entry.
	 *
	 * @author l.xue.nong
	 */
	public interface Entry {

		/**
		 * Time.
		 *
		 * @return the long
		 */
		long time();

		/**
		 * Gets the time.
		 *
		 * @return the time
		 */
		long getTime();

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
		 * Total count.
		 *
		 * @return the long
		 */
		long totalCount();

		/**
		 * Gets the total count.
		 *
		 * @return the total count
		 */
		long getTotalCount();

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
	}
}