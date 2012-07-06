/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesStats.java 2012-7-6 14:30:38 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.stats;

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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * The Class IndicesStats.
 *
 * @author l.xue.nong
 */
public class IndicesStats extends BroadcastOperationResponse implements ToXContent {

	/** The shards. */
	private ShardStats[] shards;

	/**
	 * Instantiates a new indices stats.
	 */
	IndicesStats() {

	}

	/**
	 * Instantiates a new indices stats.
	 *
	 * @param shards the shards
	 * @param clusterState the cluster state
	 * @param totalShards the total shards
	 * @param successfulShards the successful shards
	 * @param failedShards the failed shards
	 * @param shardFailures the shard failures
	 */
	IndicesStats(ShardStats[] shards, ClusterState clusterState, int totalShards, int successfulShards,
			int failedShards, List<ShardOperationFailedException> shardFailures) {
		super(totalShards, successfulShards, failedShards, shardFailures);
		this.shards = shards;
	}

	/**
	 * Shards.
	 *
	 * @return the shard stats[]
	 */
	public ShardStats[] shards() {
		return this.shards;
	}

	/**
	 * Gets the shards.
	 *
	 * @return the shards
	 */
	public ShardStats[] getShards() {
		return this.shards;
	}

	/**
	 * Gets the at.
	 *
	 * @param position the position
	 * @return the at
	 */
	public ShardStats getAt(int position) {
		return shards[position];
	}

	/**
	 * Index.
	 *
	 * @param index the index
	 * @return the index stats
	 */
	public IndexStats index(String index) {
		return indices().get(index);
	}

	/**
	 * Gets the indices.
	 *
	 * @return the indices
	 */
	public Map<String, IndexStats> getIndices() {
		return indices();
	}

	/** The indices stats. */
	private Map<String, IndexStats> indicesStats;

	/**
	 * Indices.
	 *
	 * @return the map
	 */
	public Map<String, IndexStats> indices() {
		if (indicesStats != null) {
			return indicesStats;
		}
		Map<String, IndexStats> indicesStats = Maps.newHashMap();

		Set<String> indices = Sets.newHashSet();
		for (ShardStats shard : shards) {
			indices.add(shard.index());
		}

		for (String index : indices) {
			List<ShardStats> shards = Lists.newArrayList();
			for (ShardStats shard : this.shards) {
				if (shard.shardRouting().index().equals(index)) {
					shards.add(shard);
				}
			}
			indicesStats.put(index, new IndexStats(index, shards.toArray(new ShardStats[shards.size()])));
		}
		this.indicesStats = indicesStats;
		return indicesStats;
	}

	/** The total. */
	private CommonStats total = null;

	/**
	 * Gets the total.
	 *
	 * @return the total
	 */
	public CommonStats getTotal() {
		return total();
	}

	/**
	 * Total.
	 *
	 * @return the common stats
	 */
	public CommonStats total() {
		if (total != null) {
			return total;
		}
		CommonStats stats = new CommonStats();
		for (ShardStats shard : shards) {
			stats.add(shard.stats());
		}
		total = stats;
		return stats;
	}

	/** The primary. */
	private CommonStats primary = null;

	/**
	 * Gets the primaries.
	 *
	 * @return the primaries
	 */
	public CommonStats getPrimaries() {
		return primaries();
	}

	/**
	 * Primaries.
	 *
	 * @return the common stats
	 */
	public CommonStats primaries() {
		if (primary != null) {
			return primary;
		}
		CommonStats stats = new CommonStats();
		for (ShardStats shard : shards) {
			if (shard.shardRouting().primary()) {
				stats.add(shard.stats());
			}
		}
		primary = stats;
		return stats;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationResponse#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		shards = new ShardStats[in.readVInt()];
		for (int i = 0; i < shards.length; i++) {
			shards[i] = ShardStats.readShardStats(in);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationResponse#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeVInt(shards.length);
		for (ShardStats shard : shards) {
			shard.writeTo(out);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject("_all");

		builder.startObject("primaries");
		primaries().toXContent(builder, params);
		builder.endObject();

		builder.startObject("total");
		total().toXContent(builder, params);
		builder.endObject();

		builder.startObject(Fields.INDICES);
		for (IndexStats indexStats : indices().values()) {
			builder.startObject(indexStats.index(), XContentBuilder.FieldCaseConversion.NONE);

			builder.startObject("primaries");
			indexStats.primaries().toXContent(builder, params);
			builder.endObject();

			builder.startObject("total");
			indexStats.total().toXContent(builder, params);
			builder.endObject();

			if ("shards".equalsIgnoreCase(params.param("level", null))) {
				builder.startObject(Fields.SHARDS);
				for (IndexShardStats indexShardStats : indexStats) {
					builder.startArray(Integer.toString(indexShardStats.shardId().id()));
					for (ShardStats shardStats : indexShardStats) {
						builder.startObject();

						builder.startObject(Fields.ROUTING).field(Fields.STATE, shardStats.shardRouting().state())
								.field(Fields.PRIMARY, shardStats.shardRouting().primary())
								.field(Fields.NODE, shardStats.shardRouting().currentNodeId())
								.field(Fields.RELOCATING_NODE, shardStats.shardRouting().relocatingNodeId())
								.endObject();

						shardStats.stats().toXContent(builder, params);

						builder.endObject();
					}
					builder.endArray();
				}
				builder.endObject();
			}

			builder.endObject();
		}
		builder.endObject();

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
	}
}
