/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndicesStatsRequest.java 2012-3-29 15:02:45 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.stats;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest;


/**
 * The Class IndicesStatsRequest.
 *
 * @author l.xue.nong
 */
public class IndicesStatsRequest extends BroadcastOperationRequest {

	
	/** The docs. */
	private boolean docs = true;

	
	/** The store. */
	private boolean store = true;

	
	/** The indexing. */
	private boolean indexing = true;

	
	/** The get. */
	private boolean get = true;

	
	/** The search. */
	private boolean search = true;

	
	/** The merge. */
	private boolean merge = false;

	
	/** The refresh. */
	private boolean refresh = false;

	
	/** The flush. */
	private boolean flush = false;

	
	/** The types. */
	private String[] types = null;

	
	/** The groups. */
	private String[] groups = null;

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#indices(java.lang.String[])
	 */
	public IndicesStatsRequest indices(String... indices) {
		this.indices = indices;
		return this;
	}

	
	/**
	 * All.
	 *
	 * @return the indices stats request
	 */
	public IndicesStatsRequest all() {
		docs = true;
		store = true;
		get = true;
		indexing = true;
		search = true;
		merge = true;
		refresh = true;
		flush = true;
		types = null;
		groups = null;
		return this;
	}

	
	/**
	 * Clear.
	 *
	 * @return the indices stats request
	 */
	public IndicesStatsRequest clear() {
		docs = false;
		store = false;
		get = false;
		indexing = false;
		search = false;
		merge = false;
		refresh = false;
		flush = false;
		types = null;
		groups = null;
		return this;
	}

	
	/**
	 * Types.
	 *
	 * @param types the types
	 * @return the indices stats request
	 */
	public IndicesStatsRequest types(String... types) {
		this.types = types;
		return this;
	}

	
	/**
	 * Types.
	 *
	 * @return the string[]
	 */
	public String[] types() {
		return this.types;
	}

	
	/**
	 * Groups.
	 *
	 * @param groups the groups
	 * @return the indices stats request
	 */
	public IndicesStatsRequest groups(String... groups) {
		this.groups = groups;
		return this;
	}

	
	/**
	 * Groups.
	 *
	 * @return the string[]
	 */
	public String[] groups() {
		return this.groups;
	}

	
	/**
	 * Docs.
	 *
	 * @param docs the docs
	 * @return the indices stats request
	 */
	public IndicesStatsRequest docs(boolean docs) {
		this.docs = docs;
		return this;
	}

	
	/**
	 * Docs.
	 *
	 * @return true, if successful
	 */
	public boolean docs() {
		return this.docs;
	}

	
	/**
	 * Store.
	 *
	 * @param store the store
	 * @return the indices stats request
	 */
	public IndicesStatsRequest store(boolean store) {
		this.store = store;
		return this;
	}

	
	/**
	 * Store.
	 *
	 * @return true, if successful
	 */
	public boolean store() {
		return this.store;
	}

	
	/**
	 * Indexing.
	 *
	 * @param indexing the indexing
	 * @return the indices stats request
	 */
	public IndicesStatsRequest indexing(boolean indexing) {
		this.indexing = indexing;
		return this;
	}

	
	/**
	 * Indexing.
	 *
	 * @return true, if successful
	 */
	public boolean indexing() {
		return this.indexing;
	}

	
	/**
	 * Gets the.
	 *
	 * @param get the get
	 * @return the indices stats request
	 */
	public IndicesStatsRequest get(boolean get) {
		this.get = get;
		return this;
	}

	
	/**
	 * Gets the.
	 *
	 * @return true, if successful
	 */
	public boolean get() {
		return this.get;
	}

	
	/**
	 * Search.
	 *
	 * @param search the search
	 * @return the indices stats request
	 */
	public IndicesStatsRequest search(boolean search) {
		this.search = search;
		return this;
	}

	
	/**
	 * Search.
	 *
	 * @return true, if successful
	 */
	public boolean search() {
		return this.search;
	}

	
	/**
	 * Merge.
	 *
	 * @param merge the merge
	 * @return the indices stats request
	 */
	public IndicesStatsRequest merge(boolean merge) {
		this.merge = merge;
		return this;
	}

	
	/**
	 * Merge.
	 *
	 * @return true, if successful
	 */
	public boolean merge() {
		return this.merge;
	}

	
	/**
	 * Refresh.
	 *
	 * @param refresh the refresh
	 * @return the indices stats request
	 */
	public IndicesStatsRequest refresh(boolean refresh) {
		this.refresh = refresh;
		return this;
	}

	
	/**
	 * Refresh.
	 *
	 * @return true, if successful
	 */
	public boolean refresh() {
		return this.refresh;
	}

	
	/**
	 * Flush.
	 *
	 * @param flush the flush
	 * @return the indices stats request
	 */
	public IndicesStatsRequest flush(boolean flush) {
		this.flush = flush;
		return this;
	}

	
	/**
	 * Flush.
	 *
	 * @return true, if successful
	 */
	public boolean flush() {
		return this.flush;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeBoolean(docs);
		out.writeBoolean(store);
		out.writeBoolean(indexing);
		out.writeBoolean(get);
		out.writeBoolean(search);
		out.writeBoolean(merge);
		out.writeBoolean(flush);
		out.writeBoolean(refresh);
		if (types == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(types.length);
			for (String type : types) {
				out.writeUTF(type);
			}
		}
		if (groups == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(groups.length);
			for (String group : groups) {
				out.writeUTF(group);
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		docs = in.readBoolean();
		store = in.readBoolean();
		indexing = in.readBoolean();
		get = in.readBoolean();
		search = in.readBoolean();
		merge = in.readBoolean();
		flush = in.readBoolean();
		refresh = in.readBoolean();
		int size = in.readVInt();
		if (size > 0) {
			types = new String[size];
			for (int i = 0; i < size; i++) {
				types[i] = in.readUTF();
			}
		}
		size = in.readVInt();
		if (size > 0) {
			groups = new String[size];
			for (int i = 0; i < size; i++) {
				groups[i] = in.readUTF();
			}
		}
	}
}
