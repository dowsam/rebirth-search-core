/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PlainOperationRoutingModule.java 2012-3-29 15:02:09 l.xue.nong$$
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
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(OperationRouting.class).to(PlainOperationRouting.class).asEagerSingleton();
	}
}
