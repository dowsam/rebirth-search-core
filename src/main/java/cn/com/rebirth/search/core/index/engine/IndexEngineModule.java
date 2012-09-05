/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexEngineModule.java 2012-7-6 14:30:14 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.core.inject.Module;
import cn.com.rebirth.core.inject.Modules;
import cn.com.rebirth.core.inject.SpawnModules;
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
	 * @see cn.com.rebirth.search.commons.inject.SpawnModules#spawnModules()
	 */
	@Override
	public Iterable<? extends Module> spawnModules() {
		return ImmutableList.of(Modules.createModule(settings.getAsClass(EngineSettings.ENGINE_TYPE,
				EngineSettings.DEFAULT_INDEX_ENGINE, "cn.com.rebirth.search.core.index.engine.", "IndexEngineModule"),
				settings));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
	}
}