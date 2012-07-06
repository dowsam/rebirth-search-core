/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core HttpServerTransport.java 2012-4-25 10:02:49 l.xue.nong$$
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
