/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FsGatewayModule.java 2012-7-6 14:29:07 l.xue.nong$$
 */

package cn.com.rebirth.search.core.gateway.fs;

import cn.com.rebirth.search.core.gateway.Gateway;
import cn.com.rebirth.search.core.gateway.blobstore.BlobStoreGatewayModule;

/**
 * The Class FsGatewayModule.
 *
 * @author l.xue.nong
 */
public class FsGatewayModule extends BlobStoreGatewayModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(Gateway.class).to(FsGateway.class).asEagerSingleton();
	}
}
