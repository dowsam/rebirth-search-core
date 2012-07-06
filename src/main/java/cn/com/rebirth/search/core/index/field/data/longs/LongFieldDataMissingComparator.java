/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LongFieldDataMissingComparator.java 2012-7-6 14:29:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.longs;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.support.NumericFieldDataComparator;

/**
 * The Class LongFieldDataMissingComparator.
 *
 * @author l.xue.nong
 */
public class LongFieldDataMissingComparator extends NumericFieldDataComparator {

	/** The values. */
	private final long[] values;

	/** The bottom. */
	private long bottom;

	/** The missing value. */
	private final long missingValue;

	/**
	 * Instantiates a new long field data missing comparator.
	 *
	 * @param numHits the num hits
	 * @param fieldName the field name
	 * @param fieldDataCache the field data cache
	 * @param missingValue the missing value
	 */
	public LongFieldDataMissingComparator(int numHits, String fieldName, FieldDataCache fieldDataCache,
			long missingValue) {
		super(fieldName, fieldDataCache);
		values = new long[numHits];
		this.missingValue = missingValue;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.support.NumericFieldDataComparator#fieldDataType()
	 */
	@Override
	public FieldDataType fieldDataType() {
		return FieldDataType.DefaultTypes.LONG;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#compare(int, int)
	 */
	@Override
	public int compare(int slot1, int slot2) {

		final long v1 = values[slot1];
		final long v2 = values[slot2];
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

		long v2 = missingValue;
		if (currentFieldData.hasValue(doc)) {
			v2 = currentFieldData.longValue(doc);
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
		long value = missingValue;
		if (currentFieldData.hasValue(doc)) {
			value = currentFieldData.longValue(doc);
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
		return Long.valueOf(values[slot]);
	}

}
