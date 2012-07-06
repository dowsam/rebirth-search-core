/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexResponse.java 2012-3-29 15:02:32 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.action.ActionResponse;

import com.google.common.collect.ImmutableList;


/**
 * The Class IndexResponse.
 *
 * @author l.xue.nong
 */
public class IndexResponse implements ActionResponse, Streamable {

	
	/** The index. */
	private String index;

	
	/** The id. */
	private String id;

	
	/** The type. */
	private String type;

	
	/** The version. */
	private long version;

	
	/** The matches. */
	private List<String> matches;

	
	/**
	 * Instantiates a new index response.
	 */
	public IndexResponse() {

	}

	
	/**
	 * Instantiates a new index response.
	 *
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 * @param version the version
	 */
	public IndexResponse(String index, String type, String id, long version) {
		this.index = index;
		this.id = id;
		this.type = type;
		this.version = version;
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
		return version();
	}

	
	/**
	 * Matches.
	 *
	 * @return the list
	 */
	public List<String> matches() {
		return this.matches;
	}

	
	/**
	 * Gets the matches.
	 *
	 * @return the matches
	 */
	public List<String> getMatches() {
		return this.matches;
	}

	
	/**
	 * Matches.
	 *
	 * @param matches the matches
	 */
	public void matches(List<String> matches) {
		this.matches = matches;
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
		if (in.readBoolean()) {
			int size = in.readVInt();
			if (size == 0) {
				matches = ImmutableList.of();
			} else if (size == 1) {
				matches = ImmutableList.of(in.readUTF());
			} else if (size == 2) {
				matches = ImmutableList.of(in.readUTF(), in.readUTF());
			} else if (size == 3) {
				matches = ImmutableList.of(in.readUTF(), in.readUTF(), in.readUTF());
			} else if (size == 4) {
				matches = ImmutableList.of(in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF());
			} else if (size == 5) {
				matches = ImmutableList.of(in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF());
			} else {
				matches = new ArrayList<String>();
				for (int i = 0; i < size; i++) {
					matches.add(in.readUTF());
				}
			}
		}
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
		if (matches == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeVInt(matches.size());
			for (String match : matches) {
				out.writeUTF(match);
			}
		}
	}
}
