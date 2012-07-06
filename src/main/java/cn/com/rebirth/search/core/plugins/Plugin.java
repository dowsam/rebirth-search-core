/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core Plugin.java 2012-3-29 15:02:45 l.xue.nong$$
 */


package cn.com.rebirth.search.core.plugins;

import java.util.Collection;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.LifecycleComponent;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.core.index.CloseableIndexComponent;


/**
 * The Interface Plugin.
 *
 * @author l.xue.nong
 */
public interface Plugin {

	
	/**
	 * Name.
	 *
	 * @return the string
	 */
	String name();

	
	/**
	 * Description.
	 *
	 * @return the string
	 */
	String description();

	
	/**
	 * Modules.
	 *
	 * @return the collection< class<? extends module>>
	 */
	Collection<Class<? extends Module>> modules();

	
	/**
	 * Modules.
	 *
	 * @param settings the settings
	 * @return the collection
	 */
	Collection<Module> modules(Settings settings);

	
	/**
	 * Services.
	 *
	 * @return the collection< class<? extends lifecycle component>>
	 */
	Collection<Class<? extends LifecycleComponent>> services();

	
	/**
	 * Index modules.
	 *
	 * @return the collection< class<? extends module>>
	 */
	Collection<Class<? extends Module>> indexModules();

	
	/**
	 * Index modules.
	 *
	 * @param settings the settings
	 * @return the collection
	 */
	Collection<Module> indexModules(Settings settings);

	
	/**
	 * Index services.
	 *
	 * @return the collection< class<? extends closeable index component>>
	 */
	Collection<Class<? extends CloseableIndexComponent>> indexServices();

	
	/**
	 * Shard modules.
	 *
	 * @return the collection< class<? extends module>>
	 */
	Collection<Class<? extends Module>> shardModules();

	
	/**
	 * Shard modules.
	 *
	 * @param settings the settings
	 * @return the collection
	 */
	Collection<Module> shardModules(Settings settings);

	
	/**
	 * Shard services.
	 *
	 * @return the collection< class<? extends closeable index component>>
	 */
	Collection<Class<? extends CloseableIndexComponent>> shardServices();

	
	/**
	 * Process module.
	 *
	 * @param module the module
	 */
	void processModule(Module module);

	
	/**
	 * Additional settings.
	 *
	 * @return the settings
	 */
	Settings additionalSettings();
}
