/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core OpenIndexRequest.java 2012-7-6 14:29:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.open;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest;

/**
 * The Class OpenIndexRequest.
 *
 * @author l.xue.nong
 */
public class OpenIndexRequest extends MasterNodeOperationRequest {

	/** The index. */
	private String index;

	/** The timeout. */
	private TimeValue timeout = TimeValue.timeValueSeconds(10);

	/**
	 * Instantiates a new open index request.
	 */
	OpenIndexRequest() {
	}

	/**
	 * Instantiates a new open index request.
	 *
	 * @param index the index
	 */
	public OpenIndexRequest(String index) {
		this.index = index;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;
		if (index == null) {
			validationException = ValidateActions.addValidationError("index is missing", validationException);
		}
		return validationException;
	}

	/**
	 * Index.
	 *
	 * @return the string
	 */
	String index() {
		return index;
	}

	/**
	 * Index.
	 *
	 * @param index the index
	 * @return the open index request
	 */
	public OpenIndexRequest index(String index) {
		this.index = index;
		return this;
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
	 * @return the open index request
	 */
	public OpenIndexRequest timeout(TimeValue timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the open index request
	 */
	public OpenIndexRequest timeout(String timeout) {
		return timeout(TimeValue.parseTimeValue(timeout, null));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		index = in.readUTF();
		timeout = TimeValue.readTimeValue(in);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeUTF(index);
		timeout.writeTo(out);
	}
}