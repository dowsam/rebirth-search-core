/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodesRestartRequest.java 2012-7-6 14:29:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.node.restart;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest;

/**
 * The Class NodesRestartRequest.
 *
 * @author l.xue.nong
 */
public class NodesRestartRequest extends NodesOperationRequest {

	/** The delay. */
	TimeValue delay = TimeValue.timeValueSeconds(1);

	/**
	 * Instantiates a new nodes restart request.
	 */
	protected NodesRestartRequest() {
	}

	/**
	 * Instantiates a new nodes restart request.
	 *
	 * @param nodesIds the nodes ids
	 */
	public NodesRestartRequest(String... nodesIds) {
		super(nodesIds);
	}

	/**
	 * Delay.
	 *
	 * @param delay the delay
	 * @return the nodes restart request
	 */
	public NodesRestartRequest delay(TimeValue delay) {
		this.delay = delay;
		return this;
	}

	/**
	 * Delay.
	 *
	 * @param delay the delay
	 * @return the nodes restart request
	 */
	public NodesRestartRequest delay(String delay) {
		return delay(TimeValue.parseTimeValue(delay, null));
	}

	/**
	 * Delay.
	 *
	 * @return the time value
	 */
	public TimeValue delay() {
		return this.delay;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		delay = TimeValue.readTimeValue(in);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		delay.writeTo(out);
	}
}