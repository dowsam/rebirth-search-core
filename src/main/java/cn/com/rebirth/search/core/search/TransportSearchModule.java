/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportSearchModule.java 2012-3-29 15:00:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.inject.SpawnModules;
import cn.com.rebirth.search.core.search.facet.TransportFacetModule;

import com.google.common.collect.ImmutableList;

/**
 * The Class TransportSearchModule.
 *
 * @author l.xue.nong
 */
public class TransportSearchModule extends AbstractModule implements SpawnModules {

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.SpawnModules#spawnModules()
	 */
	@Override
	public Iterable<? extends Module> spawnModules() {
		return ImmutableList.of(new TransportFacetModule());
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {

	}
}
