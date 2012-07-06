/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DocFieldData.java 2012-7-6 14:29:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data;

/**
 * The Class DocFieldData.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public abstract class DocFieldData<T extends FieldData> {

	/** The field data. */
	protected final T fieldData;

	/** The doc id. */
	protected int docId;

	/**
	 * Instantiates a new doc field data.
	 *
	 * @param fieldData the field data
	 */
	protected DocFieldData(T fieldData) {
		this.fieldData = fieldData;
	}

	/**
	 * Sets the doc id.
	 *
	 * @param docId the new doc id
	 */
	void setDocId(int docId) {
		this.docId = docId;
	}

	/**
	 * Gets the field name.
	 *
	 * @return the field name
	 */
	public String getFieldName() {
		return fieldData.fieldName();
	}

	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		return !fieldData.hasValue(docId);
	}

	/**
	 * String value.
	 *
	 * @return the string
	 */
	public String stringValue() {
		return fieldData.stringValue(docId);
	}

	/**
	 * Gets the string value.
	 *
	 * @return the string value
	 */
	public String getStringValue() {
		return stringValue();
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public FieldDataType getType() {
		return fieldData.type();
	}

	/**
	 * Checks if is multi valued.
	 *
	 * @return true, if is multi valued
	 */
	public boolean isMultiValued() {
		return fieldData.multiValued();
	}
}
