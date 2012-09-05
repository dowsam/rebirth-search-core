/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DeleteByQueryRequest.java 2012-7-6 14:28:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.deletebyquery;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.lucene.util.UnicodeUtil;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.Unicode;
import cn.com.rebirth.commons.io.BytesStream;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentFactory;
import cn.com.rebirth.commons.xcontent.XContentType;
import cn.com.rebirth.search.commons.Required;
import cn.com.rebirth.search.core.RestartGenerationException;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;
import cn.com.rebirth.search.core.action.support.replication.IndicesReplicationOperationRequest;
import cn.com.rebirth.search.core.action.support.replication.ReplicationType;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.index.query.QueryBuilder;

/**
 * The Class DeleteByQueryRequest.
 *
 * @author l.xue.nong
 */
public class DeleteByQueryRequest extends IndicesReplicationOperationRequest {

	/** The Constant contentType. */
	private static final XContentType contentType = Requests.CONTENT_TYPE;

	/** The query source. */
	private byte[] querySource;

	/** The query source offset. */
	private int querySourceOffset;

	/** The query source length. */
	private int querySourceLength;

	/** The query source unsafe. */
	private boolean querySourceUnsafe;

	/** The types. */
	private String[] types = Strings.EMPTY_ARRAY;

	/** The routing. */
	@Nullable
	private String routing;

	/**
	 * Instantiates a new delete by query request.
	 *
	 * @param indices the indices
	 */
	public DeleteByQueryRequest(String... indices) {
		this.indices = indices;
	}

