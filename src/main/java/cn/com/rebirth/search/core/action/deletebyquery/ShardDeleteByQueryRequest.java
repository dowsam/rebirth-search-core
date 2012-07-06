/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardDeleteByQueryRequest.java 2012-7-6 14:29:27 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.deletebyquery;

import gnu.trove.set.hash.THashSet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.Unicode;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest;

/**
 * The Class ShardDeleteByQueryRequest.
 *
 * @author l.xue.nong
 */
public class ShardDeleteByQueryRequest extends ShardReplicationOperationRequest {

	/** The shard id. */
	private int shardId;

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
	 * Instantiates a new shard delete by query request.
	 *
	 * @param request the request
	 * @param shardId the shard id
	 */
	ShardDeleteByQueryRequest(IndexDeleteByQueryRequest request, int shardId) {
		this.index = request.index();
		this.querySource = request.querySource();
		this.types = request.types();
		this.shardId = shardId;
		replicationType(request.replicationType());
		consistencyLevel(request.consistencyLevel());
		timeout = request.timeout();
		this.routing = request.routing();
		filteringAliases = request.filteringAliases();
	}

	/**
	 * Instantiates a new shard delete by query request.
	 */
	ShardDeleteByQueryRequest() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = super.validate();
		if (querySource == null) {
			ValidateActions.addValidationError("querySource is missing", validationException);
		}
		return validationException;
	}

	/**
	 * Shard id.
	 *
	 * @return the int
	 */
	public int shardId() {
		return this.shardId;
	}

	/**
	 * Query source.
	 *
	 * @return the bytes holder
	 */
	BytesHolder querySource() {
		return querySource;
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
	 * Routing.
	 *
	 * @return the sets the
	 */
	public Set<String> routing() {
		return this.routing;
	}

	/**
	 * Filtering aliases.
	 *
	 * @return the string[]
	 */
	public String[] filteringAliases() {
		return filteringAliases;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		querySource = in.readBytesReference();
		shardId = in.readVInt();
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
	 * @see cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeBytesHolder(querySource);
		out.writeVInt(shardId);
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String sSource = "_na_";
		try {
			sSource = Unicode.fromBytes(querySource.bytes(), querySource.offset(), querySource.length());
		} catch (Exception e) {

		}
		return "delete_by_query {[" + index + "]" + Arrays.toString(types) + ", query [" + sSource + "]}";
	}
}