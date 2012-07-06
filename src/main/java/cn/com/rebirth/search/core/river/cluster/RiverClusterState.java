/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RiverClusterState.java 2012-3-29 15:00:44 l.xue.nong$$
 */


package cn.com.rebirth.search.core.river.cluster;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.river.routing.RiversRouting;


/**
 * The Class RiverClusterState.
 *
 * @author l.xue.nong
 */
public class RiverClusterState {

	
	/** The version. */
	private final long version;

	
	/** The routing. */
	private final RiversRouting routing;

	
	/**
	 * Instantiates a new river cluster state.
	 *
	 * @param version the version
	 * @param state the state
	 */
	public RiverClusterState(long version, RiverClusterState state) {
		this.version = version;
		this.routing = state.routing();
	}

	
	/**
	 * Instantiates a new river cluster state.
	 *
	 * @param version the version
	 * @param routing the routing
	 */
	RiverClusterState(long version, RiversRouting routing) {
		this.version = version;
		this.routing = routing;
	}

	
	/**
	 * Version.
	 *
	 * @return the long
	 */
	public long version() {
		return this.version;
	}

	
	/**
	 * Routing.
	 *
	 * @return the rivers routing
	 */
	public RiversRouting routing() {
		return routing;
	}

	
	/**
	 * Builder.
	 *
	 * @return the builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	
	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder {

		
		/** The version. */
		private long version = 0;

		
		/** The routing. */
		private RiversRouting routing = RiversRouting.EMPTY;

		
		/**
		 * State.
		 *
		 * @param state the state
		 * @return the builder
		 */
		public Builder state(RiverClusterState state) {
			this.version = state.version();
			this.routing = state.routing();
			return this;
		}

		
		/**
		 * Routing.
		 *
		 * @param builder the builder
		 * @return the builder
		 */
		public Builder routing(RiversRouting.Builder builder) {
			return routing(builder.build());
		}

		
		/**
		 * Routing.
		 *
		 * @param routing the routing
		 * @return the builder
		 */
		public Builder routing(RiversRouting routing) {
			this.routing = routing;
			return this;
		}

		
		/**
		 * Builds the.
		 *
		 * @return the river cluster state
		 */
		public RiverClusterState build() {
			return new RiverClusterState(version, routing);
		}

		
		/**
		 * Read from.
		 *
		 * @param in the in
		 * @return the river cluster state
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static RiverClusterState readFrom(StreamInput in) throws IOException {
			Builder builder = new Builder();
			builder.version = in.readVLong();
			builder.routing = RiversRouting.Builder.readFrom(in);
			return builder.build();
		}

		
		/**
		 * Write to.
		 *
		 * @param clusterState the cluster state
		 * @param out the out
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void writeTo(RiverClusterState clusterState, StreamOutput out) throws IOException {
			out.writeVLong(clusterState.version);
			RiversRouting.Builder.writeTo(clusterState.routing, out);
		}
	}
}
