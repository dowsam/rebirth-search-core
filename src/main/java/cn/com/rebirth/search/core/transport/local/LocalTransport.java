/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LocalTransport.java 2012-7-6 14:29:49 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport.local;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.commons.concurrent.ConcurrentCollections;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.io.ThrowableObjectInputStream;
import cn.com.rebirth.commons.io.stream.BytesStreamInput;
import cn.com.rebirth.commons.io.stream.BytesStreamOutput;
import cn.com.rebirth.commons.io.stream.CachedStreamInput;
import cn.com.rebirth.commons.io.stream.CachedStreamOutput;
import cn.com.rebirth.commons.io.stream.HandlesStreamOutput;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.ImmutableSettings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.commons.transport.BoundTransportAddress;
import cn.com.rebirth.search.commons.transport.LocalTransportAddress;
import cn.com.rebirth.search.commons.transport.TransportAddress;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.transport.ActionNotFoundTransportException;
import cn.com.rebirth.search.core.transport.ConnectTransportException;
import cn.com.rebirth.search.core.transport.NodeNotConnectedException;
import cn.com.rebirth.search.core.transport.RemoteTransportException;
import cn.com.rebirth.search.core.transport.ResponseHandlerFailureTransportException;
import cn.com.rebirth.search.core.transport.Transport;
import cn.com.rebirth.search.core.transport.TransportException;
import cn.com.rebirth.search.core.transport.TransportRequestHandler;
import cn.com.rebirth.search.core.transport.TransportRequestOptions;
import cn.com.rebirth.search.core.transport.TransportResponseHandler;
import cn.com.rebirth.search.core.transport.TransportSerializationException;
import cn.com.rebirth.search.core.transport.TransportServiceAdapter;
import cn.com.rebirth.search.core.transport.support.TransportStreams;

/**
 * The Class LocalTransport.
 *
 * @author l.xue.nong
 */
public class LocalTransport extends AbstractLifecycleComponent<Transport> implements Transport {

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The transport service adapter. */
	private volatile TransportServiceAdapter transportServiceAdapter;

	/** The bound address. */
	private volatile BoundTransportAddress boundAddress;

	/** The local address. */
	private volatile LocalTransportAddress localAddress;

	/** The Constant transports. */
	private final static ConcurrentMap<TransportAddress, LocalTransport> transports = ConcurrentCollections
			.newConcurrentMap();

	/** The Constant transportAddressIdGenerator. */
	private static final AtomicLong transportAddressIdGenerator = new AtomicLong();

	/** The connected nodes. */
	private final ConcurrentMap<DiscoveryNode, LocalTransport> connectedNodes = ConcurrentCollections
			.newConcurrentMap();

	/**
	 * Instantiates a new local transport.
	 *
	 * @param threadPool the thread pool
	 */
	public LocalTransport(ThreadPool threadPool) {
		this(ImmutableSettings.Builder.EMPTY_SETTINGS, threadPool);
	}

