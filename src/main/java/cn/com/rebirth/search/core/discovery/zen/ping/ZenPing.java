/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ZenPing.java 2012-7-6 14:29:03 l.xue.nong$$
 */

package cn.com.rebirth.search.core.discovery.zen.ping;

import static cn.com.rebirth.search.core.cluster.ClusterName.readClusterName;
import static cn.com.rebirth.search.core.cluster.node.DiscoveryNode.readNode;

import java.io.IOException;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.LifecycleComponent;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.discovery.zen.DiscoveryNodesProvider;

/**
 * The Interface ZenPing.
 *
 * @author l.xue.nong
 */
public interface ZenPing extends LifecycleComponent<ZenPing> {

	/**
	 * Sets the nodes provider.
	 *
	 * @param nodesProvider the new nodes provider
	 */
	void setNodesProvider(DiscoveryNodesProvider nodesProvider);

	/**
	 * Ping.
	 *
	 * @param listener the listener
	 * @param timeout the timeout
	 * @throws RebirthException the rebirth exception
	 */
	void ping(PingListener listener, TimeValue timeout) throws RebirthException;

	/**
	 * The listener interface for receiving ping events.
	 * The class that is interested in processing a ping
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addPingListener<code> method. When
	 * the ping event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see PingEvent
	 */
	public interface PingListener {

		/**
		 * On ping.
		 *
		 * @param pings the pings
		 */
		void onPing(PingResponse[] pings);
	}

	/**
	 * The Class PingResponse.
	 *
	 * @author l.xue.nong
	 */
	public class PingResponse implements Streamable {

		/** The cluster name. */
		private ClusterName clusterName;

		/** The target. */
		private DiscoveryNode target;

		/** The master. */
		private DiscoveryNode master;

		/**
		 * Instantiates a new ping response.
		 */
		private PingResponse() {
		}

		/**
		 * Instantiates a new ping response.
		 *
		 * @param target the target
		 * @param master the master
		 * @param clusterName the cluster name
		 */
		public PingResponse(DiscoveryNode target, DiscoveryNode master, ClusterName clusterName) {
			this.target = target;
			this.master = master;
			this.clusterName = clusterName;
		}

		/**
		 * Cluster name.
		 *
		 * @return the cluster name
		 */
		public ClusterName clusterName() {
			return this.clusterName;
		}

		/**
		 * Target.
		 *
		 * @return the discovery node
		 */
		public DiscoveryNode target() {
			return target;
		}

		/**
		 * Master.
		 *
		 * @return the discovery node
		 */
		public DiscoveryNode master() {
			return master;
		}

		/**
		 * Read ping response.
		 *
		 * @param in the in
		 * @return the ping response
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static PingResponse readPingResponse(StreamInput in) throws IOException {
			PingResponse response = new PingResponse();
			response.readFrom(in);
			return response;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			clusterName = readClusterName(in);
			target = readNode(in);
			if (in.readBoolean()) {
				master = readNode(in);
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			clusterName.writeTo(out);
			target.writeTo(out);
			if (master == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				master.writeTo(out);
			}
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "ping_response{target [" + target + "], master [" + master + "], cluster_name["
					+ clusterName.value() + "]}";
		}
	}
}
