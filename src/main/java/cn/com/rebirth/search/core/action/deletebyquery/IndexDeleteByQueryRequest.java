/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexDeleteByQueryRequest.java 2012-3-29 15:01:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.deletebyquery;

import gnu.trove.set.hash.THashSet;

import java.io.IOException;
import java.util.Set;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.support.replication.IndexReplicationOperationRequest;


/**
 * The Class IndexDeleteByQueryRequest.
 *
 * @author l.xue.nong
 */
public class IndexDeleteByQueryRequest extends IndexReplicationOperationRequest {

	
	/** The query source. */
	private BytesHolder querySource;

	
	/** The types. */
	private String[] types = Strings.EMPTY_ARRAY;

	
	/** The routing. */
	@Nullable
	private Set<String> routing;

	
	/** The filtering aliases. */
	@Nullable
	private String[] filteringAliases;

	
	/**
	 * Instantiates a new index delete by query request.
	 *
	 * @param request the request
	 * @param index the index
	 * @param routing the routing
	 * @param filteringAliases the filtering aliases
	 */
	IndexDeleteByQueryRequest(DeleteByQueryRequest request, String index, @Nullable Set<String> routing,
			@Nullable String[] filteringAliases) {
		this.index = index;
		this.timeout = request.timeout();
		this.querySource = request.querySource();
		this.types = request.types();
		this.replicationType = request.replicationType();
		this.consistencyLevel = request.consistencyLevel();
		this.routing = routing;
		this.filteringAliases = filteringAliases;
	}

	
	/**
	 * Instantiates a new index delete by query request.
	 */
	IndexDeleteByQueryRequest() {
	}

	
	/**
	 * Query source.
	 *
	 * @return the bytes holder
	 */
	BytesHolder querySource() {
		return querySource;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.IndexReplicationOperationRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = super.validate();
		if (querySource == null) {
			validationException = ValidateActions.addValidationError("querySource is missing", validationException);
		}
		return validationException;
	}

	
	/**
	 * Routing.
	 *
	 * @return the sets the
	 */
	Set<String> routing() {
		return this.routing;
	}

	
	/**
	 * Types.
	 *
	 * @return the string[]
	 */
	String[] types() {
		return this.types;
	}

	
	/**
	 * Filtering aliases.
	 *
	 * @return the string[]
	 */
	String[] filteringAliases() {
		return filteringAliases;
	}

	
	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the index delete by query request
	 */
	public IndexDeleteByQueryRequest timeout(TimeValue timeout) {
		this.timeout = timeout;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.IndexReplicationOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		querySource = in.readBytesReference();
		int typesSize = in.readVInt();
		if (typesSize > 0) {
			types = new String[typesSize];
			for (int i = 0; i < typesSize; i++) {
				types[i] = in.readUTF();
			}
		}
		int routingSize = in.readVInt();
		if (routingSize > 0) {
			routing = new THashSet<String>(routingSize);
			for (int i = 0; i < routingSize; i++) {
				routing.add(in.readUTF());
			}
		}
		int aliasesSize = in.readVInt();
		if (aliasesSize > 0) {
			filteringAliases = new String[aliasesSize];
			for (int i = 0; i < aliasesSize; i++) {
				filteringAliases[i] = in.readUTF();
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.IndexReplicationOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeBytesHolder(querySource);
		out.writeVInt(types.length);
		for (String type : types) {
			out.writeUTF(type);
		}
		if (routing != null) {
			out.writeVInt(routing.size());
			for (String r : routing) {
				out.writeUTF(r);
			}
		} else {
			out.writeVInt(0);
		}
		if (filteringAliases != null) {
			out.writeVInt(filteringAliases.length);
			for (String alias : filteringAliases) {
				out.writeUTF(alias);
			}
		} else {
			out.writeVInt(0);
		}
	}
}