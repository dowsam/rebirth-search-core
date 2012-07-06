/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PercolateResponse.java 2012-3-29 15:00:57 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.percolate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionResponse;


/**
 * The Class PercolateResponse.
 *
 * @author l.xue.nong
 */
public class PercolateResponse implements ActionResponse, Iterable<String> {

	
	/** The matches. */
	private List<String> matches;

	
	/**
	 * Instantiates a new percolate response.
	 */
	PercolateResponse() {

	}

	
	/**
	 * Instantiates a new percolate response.
	 *
	 * @param matches the matches
	 */
	public PercolateResponse(List<String> matches) {
		this.matches = matches;
	}

	
	/**
	 * Matches.
	 *
	 * @return the list
	 */
	public List<String> matches() {
		return this.matches;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<String> iterator() {
		return matches.iterator();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		int size = in.readVInt();
		matches = new ArrayList<String>(size);
		for (int i = 0; i < size; i++) {
			matches.add(in.readUTF());
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVInt(matches.size());
		for (String match : matches) {
			out.writeUTF(match);
		}
	}
}
