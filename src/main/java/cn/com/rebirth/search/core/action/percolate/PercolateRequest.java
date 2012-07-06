/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PercolateRequest.java 2012-7-6 14:29:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.percolate;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.lucene.util.UnicodeUtil;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.Unicode;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.commons.Required;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.RestartGenerationException;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.support.single.custom.SingleCustomOperationRequest;

/**
 * The Class PercolateRequest.
 *
 * @author l.xue.nong
 */
public class PercolateRequest extends SingleCustomOperationRequest {

	/** The index. */
	private String index;

	/** The type. */
	private String type;

	/** The source. */
	private byte[] source;

	/** The source offset. */
	private int sourceOffset;

	/** The source length. */
	private int sourceLength;

	/** The source unsafe. */
	private boolean sourceUnsafe;

	/**
	 * Instantiates a new percolate request.
	 */
	public PercolateRequest() {

	}

	/**
	 * Instantiates a new percolate request.
	 *
	 * @param index the index
	 * @param type the type
	 */
	public PercolateRequest(String index, String type) {
		this.index = index;
		this.type = type;
	}

	/**
	 * Index.
	 *
	 * @param index the index
	 * @return the percolate request
	 */
	public PercolateRequest index(String index) {
		this.index = index;
		return this;
	}

	/**
	 * Type.
	 *
	 * @param type the type
	 * @return the percolate request
	 */
	public PercolateRequest type(String type) {
		this.type = type;
		return this;
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
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		return this.type;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.custom.SingleCustomOperationRequest#beforeLocalFork()
	 */
	@Override
	public void beforeLocalFork() {
		if (sourceUnsafe) {
			source();
		}
	}

	/**
	 * Source.
	 *
	 * @return the byte[]
	 */
	public byte[] source() {
		if (sourceUnsafe || sourceOffset > 0 || source.length != sourceLength) {
			source = Arrays.copyOfRange(source, sourceOffset, sourceOffset + sourceLength);
			sourceOffset = 0;
			sourceUnsafe = false;
		}
		return source;
	}

	/**
	 * Underlying source.
	 *
	 * @return the byte[]
	 */
	public byte[] underlyingSource() {
		if (sourceUnsafe) {
			source();
		}
		return this.source;
	}

	/**
	 * Underlying source offset.
	 *
	 * @return the int
	 */
	public int underlyingSourceOffset() {
		if (sourceUnsafe) {
			source();
		}
		return this.sourceOffset;
	}

	/**
	 * Underlying source length.
	 *
	 * @return the int
	 */
	public int underlyingSourceLength() {
		if (sourceUnsafe) {
			source();
		}
		return this.sourceLength;
	}

	/**
	 * Source.
	 *
	 * @param source the source
	 * @return the percolate request
	 * @throws RestartGenerationException the restart generation exception
	 */
	@Required
	public PercolateRequest source(Map source) throws RestartGenerationException {
		return source(source, XContentType.SMILE);
	}

	/**
	 * Source.
	 *
	 * @param source the source
	 * @param contentType the content type
	 * @return the percolate request
	 * @throws RestartGenerationException the restart generation exception
	 */
	@Required
	public PercolateRequest source(Map source, XContentType contentType) throws RestartGenerationException {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(contentType);
			builder.map(source);
			return source(builder);
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + source + "]", e);
		}
	}

	/**
	 * Source.
	 *
	 * @param source the source
	 * @return the percolate request
	 */
	@Required
	public PercolateRequest source(String source) {
		UnicodeUtil.UTF8Result result = Unicode.fromStringAsUtf8(source);
		this.source = result.result;
		this.sourceOffset = 0;
		this.sourceLength = result.length;
		this.sourceUnsafe = true;
		return this;
	}

	/**
	 * Source.
	 *
	 * @param sourceBuilder the source builder
	 * @return the percolate request
	 */
	@Required
	public PercolateRequest source(XContentBuilder sourceBuilder) {
		try {
			source = sourceBuilder.underlyingBytes();
			sourceOffset = 0;
			sourceLength = sourceBuilder.underlyingBytesLength();
			sourceUnsafe = false;
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + sourceBuilder + "]", e);
		}
		return this;
	}

	/**
	 * Source.
	 *
	 * @param source the source
	 * @return the percolate request
	 */
	public PercolateRequest source(byte[] source) {
		return source(source, 0, source.length);
	}

	/**
	 * Source.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @return the percolate request
	 */
	@Required
	public PercolateRequest source(byte[] source, int offset, int length) {
		return source(source, offset, length, false);
	}

	/**
	 * Source.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @param unsafe the unsafe
	 * @return the percolate request
	 */
	@Required
	public PercolateRequest source(byte[] source, int offset, int length, boolean unsafe) {
		this.source = source;
		this.sourceOffset = offset;
		this.sourceLength = length;
		this.sourceUnsafe = unsafe;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.custom.SingleCustomOperationRequest#preferLocal(boolean)
	 */
	@Override
	public PercolateRequest preferLocal(boolean preferLocal) {
		super.preferLocal(preferLocal);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.custom.SingleCustomOperationRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = super.validate();
		if (index == null) {
			validationException = ValidateActions.addValidationError("index is missing", validationException);
		}
		if (type == null) {
			validationException = ValidateActions.addValidationError("type is missing", validationException);
		}
		if (source == null) {
			validationException = ValidateActions.addValidationError("source is missing", validationException);
		}
		return validationException;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.custom.SingleCustomOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		index = in.readUTF();
		type = in.readUTF();

		BytesHolder bytes = in.readBytesReference();
		sourceUnsafe = false;
		source = bytes.bytes();
		sourceOffset = bytes.offset();
		sourceLength = bytes.length();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.custom.SingleCustomOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeUTF(index);
		out.writeUTF(type);
		out.writeBytesHolder(source, sourceOffset, sourceLength);
	}
}
