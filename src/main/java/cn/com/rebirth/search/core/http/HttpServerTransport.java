/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core HttpServerTransport.java 2012-7-6 14:30:26 l.xue.nong$$
 */

package cn.com.rebirth.search.core.http;

import cn.com.rebirth.search.commons.component.LifecycleComponent;
import cn.com.rebirth.search.commons.transport.BoundTransportAddress;

/**
 * The Interface HttpServerTransport.
 *
 * @author l.xue.nong
 */
public interface HttpServerTransport extends LifecycleComponent<HttpServerTransport> {

	/**
	 * Bound address.
	 *
	 * @return the bound transport address
	 */
	BoundTransportAddress boundAddress();

	/**
	 * Stats.
	 *
	 * @return the http stats
	 */
	HttpStats stats();

	/**
	 * Http server adapter.
	 *
	 * @param httpServerAdapter the http server adapter
	 */
	void httpServerAdapter(HttpServerAdapter httpServerAdapter);
}
