/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NettyTransportModule.java 2012-7-6 14:30:41 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport.netty;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.jmx.JmxService;
import cn.com.rebirth.search.core.transport.Transport;

/**
 * The Class NettyTransportModule.
 *
 * @author l.xue.nong
 */
public class NettyTransportModule extends AbstractModule {

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new netty transport module.
	 *
	 * @param settings the settings
	 */
	public NettyTransportModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(NettyTransport.class).asEagerSingleton();
		bind(Transport.class).to(NettyTransport.class).asEagerSingleton();
		if (JmxService.shouldExport(settings)) {
			bind(NettyTransportManagement.class).asEagerSingleton();
		}
	}
}
