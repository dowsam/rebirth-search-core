/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BulkResponse.java 2012-3-29 15:02:21 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.bulk;

import java.io.IOException;
import java.util.Iterator;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionResponse;

import com.google.common.collect.Iterators;


/**
 * The Class BulkResponse.
 *
 * @author l.xue.nong
 */
public class BulkResponse implements ActionResponse, Iterable<BulkItemResponse> {

	
	/** The responses. */
	private BulkItemResponse[] responses;

	
	/** The took in millis. */
	private long tookInMillis;

	
	/**
	 * Instantiates a new bulk response.
	 */
	BulkResponse() {
	}

	
	/**
	 * Instantiates a new bulk response.
	 *
	 * @param responses the responses
	 * @param tookInMillis the took in millis
	 */
	public BulkResponse(BulkItemResponse[] responses, long tookInMillis) {
		this.responses = responses;
		this.tookInMillis = tookInMillis;
	}

	
	/**
	 * Took.
	 *
	 * @return the time value
	 */
	public TimeValue took() {
		return new TimeValue(tookInMillis);
	}

	
	/**
	 * Gets the took.
	 *
	 * @return the took
	 */
	public TimeValue getTook() {
		return took();
	}

	
	/**
	 * Took in millis.
	 *
	 * @return the long
	 */
	public long tookInMillis() {
		return tookInMillis;
	}

	
	/**
	 * Gets the took in millis.
	 *
	 * @return the took in millis
	 */
	public long getTookInMillis() {
		return tookInMillis();
	}

	
	/**
	 * Checks for failures.
	 *
	 * @return true, if successful
	 */
	public boolean hasFailures() {
		for (BulkItemResponse response : responses) {
			if (response.failed()) {
				return true;
			}
		}
		return false;
	}

	
	/**
	 * Builds the failure message.
	 *
	 * @return the string
	 */
	public String buildFailureMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("failure in bulk execution:");
		for (int i = 0; i < responses.length; i++) {
			BulkItemResponse response = responses[i];
			if (response.failed()) {
				sb.append("\n[").append(i).append("]: index [").append(response.index()).append("], type [")
						.append(response.type()).append("], id [").append(response.id()).append("], message [")
						.append(response.failureMessage()).append("]");
			}
		}
		return sb.toString();
	}

	
	/**
	 * Items.
	 *
	 * @return the bulk item response[]
	 */
	public BulkItemResponse[] items() {
		return responses;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<BulkItemResponse> iterator() {
		return Iterators.forArray(responses);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		responses = new BulkItemResponse[in.readVInt()];
		for (int i = 0; i < responses.length; i++) {
			responses[i] = BulkItemResponse.readBulkItem(in);
		}
		tookInMillis = in.readVLong();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVInt(responses.length);
		for (BulkItemResponse response : responses) {
			response.writeTo(out);
		}
		out.writeVLong(tookInMillis);
	}
}
