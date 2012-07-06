/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexGatewayModule.java 2012-3-29 15:01:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.gateway;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.inject.Modules;
import cn.com.rebirth.search.commons.inject.SpawnModules;
import cn.com.rebirth.search.core.gateway.Gateway;

import com.google.common.collect.ImmutableList;

/**
 * The Class IndexGatewayModule.
 *
 * @author l.xue.nong
 */
public class IndexGatewayModule extends AbstractModule implements SpawnModules {

	/** The settings. */
	private final Settings settings;

	/** The gateway. */
	private final Gateway gateway;

	/**
	 * Instantiates a new index gateway module.
	 *
	 * @param settings the settings
	 * @param gateway the gateway
	 */
	public IndexGatewayModule(Settings settings, Gateway gateway) {
		this.settings = settings;
		this.gateway = gateway;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.SpawnModules#spawnModules()
	 */
	@Override
	public Iterable<? extends Module> spawnModules() {
		return ImmutableList.of(Modules.createModule(settings.getAsClass("index.gateway.type",
				gateway.suggestIndexGateway(), "cn.com.summall.search.core.index.gateway.", "IndexGatewayModule"),
				settings));
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
	}
}