/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FieldLookup.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.lookup;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;

import cn.com.rebirth.search.core.index.mapper.FieldMapper;

/**
 * The Class FieldLookup.
 *
 * @author l.xue.nong
 */
public class FieldLookup {

	/** The mapper. */
	private final FieldMapper mapper;

	/** The doc. */
	private Document doc;

	/** The value. */
	private Object value;

	/** The value loaded. */
	private boolean valueLoaded = false;

	/** The values. */
	private List<Object> values = new ArrayList<Object>();

	/** The values loaded. */
	private boolean valuesLoaded = false;

	/**
	 * Instantiates a new field lookup.
	 *
	 * @param mapper the mapper
	 */
	FieldLookup(FieldMapper mapper) {
		this.mapper = mapper;
	}

	/**
	 * Mapper.
	 *
	 * @return the field mapper
	 */
	public FieldMapper mapper() {
		return mapper;
	}

	/**
	 * Doc.
	 *
	 * @return the document
	 */
	public Document doc() {
		return doc;
	}

	/**
	 * Doc.
	 *
	 * @param doc the doc
	 */
	public void doc(Document doc) {
		this.doc = doc;
	}

	/**
	 * Clear.
	 */
	public void clear() {
		value = null;
		valueLoaded = false;
		values.clear();
		valuesLoaded = false;
		doc = null;
	}

	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		if (valueLoaded) {
			return value == null;
		}
		if (valuesLoaded) {
			return values.isEmpty();
		}
		return getValue() == null;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public Object getValue() {
		if (valueLoaded) {
			return value;
		}
		valueLoaded = true;
		value = null;
		Fieldable field = doc.getFieldable(mapper.names().indexName());
		if (field == null) {
			return null;
		}
		value = mapper.value(field);
		return value;
	}

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public List<Object> getValues() {
		if (valuesLoaded) {
			return values;
		}
		valuesLoaded = true;
		values.clear();
		Fieldable[] fields = doc.getFieldables(mapper.names().indexName());
		for (Fieldable field : fields) {
			values.add(mapper.value(field));
		}
		return values;
	}
}
