/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IntFieldDataComparator.java 2012-7-6 14:30:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.ints;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.support.NumericFieldDataComparator;

/**
 * The Class IntFieldDataComparator.
 *
 * @author l.xue.nong
 */
public class IntFieldDataComparator extends NumericFieldDataComparator {

	/** The values. */
	private final int[] values;

	/** The bottom. */
	private int bottom;

	/**
	 * Instantiates a new int field data comparator.
	 *
	 * @param numHits the num hits
	 * @param fieldName the field name
	 * @param fieldDataCache the field data cache
	 */
	public IntFieldDataComparator(int numHits, String fieldName, FieldDataCache fieldDataCache) {
		super(fieldName, fieldDataCache);
		values = new int[numHits];
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.support.NumericFieldDataComparator#fieldDataType()
	 */
	@Override
	public FieldDataType fieldDataType() {
		return FieldDataType.DefaultTypes.INT;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#compare(int, int)
	 */
	@Override
	public int compare(int slot1, int slot2) {

		final int v1 = values[slot1];
		final int v2 = values[slot2];
		if (v1 > v2) {
			return 1;
		} else if (v1 < v2) {
			return -1;
		} else {
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#compareBottom(int)
	 */
	@Override
	public int compareBottom(int doc) {

		final int v2 = currentFieldData.intValue(doc);
		if (bottom > v2) {
			return 1;
		} else if (bottom < v2) {
			return -1;
		} else {
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#copy(int, int)
	 */
	@Override
	public void copy(int slot, int doc) {
		values[slot] = currentFieldData.intValue(doc);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#setBottom(int)
	 */
	@Override
	public void setBottom(final int bottom) {
		this.bottom = values[bottom];
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#value(int)
	 */
	@Override
	public Comparable value(int slot) {
		return Integer.valueOf(values[slot]);
	}
}
