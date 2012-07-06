/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BlobStoreGatewayModule.java 2012-3-29 15:01:16 l.xue.nong$$
 */

package cn.com.rebirth.search.core.gateway.blobstore;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.inject.PreProcessModule;
import cn.com.rebirth.search.core.cluster.routing.allocation.allocator.ShardsAllocatorModule;

/**
 * The Class BlobStoreGatewayModule.
 *
 * @author l.xue.nong
 */
public abstract class BlobStoreGatewayModule extends AbstractModule implements PreProcessModule {

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.PreProcessModule#processModule(cn.com.summall.search.commons.inject.Module)
	 */
	@Override
	public void processModule(Module module) {
		if (module instanceof ShardsAllocatorModule) {
			((ShardsAllocatorModule) module).setGatewayAllocator(BlobReuseExistingGatewayAllocator.class);
		}
	}
}
