/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NumericDocFieldData.java 2012-7-6 14:29:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data;

/**
 * The Class NumericDocFieldData.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public class NumericDocFieldData<T extends NumericFieldData> extends DocFieldData<T> {

	/**
	 * Instantiates a new numeric doc field data.
	 *
	 * @param fieldData the field data
	 */
	public NumericDocFieldData(T fieldData) {
		super(fieldData);
	}

	/**
	 * Gets the int value.
	 *
	 * @return the int value
	 */
	public int getIntValue() {
		return fieldData.intValue(docId);
	}

	/**
	 * Gets the long value.
	 *
	 * @return the long value
	 */
	public long getLongValue() {
		return fieldData.longValue(docId);
	}

	/**
	 * Gets the float value.
	 *
	 * @return the float value
	 */
	public float getFloatValue() {
		return fieldData.floatValue(docId);
	}

	/**
	 * Gets the double value.
	 *
	 * @return the double value
	 */
	public double getDoubleValue() {
		return fieldData.doubleValue(docId);
	}

	/**
	 * Gets the short value.
	 *
	 * @return the short value
	 */
	public short getShortValue() {
		return fieldData.shortValue(docId);
	}

	/**
	 * Gets the byte value.
	 *
	 * @return the byte value
	 */
	public byte getByteValue() {
		return fieldData.byteValue(docId);
	}

	/**
	 * Gets the double values.
	 *
	 * @return the double values
	 */
	public double[] getDoubleValues() {
		return fieldData.doubleValues(docId);
	}
}
