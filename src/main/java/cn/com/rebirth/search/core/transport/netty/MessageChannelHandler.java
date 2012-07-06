/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MessageChannelHandler.java 2012-7-6 14:28:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport.netty;

import java.io.IOException;
import java.io.StreamCorruptedException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.rebirth.commons.io.stream.CachedStreamInput;
import cn.com.rebirth.commons.io.stream.HandlesStreamInput;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.io.ThrowableObjectInputStream;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.ActionNotFoundTransportException;
import cn.com.rebirth.search.core.transport.RemoteTransportException;
import cn.com.rebirth.search.core.transport.ResponseHandlerFailureTransportException;
import cn.com.rebirth.search.core.transport.TransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportResponseHandler;
import cn.com.rebirth.search.core.transport.TransportSerializationException;
import cn.com.rebirth.search.core.transport.TransportServiceAdapter;
import cn.com.rebirth.search.core.transport.support.TransportStreams;

/**
 * The Class MessageChannelHandler.
 *
 * @author l.xue.nong
 */
public class MessageChannelHandler extends SimpleChannelUpstreamHandler {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The transport service adapter. */
	private final TransportServiceAdapter transportServiceAdapter;

	/** The transport. */
	private final NettyTransport transport;

	/** The cumulation. */
	private ChannelBuffer cumulation;

