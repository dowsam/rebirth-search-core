/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesSegmentResponse.java 2012-7-6 14:29:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.segments;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.commons.xcontent.ToXContent.Params;
import cn.com.rebirth.search.core.action.ShardOperationFailedException;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationResponse;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.index.engine.Segment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * The Class IndicesSegmentResponse.
 *
 * @author l.xue.nong
 */
public class IndicesSegmentResponse extends BroadcastOperationResponse implements ToXContent {

	/** The shards. */
	private ShardSegments[] shards;

	/** The indices segments. */
	private Map<String, IndexSegments> indicesSegments;

	/**
	 * Instantiates a new indices segment response.
	 */
	IndicesSegmentResponse() {

	}

	/**
	 * Instantiates a new indices segment response.
	 *
	 * @param shards the shards
	 * @param clusterState the cluster state
	 * @param totalShards the total shards
	 * @param successfulShards the successful shards
	 * @param failedShards the failed shards
	 * @param shardFailures the shard failures
	 */
	IndicesSegmentResponse(ShardSegments[] shards, ClusterState clusterState, int totalShards, int successfulShards,
			int failedShards, List<ShardOperationFailedException> shardFailures) {
		super(totalShards, successfulShards, failedShards, shardFailures);
		this.shards = shards;
	}

	/**
	 * Gets the indices.
	 *
	 * @return the indices
	 */
	public Map<String, IndexSegments> getIndices() {
		return this.indices();
	}

	/**
	 * Indices.
	 *
	 * @return the map
	 */
	public Map<String, IndexSegments> indices() {
		if (indicesSegments != null) {
			return indicesSegments;
		}
		Map<String, IndexSegments> indicesSegments = Maps.newHashMap();

		Set<String> indices = Sets.newHashSet();
		for (ShardSegments shard : shards) {
			indices.add(shard.index());
		}

		for (String index : indices) {
			List<ShardSegments> shards = Lists.newArrayList();
			for (ShardSegments shard : this.shards) {
				if (shard.shardRouting().index().equals(index)) {
					shards.add(shard);
				}
			}
			indicesSegments.put(index, new IndexSegments(index, shards.toArray(new ShardSegments[shards.size()])));
		}
		this.indicesSegments = indicesSegments;
		return indicesSegments;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationResponse#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		shards = new ShardSegments[in.readVInt()];
		for (int i = 0; i < shards.length; i++) {
			shards[i] = ShardSegments.readShardSegments(in);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationResponse#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeVInt(shards.length);
		for (ShardSegments shard : shards) {
			shard.writeTo(out);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.INDICES);

		for (IndexSegments indexSegments : indices().values()) {
			builder.startObject(indexSegments.index(), XContentBuilder.FieldCaseConversion.NONE);

			builder.startObject(Fields.SHARDS);
			for (IndexShardSegments indexSegment : indexSegments) {
				builder.startArray(Integer.toString(indexSegment.shardId().id()));
				for (ShardSegments shardSegments : indexSegment) {
					builder.startObject();

					builder.startObject(Fields.ROUTING);
					builder.field(Fields.STATE, shardSegments.shardRouting().state());
					builder.field(Fields.PRIMARY, shardSegments.shardRouting().primary());
					builder.field(Fields.NODE, shardSegments.shardRouting().currentNodeId());
					if (shardSegments.shardRouting().relocatingNodeId() != null) {
						builder.field(Fields.RELOCATING_NODE, shardSegments.shardRouting().relocatingNodeId());
					}
					builder.endObject();

					builder.field(Fields.NUM_COMMITTED_SEGMENTS, shardSegments.numberOfCommitted());
					builder.field(Fields.NUM_SEARCH_SEGMENTS, shardSegments.numberOfSearch());

					builder.startObject(Fields.SEGMENTS);
					for (Segment segment : shardSegments) {
						builder.startObject(segment.name());
						builder.field(Fields.GENERATION, segment.generation());
						builder.field(Fields.NUM_DOCS, segment.numDocs());
						builder.field(Fields.DELETED_DOCS, segment.deletedDocs());
						builder.field(Fields.SIZE, segment.size().toString());
						builder.field(Fields.SIZE_IN_BYTES, segment.sizeInBytes());
						builder.field(Fields.COMMITTED, segment.committed());
						builder.field(Fields.SEARCH, segment.search());
						builder.endObject();
					}
					builder.endObject();

					builder.endObject();
				}
				builder.endArray();
			}
			builder.endObject();

			builder.endObject();
		}

		builder.endObject();
		return builder;
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant INDICES. */
		static final XContentBuilderString INDICES = new XContentBuilderString("indices");

		/** The Constant SHARDS. */
		static final XContentBuilderString SHARDS = new XContentBuilderString("shards");

		/** The Constant ROUTING. */
		static final XContentBuilderString ROUTING = new XContentBuilderString("routing");

		/** The Constant STATE. */
		static final XContentBuilderString STATE = new XContentBuilderString("state");

		/** The Constant PRIMARY. */
		static final XContentBuilderString PRIMARY = new XContentBuilderString("primary");

		/** The Constant NODE. */
		static final XContentBuilderString NODE = new XContentBuilderString("node");

		/** The Constant RELOCATING_NODE. */
		static final XContentBuilderString RELOCATING_NODE = new XContentBuilderString("relocating_node");

		/** The Constant SEGMENTS. */
		static final XContentBuilderString SEGMENTS = new XContentBuilderString("segments");

		/** The Constant GENERATION. */
		static final XContentBuilderString GENERATION = new XContentBuilderString("generation");

		/** The Constant NUM_COMMITTED_SEGMENTS. */
		static final XContentBuilderString NUM_COMMITTED_SEGMENTS = new XContentBuilderString("num_committed_segments");

		/** The Constant NUM_SEARCH_SEGMENTS. */
		static final XContentBuilderString NUM_SEARCH_SEGMENTS = new XContentBuilderString("num_search_segments");

		/** The Constant NUM_DOCS. */
		static final XContentBuilderString NUM_DOCS = new XContentBuilderString("num_docs");

		/** The Constant DELETED_DOCS. */
		static final XContentBuilderString DELETED_DOCS = new XContentBuilderString("deleted_docs");

		/** The Constant SIZE. */
		static final XContentBuilderString SIZE = new XContentBuilderString("size");

		/** The Constant SIZE_IN_BYTES. */
		static final XContentBuilderString SIZE_IN_BYTES = new XContentBuilderString("size_in_bytes");

		/** The Constant COMMITTED. */
		static final XContentBuilderString COMMITTED = new XContentBuilderString("committed");

		/** The Constant SEARCH. */
		static final XContentBuilderString SEARCH = new XContentBuilderString("search");
	}
}