/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TermsFacet.java 2012-7-6 14:30:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.terms;

import java.util.Comparator;
import java.util.List;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.search.core.search.facet.Facet;

/**
 * The Interface TermsFacet.
 *
 * @author l.xue.nong
 */
public interface TermsFacet extends Facet, Iterable<TermsFacet.Entry> {

	/** The Constant TYPE. */
	public static final String TYPE = "terms";

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
		 * @return the int
		 */
		int count();

		/**
		 * Gets the count.
		 *
		 * @return the count
		 */
		int getCount();
	}

	/**
	 * The Enum ComparatorType.
	 *
	 * @author l.xue.nong
	 */
	public static enum ComparatorType {

		/** The count. */
		COUNT((byte) 0, new Comparator<Entry>() {

			@Override
			public int compare(Entry o1, Entry o2) {
				int i = o2.count() - o1.count();
				if (i == 0) {
					i = o2.compareTo(o1);
					if (i == 0) {
						i = System.identityHashCode(o2) - System.identityHashCode(o1);
					}
				}
				return i;
			}
		}),

		/** The reverse count. */
		REVERSE_COUNT((byte) 1, new Comparator<Entry>() {

			@Override
			public int compare(Entry o1, Entry o2) {
				return -COUNT.comparator().compare(o1, o2);
			}
		}),

		/** The term. */
		TERM((byte) 2, new Comparator<Entry>() {

			@Override
			public int compare(Entry o1, Entry o2) {
				return o1.compareTo(o2);
			}
		}),

		/** The reverse term. */
		REVERSE_TERM((byte) 3, new Comparator<Entry>() {

			@Override
			public int compare(Entry o1, Entry o2) {
				return -TERM.comparator().compare(o1, o2);
			}
		});

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
			}
			throw new RebirthIllegalArgumentException("No type argument match for terms facet comparator [" + id + "]");
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
			}
			throw new RebirthIllegalArgumentException("No type argument match for terms facet comparator [" + type
					+ "]");
		}
	}

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
	 * Other count.
	 *
	 * @return the long
	 */
	long otherCount();

	/**
	 * Gets the other count.
	 *
	 * @return the other count
	 */
	long getOtherCount();

	/**
	 * Entries.
	 *
	 * @return the list<? extends terms facet. entry>
	 */
	List<? extends TermsFacet.Entry> entries();

	/**
	 * Gets the entries.
	 *
	 * @return the entries
	 */
	List<? extends TermsFacet.Entry> getEntries();
}