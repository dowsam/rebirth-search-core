/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AllocationModule.java 2012-7-6 14:28:59 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing.allocation;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.inject.SpawnModules;
import cn.com.rebirth.search.core.cluster.routing.allocation.allocator.ShardsAllocatorModule;
import cn.com.rebirth.search.core.cluster.routing.allocation.decider.AllocationDecidersModule;

import com.google.common.collect.ImmutableList;

/**
 * The Class AllocationModule.
 *
 * @author l.xue.nong
 */
public class AllocationModule extends AbstractModule implements SpawnModules {

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new allocation module.
	 *
	 * @param settings the settings
	 */
	public AllocationModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.SpawnModules#spawnModules()
	 */
	@Override
	public Iterable<? extends Module> spawnModules() {
		return ImmutableList.of(new ShardsAllocatorModule(settings), new AllocationDecidersModule(settings));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(AllocationService.class).asEagerSingleton();
	}
}
