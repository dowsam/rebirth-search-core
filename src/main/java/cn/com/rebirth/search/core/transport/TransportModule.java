/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportModule.java 2012-7-6 14:30:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.core.inject.Module;
import cn.com.rebirth.core.inject.Modules;
import cn.com.rebirth.core.inject.SpawnModules;
import cn.com.rebirth.search.core.jmx.JmxService;
import cn.com.rebirth.search.core.transport.local.LocalTransportModule;
import cn.com.rebirth.search.core.transport.netty.NettyTransportModule;

import com.google.common.collect.ImmutableList;

/**
 * The Class TransportModule.
 *
 * @author l.xue.nong
 */
public class TransportModule extends AbstractModule implements SpawnModules {

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new transport module.
	 *
	 * @param settings the settings
	 */
	public TransportModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.SpawnModules#spawnModules()
	 */
	@Override
	public Iterable<? extends Module> spawnModules() {
		Class<? extends Module> defaultTransportModule;
		if (settings.getAsBoolean("node.local", false)) {
			defaultTransportModule = LocalTransportModule.class;
		} else {
			defaultTransportModule = NettyTransportModule.class;
		}
		return ImmutableList.of(Modules.createModule(settings.getAsClass("transport.type", defaultTransportModule,
				"cn.com.rebirth.search.core.transport.", "TransportModule"), settings));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(TransportService.class).asEagerSingleton();
		if (JmxService.shouldExport(settings)) {
			bind(TransportServiceManagement.class).asEagerSingleton();
		}
	}
}