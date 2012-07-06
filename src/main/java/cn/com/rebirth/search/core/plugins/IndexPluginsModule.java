/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexPluginsModule.java 2012-7-6 14:29:49 l.xue.nong$$
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
 * The Class IndexPluginsModule.
 *
 * @author l.xue.nong
 */
public class IndexPluginsModule extends AbstractModule implements SpawnModules, PreProcessModule {

	/** The settings. */
	private final Settings settings;

	/** The plugins service. */
	private final PluginsService pluginsService;

	/**
	 * Instantiates a new index plugins module.
	 *
	 * @param settings the settings
	 * @param pluginsService the plugins service
	 */
	public IndexPluginsModule(Settings settings, PluginsService pluginsService) {
		this.settings = settings;
		this.pluginsService = pluginsService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.SpawnModules#spawnModules()
	 */
	@Override
	public Iterable<? extends Module> spawnModules() {
		List<Module> modules = Lists.newArrayList();
		Collection<Class<? extends Module>> modulesClasses = pluginsService.indexModules();
		for (Class<? extends Module> moduleClass : modulesClasses) {
			modules.add(Modules.createModule(moduleClass, settings));
		}
		modules.addAll(pluginsService.indexModules(settings));
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
	}
}