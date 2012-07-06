/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RecoveryFileChunkRequest.java 2012-3-29 15:01:50 l.xue.nong$$
 */


package cn.com.rebirth.search.core.indices.recovery;

import java.io.IOException;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class RecoveryFileChunkRequest.
 *
 * @author l.xue.nong
 */
class RecoveryFileChunkRequest implements Streamable {

	
	/** The shard id. */
	private ShardId shardId;

	
	/** The name. */
	private String name;

	
	/** The position. */
	private long position;

	
	/** The length. */
	private long length;

	
	/** The checksum. */
	private String checksum;

	
	/** The content. */
	private BytesHolder content;

	
	/**
	 * Instantiates a new recovery file chunk request.
	 */
	RecoveryFileChunkRequest() {
	}

	
	/**
	 * Instantiates a new recovery file chunk request.
	 *
	 * @param shardId the shard id
	 * @param name the name
	 * @param position the position
	 * @param length the length
	 * @param checksum the checksum
	 * @param content the content
	 */
	RecoveryFileChunkRequest(ShardId shardId, String name, long position, long length, String checksum,
			BytesHolder content) {
		this.shardId = shardId;
		this.name = name;
		this.position = position;
		this.length = length;
		this.checksum = checksum;
		this.content = content;
	}

	
	/**
	 * Shard id.
	 *
	 * @return the shard id
	 */
	public ShardId shardId() {
		return shardId;
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
	 * Position.
	 *
	 * @return the long
	 */
	public long position() {
		return position;
	}

	
	/**
	 * Checksum.
	 *
	 * @return the string
	 */
	@Nullable
	public String checksum() {
		return this.checksum;
	}

	
	/**
	 * Length.
	 *
	 * @return the long
	 */
	public long length() {
		return length;
	}

	
	/**
	 * Content.
	 *
	 * @return the bytes holder
	 */
	public BytesHolder content() {
		return content;
	}

	
	/**
	 * Read file chunk.
	 *
	 * @param in the in
	 * @return the recovery file chunk request
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public RecoveryFileChunkRequest readFileChunk(StreamInput in) throws IOException {
		RecoveryFileChunkRequest request = new RecoveryFileChunkRequest();
		request.readFrom(in);
		return request;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		shardId = ShardId.readShardId(in);
		name = in.readUTF();
		position = in.readVLong();
		length = in.readVLong();
		if (in.readBoolean()) {
			checksum = in.readUTF();
		}
		content = in.readBytesReference();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		shardId.writeTo(out);
		out.writeUTF(name);
		out.writeVLong(position);
		out.writeVLong(length);
		if (checksum == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(checksum);
		}
		out.writeBytesHolder(content);
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return shardId + ": name='" + name + '\'' + ", position=" + position + ", length=" + length;
	}
}
