/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PutMappingRequest.java 2012-7-6 14:29:00 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.mapping.put;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentFactory;
import cn.com.rebirth.commons.xcontent.XContentType;
import cn.com.rebirth.search.commons.Required;
import cn.com.rebirth.search.core.RestartGenerationException;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest;

/**
 * The Class PutMappingRequest.
 *
 * @author l.xue.nong
 */
public class PutMappingRequest extends MasterNodeOperationRequest {

	/** The indices. */
	private String[] indices;

	/** The mapping type. */
	private String mappingType;

	/** The mapping source. */
	private String mappingSource;

	/** The timeout. */
	private TimeValue timeout = new TimeValue(10, TimeUnit.SECONDS);

	/** The ignore conflicts. */
	private boolean ignoreConflicts = false;

	/**
	 * Instantiates a new put mapping request.
	 */
	PutMappingRequest() {
	}

	/**
	 * Instantiates a new put mapping request.
	 *
	 * @param indices the indices
	 */
	public PutMappingRequest(String... indices) {
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
		if (mappingSource == null) {
			validationException = ValidateActions.addValidationError("mapping source is missing", validationException);
		}
		return validationException;
	}

	/**
	 * Indices.
	 *
	 * @param indices the indices
	 * @return the put mapping request
	 */
	public PutMappingRequest indices(String[] indices) {
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
	 * @return the put mapping request
	 */
	@Required
	public PutMappingRequest type(String mappingType) {
		this.mappingType = mappingType;
		return this;
	}

	/**
	 * Source.
	 *
	 * @return the string
	 */
	String source() {
		return mappingSource;
	}

	/**
	 * Source.
	 *
	 * @param mappingBuilder the mapping builder
	 * @return the put mapping request
	 */
	@Required
	public PutMappingRequest source(XContentBuilder mappingBuilder) {
		try {
			return source(mappingBuilder.string());
		} catch (IOException e) {
			throw new RebirthIllegalArgumentException("Failed to build json for mapping request", e);
		}
	}

	/**
	 * Source.
	 *
	 * @param mappingSource the mapping source
	 * @return the put mapping request
	 */
	@Required
	public PutMappingRequest source(Map mappingSource) {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
			builder.map(mappingSource);
			return source(builder.string());
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + mappingSource + "]", e);
		}
	}

	/**
	 * Source.
	 *
	 * @param mappingSource the mapping source
	 * @return the put mapping request
	 */
	@Required
	public PutMappingRequest source(String mappingSource) {
		this.mappingSource = mappingSource;
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
	 * @return the put mapping request
	 */
	public PutMappingRequest timeout(TimeValue timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the put mapping request
	 */
	public PutMappingRequest timeout(String timeout) {
		return timeout(TimeValue.parseTimeValue(timeout, null));
	}

	/**
	 * Ignore conflicts.
	 *
	 * @return true, if successful
	 */
	public boolean ignoreConflicts() {
		return ignoreConflicts;
	}

	/**
	 * Ignore conflicts.
	 *
	 * @param ignoreDuplicates the ignore duplicates
	 * @return the put mapping request
	 */
	public PutMappingRequest ignoreConflicts(boolean ignoreDuplicates) {
		this.ignoreConflicts = ignoreDuplicates;
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
		mappingSource = in.readUTF();
		timeout = TimeValue.readTimeValue(in);
		ignoreConflicts = in.readBoolean();
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
		out.writeUTF(mappingSource);
		timeout.writeTo(out);
		out.writeBoolean(ignoreConflicts);
	}
}