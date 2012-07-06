/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodesOperationRequest.java 2012-7-6 14:29:26 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support.nodes;

import java.io.IOException;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;

/**
 * The Class NodesOperationRequest.
 *
 * @author l.xue.nong
 */
public abstract class NodesOperationRequest implements ActionRequest {

	/** The all nodes. */
	public static String[] ALL_NODES = Strings.EMPTY_ARRAY;

	/** The nodes ids. */
	private String[] nodesIds;

	/** The listener threaded. */
	private boolean listenerThreaded = false;

	/** The timeout. */
	private TimeValue timeout;

	/**
	 * Instantiates a new nodes operation request.
	 */
	protected NodesOperationRequest() {

	}

	/**
	 * Instantiates a new nodes operation request.
	 *
	 * @param nodesIds the nodes ids
	 */
	protected NodesOperationRequest(String... nodesIds) {
		this.nodesIds = nodesIds;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#listenerThreaded(boolean)
	 */
	@Override
	public NodesOperationRequest listenerThreaded(boolean listenerThreaded) {
		this.listenerThreaded = listenerThreaded;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#listenerThreaded()
	 */
	@Override
	public boolean listenerThreaded() {
		return this.listenerThreaded;
	}

	/**
	 * Nodes ids.
	 *
	 * @return the string[]
	 */
	public String[] nodesIds() {
		return nodesIds;
	}

	/**
	 * Nodes ids.
	 *
	 * @param nodesIds the nodes ids
	 * @return the nodes operation request
	 */
	public NodesOperationRequest nodesIds(String... nodesIds) {
		this.nodesIds = nodesIds;
		return this;
	}

	/**
	 * Timeout.
	 *
	 * @return the time value
	 */
	public TimeValue timeout() {
		return this.timeout;
	}

	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the nodes operation request
	 */
	public NodesOperationRequest timeout(TimeValue timeout) {
		this.timeout = timeout;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		nodesIds = new String[in.readVInt()];
		for (int i = 0; i < nodesIds.length; i++) {
			nodesIds[i] = in.readUTF();
		}
		if (in.readBoolean()) {
			timeout = TimeValue.readTimeValue(in);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		if (nodesIds == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(nodesIds.length);
			for (String nodeId : nodesIds) {
				out.writeUTF(nodeId);
			}
		}
		if (timeout == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			timeout.writeTo(out);
		}
	}
}
