/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DeleteMappingRequest.java 2012-7-6 14:28:44 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.mapping.delete;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest;

/**
 * The Class DeleteMappingRequest.
 *
 * @author l.xue.nong
 */
public class DeleteMappingRequest extends MasterNodeOperationRequest {

	/** The indices. */
	private String[] indices;

	/** The mapping type. */
	private String mappingType;

	/**
	 * Instantiates a new delete mapping request.
	 */
	DeleteMappingRequest() {
	}

	/**
	 * Instantiates a new delete mapping request.
	 *
	 * @param indices the indices
	 */
	public DeleteMappingRequest(String... indices) {
		this.indices = indices;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;
		if (mappingType == null) {
			validationException = ValidateActions.addValidationError("mapping type is missing", validationException);
		}
		return validationException;
	}

	/**
	 * Indices.
	 *
	 * @param indices the indices
	 * @return the delete mapping request
	 */
	public DeleteMappingRequest indices(String[] indices) {
		this.indices = indices;
		return this;
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
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		return mappingType;
	}

	/**
	 * Type.
	 *
	 * @param mappingType the mapping type
	 * @return the delete mapping request
	 */
	public DeleteMappingRequest type(String mappingType) {
		this.mappingType = mappingType;
		return this;
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
		if (in.readBoolean()) {
			mappingType = in.readUTF();
		}
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
		if (mappingType == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(mappingType);
		}
	}
}