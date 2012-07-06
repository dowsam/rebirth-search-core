/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DeleteByQueryResponse.java 2012-3-29 15:02:40 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.deletebyquery;

import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.action.ActionResponse;


/**
 * The Class DeleteByQueryResponse.
 *
 * @author l.xue.nong
 */
public class DeleteByQueryResponse implements ActionResponse, Streamable, Iterable<IndexDeleteByQueryResponse> {

	
	/** The indices. */
	private Map<String, IndexDeleteByQueryResponse> indices = newHashMap();

	
	/**
	 * Instantiates a new delete by query response.
	 */
	DeleteByQueryResponse() {

	}

	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<IndexDeleteByQueryResponse> iterator() {
		return indices.values().iterator();
	}

	
	/**
	 * Indices.
	 *
	 * @return the map
	 */
	public Map<String, IndexDeleteByQueryResponse> indices() {
		return indices;
	}

	
	/**
	 * Gets the indices.
	 *
	 * @return the indices
	 */
	public Map<String, IndexDeleteByQueryResponse> getIndices() {
		return indices;
	}

	
	/**
	 * Index.
	 *
	 * @param index the index
	 * @return the index delete by query response
	 */
	public IndexDeleteByQueryResponse index(String index) {
		return indices.get(index);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		int size = in.readVInt();
		for (int i = 0; i < size; i++) {
			IndexDeleteByQueryResponse response = new IndexDeleteByQueryResponse();
			response.readFrom(in);
			indices.put(response.index(), response);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVInt(indices.size());
		for (IndexDeleteByQueryResponse indexResponse : indices.values()) {
			indexResponse.writeTo(out);
		}
	}
}
