/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ByteFieldDataComparator.java 2012-7-6 14:30:27 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.bytes;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.support.NumericFieldDataComparator;

/**
 * The Class ByteFieldDataComparator.
 *
 * @author l.xue.nong
 */
public class ByteFieldDataComparator extends NumericFieldDataComparator {

	/** The values. */
	private final byte[] values;

	/** The bottom. */
	private short bottom;

	/**
	 * Instantiates a new byte field data comparator.
	 *
	 * @param numHits the num hits
	 * @param fieldName the field name
	 * @param fieldDataCache the field data cache
	 */
	public ByteFieldDataComparator(int numHits, String fieldName, FieldDataCache fieldDataCache) {
		super(fieldName, fieldDataCache);
		values = new byte[numHits];
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.support.NumericFieldDataComparator#fieldDataType()
	 */
	@Override
	public FieldDataType fieldDataType() {
		return FieldDataType.DefaultTypes.BYTE;
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
		return bottom - currentFieldData.byteValue(doc);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#copy(int, int)
	 */
	@Override
	public void copy(int slot, int doc) {
		values[slot] = currentFieldData.byteValue(doc);
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
		return Byte.valueOf(values[slot]);
	}
}
