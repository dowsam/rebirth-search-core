/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core HighlightField.java 2012-3-29 15:00:46 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.highlight;

import java.io.IOException;
import java.util.Arrays;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;


/**
 * The Class HighlightField.
 *
 * @author l.xue.nong
 */
public class HighlightField implements Streamable {

	
	/** The name. */
	private String name;

	
	/** The fragments. */
	private String[] fragments;

	
	/**
	 * Instantiates a new highlight field.
	 */
	HighlightField() {
	}

	
	/**
	 * Instantiates a new highlight field.
	 *
	 * @param name the name
	 * @param fragments the fragments
	 */
	public HighlightField(String name, String[] fragments) {
		this.name = name;
		this.fragments = fragments;
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
		return name();
	}

	
	/**
	 * Fragments.
	 *
	 * @return the string[]
	 */
	public String[] fragments() {
		return fragments;
	}

	
	/**
	 * Gets the fragments.
	 *
	 * @return the fragments
	 */
	public String[] getFragments() {
		return fragments();
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + name + "], fragments[" + Arrays.toString(fragments) + "]";
	}

	
	/**
	 * Read highlight field.
	 *
	 * @param in the in
	 * @return the highlight field
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static HighlightField readHighlightField(StreamInput in) throws IOException {
		HighlightField field = new HighlightField();
		field.readFrom(in);
		return field;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		name = in.readUTF();
		if (in.readBoolean()) {
			int size = in.readVInt();
			if (size == 0) {
				fragments = Strings.EMPTY_ARRAY;
			} else {
				fragments = new String[size];
				for (int i = 0; i < size; i++) {
					fragments[i] = in.readUTF();
				}
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(name);
		if (fragments == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeVInt(fragments.length);
			for (String fragment : fragments) {
				out.writeUTF(fragment);
			}
		}
	}
}
