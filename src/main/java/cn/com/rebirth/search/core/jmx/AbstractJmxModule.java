/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AbstractJmxModule.java 2012-3-29 15:01:41 l.xue.nong$$
 */


package cn.com.rebirth.search.core.jmx;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;


/**
 * The Class AbstractJmxModule.
 *
 * @author l.xue.nong
 */
public abstract class AbstractJmxModule extends AbstractModule {

	
	/** The settings. */
	private final Settings settings;

	
	/**
	 * Instantiates a new abstract jmx module.
	 *
	 * @param settings the settings
	 */
	protected AbstractJmxModule(Settings settings) {
		this.settings = settings;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		if (JmxService.shouldExport(settings)) {
			doConfigure();
		}
	}

	
	/**
	 * Do configure.
	 */
	protected abstract void doConfigure();
}
