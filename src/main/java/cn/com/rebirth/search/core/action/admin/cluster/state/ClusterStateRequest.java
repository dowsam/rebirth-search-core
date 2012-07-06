/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterStateRequest.java 2012-3-29 15:02:02 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.state;

import java.io.IOException;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest;


/**
 * The Class ClusterStateRequest.
 *
 * @author l.xue.nong
 */
public class ClusterStateRequest extends MasterNodeOperationRequest {

	
	/** The filter routing table. */
	private boolean filterRoutingTable = false;

	
	/** The filter nodes. */
	private boolean filterNodes = false;

	
	/** The filter meta data. */
	private boolean filterMetaData = false;

	
	/** The filter blocks. */
	private boolean filterBlocks = false;

	
	/** The filtered indices. */
	private String[] filteredIndices = Strings.EMPTY_ARRAY;

	
	/** The filtered index templates. */
	private String[] filteredIndexTemplates = Strings.EMPTY_ARRAY;

	
	/** The local. */
	private boolean local = false;

	
	/**
	 * Instantiates a new cluster state request.
	 */
	public ClusterStateRequest() {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		return null;
	}

	
	/**
	 * Filter all.
	 *
	 * @return the cluster state request
	 */
	public ClusterStateRequest filterAll() {
		filterRoutingTable = true;
		filterNodes = true;
		filterMetaData = true;
		filterBlocks = true;
		filteredIndices = Strings.EMPTY_ARRAY;
		filteredIndexTemplates = Strings.EMPTY_ARRAY;
		return this;
	}

	
	/**
	 * Filter routing table.
	 *
	 * @return true, if successful
	 */
	public boolean filterRoutingTable() {
		return filterRoutingTable;
	}

	
	/**
	 * Filter routing table.
	 *
	 * @param filterRoutingTable the filter routing table
	 * @return the cluster state request
	 */
	public ClusterStateRequest filterRoutingTable(boolean filterRoutingTable) {
		this.filterRoutingTable = filterRoutingTable;
		return this;
	}

	
	/**
	 * Filter nodes.
	 *
	 * @return true, if successful
	 */
	public boolean filterNodes() {
		return filterNodes;
	}

	
	/**
	 * Filter nodes.
	 *
	 * @param filterNodes the filter nodes
	 * @return the cluster state request
	 */
	public ClusterStateRequest filterNodes(boolean filterNodes) {
		this.filterNodes = filterNodes;
		return this;
	}

	
	/**
	 * Filter meta data.
	 *
	 * @return true, if successful
	 */
	public boolean filterMetaData() {
		return filterMetaData;
	}

	
	/**
	 * Filter meta data.
	 *
	 * @param filterMetaData the filter meta data
	 * @return the cluster state request
	 */
	public ClusterStateRequest filterMetaData(boolean filterMetaData) {
		this.filterMetaData = filterMetaData;
		return this;
	}

	
	/**
	 * Filter blocks.
	 *
	 * @return true, if successful
	 */
	public boolean filterBlocks() {
		return filterBlocks;
	}

	
	/**
	 * Filter blocks.
	 *
	 * @param filterBlocks the filter blocks
	 * @return the cluster state request
	 */
	public ClusterStateRequest filterBlocks(boolean filterBlocks) {
		this.filterBlocks = filterBlocks;
		return this;
	}

	
	/**
	 * Filtered indices.
	 *
	 * @return the string[]
	 */
	public String[] filteredIndices() {
		return filteredIndices;
	}

	
	/**
	 * Filtered indices.
	 *
	 * @param filteredIndices the filtered indices
	 * @return the cluster state request
	 */
	public ClusterStateRequest filteredIndices(String... filteredIndices) {
		this.filteredIndices = filteredIndices;
		return this;
	}

	
	/**
	 * Filtered index templates.
	 *
	 * @return the string[]
	 */
	public String[] filteredIndexTemplates() {
		return this.filteredIndexTemplates;
	}

	
	/**
	 * Filtered index templates.
	 *
	 * @param filteredIndexTemplates the filtered index templates
	 * @return the cluster state request
	 */
	public ClusterStateRequest filteredIndexTemplates(String... filteredIndexTemplates) {
		this.filteredIndexTemplates = filteredIndexTemplates;
		return this;
	}

	
	/**
	 * Local.
	 *
	 * @param local the local
	 * @return the cluster state request
	 */
	public ClusterStateRequest local(boolean local) {
		this.local = local;
		return this;
	}

	
	/**
	 * Local.
	 *
	 * @return true, if successful
	 */
	public boolean local() {
		return this.local;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		filterRoutingTable = in.readBoolean();
		filterNodes = in.readBoolean();
		filterMetaData = in.readBoolean();
		filterBlocks = in.readBoolean();
		int size = in.readVInt();
		if (size > 0) {
			filteredIndices = new String[size];
			for (int i = 0; i < filteredIndices.length; i++) {
				filteredIndices[i] = in.readUTF();
			}
		}
		size = in.readVInt();
		if (size > 0) {
			filteredIndexTemplates = new String[size];
			for (int i = 0; i < filteredIndexTemplates.length; i++) {
				filteredIndexTemplates[i] = in.readUTF();
			}
		}
		local = in.readBoolean();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeBoolean(filterRoutingTable);
		out.writeBoolean(filterNodes);
		out.writeBoolean(filterMetaData);
		out.writeBoolean(filterBlocks);
		out.writeVInt(filteredIndices.length);
		for (String filteredIndex : filteredIndices) {
			out.writeUTF(filteredIndex);
		}
		out.writeVInt(filteredIndexTemplates.length);
		for (String filteredIndexTemplate : filteredIndexTemplates) {
			out.writeUTF(filteredIndexTemplate);
		}
		out.writeBoolean(local);
	}
}
