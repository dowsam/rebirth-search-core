/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RiverClusterChangedEvent.java 2012-7-6 14:28:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.river.cluster;

/**
 * The Class RiverClusterChangedEvent.
 *
 * @author l.xue.nong
 */
public class RiverClusterChangedEvent {

	/** The source. */
	private final String source;

	/** The previous state. */
	private final RiverClusterState previousState;

	/** The state. */
	private final RiverClusterState state;

	/**
	 * Instantiates a new river cluster changed event.
	 *
	 * @param source the source
	 * @param state the state
	 * @param previousState the previous state
	 */
	public RiverClusterChangedEvent(String source, RiverClusterState state, RiverClusterState previousState) {
		this.source = source;
		this.state = state;
		this.previousState = previousState;
	}

	/**
	 * Source.
	 *
	 * @return the string
	 */
	public String source() {
		return this.source;
	}

	/**
	 * State.
	 *
	 * @return the river cluster state
	 */
	public RiverClusterState state() {
		return this.state;
	}

	/**
	 * Previous state.
	 *
	 * @return the river cluster state
	 */
	public RiverClusterState previousState() {
		return this.previousState;
	}
}
