/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DeleteIndexTemplateRequest.java 2012-3-29 15:01:41 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.template.delete;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest;


/**
 * The Class DeleteIndexTemplateRequest.
 *
 * @author l.xue.nong
 */
public class DeleteIndexTemplateRequest extends MasterNodeOperationRequest {

	
	/** The name. */
	private String name;

	
	/** The timeout. */
	private TimeValue timeout = TimeValue.timeValueSeconds(10);

	
	/**
	 * Instantiates a new delete index template request.
	 */
	DeleteIndexTemplateRequest() {
	}

	
	/**
	 * Instantiates a new delete index template request.
	 *
	 * @param name the name
	 */
	public DeleteIndexTemplateRequest(String name) {
		this.name = name;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;
		if (name == null) {
			validationException = ValidateActions.addValidationError("name is missing", validationException);
		}
		return validationException;
	}

	
	/**
	 * Name.
	 *
	 * @return the string
	 */
	String name() {
		return name;
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
	 * @return the delete index template request
	 */
	public DeleteIndexTemplateRequest timeout(TimeValue timeout) {
		this.timeout = timeout;
		return this;
	}

	
	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the delete index template request
	 */
	public DeleteIndexTemplateRequest timeout(String timeout) {
		return timeout(TimeValue.parseTimeValue(timeout, null));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		name = in.readUTF();
		timeout = TimeValue.readTimeValue(in);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeUTF(name);
		timeout.writeTo(out);
	}
}