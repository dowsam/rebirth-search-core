/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PluginsModule.java 2012-3-29 15:02:45 l.xue.nong$$
 */


package cn.com.rebirth.search.core.plugins;

import java.util.Collection;
import java.util.List;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.inject.Modules;
import cn.com.rebirth.search.commons.inject.PreProcessModule;
import cn.com.rebirth.search.commons.inject.SpawnModules;

import com.google.common.collect.Lists;


/**
 * The Class PluginsModule.
 *
 * @author l.xue.nong
 */
public class PluginsModule extends AbstractModule implements SpawnModules, PreProcessModule {

	
	/** The settings. */
	private final Settings settings;

	
	/** The plugins service. */
	private final PluginsService pluginsService;

	
	/**
	 * Instantiates a new plugins module.
	 *
	 * @param settings the settings
	 * @param pluginsService the plugins service
	 */
	public PluginsModule(Settings settings, PluginsService pluginsService) {
		this.settings = settings;
		this.pluginsService = pluginsService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.SpawnModules#spawnModules()
	 */
	@Override
	public Iterable<? extends Module> spawnModules() {
		List<Module> modules = Lists.newArrayList();
		Collection<Class<? extends Module>> modulesClasses = pluginsService.modules();
		for (Class<? extends Module> moduleClass : modulesClasses) {
			modules.add(Modules.createModule(moduleClass, settings));
		}
		modules.addAll(pluginsService.modules(settings));
		return modules;
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
		bind(PluginsService.class).toInstance(pluginsService);
	}
}
