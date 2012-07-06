/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DiscoveryModule.java 2012-3-29 15:02:28 l.xue.nong$$
 */


package cn.com.rebirth.search.core.discovery;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.inject.Modules;
import cn.com.rebirth.search.commons.inject.SpawnModules;
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
	 * @see cn.com.summall.search.commons.inject.SpawnModules#spawnModules()
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
				"cn.com.summall.search.core.discovery.", "DiscoveryModule"), settings));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(DiscoveryService.class).asEagerSingleton();
	}
}