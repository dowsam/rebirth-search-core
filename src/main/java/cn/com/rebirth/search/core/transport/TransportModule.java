/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportModule.java 2012-3-29 15:01:12 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.inject.Modules;
import cn.com.rebirth.search.commons.inject.SpawnModules;
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
	 * @see cn.com.summall.search.commons.inject.SpawnModules#spawnModules()
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
				"cn.com.summall.search.core.transport.", "TransportModule"), settings));
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(TransportService.class).asEagerSingleton();
		if (JmxService.shouldExport(settings)) {
			bind(TransportServiceManagement.class).asEagerSingleton();
		}
	}
}