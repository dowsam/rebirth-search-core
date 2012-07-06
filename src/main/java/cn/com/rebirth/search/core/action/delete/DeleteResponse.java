/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DeleteResponse.java 2012-3-29 15:02:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.delete;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.action.ActionResponse;


/**
 * The Class DeleteResponse.
 *
 * @author l.xue.nong
 */
public class DeleteResponse implements ActionResponse, Streamable {

	
	/** The index. */
	private String index;

	
	/** The id. */
	private String id;

	
	/** The type. */
	private String type;

	
	/** The version. */
	private long version;

	
	/** The not found. */
	private boolean notFound;

	
	/**
	 * Instantiates a new delete response.
	 */
	public DeleteResponse() {

	}

	
	/**
	 * Instantiates a new delete response.
	 *
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 * @param version the version
	 * @param notFound the not found
	 */
	public DeleteResponse(String index, String type, String id, long version, boolean notFound) {
		this.index = index;
		this.id = id;
		this.type = type;
		this.version = version;
		this.notFound = notFound;
	}

	
	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		return this.index;
	}

	
	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public String getIndex() {
		return index;
	}

	
	/**
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		return this.type;
	}

	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	
	/**
	 * Id.
	 *
	 * @return the string
	 */
	public String id() {
		return this.id;
	}

	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	
	/**
	 * Version.
	 *
	 * @return the long
	 */
	public long version() {
		return this.version;
	}

	
	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public long getVersion() {
		return this.version;
	}

	
	/**
	 * Not found.
	 *
	 * @return true, if successful
	 */
	public boolean notFound() {
		return notFound;
	}

	
	/**
	 * Checks if is not found.
	 *
	 * @return true, if is not found
	 */
	public boolean isNotFound() {
		return notFound;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		index = in.readUTF();
		id = in.readUTF();
		type = in.readUTF();
		version = in.readLong();
		notFound = in.readBoolean();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(index);
		out.writeUTF(id);
		out.writeUTF(type);
		out.writeLong(version);
		out.writeBoolean(notFound);
	}
}
