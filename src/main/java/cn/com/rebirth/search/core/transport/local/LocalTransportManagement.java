/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LocalTransportManagement.java 2012-7-6 14:29:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport.local;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.jmx.MBean;
import cn.com.rebirth.search.core.transport.Transport;

/**
 * The Class LocalTransportManagement.
 *
 * @author l.xue.nong
 */
@MBean(objectName = "service=transport,transportType=local", description = "Local Transport")
public class LocalTransportManagement {

	/**
	 * Instantiates a new local transport management.
	 *
	 * @param transport the transport
	 */
	@Inject
	public LocalTransportManagement(Transport transport) {
	}
}