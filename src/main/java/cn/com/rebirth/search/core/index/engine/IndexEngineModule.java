/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexEngineModule.java 2012-3-29 15:01:44 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.inject.Modules;
import cn.com.rebirth.search.commons.inject.SpawnModules;
import cn.com.rebirth.search.core.index.engine.robin.RobinEngineModule;
import cn.com.rebirth.search.core.index.engine.robin.RobinIndexEngineModule;

import com.google.common.collect.ImmutableList;

/**
 * The Class IndexEngineModule.
 *
 * @author l.xue.nong
 */
public class IndexEngineModule extends AbstractModule implements SpawnModules {

	/**
	 * The Class EngineSettings.
	 *
	 * @author l.xue.nong
	 */
	public static final class EngineSettings {

		/** The Constant ENGINE_TYPE. */
		public static final String ENGINE_TYPE = "index.engine.type";

		/** The Constant DEFAULT_INDEX_ENGINE. */
		public static final Class<? extends Module> DEFAULT_INDEX_ENGINE = RobinIndexEngineModule.class;

		/** The Constant DEFAULT_ENGINE. */
		public static final Class<? extends Module> DEFAULT_ENGINE = RobinEngineModule.class;
	}

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new index engine module.
	 *
	 * @param settings the settings
	 */
	public IndexEngineModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.SpawnModules#spawnModules()
	 */
	@Override
	public Iterable<? extends Module> spawnModules() {
		return ImmutableList.of(Modules.createModule(settings.getAsClass(EngineSettings.ENGINE_TYPE,
				EngineSettings.DEFAULT_INDEX_ENGINE, "cn.com.summall.search.core.index.engine.", "IndexEngineModule"),
				settings));
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
	}
}