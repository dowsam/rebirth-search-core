/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core VoidTransportResponseHandler.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.search.commons.io.stream.VoidStreamable;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

/**
 * The Class VoidTransportResponseHandler.
 *
 * @author l.xue.nong
 */
public class VoidTransportResponseHandler implements TransportResponseHandler<VoidStreamable> {

	/** The Constant INSTANCE_SAME. */
	public static final VoidTransportResponseHandler INSTANCE_SAME = new VoidTransportResponseHandler(
			ThreadPool.Names.SAME);

	/** The executor. */
	private final String executor;

	/**
	 * Instantiates a new void transport response handler.
	 *
	 * @param executor the executor
	 */
	public VoidTransportResponseHandler(String executor) {
		this.executor = executor;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.TransportResponseHandler#newInstance()
	 */
	@Override
	public VoidStreamable newInstance() {
		return VoidStreamable.INSTANCE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.TransportResponseHandler#handleResponse(cn.com.rebirth.commons.io.stream.Streamable)
	 */
	@Override
	public void handleResponse(VoidStreamable response) {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.TransportResponseHandler#handleException(cn.com.rebirth.search.core.transport.TransportException)
	 */
	@Override
	public void handleException(TransportException exp) {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.TransportResponseHandler#executor()
	 */
	@Override
	public String executor() {
		return executor;
	}
}
