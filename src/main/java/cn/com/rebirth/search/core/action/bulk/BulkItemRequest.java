/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BulkItemRequest.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.bulk;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.delete.DeleteRequest;
import cn.com.rebirth.search.core.action.index.IndexRequest;

/**
 * The Class BulkItemRequest.
 *
 * @author l.xue.nong
 */
public class BulkItemRequest implements Streamable {

	/** The id. */
	private int id;

	/** The request. */
	private ActionRequest request;

	/**
	 * Instantiates a new bulk item request.
	 */
	BulkItemRequest() {

	}

	/**
	 * Instantiates a new bulk item request.
	 *
	 * @param id the id
	 * @param request the request
	 */
	public BulkItemRequest(int id, ActionRequest request) {
		this.id = id;
		this.request = request;
	}

	/**
	 * Id.
	 *
	 * @return the int
	 */
	public int id() {
		return id;
	}

	/**
	 * Request.
	 *
	 * @return the action request
	 */
	public ActionRequest request() {
		return request;
	}

	/**
	 * Read bulk item.
	 *
	 * @param in the in
	 * @return the bulk item request
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static BulkItemRequest readBulkItem(StreamInput in) throws IOException {
		BulkItemRequest item = new BulkItemRequest();
		item.readFrom(in);
		return item;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		id = in.readVInt();
		byte type = in.readByte();
		if (type == 0) {
			request = new IndexRequest();
		} else if (type == 1) {
			request = new DeleteRequest();
		}
		request.readFrom(in);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVInt(id);
		if (request instanceof IndexRequest) {
			out.writeByte((byte) 0);
		} else if (request instanceof DeleteRequest) {
			out.writeByte((byte) 1);
		}
		request.writeTo(out);
	}
}
