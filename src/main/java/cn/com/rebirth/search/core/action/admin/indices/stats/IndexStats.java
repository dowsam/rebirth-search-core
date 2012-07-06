/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexStats.java 2012-3-29 15:00:56 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.stats;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * The Class IndexStats.
 *
 * @author l.xue.nong
 */
public class IndexStats implements Iterable<IndexShardStats> {

    /** The index. */
    private final String index;

    /** The shards. */
    private final ShardStats shards[];

    /**
     * Instantiates a new index stats.
     *
     * @param index the index
     * @param shards the shards
     */
    public IndexStats(String index, ShardStats[] shards) {
        this.index = index;
        this.shards = shards;
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
     * Shards.
     *
     * @return the shard stats[]
     */
    public ShardStats[] shards() {
        return this.shards;
    }

    /** The index shards. */
    private Map<Integer, IndexShardStats> indexShards;

    /**
     * Index shards.
     *
     * @return the map
     */
    public Map<Integer, IndexShardStats> indexShards() {
        if (indexShards != null) {
            return indexShards;
        }
        Map<Integer, List<ShardStats>> tmpIndexShards = Maps.newHashMap();
        for (ShardStats shard : shards) {
            List<ShardStats> lst = tmpIndexShards.get(shard.shardRouting().id());
            if (lst == null) {
                lst = Lists.newArrayList();
                tmpIndexShards.put(shard.shardRouting().id(), lst);
            }
            lst.add(shard);
        }
        indexShards = Maps.newHashMap();
        for (Map.Entry<Integer, List<ShardStats>> entry : tmpIndexShards.entrySet()) {
            indexShards.put(entry.getKey(), new IndexShardStats(entry.getValue().get(0).shardRouting().shardId(), entry.getValue().toArray(new ShardStats[entry.getValue().size()])));
        }
        return indexShards;
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<IndexShardStats> iterator() {
        return indexShards().values().iterator();
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
}
