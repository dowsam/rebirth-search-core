/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexModule.java 2012-3-29 15:01:12 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.service.InternalIndexService;
import cn.com.rebirth.search.core.jmx.JmxService;


/**
 * The Class IndexModule.
 *
 * @author l.xue.nong
 */
public class IndexModule extends AbstractModule {

	
	/** The settings. */
	private final Settings settings;

	
	/**
	 * Instantiates a new index module.
	 *
	 * @param settings the settings
	 */
	public IndexModule(Settings settings) {
		this.settings = settings;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndexService.class).to(InternalIndexService.class).asEagerSingleton();
		if (JmxService.shouldExport(settings)) {
			bind(IndexServiceManagement.class).asEagerSingleton();
		}
	}
}
