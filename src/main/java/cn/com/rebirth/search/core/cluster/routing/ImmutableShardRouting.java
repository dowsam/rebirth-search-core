/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ImmutableShardRouting.java 2012-3-29 15:01:03 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing;

import java.io.IOException;
import java.io.Serializable;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.index.shard.ShardId;

import com.google.common.collect.ImmutableList;


/**
 * The Class ImmutableShardRouting.
 *
 * @author l.xue.nong
 */
public class ImmutableShardRouting implements Streamable, Serializable, ShardRouting {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1722885034961889998L;

	
	/** The index. */
	protected String index;

	
	/** The shard id. */
	protected int shardId;

	
	/** The current node id. */
	protected String currentNodeId;

	
	/** The relocating node id. */
	protected String relocatingNodeId;

	
	/** The primary. */
	protected boolean primary;

	
	/** The state. */
	protected ShardRoutingState state;

	
	/** The version. */
	protected long version;

	
	/** The shard identifier. */
	private transient ShardId shardIdentifier;

	
	/** The as list. */
	private final transient ImmutableList<ShardRouting> asList;

	
	/**
	 * Instantiates a new immutable shard routing.
	 */
	ImmutableShardRouting() {
		this.asList = ImmutableList.of((ShardRouting) this);
	}

	
	/**
	 * Instantiates a new immutable shard routing.
	 *
	 * @param copy the copy
	 */
	public ImmutableShardRouting(ShardRouting copy) {
		this(copy.index(), copy.id(), copy.currentNodeId(), copy.primary(), copy.state(), copy.version());
		this.relocatingNodeId = copy.relocatingNodeId();
	}

	
	/**
	 * Instantiates a new immutable shard routing.
	 *
	 * @param copy the copy
	 * @param version the version
	 */
	public ImmutableShardRouting(ShardRouting copy, long version) {
		this(copy.index(), copy.id(), copy.currentNodeId(), copy.primary(), copy.state(), copy.version());
		this.relocatingNodeId = copy.relocatingNodeId();
		this.version = version;
	}

	
	/**
	 * Instantiates a new immutable shard routing.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 * @param currentNodeId the current node id
	 * @param relocatingNodeId the relocating node id
	 * @param primary the primary
	 * @param state the state
	 * @param version the version
	 */
	public ImmutableShardRouting(String index, int shardId, String currentNodeId, String relocatingNodeId,
			boolean primary, ShardRoutingState state, long version) {
		this(index, shardId, currentNodeId, primary, state, version);
		this.relocatingNodeId = relocatingNodeId;
	}

	
	/**
	 * Instantiates a new immutable shard routing.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 * @param currentNodeId the current node id
	 * @param primary the primary
	 * @param state the state
	 * @param version the version
	 */
	public ImmutableShardRouting(String index, int shardId, String currentNodeId, boolean primary,
			ShardRoutingState state, long version) {
		this.index = index;
		this.shardId = shardId;
		this.currentNodeId = currentNodeId;
		this.primary = primary;
		this.state = state;
		this.asList = ImmutableList.of((ShardRouting) this);
		this.version = version;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#index()
	 */
	@Override
	public String index() {
		return this.index;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#getIndex()
	 */
	@Override
	public String getIndex() {
		return index();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#id()
	 */
	@Override
	public int id() {
		return this.shardId;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#getId()
	 */
	@Override
	public int getId() {
		return id();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#version()
	 */
	@Override
	public long version() {
		return this.version;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#unassigned()
	 */
	@Override
	public boolean unassigned() {
		return state == ShardRoutingState.UNASSIGNED;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#initializing()
	 */
	@Override
	public boolean initializing() {
		return state == ShardRoutingState.INITIALIZING;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#active()
	 */
	@Override
	public boolean active() {
		return started() || relocating();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#started()
	 */
	@Override
	public boolean started() {
		return state == ShardRoutingState.STARTED;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#relocating()
	 */
	@Override
	public boolean relocating() {
		return state == ShardRoutingState.RELOCATING;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#assignedToNode()
	 */
	@Override
	public boolean assignedToNode() {
		return currentNodeId != null;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#currentNodeId()
	 */
	@Override
	public String currentNodeId() {
		return this.currentNodeId;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#relocatingNodeId()
	 */
	@Override
	public String relocatingNodeId() {
		return this.relocatingNodeId;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#primary()
	 */
	@Override
	public boolean primary() {
		return this.primary;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#state()
	 */
	@Override
	public ShardRoutingState state() {
		return this.state;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#shardId()
	 */
	@Override
	public ShardId shardId() {
		if (shardIdentifier != null) {
			return shardIdentifier;
		}
		shardIdentifier = new ShardId(index, shardId);
		return shardIdentifier;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#shardsIt()
	 */
	@Override
	public ShardIterator shardsIt() {
		return new PlainShardIterator(shardId(), asList);
	}

	
	/**
	 * Read shard routing entry.
	 *
	 * @param in the in
	 * @return the immutable shard routing
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ImmutableShardRouting readShardRoutingEntry(StreamInput in) throws IOException {
		ImmutableShardRouting entry = new ImmutableShardRouting();
		entry.readFrom(in);
		return entry;
	}

	
	/**
	 * Read shard routing entry.
	 *
	 * @param in the in
	 * @param index the index
	 * @param shardId the shard id
	 * @return the immutable shard routing
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ImmutableShardRouting readShardRoutingEntry(StreamInput in, String index, int shardId)
			throws IOException {
		ImmutableShardRouting entry = new ImmutableShardRouting();
		entry.readFrom(in, index, shardId);
		return entry;
	}

	
	/**
	 * Read from.
	 *
	 * @param in the in
	 * @param index the index
	 * @param shardId the shard id
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void readFrom(StreamInput in, String index, int shardId) throws IOException {
		this.index = index;
		this.shardId = shardId;
		readFromThin(in);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#readFromThin(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFromThin(StreamInput in) throws IOException {
		version = in.readLong();
		if (in.readBoolean()) {
			currentNodeId = in.readUTF();
		}

		if (in.readBoolean()) {
			relocatingNodeId = in.readUTF();
		}

		primary = in.readBoolean();
		state = ShardRoutingState.fromValue(in.readByte());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		readFrom(in, in.readUTF(), in.readVInt());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#writeToThin(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	public void writeToThin(StreamOutput out) throws IOException {
		out.writeLong(version);
		if (currentNodeId != null) {
			out.writeBoolean(true);
			out.writeUTF(currentNodeId);
		} else {
			out.writeBoolean(false);
		}

		if (relocatingNodeId != null) {
			out.writeBoolean(true);
			out.writeUTF(relocatingNodeId);
		} else {
			out.writeBoolean(false);
		}

		out.writeBoolean(primary);
		out.writeByte(state.value());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(index);
		out.writeVInt(shardId);
		writeToThin(out);
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

		ImmutableShardRouting that = (ImmutableShardRouting) o;

		if (primary != that.primary)
			return false;
		if (shardId != that.shardId)
			return false;
		if (currentNodeId != null ? !currentNodeId.equals(that.currentNodeId) : that.currentNodeId != null)
			return false;
		if (index != null ? !index.equals(that.index) : that.index != null)
			return false;
		if (relocatingNodeId != null ? !relocatingNodeId.equals(that.relocatingNodeId) : that.relocatingNodeId != null)
			return false;
		if (state != that.state)
			return false;

		return true;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = index != null ? index.hashCode() : 0;
		result = 31 * result + shardId;
		result = 31 * result + (currentNodeId != null ? currentNodeId.hashCode() : 0);
		result = 31 * result + (relocatingNodeId != null ? relocatingNodeId.hashCode() : 0);
		result = 31 * result + (primary ? 1 : 0);
		result = 31 * result + (state != null ? state.hashCode() : 0);
		return result;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return shortSummary();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.routing.ShardRouting#shortSummary()
	 */
	@Override
	public String shortSummary() {
		StringBuilder sb = new StringBuilder();
		sb.append('[').append(index).append(']').append('[').append(shardId).append(']');
		sb.append(", node[").append(currentNodeId).append("], ");
		if (relocatingNodeId != null) {
			sb.append("relocating [").append(relocatingNodeId).append("], ");
		}
		if (primary) {
			sb.append("[P]");
		} else {
			sb.append("[R]");
		}
		sb.append(", s[").append(state).append("]");
		return sb.toString();
	}

}