	/**
	 * Instantiates a new local transport.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 */
	@Inject
	public LocalTransport(Settings settings, ThreadPool threadPool) {
		super(settings);
		this.threadPool = threadPool;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#addressesFromString(java.lang.String)
	 */
	@Override
	public TransportAddress[] addressesFromString(String address) {
		return new TransportAddress[] { new LocalTransportAddress(address) };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#addressSupported(java.lang.Class)
	 */
	@Override
	public boolean addressSupported(Class<? extends TransportAddress> address) {
		return LocalTransportAddress.class.equals(address);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RebirthException {
		localAddress = new LocalTransportAddress(Long.toString(transportAddressIdGenerator.incrementAndGet()));
		transports.put(localAddress, this);
		boundAddress = new BoundTransportAddress(localAddress, localAddress);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RebirthException {
		transports.remove(localAddress);

		for (final LocalTransport targetTransport : transports.values()) {
			for (final Map.Entry<DiscoveryNode, LocalTransport> entry : targetTransport.connectedNodes.entrySet()) {
				if (entry.getValue() == this) {
					targetTransport.disconnectFromNode(entry.getKey());
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RebirthException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#transportServiceAdapter(cn.com.rebirth.search.core.transport.TransportServiceAdapter)
	 */
	@Override
	public void transportServiceAdapter(TransportServiceAdapter transportServiceAdapter) {
		this.transportServiceAdapter = transportServiceAdapter;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#boundAddress()
	 */
	@Override
	public BoundTransportAddress boundAddress() {
		return boundAddress;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#nodeConnected(cn.com.rebirth.search.core.cluster.node.DiscoveryNode)
	 */
	@Override
	public boolean nodeConnected(DiscoveryNode node) {
		return connectedNodes.containsKey(node);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#connectToNodeLight(cn.com.rebirth.search.core.cluster.node.DiscoveryNode)
	 */
	@Override
	public void connectToNodeLight(DiscoveryNode node) throws ConnectTransportException {
		connectToNode(node);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#connectToNode(cn.com.rebirth.search.core.cluster.node.DiscoveryNode)
	 */
	@Override
	public void connectToNode(DiscoveryNode node) throws ConnectTransportException {
		synchronized (this) {
			if (connectedNodes.containsKey(node)) {
				return;
			}
			final LocalTransport targetTransport = transports.get(node.address());
			if (targetTransport == null) {
				throw new ConnectTransportException(node, "Failed to connect");
			}
			connectedNodes.put(node, targetTransport);
			transportServiceAdapter.raiseNodeConnected(node);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#disconnectFromNode(cn.com.rebirth.search.core.cluster.node.DiscoveryNode)
	 */
	@Override
	public void disconnectFromNode(DiscoveryNode node) {
		synchronized (this) {
			LocalTransport removed = connectedNodes.remove(node);
			if (removed != null) {
				transportServiceAdapter.raiseNodeDisconnected(node);
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#serverOpen()
	 */
	@Override
	public long serverOpen() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.Transport#sendRequest(cn.com.rebirth.search.core.cluster.node.DiscoveryNode, long, java.lang.String, cn.com.rebirth.commons.io.stream.Streamable, cn.com.rebirth.search.core.transport.TransportRequestOptions)
	 */
	@Override
	public <T extends Streamable> void sendRequest(final DiscoveryNode node, final long requestId, final String action,
			final Streamable message, TransportRequestOptions options) throws IOException, TransportException {
		CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
		try {
			HandlesStreamOutput stream = cachedEntry.cachedHandlesBytes();

			stream.writeLong(requestId);
			byte status = 0;
			status = TransportStreams.statusSetRequest(status);
			stream.writeByte(status);

			stream.writeUTF(action);
			message.writeTo(stream);

			final LocalTransport targetTransport = connectedNodes.get(node);
			if (targetTransport == null) {
				throw new NodeNotConnectedException(node, "Node not connected");
			}

			final byte[] data = ((BytesStreamOutput) stream.wrappedOut()).copiedByteArray();

			transportServiceAdapter.sent(data.length);

			threadPool.generic().execute(new Runnable() {
				@Override
				public void run() {
					targetTransport.messageReceived(data, action, LocalTransport.this, requestId);
				}
			});
		} finally {
			CachedStreamOutput.pushEntry(cachedEntry);
		}
	}

	/**
	 * Thread pool.
	 *
	 * @return the thread pool
	 */
	ThreadPool threadPool() {
		return this.threadPool;
	}

	/**
	 * Message received.
	 *
	 * @param data the data
	 * @param action the action
	 * @param sourceTransport the source transport
	 * @param sendRequestId the send request id
	 */
	void messageReceived(byte[] data, String action, LocalTransport sourceTransport, @Nullable final Long sendRequestId) {
		transportServiceAdapter.received(data.length);
		StreamInput stream = new BytesStreamInput(data, false);
		stream = CachedStreamInput.cachedHandles(stream);

		try {
			long requestId = stream.readLong();
			byte status = stream.readByte();
			boolean isRequest = TransportStreams.statusIsRequest(status);

			if (isRequest) {
				handleRequest(stream, requestId, sourceTransport);
			} else {
				final TransportResponseHandler handler = transportServiceAdapter.remove(requestId);

				if (handler != null) {
					if (TransportStreams.statusIsError(status)) {
						handlerResponseError(stream, handler);
					} else {
						handleResponse(stream, handler);
					}
				}
			}
		} catch (Exception e) {
			if (sendRequestId != null) {
				TransportResponseHandler handler = transportServiceAdapter.remove(sendRequestId);
				if (handler != null) {
					handler.handleException(new RemoteTransportException(nodeName(), localAddress, action, e));
				}
			} else {
				logger.warn("Failed to receive message for action [" + action + "]", e);
			}
		}
	}

	/**
	 * Handle request.
	 *
	 * @param stream the stream
	 * @param requestId the request id
	 * @param sourceTransport the source transport
	 * @throws Exception the exception
	 */
	private void handleRequest(StreamInput stream, long requestId, LocalTransport sourceTransport) throws Exception {
		final String action = stream.readUTF();
		final LocalTransportChannel transportChannel = new LocalTransportChannel(this, sourceTransport, action,
				requestId);
		try {
			final TransportRequestHandler handler = transportServiceAdapter.handler(action);
			if (handler == null) {
				throw new ActionNotFoundTransportException("Action [" + action + "] not found");
			}
			final Streamable streamable = handler.newInstance();
			streamable.readFrom(stream);
			handler.messageReceived(streamable, transportChannel);
		} catch (Exception e) {
			try {
				transportChannel.sendResponse(e);
			} catch (IOException e1) {
				logger.warn("Failed to send error message back to client for action [" + action + "]", e);
				logger.warn("Actual Exception", e1);
			}
		}
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
		threadPool.executor(handler.executor()).execute(new Runnable() {
			@SuppressWarnings({ "unchecked" })
			@Override
			public void run() {
				try {
					handler.handleResponse(streamable);
				} catch (Exception e) {
					handleException(handler, new ResponseHandlerFailureTransportException(e));
				}
			}
		});
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
			ThrowableObjectInputStream ois = new ThrowableObjectInputStream(buffer, settings.getClassLoader());
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
			error = new RemoteTransportException("None remote transport exception", error);
		}
		final RemoteTransportException rtx = (RemoteTransportException) error;
		handler.handleException(rtx);
	}
}
