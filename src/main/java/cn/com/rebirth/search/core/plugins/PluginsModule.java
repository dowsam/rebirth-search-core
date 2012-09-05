/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PluginsModule.java 2012-7-6 14:30:35 l.xue.nong$$
 */

package cn.com.rebirth.search.core.plugins;

import java.util.Collection;
import java.util.List;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.core.inject.Module;
import cn.com.rebirth.core.inject.Modules;
import cn.com.rebirth.core.inject.PreProcessModule;
import cn.com.rebirth.core.inject.SpawnModules;

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
	 * @see cn.com.rebirth.search.commons.inject.SpawnModules#spawnModules()
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
	 * @see cn.com.rebirth.search.commons.inject.PreProcessModule#processModule(cn.com.rebirth.search.commons.inject.Module)
	 */
	@Override
	public void processModule(Module module) {
		pluginsService.processModule(module);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(PluginsService.class).toInstance(pluginsService);
	}
}
