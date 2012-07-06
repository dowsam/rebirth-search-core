/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShortFieldDataMissingComparator.java 2012-7-6 14:29:19 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.shorts;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.support.NumericFieldDataComparator;

/**
 * The Class ShortFieldDataMissingComparator.
 *
 * @author l.xue.nong
 */
public class ShortFieldDataMissingComparator extends NumericFieldDataComparator {

	/** The values. */
	private final short[] values;

	/** The bottom. */
	private short bottom;

	/** The missing value. */
	private final short missingValue;

	/**
	 * Instantiates a new short field data missing comparator.
	 *
	 * @param numHits the num hits
	 * @param fieldName the field name
	 * @param fieldDataCache the field data cache
	 * @param missingValue the missing value
	 */
	public ShortFieldDataMissingComparator(int numHits, String fieldName, FieldDataCache fieldDataCache,
			short missingValue) {
		super(fieldName, fieldDataCache);
		values = new short[numHits];
		this.missingValue = missingValue;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.support.NumericFieldDataComparator#fieldDataType()
	 */
	@Override
	public FieldDataType fieldDataType() {
		return FieldDataType.DefaultTypes.SHORT;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#compare(int, int)
	 */
	@Override
	public int compare(int slot1, int slot2) {
		return values[slot1] - values[slot2];
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#compareBottom(int)
	 */
	@Override
	public int compareBottom(int doc) {
		short value = missingValue;
		if (currentFieldData.hasValue(doc)) {
			value = currentFieldData.shortValue(doc);
		}
		return bottom - value;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#copy(int, int)
	 */
	@Override
	public void copy(int slot, int doc) {
		short value = missingValue;
		if (currentFieldData.hasValue(doc)) {
			value = currentFieldData.shortValue(doc);
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
		return Short.valueOf(values[slot]);
	}
}
