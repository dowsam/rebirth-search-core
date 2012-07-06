/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core EngineModule.java 2012-3-29 15:01:02 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.inject.Modules;
import cn.com.rebirth.search.commons.inject.SpawnModules;

import com.google.common.collect.ImmutableList;


/**
 * The Class EngineModule.
 *
 * @author l.xue.nong
 */
public class EngineModule extends AbstractModule implements SpawnModules {

	
	/** The settings. */
	private final Settings settings;

	
	/**
	 * Instantiates a new engine module.
	 *
	 * @param settings the settings
	 */
	public EngineModule(Settings settings) {
		this.settings = settings;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.SpawnModules#spawnModules()
	 */
	@Override
	public Iterable<? extends Module> spawnModules() {
		return ImmutableList.of(Modules.createModule(settings.getAsClass(IndexEngineModule.EngineSettings.ENGINE_TYPE,
				IndexEngineModule.EngineSettings.DEFAULT_ENGINE, "cn.com.summall.search.core.index.engine.", "EngineModule"),
				settings));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
	}
}
