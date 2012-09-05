/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NettyTransportChannel.java 2012-7-6 14:30:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport.netty;

import java.io.IOException;
import java.io.NotSerializableException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import cn.com.rebirth.commons.io.ThrowableObjectOutputStream;
import cn.com.rebirth.commons.io.stream.BytesStreamOutput;
import cn.com.rebirth.commons.io.stream.CachedStreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.transport.NotSerializableTransportException;
import cn.com.rebirth.search.core.transport.RemoteTransportException;
import cn.com.rebirth.search.core.transport.TransportChannel;
import cn.com.rebirth.search.core.transport.TransportResponseOptions;
import cn.com.rebirth.search.core.transport.support.TransportStreams;

/**
 * The Class NettyTransportChannel.
 *
 * @author l.xue.nong
 */
public class NettyTransportChannel implements TransportChannel {

	/** The Constant LENGTH_PLACEHOLDER. */
	private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

	/** The transport. */
	private final NettyTransport transport;

	/** The action. */
	private final String action;

	/** The channel. */
	private final Channel channel;

	/** The request id. */
	private final long requestId;

	/**
	 * Instantiates a new netty transport channel.
	 *
	 * @param transport the transport
	 * @param action the action
	 * @param channel the channel
	 * @param requestId the request id
	 */
	public NettyTransportChannel(NettyTransport transport, String action, Channel channel, long requestId) {
		this.transport = transport;
		this.action = action;
		this.channel = channel;
		this.requestId = requestId;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.TransportChannel#action()
	 */
	@Override
	public String action() {
		return this.action;
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
		if (transport.compress) {
			options.withCompress(true);
		}
		CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
		TransportStreams.buildResponse(cachedEntry, requestId, message, options);
		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(cachedEntry.bytes().underlyingBytes(), 0, cachedEntry
				.bytes().size());
		ChannelFuture future = channel.write(buffer);
		future.addListener(new NettyTransport.CacheFutureListener(cachedEntry));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.TransportChannel#sendResponse(java.lang.Throwable)
	 */
	@Override
	public void sendResponse(Throwable error) throws IOException {
		CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
		BytesStreamOutput stream;
		try {
			stream = cachedEntry.cachedBytes();
			writeResponseExceptionHeader(stream);
			RemoteTransportException tx = new RemoteTransportException(transport.nodeName(),
					transport.wrapAddress(channel.getLocalAddress()), action, error);
			ThrowableObjectOutputStream too = new ThrowableObjectOutputStream(stream);
			too.writeObject(tx);
			too.close();
		} catch (NotSerializableException e) {
			stream = cachedEntry.cachedBytes();
			writeResponseExceptionHeader(stream);
			RemoteTransportException tx = new RemoteTransportException(transport.nodeName(),
					transport.wrapAddress(channel.getLocalAddress()), action, new NotSerializableTransportException(
							error));
			ThrowableObjectOutputStream too = new ThrowableObjectOutputStream(stream);
			too.writeObject(tx);
			too.close();
		}
		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(stream.underlyingBytes(), 0, stream.size());
		buffer.setInt(0, buffer.writerIndex() - 4);
		ChannelFuture future = channel.write(buffer);
		future.addListener(new NettyTransport.CacheFutureListener(cachedEntry));
	}

	/**
	 * Write response exception header.
	 *
	 * @param stream the stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeResponseExceptionHeader(BytesStreamOutput stream) throws IOException {
		stream.writeBytes(LENGTH_PLACEHOLDER);
		stream.writeLong(requestId);
		byte status = 0;
		status = TransportStreams.statusSetResponse(status);
		status = TransportStreams.statusSetError(status);
		stream.writeByte(status);
	}
}
