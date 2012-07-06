/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MutableShardRouting.java 2012-3-29 15:02:51 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing;


/**
 * The Class MutableShardRouting.
 *
 * @author l.xue.nong
 */
public class MutableShardRouting extends ImmutableShardRouting {

    /**
     * Instantiates a new mutable shard routing.
     *
     * @param copy the copy
     */
    public MutableShardRouting(ShardRouting copy) {
        super(copy);
    }

    /**
     * Instantiates a new mutable shard routing.
     *
     * @param copy the copy
     * @param version the version
     */
    public MutableShardRouting(ShardRouting copy, long version) {
        super(copy);
        this.version = version;
    }

    /**
     * Instantiates a new mutable shard routing.
     *
     * @param index the index
     * @param shardId the shard id
     * @param currentNodeId the current node id
     * @param primary the primary
     * @param state the state
     * @param version the version
     */
    public MutableShardRouting(String index, int shardId, String currentNodeId, boolean primary, ShardRoutingState state, long version) {
        super(index, shardId, currentNodeId, primary, state, version);
    }

    /**
     * Instantiates a new mutable shard routing.
     *
     * @param index the index
     * @param shardId the shard id
     * @param currentNodeId the current node id
     * @param relocatingNodeId the relocating node id
     * @param primary the primary
     * @param state the state
     * @param version the version
     */
    public MutableShardRouting(String index, int shardId, String currentNodeId,
                               String relocatingNodeId, boolean primary, ShardRoutingState state, long version) {
        super(index, shardId, currentNodeId, relocatingNodeId, primary, state, version);
    }

    /**
     * Assign to node.
     *
     * @param nodeId the node id
     */
    public void assignToNode(String nodeId) {
        version++;
        if (currentNodeId == null) {
            assert state == ShardRoutingState.UNASSIGNED;

            state = ShardRoutingState.INITIALIZING;
            currentNodeId = nodeId;
            relocatingNodeId = null;
        } else if (state == ShardRoutingState.STARTED) {
            state = ShardRoutingState.RELOCATING;
            relocatingNodeId = nodeId;
        } else if (state == ShardRoutingState.RELOCATING) {
            assert nodeId.equals(relocatingNodeId);
        }
    }

    /**
     * Relocate.
     *
     * @param relocatingNodeId the relocating node id
     */
    public void relocate(String relocatingNodeId) {
        version++;
        assert state == ShardRoutingState.STARTED;
        state = ShardRoutingState.RELOCATING;
        this.relocatingNodeId = relocatingNodeId;
    }

    /**
     * Cancel relocation.
     */
    public void cancelRelocation() {
        version++;
        assert state == ShardRoutingState.RELOCATING;
        assert assignedToNode();
        assert relocatingNodeId != null;

        state = ShardRoutingState.STARTED;
        relocatingNodeId = null;
    }

    /**
     * Deassign node.
     */
    public void deassignNode() {
        version++;
        assert state != ShardRoutingState.UNASSIGNED;

        state = ShardRoutingState.UNASSIGNED;
        this.currentNodeId = null;
        this.relocatingNodeId = null;
    }

    /**
     * Move to started.
     */
    public void moveToStarted() {
        version++;
        assert state == ShardRoutingState.INITIALIZING || state == ShardRoutingState.RELOCATING;
        relocatingNodeId = null;
        state = ShardRoutingState.STARTED;
    }

    /**
     * Move to primary.
     */
    public void moveToPrimary() {
        version++;
        if (primary) {
            throw new IllegalShardRoutingStateException(this, "Already primary, can't move to primary");
        }
        primary = true;
    }

    /**
     * Move from primary.
     */
    public void moveFromPrimary() {
        version++;
        if (!primary) {
            throw new IllegalShardRoutingStateException(this, "Already primary, can't move to replica");
        }
        primary = false;
    }
}

