/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MultiGetItemResponse.java 2012-3-29 15:02:32 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.get;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;


/**
 * The Class MultiGetItemResponse.
 *
 * @author l.xue.nong
 */
public class MultiGetItemResponse implements Streamable {

	
	/** The response. */
	private GetResponse response;

	
	/** The failure. */
	private MultiGetResponse.Failure failure;

	
	/**
	 * Instantiates a new multi get item response.
	 */
	MultiGetItemResponse() {

	}

	
	/**
	 * Instantiates a new multi get item response.
	 *
	 * @param response the response
	 * @param failure the failure
	 */
	public MultiGetItemResponse(GetResponse response, MultiGetResponse.Failure failure) {
		this.response = response;
		this.failure = failure;
	}

	
	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		if (failure != null) {
			return failure.index();
		}
		return response.index();
	}

	
	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public String getIndex() {
		return index();
	}

	
	/**
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		if (failure != null) {
			return failure.type();
		}
		return response.type();
	}

	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type();
	}

	
	/**
	 * Id.
	 *
	 * @return the string
	 */
	public String id() {
		if (failure != null) {
			return failure.id();
		}
		return response.id();
	}

	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id();
	}

	
	/**
	 * Failed.
	 *
	 * @return true, if successful
	 */
	public boolean failed() {
		return failure != null;
	}

	
	/**
	 * Checks if is failed.
	 *
	 * @return true, if is failed
	 */
	public boolean isFailed() {
		return failed();
	}

	
	/**
	 * Response.
	 *
	 * @return the gets the response
	 */
	public GetResponse response() {
		return this.response;
	}

	
	/**
	 * Gets the response.
	 *
	 * @return the response
	 */
	public GetResponse getResponse() {
		return this.response;
	}

	
	/**
	 * Failure.
	 *
	 * @return the multi get response. failure
	 */
	public MultiGetResponse.Failure failure() {
		return this.failure;
	}

	
	/**
	 * Gets the failure.
	 *
	 * @return the failure
	 */
	public MultiGetResponse.Failure getFailure() {
		return failure();
	}

	
	/**
	 * Read item response.
	 *
	 * @param in the in
	 * @return the multi get item response
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static MultiGetItemResponse readItemResponse(StreamInput in) throws IOException {
		MultiGetItemResponse response = new MultiGetItemResponse();
		response.readFrom(in);
		return response;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		if (in.readBoolean()) {
			failure = MultiGetResponse.Failure.readFailure(in);
		} else {
			response = new GetResponse();
			response.readFrom(in);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		if (failure != null) {
			out.writeBoolean(true);
			failure.writeTo(out);
		} else {
			out.writeBoolean(false);
			response.writeTo(out);
		}
	}
}