/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MapperServiceModule.java 2012-7-6 14:30:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper;

import cn.com.rebirth.search.commons.inject.AbstractModule;

/**
 * The Class MapperServiceModule.
 *
 * @author l.xue.nong
 */
public class MapperServiceModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(MapperService.class).asEagerSingleton();
	}
}
