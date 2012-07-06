/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GetField.java 2012-7-6 14:30:27 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.get;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;

/**
 * The Class GetField.
 *
 * @author l.xue.nong
 */
public class GetField implements Streamable, Iterable<Object> {

	/** The name. */
	private String name;

	/** The values. */
	private List<Object> values;

	/**
	 * Instantiates a new gets the field.
	 */
	private GetField() {

	}

	/**
	 * Instantiates a new gets the field.
	 *
	 * @param name the name
	 * @param values the values
	 */
	public GetField(String name, List<Object> values) {
		this.name = name;
		this.values = values;
	}

	/**
	 * Name.
	 *
	 * @return the string
	 */
	public String name() {
		return name;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Value.
	 *
	 * @return the object
	 */
	public Object value() {
		if (values != null && !values.isEmpty()) {
			return values.get(0);
		}
		return null;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public Object getValue() {
		return value();
	}

	/**
	 * Values.
	 *
	 * @return the list
	 */
	public List<Object> values() {
		return values;
	}

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public List<Object> getValues() {
		return values;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Object> iterator() {
		return values.iterator();
	}

	/**
	 * Read get field.
	 *
	 * @param in the in
	 * @return the gets the field
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static GetField readGetField(StreamInput in) throws IOException {
		GetField result = new GetField();
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
		for (Object obj : values) {
			out.writeGenericValue(obj);
		}
	}
}
