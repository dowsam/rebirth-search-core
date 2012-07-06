/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PlainOperationRoutingModule.java 2012-7-6 14:29:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing.operation.plain;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.cluster.routing.operation.OperationRouting;

/**
 * The Class PlainOperationRoutingModule.
 *
 * @author l.xue.nong
 */
public class PlainOperationRoutingModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(OperationRouting.class).to(PlainOperationRouting.class).asEagerSingleton();
	}
}
