/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LocalTransportChannel.java 2012-7-6 14:29:37 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport.local;

import java.io.IOException;
import java.io.NotSerializableException;

import cn.com.rebirth.commons.io.ThrowableObjectOutputStream;
import cn.com.rebirth.commons.io.stream.BytesStreamOutput;
import cn.com.rebirth.commons.io.stream.CachedStreamOutput;
import cn.com.rebirth.commons.io.stream.HandlesStreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.transport.NotSerializableTransportException;
import cn.com.rebirth.search.core.transport.RemoteTransportException;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportResponseOptions;
import cn.com.rebirth.search.core.transport.support.TransportStreams;

/**
 * The Class LocalTransportChannel.
 *
 * @author l.xue.nong
 */
public class LocalTransportChannel implements TransportChannel {

	/** The source transport. */
	private final LocalTransport sourceTransport;

	/** The target transport. */
	private final LocalTransport targetTransport;

	/** The action. */
	private final String action;

	/** The request id. */
	private final long requestId;

	/**
	 * Instantiates a new local transport channel.
	 *
	 * @param sourceTransport the source transport
	 * @param targetTransport the target transport
	 * @param action the action
	 * @param requestId the request id
	 */
	public LocalTransportChannel(LocalTransport sourceTransport, LocalTransport targetTransport, String action,
			long requestId) {
		this.sourceTransport = sourceTransport;
		this.targetTransport = targetTransport;
		this.action = action;
		this.requestId = requestId;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.TransportChannel#action()
	 */
	@Override
	public String action() {
		return action;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.TransportChannel#sendResponse(cn.com.rebirth.commons.io.stream.Streamable)
	 */
	@Override
	public void sendResponse(Streamable message) throws IOException {
		sendResponse(message, TransportResponseOptions.EMPTY);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.TransportChannel#sendResponse(cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportResponseOptions)
	 */
	@Override
	public void sendResponse(Streamable message, TransportResponseOptions options) throws IOException {
		CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
		try {
			HandlesStreamOutput stream = cachedEntry.cachedHandlesBytes();
			stream.writeLong(requestId);
			byte status = 0;
			status = TransportStreams.statusSetResponse(status);
			stream.writeByte(status);
			message.writeTo(stream);
			final byte[] data = cachedEntry.bytes().copiedByteArray();
			targetTransport.threadPool().generic().execute(new Runnable() {
				@Override
				public void run() {
					targetTransport.messageReceived(data, action, sourceTransport, null);
				}
			});
		} finally {
			CachedStreamOutput.pushEntry(cachedEntry);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.TransportChannel#sendResponse(java.lang.Throwable)
	 */
	@Override
	public void sendResponse(Throwable error) throws IOException {
		CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
		try {
			BytesStreamOutput stream;
			try {
				stream = cachedEntry.cachedBytes();
				writeResponseExceptionHeader(stream);
				RemoteTransportException tx = new RemoteTransportException(targetTransport.nodeName(), targetTransport
						.boundAddress().boundAddress(), action, error);
				ThrowableObjectOutputStream too = new ThrowableObjectOutputStream(stream);
				too.writeObject(tx);
				too.close();
			} catch (NotSerializableException e) {
				stream = cachedEntry.cachedBytes();
				writeResponseExceptionHeader(stream);
				RemoteTransportException tx = new RemoteTransportException(targetTransport.nodeName(), targetTransport
						.boundAddress().boundAddress(), action, new NotSerializableTransportException(error));
				ThrowableObjectOutputStream too = new ThrowableObjectOutputStream(stream);
				too.writeObject(tx);
				too.close();
			}
			final byte[] data = stream.copiedByteArray();
			targetTransport.threadPool().generic().execute(new Runnable() {
				@Override
				public void run() {
					targetTransport.messageReceived(data, action, sourceTransport, null);
				}
			});
		} finally {
			CachedStreamOutput.pushEntry(cachedEntry);
		}
	}

	/**
	 * Write response exception header.
	 *
	 * @param stream the stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeResponseExceptionHeader(BytesStreamOutput stream) throws IOException {
		stream.writeLong(requestId);
		byte status = 0;
		status = TransportStreams.statusSetResponse(status);
		status = TransportStreams.statusSetError(status);
		stream.writeByte(status);
	}
}
