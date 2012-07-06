/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LongDocFieldData.java 2012-7-6 14:30:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.longs;

import org.joda.time.MutableDateTime;

import cn.com.rebirth.search.core.index.field.data.NumericDocFieldData;

/**
 * The Class LongDocFieldData.
 *
 * @author l.xue.nong
 */
public class LongDocFieldData extends NumericDocFieldData<LongFieldData> {

	/**
	 * Instantiates a new long doc field data.
	 *
	 * @param fieldData the field data
	 */
	public LongDocFieldData(LongFieldData fieldData) {
		super(fieldData);
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public long getValue() {
		return fieldData.value(docId);
	}

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public long[] getValues() {
		return fieldData.values(docId);
	}

	/**
	 * Gets the date.
	 *
	 * @return the date
	 */
	public MutableDateTime getDate() {
		return fieldData.date(docId);
	}

	/**
	 * Gets the dates.
	 *
	 * @return the dates
	 */
	public MutableDateTime[] getDates() {
		return fieldData.dates(docId);
	}
}