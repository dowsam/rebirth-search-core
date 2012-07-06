/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DoubleDocFieldData.java 2012-7-6 14:29:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.doubles;

import cn.com.rebirth.search.core.index.field.data.NumericDocFieldData;

/**
 * The Class DoubleDocFieldData.
 *
 * @author l.xue.nong
 */
public class DoubleDocFieldData extends NumericDocFieldData<DoubleFieldData> {

	/**
	 * Instantiates a new double doc field data.
	 *
	 * @param fieldData the field data
	 */
	public DoubleDocFieldData(DoubleFieldData fieldData) {
		super(fieldData);
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public double getValue() {
		return fieldData.value(docId);
	}

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public double[] getValues() {
		return fieldData.values(docId);
	}
}