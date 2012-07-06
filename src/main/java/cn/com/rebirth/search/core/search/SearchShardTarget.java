/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SearchShardTarget.java 2012-3-29 15:02:27 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search;

import java.io.IOException;
import java.io.Serializable;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;


/**
 * The Class SearchShardTarget.
 *
 * @author l.xue.nong
 */
public class SearchShardTarget implements Streamable, Serializable, Comparable<SearchShardTarget> {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 243063810885861219L;

	
	/** The node id. */
	private String nodeId;

	
	/** The index. */
	private String index;

	
	/** The shard id. */
	private int shardId;

	
	/**
	 * Instantiates a new search shard target.
	 */
	private SearchShardTarget() {

	}

	
	/**
	 * Instantiates a new search shard target.
	 *
	 * @param nodeId the node id
	 * @param index the index
	 * @param shardId the shard id
	 */
	public SearchShardTarget(String nodeId, String index, int shardId) {
		this.nodeId = nodeId;
		this.index = index;
		this.shardId = shardId;
	}

	
	/**
	 * Node id.
	 *
	 * @return the string
	 */
	@Nullable
	public String nodeId() {
		return nodeId;
	}

	
	/**
	 * Gets the node id.
	 *
	 * @return the node id
	 */
	@Nullable
	public String getNodeId() {
		return nodeId;
	}

	
	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		return index;
	}

	
	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public String getIndex() {
		return index;
	}

	
	/**
	 * Shard id.
	 *
	 * @return the int
	 */
	public int shardId() {
		return shardId;
	}

	
	/**
	 * Gets the shard id.
	 *
	 * @return the shard id
	 */
	public int getShardId() {
		return shardId;
	}

	
	/**
	 * Read search shard target.
	 *
	 * @param in the in
	 * @return the search shard target
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static SearchShardTarget readSearchShardTarget(StreamInput in) throws IOException {
		SearchShardTarget result = new SearchShardTarget();
		result.readFrom(in);
		return result;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SearchShardTarget o) {
		int i = index.compareTo(o.index());
		if (i == 0) {
			i = shardId - o.shardId;
		}
		return i;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		if (in.readBoolean()) {
			nodeId = in.readUTF();
		}
		index = in.readUTF();
		shardId = in.readVInt();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		if (nodeId == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(nodeId);
		}
		out.writeUTF(index);
		out.writeVInt(shardId);
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

		SearchShardTarget that = (SearchShardTarget) o;

		if (shardId != that.shardId)
			return false;
		if (index != null ? !index.equals(that.index) : that.index != null)
			return false;
		if (nodeId != null ? !nodeId.equals(that.nodeId) : that.nodeId != null)
			return false;

		return true;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = nodeId != null ? nodeId.hashCode() : 0;
		result = 31 * result + (index != null ? index.hashCode() : 0);
		result = 31 * result + shardId;
		return result;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (nodeId == null) {
			return "[_na_][" + index + "][" + shardId + "]";
		}
		return "[" + nodeId + "][" + index + "][" + shardId + "]";
	}
}
