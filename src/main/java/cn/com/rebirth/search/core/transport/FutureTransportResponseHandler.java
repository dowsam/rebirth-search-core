/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FutureTransportResponseHandler.java 2012-7-6 14:28:59 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.core.threadpool.ThreadPool;

/**
 * The Class FutureTransportResponseHandler.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public abstract class FutureTransportResponseHandler<T extends Streamable> extends BaseTransportResponseHandler<T> {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.transport.TransportResponseHandler#handleResponse(cn.com.rebirth.commons.io.stream.Streamable)
	 */
	@Override
	public void handleResponse(T response) {
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
		return ThreadPool.Names.SAME;
	}
}
