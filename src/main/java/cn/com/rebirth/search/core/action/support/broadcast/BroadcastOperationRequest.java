/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BroadcastOperationRequest.java 2012-7-6 14:29:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support.broadcast;

import java.io.IOException;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;

/**
 * The Class BroadcastOperationRequest.
 *
 * @author l.xue.nong
 */
public abstract class BroadcastOperationRequest implements ActionRequest {

	/** The indices. */
	protected String[] indices;

	/** The listener threaded. */
	private boolean listenerThreaded = false;

	/** The operation threading. */
	private BroadcastOperationThreading operationThreading = BroadcastOperationThreading.SINGLE_THREAD;

	/**
	 * Instantiates a new broadcast operation request.
	 */
	protected BroadcastOperationRequest() {

	}

	/**
	 * Instantiates a new broadcast operation request.
	 *
	 * @param indices the indices
	 */
	protected BroadcastOperationRequest(String[] indices) {
		this.indices = indices;
	}

	/**
	 * Indices.
	 *
	 * @return the string[]
	 */
	public String[] indices() {
		return indices;
	}

	/**
	 * Indices.
	 *
	 * @param indices the indices
	 * @return the broadcast operation request
	 */
	public BroadcastOperationRequest indices(String... indices) {
		this.indices = indices;
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
	 * @see cn.com.rebirth.search.core.action.ActionRequest#listenerThreaded()
	 */
	@Override
	public boolean listenerThreaded() {
		return this.listenerThreaded;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#listenerThreaded(boolean)
	 */
	@Override
	public BroadcastOperationRequest listenerThreaded(boolean listenerThreaded) {
		this.listenerThreaded = listenerThreaded;
		return this;
	}

	/**
	 * Operation threading.
	 *
	 * @return the broadcast operation threading
	 */
	public BroadcastOperationThreading operationThreading() {
		return operationThreading;
	}

	/**
	 * Operation threading.
	 *
	 * @param operationThreading the operation threading
	 * @return the broadcast operation request
	 */
	public BroadcastOperationRequest operationThreading(BroadcastOperationThreading operationThreading) {
		this.operationThreading = operationThreading;
		return this;
	}

	/**
	 * Operation threading.
	 *
	 * @param operationThreading the operation threading
	 * @return the broadcast operation request
	 */
	public BroadcastOperationRequest operationThreading(String operationThreading) {
		return operationThreading(BroadcastOperationThreading.fromString(operationThreading, this.operationThreading));
	}

	/**
	 * Before start.
	 */
	protected void beforeStart() {

	}

	/**
	 * Before local fork.
	 */
	protected void beforeLocalFork() {

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		if (indices == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(indices.length);
			for (String index : indices) {
				out.writeUTF(index);
			}
		}
		out.writeByte(operationThreading.id());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		int size = in.readVInt();
		if (size == 0) {
			indices = Strings.EMPTY_ARRAY;
		} else {
			indices = new String[size];
			for (int i = 0; i < indices.length; i++) {
				indices[i] = in.readUTF();
			}
		}
		operationThreading = BroadcastOperationThreading.fromId(in.readByte());
	}
}
