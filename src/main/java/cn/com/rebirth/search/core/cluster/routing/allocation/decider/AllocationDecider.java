/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AllocationDecider.java 2012-7-6 14:30:31 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing.allocation.decider;

import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.core.cluster.routing.RoutingNode;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.cluster.routing.allocation.RoutingAllocation;

/**
 * The Class AllocationDecider.
 *
 * @author l.xue.nong
 */
public abstract class AllocationDecider extends AbstractComponent {

	/**
	 * The Enum Decision.
	 *
	 * @author l.xue.nong
	 */
	public static enum Decision {

		/** The yes. */
		YES {
			@Override
			public boolean allocate() {
				return true;
			}
		},

		/** The no. */
		NO {
			@Override
			public boolean allocate() {
				return false;
			}
		},

		/** The throttle. */
		THROTTLE {
			@Override
			public boolean allocate() {
				return false;
			}
		};

		/**
		 * Allocate.
		 *
		 * @return true, if successful
		 */
		public abstract boolean allocate();
	}

	/**
	 * Instantiates a new allocation decider.
	 *
	 * @param settings the settings
	 */
	protected AllocationDecider(Settings settings) {
		super(settings);
	}

	/**
	 * Can rebalance.
	 *
	 * @param shardRouting the shard routing
	 * @param allocation the allocation
	 * @return true, if successful
	 */
	public boolean canRebalance(ShardRouting shardRouting, RoutingAllocation allocation) {
		return true;
	}

	/**
	 * Can allocate.
	 *
	 * @param shardRouting the shard routing
	 * @param node the node
	 * @param allocation the allocation
	 * @return the decision
	 */
	public Decision canAllocate(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
		return Decision.YES;
	}

	/**
	 * Can remain.
	 *
	 * @param shardRouting the shard routing
	 * @param node the node
	 * @param allocation the allocation
	 * @return true, if successful
	 */
	public boolean canRemain(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
		return true;
	}
}
