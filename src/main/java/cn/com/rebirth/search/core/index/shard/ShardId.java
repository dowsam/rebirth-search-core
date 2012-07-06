/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardId.java 2012-3-29 15:00:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.shard;

import java.io.IOException;
import java.io.Serializable;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.index.Index;


/**
 * The Class ShardId.
 *
 * @author l.xue.nong
 */
public class ShardId implements Serializable, Streamable {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1526492849141405503L;

	
	/** The index. */
	private Index index;

	
	/** The shard id. */
	private int shardId;

	
	/** The hash code. */
	private int hashCode;

	
	/**
	 * Instantiates a new shard id.
	 */
	private ShardId() {

	}

	
	/**
	 * Instantiates a new shard id.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 */
	public ShardId(String index, int shardId) {
		this(new Index(index), shardId);
	}

	
	/**
	 * Instantiates a new shard id.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 */
	public ShardId(Index index, int shardId) {
		this.index = index;
		this.shardId = shardId;
		this.hashCode = computeHashCode();
	}

	
	/**
	 * Index.
	 *
	 * @return the index
	 */
	public Index index() {
		return this.index;
	}

	
	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public String getIndex() {
		return index().name();
	}

	
	/**
	 * Id.
	 *
	 * @return the int
	 */
	public int id() {
		return this.shardId;
	}

	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public int getId() {
		return id();
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Index Shard [" + index.name() + "][" + shardId + "]";
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
		ShardId shardId1 = (ShardId) o;
		return shardId == shardId1.shardId && index.name().equals(shardId1.index.name());
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hashCode;
	}

	
	/**
	 * Compute hash code.
	 *
	 * @return the int
	 */
	private int computeHashCode() {
		int result = index != null ? index.hashCode() : 0;
		result = 31 * result + shardId;
		return result;
	}

	
	/**
	 * Read shard id.
	 *
	 * @param in the in
	 * @return the shard id
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ShardId readShardId(StreamInput in) throws IOException {
		ShardId shardId = new ShardId();
		shardId.readFrom(in);
		return shardId;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		index = Index.readIndexName(in);
		shardId = in.readVInt();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		index.writeTo(out);
		out.writeVInt(shardId);
	}
}