/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core Index.java 2012-7-6 14:30:10 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index;

import java.io.IOException;
import java.io.Serializable;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;

/**
 * The Class Index.
 *
 * @author l.xue.nong
 */
public class Index implements Serializable, Streamable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7405960843215378369L;

	/** The name. */
	private String name;

	/**
	 * Instantiates a new index.
	 */
	private Index() {

	}

	/**
	 * Instantiates a new index.
	 *
	 * @param name the name
	 */
	public Index(String name) {
		this.name = name.intern();
	}

	/**
	 * Name.
	 *
	 * @return the string
	 */
	public String name() {
		return this.name;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Index [" + name + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		Index index1 = (Index) o;
		return name.equals(index1.name);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Read index name.
	 *
	 * @param in the in
	 * @return the index
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Index readIndexName(StreamInput in) throws IOException {
		Index index = new Index();
		index.readFrom(in);
		return index;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		name = in.readUTF().intern();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(name);
	}
}
