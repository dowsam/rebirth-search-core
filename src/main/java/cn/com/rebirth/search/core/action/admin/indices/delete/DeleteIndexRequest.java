/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DeleteIndexRequest.java 2012-7-6 14:30:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.delete;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest;

/**
 * The Class DeleteIndexRequest.
 *
 * @author l.xue.nong
 */
public class DeleteIndexRequest extends MasterNodeOperationRequest {

	/** The indices. */
	private String[] indices;

	/** The timeout. */
	private TimeValue timeout = TimeValue.timeValueSeconds(10);

	/**
	 * Instantiates a new delete index request.
	 */
	DeleteIndexRequest() {
	}

	/**
	 * Instantiates a new delete index request.
	 *
	 * @param index the index
	 */
	public DeleteIndexRequest(String index) {
		this.indices = new String[] { index };
	}

	/**
	 * Instantiates a new delete index request.
	 *
	 * @param indices the indices
	 */
	public DeleteIndexRequest(String... indices) {
		this.indices = indices;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;
		if (indices == null) {
			validationException = ValidateActions.addValidationError("index / indices is missing", validationException);
		}
		return validationException;
	}

	/**
	 * Indices.
	 *
	 * @param indices the indices
	 * @return the delete index request
	 */
	public DeleteIndexRequest indices(String... indices) {
		this.indices = indices;
		return this;
	}

	/**
	 * Indices.
	 *
	 * @return the string[]
	 */
	String[] indices() {
		return indices;
	}

	/**
	 * Timeout.
	 *
	 * @return the time value
	 */
	TimeValue timeout() {
		return timeout;
	}

	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the delete index request
	 */
	public DeleteIndexRequest timeout(TimeValue timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the delete index request
	 */
	public DeleteIndexRequest timeout(String timeout) {
		return timeout(TimeValue.parseTimeValue(timeout, null));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		indices = new String[in.readVInt()];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = in.readUTF();
		}
		timeout = TimeValue.readTimeValue(in);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		if (indices == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(indices.length);
			for (String index : indices) {
				out.writeUTF(index);
			}
		}
		timeout.writeTo(out);
	}
}