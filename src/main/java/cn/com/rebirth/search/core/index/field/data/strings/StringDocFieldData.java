/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StringDocFieldData.java 2012-7-6 14:30:37 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.strings;

import cn.com.rebirth.search.core.index.field.data.DocFieldData;

/**
 * The Class StringDocFieldData.
 *
 * @author l.xue.nong
 */
public class StringDocFieldData extends DocFieldData<StringFieldData> {

	/**
	 * Instantiates a new string doc field data.
	 *
	 * @param fieldData the field data
	 */
	public StringDocFieldData(StringFieldData fieldData) {
		super(fieldData);
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return fieldData.value(docId);
	}

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public String[] getValues() {
		return fieldData.values(docId);
	}
}
