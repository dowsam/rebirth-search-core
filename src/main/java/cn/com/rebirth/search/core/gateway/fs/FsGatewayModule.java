/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FsGatewayModule.java 2012-3-29 15:02:32 l.xue.nong$$
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
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(Gateway.class).to(FsGateway.class).asEagerSingleton();
	}
}
