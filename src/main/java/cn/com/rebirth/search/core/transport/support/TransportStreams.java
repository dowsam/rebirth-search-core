/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportStreams.java 2012-7-6 14:30:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport.support;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.io.stream.CachedStreamOutput;
import cn.com.rebirth.search.commons.io.stream.HandlesStreamOutput;
import cn.com.rebirth.search.core.transport.TransportRequestOptions;
import cn.com.rebirth.search.core.transport.TransportResponseOptions;

/**
 * The Class TransportStreams.
 *
 * @author l.xue.nong
 */
public class TransportStreams {

	/** The Constant HEADER_SIZE. */
	public static final int HEADER_SIZE = 4 + 8 + 1;

	/** The Constant HEADER_PLACEHOLDER. */
	public static final byte[] HEADER_PLACEHOLDER = new byte[HEADER_SIZE];

	/**
	 * Write header.
	 *
	 * @param data the data
	 * @param dataLength the data length
	 * @param requestId the request id
	 * @param status the status
	 */
	public static void writeHeader(byte[] data, int dataLength, long requestId, byte status) {
		writeInt(data, 0, dataLength - 4);
		writeLong(data, 4, requestId);
		data[12] = status;
	}

	/**
	 * Write long.
	 *
	 * @param buffer the buffer
	 * @param offset the offset
	 * @param value the value
	 */
	private static void writeLong(byte[] buffer, int offset, long value) {
		buffer[offset++] = ((byte) (value >> 56));
		buffer[offset++] = ((byte) (value >> 48));
		buffer[offset++] = ((byte) (value >> 40));
		buffer[offset++] = ((byte) (value >> 32));
		buffer[offset++] = ((byte) (value >> 24));
		buffer[offset++] = ((byte) (value >> 16));
		buffer[offset++] = ((byte) (value >> 8));
		buffer[offset] = ((byte) (value));
	}

	/**
	 * Write int.
	 *
	 * @param buffer the buffer
	 * @param offset the offset
	 * @param value the value
	 */
	private static void writeInt(byte[] buffer, int offset, int value) {
		buffer[offset++] = ((byte) (value >> 24));
		buffer[offset++] = ((byte) (value >> 16));
		buffer[offset++] = ((byte) (value >> 8));
		buffer[offset] = ((byte) (value));
	}

	/** The Constant STATUS_REQRES. */
	private static final byte STATUS_REQRES = 1 << 0;

	/** The Constant STATUS_ERROR. */
	private static final byte STATUS_ERROR = 1 << 1;

	/** The Constant STATUS_COMPRESS. */
	private static final byte STATUS_COMPRESS = 1 << 2;

	/**
	 * Status is request.
	 *
	 * @param value the value
	 * @return true, if successful
	 */
	public static boolean statusIsRequest(byte value) {
		return (value & STATUS_REQRES) == 0;
	}

	/**
	 * Status set request.
	 *
	 * @param value the value
	 * @return the byte
	 */
	public static byte statusSetRequest(byte value) {
		value &= ~STATUS_REQRES;
		return value;
	}

	/**
	 * Status set response.
	 *
	 * @param value the value
	 * @return the byte
	 */
	public static byte statusSetResponse(byte value) {
		value |= STATUS_REQRES;
		return value;
	}

	/**
	 * Status is error.
	 *
	 * @param value the value
	 * @return true, if successful
	 */
	public static boolean statusIsError(byte value) {
		return (value & STATUS_ERROR) != 0;
	}

	/**
	 * Status set error.
	 *
	 * @param value the value
	 * @return the byte
	 */
	public static byte statusSetError(byte value) {
		value |= STATUS_ERROR;
		return value;
	}

	/**
	 * Status is compress.
	 *
	 * @param value the value
	 * @return true, if successful
	 */
	public static boolean statusIsCompress(byte value) {
		return (value & STATUS_COMPRESS) != 0;
	}

	/**
	 * Status set compress.
	 *
	 * @param value the value
	 * @return the byte
	 */
	public static byte statusSetCompress(byte value) {
		value |= STATUS_COMPRESS;
		return value;
	}

	/**
	 * Builds the request.
	 *
	 * @param cachedEntry the cached entry
	 * @param requestId the request id
	 * @param action the action
	 * @param message the message
	 * @param options the options
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void buildRequest(CachedStreamOutput.Entry cachedEntry, final long requestId, final String action,
			final Streamable message, TransportRequestOptions options) throws IOException {
		byte status = 0;
		status = TransportStreams.statusSetRequest(status);

		if (options.compress()) {
			status = TransportStreams.statusSetCompress(status);
			HandlesStreamOutput stream = cachedEntry.cachedHandlesLzfBytes();
			cachedEntry.bytes().write(HEADER_PLACEHOLDER);
			stream.writeUTF(action);
			message.writeTo(stream);
			stream.flush();
		} else {
			HandlesStreamOutput stream = cachedEntry.cachedHandlesBytes();
			cachedEntry.bytes().write(HEADER_PLACEHOLDER);
			stream.writeUTF(action);
			message.writeTo(stream);
			stream.flush();
		}
		TransportStreams.writeHeader(cachedEntry.bytes().underlyingBytes(), cachedEntry.bytes().size(), requestId,
				status);
	}

	/**
	 * Builds the response.
	 *
	 * @param cachedEntry the cached entry
	 * @param requestId the request id
	 * @param message the message
	 * @param options the options
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void buildResponse(CachedStreamOutput.Entry cachedEntry, final long requestId, Streamable message,
			TransportResponseOptions options) throws IOException {
		byte status = 0;
		status = TransportStreams.statusSetResponse(status);

		if (options.compress()) {
			status = TransportStreams.statusSetCompress(status);
			HandlesStreamOutput stream = cachedEntry.cachedHandlesLzfBytes();
			cachedEntry.bytes().write(HEADER_PLACEHOLDER);
			message.writeTo(stream);
			stream.flush();
		} else {
			HandlesStreamOutput stream = cachedEntry.cachedHandlesBytes();
			cachedEntry.bytes().write(HEADER_PLACEHOLDER);
			message.writeTo(stream);
			stream.flush();
		}
		TransportStreams.writeHeader(cachedEntry.bytes().underlyingBytes(), cachedEntry.bytes().size(), requestId,
				status);
	}
}
