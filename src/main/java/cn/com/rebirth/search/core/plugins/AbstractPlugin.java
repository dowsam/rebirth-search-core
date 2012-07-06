/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AbstractPlugin.java 2012-3-29 15:02:08 l.xue.nong$$
 */


package cn.com.rebirth.search.core.plugins;

import java.util.Collection;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.LifecycleComponent;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.core.index.CloseableIndexComponent;

import com.google.common.collect.ImmutableList;


/**
 * The Class AbstractPlugin.
 *
 * @author l.xue.nong
 */
public abstract class AbstractPlugin implements Plugin {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.plugins.Plugin#modules()
	 */
	@Override
	public Collection<Class<? extends Module>> modules() {
		return ImmutableList.of();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.plugins.Plugin#modules(cn.com.summall.search.commons.settings.Settings)
	 */
	@Override
	public Collection<Module> modules(Settings settings) {
		return ImmutableList.of();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.plugins.Plugin#services()
	 */
	@Override
	public Collection<Class<? extends LifecycleComponent>> services() {
		return ImmutableList.of();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.plugins.Plugin#indexModules()
	 */
	@Override
	public Collection<Class<? extends Module>> indexModules() {
		return ImmutableList.of();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.plugins.Plugin#indexModules(cn.com.summall.search.commons.settings.Settings)
	 */
	@Override
	public Collection<Module> indexModules(Settings settings) {
		return ImmutableList.of();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.plugins.Plugin#indexServices()
	 */
	@Override
	public Collection<Class<? extends CloseableIndexComponent>> indexServices() {
		return ImmutableList.of();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.plugins.Plugin#shardModules()
	 */
	@Override
	public Collection<Class<? extends Module>> shardModules() {
		return ImmutableList.of();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.plugins.Plugin#shardModules(cn.com.summall.search.commons.settings.Settings)
	 */
	@Override
	public Collection<Module> shardModules(Settings settings) {
		return ImmutableList.of();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.plugins.Plugin#shardServices()
	 */
	@Override
	public Collection<Class<? extends CloseableIndexComponent>> shardServices() {
		return ImmutableList.of();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.plugins.Plugin#processModule(cn.com.summall.search.commons.inject.Module)
	 */
	@Override
	public void processModule(Module module) {
		
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.plugins.Plugin#additionalSettings()
	 */
	@Override
	public Settings additionalSettings() {
		return ImmutableSettings.Builder.EMPTY_SETTINGS;
	}

}
