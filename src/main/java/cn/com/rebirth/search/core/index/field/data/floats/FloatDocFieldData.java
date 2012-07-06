/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FloatDocFieldData.java 2012-3-29 15:01:26 l.xue.nong$$
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