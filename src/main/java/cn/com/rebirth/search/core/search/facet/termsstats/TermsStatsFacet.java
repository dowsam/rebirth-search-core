/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TermsStatsFacet.java 2012-3-29 15:02:04 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.termsstats;

import java.util.Comparator;
import java.util.List;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.search.core.search.facet.Facet;


/**
 * The Interface TermsStatsFacet.
 *
 * @author l.xue.nong
 */
public interface TermsStatsFacet extends Facet, Iterable<TermsStatsFacet.Entry> {

	
	/** The Constant TYPE. */
	public static final String TYPE = "terms_stats";

	
	/**
	 * Missing count.
	 *
	 * @return the long
	 */
	long missingCount();

	
	/**
	 * Gets the missing count.
	 *
	 * @return the missing count
	 */
	long getMissingCount();

	
	/**
	 * Entries.
	 *
	 * @return the list<? extends terms stats facet. entry>
	 */
	List<? extends TermsStatsFacet.Entry> entries();

	
	/**
	 * Gets the entries.
	 *
	 * @return the entries
	 */
	List<? extends TermsStatsFacet.Entry> getEntries();

	
	/**
	 * The Enum ComparatorType.
	 *
	 * @author l.xue.nong
	 */
	public static enum ComparatorType {

		
		/** The COUNT. */
		COUNT((byte) 0, new Comparator<Entry>() {

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
				int i = (o2.count() < o1.count() ? -1 : (o1.count() == o2.count() ? 0 : 1));
				if (i == 0) {
					i = o2.compareTo(o1);
				}
				return i;
			}
		}),

		
		/** The REVERS e_ count. */
		REVERSE_COUNT((byte) 1, new Comparator<Entry>() {

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
				return -COUNT.comparator().compare(o1, o2);
			}
		}),

		
		/** The TERM. */
		TERM((byte) 2, new Comparator<Entry>() {

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
				int i = o1.compareTo(o2);
				if (i == 0) {
					i = COUNT.comparator().compare(o1, o2);
				}
				return i;
			}
		}),

		
		/** The REVERS e_ term. */
		REVERSE_TERM((byte) 3, new Comparator<Entry>() {

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
				return -TERM.comparator().compare(o1, o2);
			}
		}),

		
		/** The TOTAL. */
		TOTAL((byte) 4, new Comparator<Entry>() {
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
				int i = -Double.compare(o1.total(), o2.total());
				if (i == 0) {
					i = COUNT.comparator().compare(o1, o2);
				}
				return i;
			}
		}),

		
		/** The REVERS e_ total. */
		REVERSE_TOTAL((byte) 5, new Comparator<Entry>() {
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
				return -TOTAL.comparator().compare(o1, o2);
			}
		}),

		
		/** The MIN. */
		MIN((byte) 6, new Comparator<Entry>() {
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
				int i = Double.compare(o1.min(), o2.min());
				if (i == 0) {
					i = COUNT.comparator().compare(o1, o2);
				}
				return i;
			}
		}),

		
		/** The REVERS e_ min. */
		REVERSE_MIN((byte) 7, new Comparator<Entry>() {
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
				return -MIN.comparator().compare(o1, o2);
			}
		}),

		
		/** The MAX. */
		MAX((byte) 8, new Comparator<Entry>() {
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
				int i = -Double.compare(o1.max(), o2.max());
				if (i == 0) {
					i = COUNT.comparator().compare(o1, o2);
				}
				return i;
			}
		}),

		
		/** The REVERS e_ max. */
		REVERSE_MAX((byte) 9, new Comparator<Entry>() {
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
				return -MAX.comparator().compare(o1, o2);
			}
		}),

		
		/** The MEAN. */
		MEAN((byte) 10, new Comparator<Entry>() {
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
				int i = -Double.compare(o1.mean(), o2.mean());
				if (i == 0) {
					i = COUNT.comparator().compare(o1, o2);
				}
				return i;
			}
		}),

		
		/** The REVERS e_ mean. */
		REVERSE_MEAN((byte) 11, new Comparator<Entry>() {
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
				return -MEAN.comparator().compare(o1, o2);
			}
		}), ;

		
		/** The id. */
		private final byte id;

		
		/** The comparator. */
		private final Comparator<Entry> comparator;

		
		/**
		 * Instantiates a new comparator type.
		 *
		 * @param id the id
		 * @param comparator the comparator
		 */
		ComparatorType(byte id, Comparator<Entry> comparator) {
			this.id = id;
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
			if (id == COUNT.id()) {
				return COUNT;
			} else if (id == REVERSE_COUNT.id()) {
				return REVERSE_COUNT;
			} else if (id == TERM.id()) {
				return TERM;
			} else if (id == REVERSE_TERM.id()) {
				return REVERSE_TERM;
			} else if (id == TOTAL.id()) {
				return TOTAL;
			} else if (id == REVERSE_TOTAL.id()) {
				return REVERSE_TOTAL;
			} else if (id == MIN.id()) {
				return MIN;
			} else if (id == REVERSE_MIN.id()) {
				return REVERSE_MIN;
			} else if (id == MAX.id()) {
				return MAX;
			} else if (id == REVERSE_MAX.id()) {
				return REVERSE_MAX;
			} else if (id == MEAN.id()) {
				return MEAN;
			} else if (id == REVERSE_MEAN.id()) {
				return REVERSE_MEAN;
			}
			throw new RestartIllegalArgumentException("No type argument match for terms facet comparator [" + id
					+ "]");
		}

		
		/**
		 * From string.
		 *
		 * @param type the type
		 * @return the comparator type
		 */
		public static ComparatorType fromString(String type) {
			if ("count".equals(type)) {
				return COUNT;
			} else if ("term".equals(type)) {
				return TERM;
			} else if ("reverse_count".equals(type) || "reverseCount".equals(type)) {
				return REVERSE_COUNT;
			} else if ("reverse_term".equals(type) || "reverseTerm".equals(type)) {
				return REVERSE_TERM;
			} else if ("total".equals(type)) {
				return TOTAL;
			} else if ("reverse_total".equals(type) || "reverseTotal".equals(type)) {
				return REVERSE_TOTAL;
			} else if ("min".equals(type)) {
				return MIN;
			} else if ("reverse_min".equals(type) || "reverseMin".equals(type)) {
				return REVERSE_MIN;
			} else if ("max".equals(type)) {
				return MAX;
			} else if ("reverse_max".equals(type) || "reverseMax".equals(type)) {
				return REVERSE_MAX;
			} else if ("mean".equals(type)) {
				return MEAN;
			} else if ("reverse_mean".equals(type) || "reverseMean".equals(type)) {
				return REVERSE_MEAN;
			}
			throw new RestartIllegalArgumentException("No type argument match for terms stats facet comparator ["
					+ type + "]");
		}
	}

	
	/**
	 * The Interface Entry.
	 *
	 * @author l.xue.nong
	 */
	public interface Entry extends Comparable<Entry> {

		
		/**
		 * Term.
		 *
		 * @return the string
		 */
		String term();

		
		/**
		 * Gets the term.
		 *
		 * @return the term
		 */
		String getTerm();

		
		/**
		 * Term as number.
		 *
		 * @return the number
		 */
		Number termAsNumber();

		
		/**
		 * Gets the term as number.
		 *
		 * @return the term as number
		 */
		Number getTermAsNumber();

		
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
	}
}