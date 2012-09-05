/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LocalTransportModule.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.transport.local;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.search.core.jmx.JmxService;
import cn.com.rebirth.search.core.transport.Transport;

/**
 * The Class LocalTransportModule.
 *
 * @author l.xue.nong
 */
public class LocalTransportModule extends AbstractModule {

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new local transport module.
	 *
	 * @param settings the settings
	 */
	public LocalTransportModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(LocalTransport.class).asEagerSingleton();
		bind(Transport.class).to(LocalTransport.class).asEagerSingleton();
		if (JmxService.shouldExport(settings)) {
			bind(LocalTransportManagement.class).asEagerSingleton();
		}
	}
}