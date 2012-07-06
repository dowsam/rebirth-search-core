/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core Segment.java 2012-3-29 15:01:23 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.engine;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.unit.ByteSizeValue;


/**
 * The Class Segment.
 *
 * @author l.xue.nong
 */
public class Segment implements Streamable {

	
	/** The name. */
	private String name;

	
	/** The generation. */
	private long generation;

	
	/** The committed. */
	public boolean committed;

	
	/** The search. */
	public boolean search;

	
	/** The size in bytes. */
	public long sizeInBytes = -1;

	
	/** The doc count. */
	public int docCount = -1;

	
	/** The del doc count. */
	public int delDocCount = -1;

	
	/**
	 * Instantiates a new segment.
	 */
	Segment() {
	}

	
	/**
	 * Instantiates a new segment.
	 *
	 * @param name the name
	 */
	public Segment(String name) {
		this.name = name;
		this.generation = Long.parseLong(name.substring(1), Character.MAX_RADIX);
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

	
	/**
	 * Generation.
	 *
	 * @return the long
	 */
	public long generation() {
		return this.generation;
	}

	
	/**
	 * Gets the generation.
	 *
	 * @return the generation
	 */
	public long getGeneration() {
		return this.generation;
	}

	
	/**
	 * Committed.
	 *
	 * @return true, if successful
	 */
	public boolean committed() {
		return this.committed;
	}

	
	/**
	 * Checks if is committed.
	 *
	 * @return true, if is committed
	 */
	public boolean isCommitted() {
		return this.committed;
	}

	
	/**
	 * Search.
	 *
	 * @return true, if successful
	 */
	public boolean search() {
		return this.search;
	}

	
	/**
	 * Checks if is search.
	 *
	 * @return true, if is search
	 */
	public boolean isSearch() {
		return this.search;
	}

	
	/**
	 * Num docs.
	 *
	 * @return the int
	 */
	public int numDocs() {
		return this.docCount;
	}

	
	/**
	 * Gets the num docs.
	 *
	 * @return the num docs
	 */
	public int getNumDocs() {
		return this.docCount;
	}

	
	/**
	 * Deleted docs.
	 *
	 * @return the int
	 */
	public int deletedDocs() {
		return this.delDocCount;
	}

	
	/**
	 * Gets the deleted docs.
	 *
	 * @return the deleted docs
	 */
	public int getDeletedDocs() {
		return this.delDocCount;
	}

	
	/**
	 * Size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue size() {
		return new ByteSizeValue(sizeInBytes);
	}

	
	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	public ByteSizeValue getSize() {
		return size();
	}

	
	/**
	 * Size in bytes.
	 *
	 * @return the long
	 */
	public long sizeInBytes() {
		return sizeInBytes;
	}

	
	/**
	 * Gets the size in bytes.
	 *
	 * @return the size in bytes
	 */
	public long getSizeInBytes() {
		return sizeInBytes();
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Segment segment = (Segment) o;

		if (name != null ? !name.equals(segment.name) : segment.name != null)
			return false;

		return true;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}

	
	/**
	 * Read segment.
	 *
	 * @param in the in
	 * @return the segment
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Segment readSegment(StreamInput in) throws IOException {
		Segment segment = new Segment();
		segment.readFrom(in);
		return segment;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		name = in.readUTF();
		generation = Long.parseLong(name.substring(1), Character.MAX_RADIX);
		committed = in.readBoolean();
		search = in.readBoolean();
		docCount = in.readInt();
		delDocCount = in.readInt();
		sizeInBytes = in.readLong();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(name);
		out.writeBoolean(committed);
		out.writeBoolean(search);
		out.writeInt(docCount);
		out.writeInt(delDocCount);
		out.writeLong(sizeInBytes);
	}
}