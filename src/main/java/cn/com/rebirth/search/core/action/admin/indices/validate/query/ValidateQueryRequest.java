/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ValidateQueryRequest.java 2012-7-6 14:30:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.validate.query;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.lucene.util.UnicodeUtil;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.Unicode;
import cn.com.rebirth.commons.io.BytesStream;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentFactory;
import cn.com.rebirth.commons.xcontent.XContentType;
import cn.com.rebirth.search.commons.Required;
import cn.com.rebirth.search.core.RestartGenerationException;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationThreading;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.index.query.QueryBuilder;

/**
 * The Class ValidateQueryRequest.
 *
 * @author l.xue.nong
 */
public class ValidateQueryRequest extends BroadcastOperationRequest {

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

	/**
	 * Instantiates a new validate query request.
	 */
	ValidateQueryRequest() {
	}

	/**
	 * Instantiates a new validate query request.
	 *
	 * @param indices the indices
	 */
	public ValidateQueryRequest(String... indices) {
		super(indices);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = super.validate();
		return validationException;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest#operationThreading(cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationThreading)
	 */
	@Override
	public ValidateQueryRequest operationThreading(BroadcastOperationThreading operationThreading) {
		super.operationThreading(operationThreading);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest#beforeStart()
	 */
	@Override
	protected void beforeStart() {
		if (querySourceUnsafe) {
			querySource = Arrays.copyOfRange(querySource, querySourceOffset, querySourceOffset + querySourceLength);
			querySourceOffset = 0;
			querySourceUnsafe = false;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest#listenerThreaded(boolean)
	 */
	@Override
	public ValidateQueryRequest listenerThreaded(boolean threadedListener) {
		super.listenerThreaded(threadedListener);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest#indices(java.lang.String[])
	 */
	public ValidateQueryRequest indices(String... indices) {
		this.indices = indices;
		return this;
	}

	/**
	 * Query source.
	 *
	 * @return the bytes holder
	 */
	BytesHolder querySource() {
		return new BytesHolder(querySource, querySourceOffset, querySourceLength);
	}

	/**
	 * Query.
	 *
	 * @param queryBuilder the query builder
	 * @return the validate query request
	 */
	@Required
	public ValidateQueryRequest query(QueryBuilder queryBuilder) {
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
	 * @return the validate query request
	 */
	@Required
	public ValidateQueryRequest query(Map querySource) {
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
	 * @return the validate query request
	 */
	@Required
	public ValidateQueryRequest query(XContentBuilder builder) {
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
	 * @return the validate query request
	 */
	@Required
	public ValidateQueryRequest query(String querySource) {
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
	 * @return the validate query request
	 */
	@Required
	public ValidateQueryRequest query(byte[] querySource) {
		return query(querySource, 0, querySource.length, false);
	}

	/**
	 * Query.
	 *
	 * @param querySource the query source
	 * @param offset the offset
	 * @param length the length
	 * @param unsafe the unsafe
	 * @return the validate query request
	 */
	@Required
	public ValidateQueryRequest query(byte[] querySource, int offset, int length, boolean unsafe) {
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

	/**
	 * Types.
	 *
	 * @param types the types
	 * @return the validate query request
	 */
	public ValidateQueryRequest types(String... types) {
		this.types = types;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);

		BytesHolder bytes = in.readBytesReference();
		querySourceUnsafe = false;
		querySource = bytes.bytes();
		querySourceOffset = bytes.offset();
		querySourceLength = bytes.length();

		int typesSize = in.readVInt();
		if (typesSize > 0) {
			types = new String[typesSize];
			for (int i = 0; i < typesSize; i++) {
				types[i] = in.readUTF();
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);

		out.writeBytesHolder(querySource, querySourceOffset, querySourceLength);

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
		return "[" + Arrays.toString(indices) + "]" + Arrays.toString(types) + ", querySource["
				+ Unicode.fromBytes(querySource, querySourceOffset, querySourceLength) + "]";
	}
}
