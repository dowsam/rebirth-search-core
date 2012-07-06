/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IntFieldDataMissingComparator.java 2012-7-6 14:29:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.ints;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.support.NumericFieldDataComparator;

/**
 * The Class IntFieldDataMissingComparator.
 *
 * @author l.xue.nong
 */
public class IntFieldDataMissingComparator extends NumericFieldDataComparator {

	/** The values. */
	private final int[] values;

	/** The bottom. */
	private int bottom;

	/** The missing value. */
	private final int missingValue;

	/**
	 * Instantiates a new int field data missing comparator.
	 *
	 * @param numHits the num hits
	 * @param fieldName the field name
	 * @param fieldDataCache the field data cache
	 * @param missingValue the missing value
	 */
	public IntFieldDataMissingComparator(int numHits, String fieldName, FieldDataCache fieldDataCache, int missingValue) {
		super(fieldName, fieldDataCache);
		values = new int[numHits];
		this.missingValue = missingValue;
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

		int v2 = missingValue;
		if (currentFieldData.hasValue(doc)) {
			v2 = currentFieldData.intValue(doc);
		}
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
		int value = missingValue;
		if (currentFieldData.hasValue(doc)) {
			value = currentFieldData.intValue(doc);
		}
		values[slot] = value;
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
