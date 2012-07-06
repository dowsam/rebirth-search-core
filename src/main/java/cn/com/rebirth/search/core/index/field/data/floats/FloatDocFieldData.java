/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FloatDocFieldData.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.floats;

import cn.com.rebirth.search.core.index.field.data.NumericDocFieldData;

/**
 * The Class FloatDocFieldData.
 *
 * @author l.xue.nong
 */
public class FloatDocFieldData extends NumericDocFieldData<FloatFieldData> {

	/**
	 * Instantiates a new float doc field data.
	 *
	 * @param fieldData the field data
	 */
	public FloatDocFieldData(FloatFieldData fieldData) {
		super(fieldData);
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public float getValue() {
		return fieldData.value(docId);
	}

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public float[] getValues() {
		return fieldData.values(docId);
	}
}