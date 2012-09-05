/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DiscoveryModule.java 2012-7-6 14:30:02 l.xue.nong$$
 */

package cn.com.rebirth.search.core.discovery;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.core.inject.Module;
import cn.com.rebirth.core.inject.Modules;
import cn.com.rebirth.core.inject.SpawnModules;
import cn.com.rebirth.search.core.discovery.local.LocalDiscoveryModule;
import cn.com.rebirth.search.core.discovery.zen.ZenDiscoveryModule;

import com.google.common.collect.ImmutableList;

/**
 * The Class DiscoveryModule.
 *
 * @author l.xue.nong
 */
public class DiscoveryModule extends AbstractModule implements SpawnModules {

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new discovery module.
	 *
	 * @param settings the settings
	 */
	public DiscoveryModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.SpawnModules#spawnModules()
	 */
	@Override
	public Iterable<? extends Module> spawnModules() {
		Class<? extends Module> defaultDiscoveryModule;
		if (settings.getAsBoolean("node.local", false)) {
			defaultDiscoveryModule = LocalDiscoveryModule.class;
		} else {
			defaultDiscoveryModule = ZenDiscoveryModule.class;
		}
		return ImmutableList.of(Modules.createModule(settings.getAsClass("discovery.type", defaultDiscoveryModule,
				"cn.com.rebirth.search.core.discovery.", "DiscoveryModule"), settings));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(DiscoveryService.class).asEagerSingleton();
	}
}