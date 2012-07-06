/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MultiGetShardResponse.java 2012-3-29 15:01:49 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.get;

import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionResponse;


/**
 * The Class MultiGetShardResponse.
 *
 * @author l.xue.nong
 */
public class MultiGetShardResponse implements ActionResponse {

	
	/** The locations. */
	TIntArrayList locations;

	
	/** The responses. */
	List<GetResponse> responses;

	
	/** The failures. */
	List<MultiGetResponse.Failure> failures;

	
	/**
	 * Instantiates a new multi get shard response.
	 */
	MultiGetShardResponse() {
		locations = new TIntArrayList();
		responses = new ArrayList<GetResponse>();
		failures = new ArrayList<MultiGetResponse.Failure>();
	}

	
	/**
	 * Adds the.
	 *
	 * @param location the location
	 * @param response the response
	 */
	public void add(int location, GetResponse response) {
		locations.add(location);
		responses.add(response);
		failures.add(null);
	}

	
	/**
	 * Adds the.
	 *
	 * @param location the location
	 * @param failure the failure
	 */
	public void add(int location, MultiGetResponse.Failure failure) {
		locations.add(location);
		responses.add(null);
		failures.add(failure);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		int size = in.readVInt();
		locations = new TIntArrayList(size);
		responses = new ArrayList<GetResponse>(size);
		failures = new ArrayList<MultiGetResponse.Failure>(size);
		for (int i = 0; i < size; i++) {
			locations.add(in.readVInt());
			if (in.readBoolean()) {
				GetResponse response = new GetResponse();
				response.readFrom(in);
				responses.add(response);
			} else {
				responses.add(null);
			}
			if (in.readBoolean()) {
				failures.add(MultiGetResponse.Failure.readFailure(in));
			} else {
				failures.add(null);
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVInt(locations.size());
		for (int i = 0; i < locations.size(); i++) {
			out.writeVInt(locations.get(i));
			if (responses.get(i) == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				responses.get(i).writeTo(out);
			}
			if (failures.get(i) == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				failures.get(i).writeTo(out);
			}
		}
	}
}