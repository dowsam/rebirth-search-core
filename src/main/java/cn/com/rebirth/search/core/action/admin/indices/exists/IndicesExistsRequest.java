/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndicesExistsRequest.java 2012-3-29 15:01:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.exists;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest;


/**
 * The Class IndicesExistsRequest.
 *
 * @author l.xue.nong
 */
public class IndicesExistsRequest extends MasterNodeOperationRequest {

	
	/** The indices. */
	private String[] indices;

	
	/**
	 * Instantiates a new indices exists request.
	 *
	 * @param indices the indices
	 */
	public IndicesExistsRequest(String... indices) {
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
	 */
	public void indices(String[] indices) {
		this.indices = indices;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;
		if (indices == null || indices.length == 0) {
			validationException = ValidateActions.addValidationError("index/indices is missing", validationException);
		}
		return validationException;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		indices = new String[in.readVInt()];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = in.readUTF();
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeVInt(indices.length);
		for (String index : indices) {
			out.writeUTF(index);
		}
	}
}