/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CloseIndexRequest.java 2012-3-29 15:01:31 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.close;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest;


/**
 * The Class CloseIndexRequest.
 *
 * @author l.xue.nong
 */
public class CloseIndexRequest extends MasterNodeOperationRequest {

	
	/** The index. */
	private String index;

	
	/** The timeout. */
	private TimeValue timeout = TimeValue.timeValueSeconds(10);

	
	/**
	 * Instantiates a new close index request.
	 */
	CloseIndexRequest() {
	}

	
	/**
	 * Instantiates a new close index request.
	 *
	 * @param index the index
	 */
	public CloseIndexRequest(String index) {
		this.index = index;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#validate()
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
	 * @return the close index request
	 */
	public CloseIndexRequest index(String index) {
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
	 * @return the close index request
	 */
	public CloseIndexRequest timeout(TimeValue timeout) {
		this.timeout = timeout;
		return this;
	}

	
	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the close index request
	 */
	public CloseIndexRequest timeout(String timeout) {
		return timeout(TimeValue.parseTimeValue(timeout, null));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		index = in.readUTF();
		timeout = TimeValue.readTimeValue(in);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeUTF(index);
		timeout.writeTo(out);
	}
}