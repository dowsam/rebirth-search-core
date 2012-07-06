/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexSegments.java 2012-3-29 15:01:21 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.segments;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * The Class IndexSegments.
 *
 * @author l.xue.nong
 */
public class IndexSegments implements Iterable<IndexShardSegments> {

	
	/** The index. */
	private final String index;

	
	/** The index shards. */
	private final Map<Integer, IndexShardSegments> indexShards;

	
	/**
	 * Instantiates a new index segments.
	 *
	 * @param index the index
	 * @param shards the shards
	 */
	IndexSegments(String index, ShardSegments[] shards) {
		this.index = index;

		Map<Integer, List<ShardSegments>> tmpIndexShards = Maps.newHashMap();
		for (ShardSegments shard : shards) {
			List<ShardSegments> lst = tmpIndexShards.get(shard.shardRouting().id());
			if (lst == null) {
				lst = Lists.newArrayList();
				tmpIndexShards.put(shard.shardRouting().id(), lst);
			}
			lst.add(shard);
		}
		indexShards = Maps.newHashMap();
		for (Map.Entry<Integer, List<ShardSegments>> entry : tmpIndexShards.entrySet()) {
			indexShards.put(entry.getKey(), new IndexShardSegments(entry.getValue().get(0).shardRouting().shardId(),
					entry.getValue().toArray(new ShardSegments[entry.getValue().size()])));
		}
	}

	
	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		return this.index;
	}

	
	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public String getIndex() {
		return index();
	}

	
	/**
	 * Shards.
	 *
	 * @return the map
	 */
	public Map<Integer, IndexShardSegments> shards() {
		return this.indexShards;
	}

	
	/**
	 * Gets the shards.
	 *
	 * @return the shards
	 */
	public Map<Integer, IndexShardSegments> getShards() {
		return shards();
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<IndexShardSegments> iterator() {
		return indexShards.values().iterator();
	}
}