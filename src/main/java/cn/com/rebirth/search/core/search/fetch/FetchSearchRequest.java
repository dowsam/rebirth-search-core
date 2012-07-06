/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FetchSearchRequest.java 2012-7-6 14:28:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.fetch;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.trove.ExtTIntArrayList;

/**
 * The Class FetchSearchRequest.
 *
 * @author l.xue.nong
 */
public class FetchSearchRequest implements Streamable {

	/** The id. */
	private long id;

	/** The doc ids. */
	private int[] docIds;

	/** The size. */
	private int size;

	/**
	 * Instantiates a new fetch search request.
	 */
	public FetchSearchRequest() {
	}

	/**
	 * Instantiates a new fetch search request.
	 *
	 * @param id the id
	 * @param list the list
	 */
	public FetchSearchRequest(long id, ExtTIntArrayList list) {
		this.id = id;
		this.docIds = list.unsafeArray();
		this.size = list.size();
	}

	/**
	 * Instantiates a new fetch search request.
	 *
	 * @param id the id
	 * @param docIds the doc ids
	 */
	public FetchSearchRequest(long id, int[] docIds) {
		this.id = id;
		this.docIds = docIds;
		this.size = docIds.length;
	}

	/**
	 * Id.
	 *
	 * @return the long
	 */
	public long id() {
		return id;
	}

	/**
	 * Doc ids.
	 *
	 * @return the int[]
	 */
	public int[] docIds() {
		return docIds;
	}

	/**
	 * Doc ids size.
	 *
	 * @return the int
	 */
	public int docIdsSize() {
		return size;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		id = in.readLong();
		size = in.readVInt();
		docIds = new int[size];
		for (int i = 0; i < size; i++) {
			docIds[i] = in.readVInt();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeLong(id);
		out.writeVInt(size);
		for (int i = 0; i < size; i++) {
			out.writeVInt(docIds[i]);
		}
	}
}
