/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RiversModule.java 2012-7-6 14:29:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.river;

import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.core.river.cluster.RiverClusterService;
import cn.com.rebirth.search.core.river.routing.RiversRouter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * The Class RiversModule.
 *
 * @author l.xue.nong
 */
public class RiversModule extends AbstractModule {

	/** The settings. */
	private final Settings settings;

	/** The river types. */
	private Map<String, Class<? extends Module>> riverTypes = Maps.newHashMap();

	/**
	 * Instantiates a new rivers module.
	 *
	 * @param settings the settings
	 */
	public RiversModule(Settings settings) {
		this.settings = settings;
	}

	/**
	 * Register river.
	 *
	 * @param type the type
	 * @param module the module
	 */
	public void registerRiver(String type, Class<? extends Module> module) {
		riverTypes.put(type, module);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(String.class).annotatedWith(RiverIndexName.class).toInstance(RiverIndexName.Conf.indexName(settings));
		bind(RiversService.class).asEagerSingleton();
		bind(RiverClusterService.class).asEagerSingleton();
		bind(RiversRouter.class).asEagerSingleton();
		bind(RiversManager.class).asEagerSingleton();
		bind(RiversTypesRegistry.class).toInstance(new RiversTypesRegistry(ImmutableMap.copyOf(riverTypes)));
	}
}
