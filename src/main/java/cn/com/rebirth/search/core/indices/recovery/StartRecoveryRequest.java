/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StartRecoveryRequest.java 2012-7-6 14:29:14 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices.recovery;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.store.StoreFileMetaData;

import com.google.common.collect.Maps;

/**
 * The Class StartRecoveryRequest.
 *
 * @author l.xue.nong
 */
public class StartRecoveryRequest implements Streamable {

	/** The shard id. */
	private ShardId shardId;

	/** The source node. */
	private DiscoveryNode sourceNode;

	/** The target node. */
	private DiscoveryNode targetNode;

	/** The mark as relocated. */
	private boolean markAsRelocated;

	/** The existing files. */
	private Map<String, StoreFileMetaData> existingFiles;

	/**
	 * Instantiates a new start recovery request.
	 */
	StartRecoveryRequest() {
	}

	/**
	 * Instantiates a new start recovery request.
	 *
	 * @param shardId the shard id
	 * @param sourceNode the source node
	 * @param targetNode the target node
	 * @param markAsRelocated the mark as relocated
	 * @param existingFiles the existing files
	 */
	public StartRecoveryRequest(ShardId shardId, DiscoveryNode sourceNode, DiscoveryNode targetNode,
			boolean markAsRelocated, Map<String, StoreFileMetaData> existingFiles) {
		this.shardId = shardId;
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		this.markAsRelocated = markAsRelocated;
		this.existingFiles = existingFiles;
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
	 * Source node.
	 *
	 * @return the discovery node
	 */
	public DiscoveryNode sourceNode() {
		return sourceNode;
	}

	/**
	 * Target node.
	 *
	 * @return the discovery node
	 */
	public DiscoveryNode targetNode() {
		return targetNode;
	}

	/**
	 * Mark as relocated.
	 *
	 * @return true, if successful
	 */
	public boolean markAsRelocated() {
		return markAsRelocated;
	}

	/**
	 * Existing files.
	 *
	 * @return the map
	 */
	public Map<String, StoreFileMetaData> existingFiles() {
		return existingFiles;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		shardId = ShardId.readShardId(in);
		sourceNode = DiscoveryNode.readNode(in);
		targetNode = DiscoveryNode.readNode(in);
		markAsRelocated = in.readBoolean();
		int size = in.readVInt();
		existingFiles = Maps.newHashMapWithExpectedSize(size);
		for (int i = 0; i < size; i++) {
			StoreFileMetaData md = StoreFileMetaData.readStoreFileMetaData(in);
			existingFiles.put(md.name(), md);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		shardId.writeTo(out);
		sourceNode.writeTo(out);
		targetNode.writeTo(out);
		out.writeBoolean(markAsRelocated);
		out.writeVInt(existingFiles.size());
		for (StoreFileMetaData md : existingFiles.values()) {
			md.writeTo(out);
		}
	}
}
