/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodesShutdownRequest.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.node.shutdown;

import java.io.IOException;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest;

/**
 * The Class NodesShutdownRequest.
 *
 * @author l.xue.nong
 */
public class NodesShutdownRequest extends MasterNodeOperationRequest {

	/** The nodes ids. */
	String[] nodesIds = Strings.EMPTY_ARRAY;

	/** The delay. */
	TimeValue delay = TimeValue.timeValueSeconds(1);

	/** The exit. */
	boolean exit = true;

	/**
	 * Instantiates a new nodes shutdown request.
	 */
	NodesShutdownRequest() {
	}

	/**
	 * Instantiates a new nodes shutdown request.
	 *
	 * @param nodesIds the nodes ids
	 */
	public NodesShutdownRequest(String... nodesIds) {
		this.nodesIds = nodesIds;
	}

	/**
	 * Nodes ids.
	 *
	 * @param nodesIds the nodes ids
	 * @return the nodes shutdown request
	 */
	public NodesShutdownRequest nodesIds(String... nodesIds) {
		this.nodesIds = nodesIds;
		return this;
	}

	/**
	 * Delay.
	 *
	 * @param delay the delay
	 * @return the nodes shutdown request
	 */
	public NodesShutdownRequest delay(TimeValue delay) {
		this.delay = delay;
		return this;
	}

	/**
	 * Delay.
	 *
	 * @return the time value
	 */
	public TimeValue delay() {
		return this.delay;
	}

	/**
	 * Delay.
	 *
	 * @param delay the delay
	 * @return the nodes shutdown request
	 */
	public NodesShutdownRequest delay(String delay) {
		return delay(TimeValue.parseTimeValue(delay, null));
	}

	/**
	 * Exit.
	 *
	 * @param exit the exit
	 * @return the nodes shutdown request
	 */
	public NodesShutdownRequest exit(boolean exit) {
		this.exit = exit;
		return this;
	}

	/**
	 * Exit.
	 *
	 * @return true, if successful
	 */
	public boolean exit() {
		return exit;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		delay = TimeValue.readTimeValue(in);
		int size = in.readVInt();
		if (size > 0) {
			nodesIds = new String[size];
			for (int i = 0; i < nodesIds.length; i++) {
				nodesIds[i] = in.readUTF();
			}
		}
		exit = in.readBoolean();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		delay.writeTo(out);
		if (nodesIds == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(nodesIds.length);
			for (String nodeId : nodesIds) {
				out.writeUTF(nodeId);
			}
		}
		out.writeBoolean(exit);
	}
}
