/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PlainTransportFuture.java 2012-3-29 15:01:13 l.xue.nong$$
 */


package cn.com.rebirth.search.core.transport;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cn.com.rebirth.commons.concurrent.BaseFuture;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.exception.RestartInterruptedException;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.RestartTimeoutException;


/**
 * The Class PlainTransportFuture.
 *
 * @param <V> the value type
 * @author l.xue.nong
 */
public class PlainTransportFuture<V extends Streamable> extends BaseFuture<V> implements TransportFuture<V>,
		TransportResponseHandler<V> {

	
	/** The handler. */
	private final TransportResponseHandler<V> handler;

	
	/**
	 * Instantiates a new plain transport future.
	 *
	 * @param handler the handler
	 */
	public PlainTransportFuture(TransportResponseHandler<V> handler) {
		this.handler = handler;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.transport.TransportFuture#txGet()
	 */
	@Override
	public V txGet() throws RestartException {
		try {
			return get();
		} catch (InterruptedException e) {
			throw new RestartInterruptedException(e.getMessage());
		} catch (ExecutionException e) {
			if (e.getCause() instanceof RestartException) {
				throw (RestartException) e.getCause();
			} else {
				throw new TransportException("Failed execution", e);
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.transport.TransportFuture#txGet(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public V txGet(long timeout, TimeUnit unit) throws RestartException {
		try {
			return get(timeout, unit);
		} catch (TimeoutException e) {
			throw new RestartTimeoutException(e.getMessage());
		} catch (InterruptedException e) {
			throw new RestartInterruptedException(e.getMessage());
		} catch (ExecutionException e) {
			if (e.getCause() instanceof RestartException) {
				throw (RestartException) e.getCause();
			} else {
				throw new TransportException("Failed execution", e);
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.transport.TransportResponseHandler#newInstance()
	 */
	@Override
	public V newInstance() {
		return handler.newInstance();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.transport.TransportResponseHandler#executor()
	 */
	@Override
	public String executor() {
		return handler.executor();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.transport.TransportResponseHandler#handleResponse(cn.com.summall.search.commons.io.stream.Streamable)
	 */
	@Override
	public void handleResponse(V response) {
		handler.handleResponse(response);
		set(response);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.transport.TransportResponseHandler#handleException(cn.com.summall.search.core.transport.TransportException)
	 */
	@Override
	public void handleException(TransportException exp) {
		handler.handleException(exp);
		setException(exp);
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "future(" + handler.toString() + ")";
	}
}
