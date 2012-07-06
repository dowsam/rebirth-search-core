/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GatewayModule.java 2012-7-6 14:30:17 l.xue.nong$$
 */

package cn.com.rebirth.search.core.gateway;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.inject.Modules;
import cn.com.rebirth.search.commons.inject.SpawnModules;
import cn.com.rebirth.search.core.gateway.local.LocalGatewayModule;

import com.google.common.collect.ImmutableList;

/**
 * The Class GatewayModule.
 *
 * @author l.xue.nong
 */
public class GatewayModule extends AbstractModule implements SpawnModules {

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new gateway module.
	 *
	 * @param settings the settings
	 */
	public GatewayModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.SpawnModules#spawnModules()
	 */
	@Override
	public Iterable<? extends Module> spawnModules() {
		return ImmutableList.of(Modules.createModule(settings.getAsClass("gateway.type", LocalGatewayModule.class,
				"cn.com.rebirth.search.core.gateway.", "GatewayModule"), settings));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(GatewayService.class).asEagerSingleton();
	}
}
