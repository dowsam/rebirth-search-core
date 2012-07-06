/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BlobStoreGatewayModule.java 2012-7-6 14:30:35 l.xue.nong$$
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
	 * @see cn.com.rebirth.search.commons.inject.PreProcessModule#processModule(cn.com.rebirth.search.commons.inject.Module)
	 */
	@Override
	public void processModule(Module module) {
		if (module instanceof ShardsAllocatorModule) {
			((ShardsAllocatorModule) module).setGatewayAllocator(BlobReuseExistingGatewayAllocator.class);
		}
	}
}