	/**
	 * Instantiates a new delete by query request.
	 */
	public DeleteByQueryRequest() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.IndicesReplicationOperationRequest#listenerThreaded(boolean)
	 */
	@Override
	public DeleteByQueryRequest listenerThreaded(boolean threadedListener) {
		super.listenerThreaded(threadedListener);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.IndicesReplicationOperationRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = super.validate();
		if (querySource == null) {
			validationException = ValidateActions.addValidationError("query is missing", validationException);
		}
		return validationException;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.IndicesReplicationOperationRequest#indices(java.lang.String[])
	 */
	public DeleteByQueryRequest indices(String... indices) {
		this.indices = indices;
		return this;
	}

	/**
	 * Query source.
	 *
	 * @return the bytes holder
	 */
	BytesHolder querySource() {
		if (querySourceUnsafe) {
			querySource = Arrays.copyOfRange(querySource, querySourceOffset, querySourceOffset + querySourceLength);
			querySourceOffset = 0;
			querySourceUnsafe = false;
		}
		return new BytesHolder(querySource, querySourceOffset, querySourceLength);
	}

	/**
	 * Query.
	 *
	 * @param queryBuilder the query builder
	 * @return the delete by query request
	 */
	@Required
	public DeleteByQueryRequest query(QueryBuilder queryBuilder) {
		BytesStream bos = queryBuilder.buildAsBytes();
		this.querySource = bos.underlyingBytes();
		this.querySourceOffset = 0;
		this.querySourceLength = bos.size();
		this.querySourceUnsafe = false;
		return this;
	}

	/**
	 * Query.
	 *
	 * @param querySource the query source
	 * @return the delete by query request
	 */
	@Required
	public DeleteByQueryRequest query(String querySource) {
		UnicodeUtil.UTF8Result result = Unicode.fromStringAsUtf8(querySource);
		this.querySource = result.result;
		this.querySourceOffset = 0;
		this.querySourceLength = result.length;
		this.querySourceUnsafe = true;
		return this;
	}

	/**
	 * Query.
	 *
	 * @param querySource the query source
	 * @return the delete by query request
	 */
	@Required
	public DeleteByQueryRequest query(Map querySource) {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(contentType);
			builder.map(querySource);
			return query(builder);
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + querySource + "]", e);
		}
	}

	/**
	 * Query.
	 *
	 * @param builder the builder
	 * @return the delete by query request
	 */
	@Required
	public DeleteByQueryRequest query(XContentBuilder builder) {
		try {
			this.querySource = builder.underlyingBytes();
			this.querySourceOffset = 0;
			this.querySourceLength = builder.underlyingBytesLength();
			this.querySourceUnsafe = false;
			return this;
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + builder + "]", e);
		}
	}

	/**
	 * Query.
	 *
	 * @param querySource the query source
	 * @return the delete by query request
	 */
	@Required
	public DeleteByQueryRequest query(byte[] querySource) {
		return query(querySource, 0, querySource.length, false);
	}

	/**
	 * Query.
	 *
	 * @param querySource the query source
	 * @param offset the offset
	 * @param length the length
	 * @param unsafe the unsafe
	 * @return the delete by query request
	 */
	@Required
	public DeleteByQueryRequest query(byte[] querySource, int offset, int length, boolean unsafe) {
		this.querySource = querySource;
		this.querySourceOffset = offset;
		this.querySourceLength = length;
		this.querySourceUnsafe = unsafe;
		return this;
	}

	/**
	 * Types.
	 *
	 * @return the string[]
	 */
	String[] types() {
		return this.types;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.IndicesReplicationOperationRequest#routing()
	 */
	@Override
	public String routing() {
		return this.routing;
	}

	/**
	 * Routing.
	 *
	 * @param routing the routing
	 * @return the delete by query request
	 */
	public DeleteByQueryRequest routing(String routing) {
		this.routing = routing;
		return this;
	}

	/**
	 * Routing.
	 *
	 * @param routings the routings
	 * @return the delete by query request
	 */
	public DeleteByQueryRequest routing(String... routings) {
		this.routing = Strings.arrayToCommaDelimitedString(routings);
		return this;
	}

	/**
	 * Types.
	 *
	 * @param types the types
	 * @return the delete by query request
	 */
	public DeleteByQueryRequest types(String... types) {
		this.types = types;
		return this;
	}

	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the delete by query request
	 */
	public DeleteByQueryRequest timeout(TimeValue timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the delete by query request
	 */
	public DeleteByQueryRequest timeout(String timeout) {
		this.timeout = TimeValue.parseTimeValue(timeout, null);
		return this;
	}

	/**
	 * Replication type.
	 *
	 * @param replicationType the replication type
	 * @return the delete by query request
	 */
	public DeleteByQueryRequest replicationType(ReplicationType replicationType) {
		this.replicationType = replicationType;
		return this;
	}

	/**
	 * Consistency level.
	 *
	 * @param consistencyLevel the consistency level
	 * @return the delete by query request
	 */
	public DeleteByQueryRequest consistencyLevel(WriteConsistencyLevel consistencyLevel) {
		this.consistencyLevel = consistencyLevel;
		return this;
	}

	/**
	 * Replication type.
	 *
	 * @param replicationType the replication type
	 * @return the delete by query request
	 */
	public DeleteByQueryRequest replicationType(String replicationType) {
		this.replicationType = ReplicationType.fromString(replicationType);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.IndicesReplicationOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);

		BytesHolder bytes = in.readBytesReference();
		querySourceUnsafe = false;
		querySource = bytes.bytes();
		querySourceOffset = bytes.offset();
		querySourceLength = bytes.length();

		if (in.readBoolean()) {
			routing = in.readUTF();
		}

		int size = in.readVInt();
		if (size == 0) {
			types = Strings.EMPTY_ARRAY;
		} else {
			types = new String[size];
			for (int i = 0; i < size; i++) {
				types[i] = in.readUTF();
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.replication.IndicesReplicationOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);

		out.writeBytesHolder(querySource, querySourceOffset, querySourceLength);

		if (routing == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(routing);
		}

		out.writeVInt(types.length);
		for (String type : types) {
			out.writeUTF(type);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + Arrays.toString(indices) + "][" + Arrays.toString(types) + "], querySource["
				+ Unicode.fromBytes(querySource) + "]";
	}
}