	/**
	 * Instantiates a new message channel handler.
	 *
	 * @param transport the transport
	 */
	public MessageChannelHandler(NettyTransport transport) {
		this.threadPool = transport.threadPool();
		this.transportServiceAdapter = transport.transportServiceAdapter();
		this.transport = transport;
	}

	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#writeComplete(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.WriteCompletionEvent)
	 */
	@Override
	public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
		transportServiceAdapter.sent(e.getWrittenAmount());
		super.writeComplete(ctx, e);
	}

	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

		Object m = e.getMessage();
		if (!(m instanceof ChannelBuffer)) {
			ctx.sendUpstream(e);
			return;
		}

		ChannelBuffer input = (ChannelBuffer) m;
		if (!input.readable()) {
			return;
		}

		ChannelBuffer cumulation = this.cumulation;
		if (cumulation != null && cumulation.readable()) {
			cumulation.discardReadBytes();
			cumulation.writeBytes(input);
			callDecode(ctx, e.getChannel(), cumulation, true);
		} else {
			int actualSize = callDecode(ctx, e.getChannel(), input, false);
			if (input.readable()) {
				if (actualSize > 0) {
					cumulation = ChannelBuffers.dynamicBuffer(actualSize, ctx.getChannel().getConfig()
							.getBufferFactory());
				} else {
					cumulation = ChannelBuffers.dynamicBuffer(ctx.getChannel().getConfig().getBufferFactory());
				}
				cumulation.writeBytes(input);
				this.cumulation = cumulation;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelDisconnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		cleanup(ctx, e);
	}

	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelClosed(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		cleanup(ctx, e);
	}

	/**
	 * Call decode.
	 *
	 * @param ctx the ctx
	 * @param channel the channel
	 * @param buffer the buffer
	 * @param cumulationBuffer the cumulation buffer
	 * @return the int
	 * @throws Exception the exception
	 */
	private int callDecode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer, boolean cumulationBuffer)
			throws Exception {
		int actualSize = 0;
		while (buffer.readable()) {
			actualSize = 0;

			if (buffer.readableBytes() < 4) {
				break;
			}

			int dataLen = buffer.getInt(buffer.readerIndex());
			if (dataLen <= 0) {
				throw new StreamCorruptedException("invalid data length: " + dataLen);
			}

			actualSize = dataLen + 4;
			if (buffer.readableBytes() < actualSize) {
				break;
			}

			buffer.skipBytes(4);

			process(ctx, channel, buffer, dataLen);
		}

		if (cumulationBuffer) {
			assert buffer == this.cumulation;
			if (!buffer.readable()) {
				this.cumulation = null;
			} else if (buffer.readerIndex() > 0) {

				if (actualSize > 0) {
					this.cumulation = ChannelBuffers.dynamicBuffer(actualSize, ctx.getChannel().getConfig()
							.getBufferFactory());
				} else {
					this.cumulation = ChannelBuffers.dynamicBuffer(ctx.getChannel().getConfig().getBufferFactory());
				}
				this.cumulation.writeBytes(buffer);
			}
		}

		return actualSize;
	}

	/**
	 * Cleanup.
	 *
	 * @param ctx the ctx
	 * @param e the e
	 * @throws Exception the exception
	 */
	private void cleanup(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		try {
			ChannelBuffer cumulation = this.cumulation;
			if (cumulation == null) {
				return;
			} else {
				this.cumulation = null;
			}

			if (cumulation.readable()) {

				callDecode(ctx, ctx.getChannel(), cumulation, true);
			}

		} finally {
			ctx.sendUpstream(e);
		}
	}

	/**
	 * Process.
	 *
	 * @param ctx the ctx
	 * @param channel the channel
	 * @param buffer the buffer
	 * @param size the size
	 * @throws Exception the exception
	 */
	private void process(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer, int size) throws Exception {
		transportServiceAdapter.received(size + 4);

		int markedReaderIndex = buffer.readerIndex();
		int expectedIndexReader = markedReaderIndex + size;

		StreamInput streamIn = new ChannelBufferStreamInput(buffer, size);

		long requestId = buffer.readLong();
		byte status = buffer.readByte();
		boolean isRequest = TransportStreams.statusIsRequest(status);

		HandlesStreamInput wrappedStream;
		if (TransportStreams.statusIsCompress(status)) {
			wrappedStream = CachedStreamInput.cachedHandlesLzf(streamIn);
		} else {
			wrappedStream = CachedStreamInput.cachedHandles(streamIn);
		}

		if (isRequest) {
			String action = handleRequest(channel, wrappedStream, requestId);
			if (buffer.readerIndex() != expectedIndexReader) {
				if (buffer.readerIndex() < expectedIndexReader) {
					logger.warn("Message not fully read (request) for [{}] and action [{}], resetting", requestId,
							action);
				} else {
					logger.warn("Message read past expected size (request) for [{}] and action [{}], resetting",
							requestId, action);
				}
				buffer.readerIndex(expectedIndexReader);
			}
		} else {
			TransportResponseHandler handler = transportServiceAdapter.remove(requestId);

			if (handler != null) {
				if (TransportStreams.statusIsError(status)) {
					handlerResponseError(wrappedStream, handler);
				} else {
					handleResponse(wrappedStream, handler);
				}
			} else {

				buffer.readerIndex(markedReaderIndex + size);
			}
			if (buffer.readerIndex() != expectedIndexReader) {
				if (buffer.readerIndex() < expectedIndexReader) {
					logger.warn("Message not fully read (response) for [" + requestId + "] handler " + handler
							+ ", error [" + TransportStreams.statusIsError(status) + "], resetting");
				} else {
					logger.warn("Message read past expected size (response) for [" + requestId + "] handler " + handler
							+ ", error [" + TransportStreams.statusIsError(status) + "], resetting");
				}
				buffer.readerIndex(expectedIndexReader);
			}
		}
		wrappedStream.cleanHandles();
	}

	/**
	 * Handle response.
	 *
	 * @param buffer the buffer
	 * @param handler the handler
	 */
	private void handleResponse(StreamInput buffer, final TransportResponseHandler handler) {
		final Streamable streamable = handler.newInstance();
		try {
			streamable.readFrom(buffer);
		} catch (Exception e) {
			handleException(handler, new TransportSerializationException("Failed to deserialize response of type ["
					+ streamable.getClass().getName() + "]", e));
			return;
		}
		try {
			if (handler.executor() == ThreadPool.Names.SAME) {

				handler.handleResponse(streamable);
			} else {
				threadPool.executor(handler.executor()).execute(new ResponseHandler(handler, streamable));
			}
		} catch (Exception e) {
			handleException(handler, new ResponseHandlerFailureTransportException(e));
		}
	}

	/**
	 * Handler response error.
	 *
	 * @param buffer the buffer
	 * @param handler the handler
	 */
	private void handlerResponseError(StreamInput buffer, final TransportResponseHandler handler) {
		Throwable error;
		try {
			ThrowableObjectInputStream ois = new ThrowableObjectInputStream(buffer, transport.settings()
					.getClassLoader());
			error = (Throwable) ois.readObject();
		} catch (Exception e) {
			error = new TransportSerializationException("Failed to deserialize exception response from stream", e);
		}
		handleException(handler, error);
	}

	/**
	 * Handle exception.
	 *
	 * @param handler the handler
	 * @param error the error
	 */
	private void handleException(final TransportResponseHandler handler, Throwable error) {
		if (!(error instanceof RemoteTransportException)) {
			error = new RemoteTransportException(error.getMessage(), error);
		}
		final RemoteTransportException rtx = (RemoteTransportException) error;
		if (handler.executor() == ThreadPool.Names.SAME) {
			handler.handleException(rtx);
		} else {
			threadPool.executor(handler.executor()).execute(new Runnable() {
				@Override
				public void run() {
					try {
						handler.handleException(rtx);
					} catch (Exception e) {
						logger.error("Failed to handle exception response", e);
					}
				}
			});
		}
	}

	/**
	 * Handle request.
	 *
	 * @param channel the channel
	 * @param buffer the buffer
	 * @param requestId the request id
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String handleRequest(Channel channel, StreamInput buffer, long requestId) throws IOException {
		final String action = buffer.readUTF();

		final NettyTransportChannel transportChannel = new NettyTransportChannel(transport, action, channel, requestId);
		try {
			final TransportRequestHandler handler = transportServiceAdapter.handler(action);
			if (handler == null) {
				throw new ActionNotFoundTransportException(action);
			}
			final Streamable streamable = handler.newInstance();
			streamable.readFrom(buffer);
			if (handler.executor() == ThreadPool.Names.SAME) {

				handler.messageReceived(streamable, transportChannel);
			} else {
				threadPool.executor(handler.executor()).execute(
						new RequestHandler(handler, streamable, transportChannel, action));
			}
		} catch (Exception e) {
			try {
				transportChannel.sendResponse(e);
			} catch (IOException e1) {
				logger.warn("Failed to send error message back to client for action [" + action + "]", e);
				logger.warn("Actual Exception", e1);
			}
		}
		return action;
	}

	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		transport.exceptionCaught(ctx, e);
	}

	/**
	 * The Class ResponseHandler.
	 *
	 * @author l.xue.nong
	 */
	class ResponseHandler implements Runnable {

		/** The handler. */
		private final TransportResponseHandler handler;

		/** The streamable. */
		private final Streamable streamable;

		/**
		 * Instantiates a new response handler.
		 *
		 * @param handler the handler
		 * @param streamable the streamable
		 */
		public ResponseHandler(TransportResponseHandler handler, Streamable streamable) {
			this.handler = handler;
			this.streamable = streamable;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@SuppressWarnings({ "unchecked" })
		@Override
		public void run() {
			try {
				handler.handleResponse(streamable);
			} catch (Exception e) {
				handleException(handler, new ResponseHandlerFailureTransportException(e));
			}
		}
	}

	/**
	 * The Class RequestHandler.
	 *
	 * @author l.xue.nong
	 */
	class RequestHandler implements Runnable {

		/** The handler. */
		private final TransportRequestHandler handler;

		/** The streamable. */
		private final Streamable streamable;

		/** The transport channel. */
		private final NettyTransportChannel transportChannel;

		/** The action. */
		private final String action;

		/**
		 * Instantiates a new request handler.
		 *
		 * @param handler the handler
		 * @param streamable the streamable
		 * @param transportChannel the transport channel
		 * @param action the action
		 */
		public RequestHandler(TransportRequestHandler handler, Streamable streamable,
				NettyTransportChannel transportChannel, String action) {
			this.handler = handler;
			this.streamable = streamable;
			this.transportChannel = transportChannel;
			this.action = action;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@SuppressWarnings({ "unchecked" })
		@Override
		public void run() {
			try {
				handler.messageReceived(streamable, transportChannel);
			} catch (Throwable e) {
				try {
					transportChannel.sendResponse(e);
				} catch (IOException e1) {
					logger.warn("Failed to send error message back to client for action [" + action + "]", e1);
					logger.warn("Actual Exception", e);
				}
			}
		}
	}
}
