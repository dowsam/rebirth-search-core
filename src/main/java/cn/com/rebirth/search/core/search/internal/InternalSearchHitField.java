/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalSearchHitField.java 2012-7-6 14:30:23 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.search.SearchHitField;

/**
 * The Class InternalSearchHitField.
 *
 * @author l.xue.nong
 */
public class InternalSearchHitField implements SearchHitField {

	/** The name. */
	private String name;

	/** The values. */
	private List<Object> values;

	/**
	 * Instantiates a new internal search hit field.
	 */
	private InternalSearchHitField() {

	}

	/**
	 * Instantiates a new internal search hit field.
	 *
	 * @param name the name
	 * @param values the values
	 */
	public InternalSearchHitField(String name, List<Object> values) {
		this.name = name;
		this.values = values;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHitField#name()
	 */
	public String name() {
		return name;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHitField#getName()
	 */
	@Override
	public String getName() {
		return name();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHitField#value()
	 */
	@Override
	public Object value() {
		if (values == null || values.isEmpty()) {
			return null;
		}
		return values.get(0);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHitField#getValue()
	 */
	@Override
	public Object getValue() {
		return value();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHitField#values()
	 */
	public List<Object> values() {
		return values;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHitField#getValues()
	 */
	@Override
	public List<Object> getValues() {
		return values();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Object> iterator() {
		return values.iterator();
	}

	/**
	 * Read search hit field.
	 *
	 * @param in the in
	 * @return the internal search hit field
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static InternalSearchHitField readSearchHitField(StreamInput in) throws IOException {
		InternalSearchHitField result = new InternalSearchHitField();
		result.readFrom(in);
		return result;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		name = in.readUTF();
		int size = in.readVInt();
		values = new ArrayList<Object>(size);
		for (int i = 0; i < size; i++) {
			values.add(in.readGenericValue());
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(name);
		out.writeVInt(values.size());
		for (Object value : values) {
			out.writeGenericValue(value);
		}
	}
}