/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RiversPluginsModule.java 2012-3-29 15:00:56 l.xue.nong$$
 */


package cn.com.rebirth.search.core.river;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.inject.PreProcessModule;
import cn.com.rebirth.search.core.plugins.PluginsService;


/**
 * The Class RiversPluginsModule.
 *
 * @author l.xue.nong
 */
public class RiversPluginsModule extends AbstractModule implements PreProcessModule {

	
	/** The settings. */
	private final Settings settings;

	
	/** The plugins service. */
	private final PluginsService pluginsService;

	
	/**
	 * Instantiates a new rivers plugins module.
	 *
	 * @param settings the settings
	 * @param pluginsService the plugins service
	 */
	public RiversPluginsModule(Settings settings, PluginsService pluginsService) {
		this.settings = settings;
		this.pluginsService = pluginsService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.PreProcessModule#processModule(cn.com.summall.search.commons.inject.Module)
	 */
	@Override
	public void processModule(Module module) {
		pluginsService.processModule(module);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
	}
}