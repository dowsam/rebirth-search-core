/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MergePolicyModule.java 2012-3-29 15:02:48 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.merge.policy;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;


/**
 * The Class MergePolicyModule.
 *
 * @author l.xue.nong
 */
public class MergePolicyModule extends AbstractModule {

	
	/** The settings. */
	private final Settings settings;

	
	/**
	 * Instantiates a new merge policy module.
	 *
	 * @param settings the settings
	 */
	public MergePolicyModule(Settings settings) {
		this.settings = settings;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(MergePolicyProvider.class).to(
				settings.getAsClass("index.merge.policy.type", TieredMergePolicyProvider.class,
						"cn.com.summall.search.core.index.merge.policy.", "MergePolicyProvider")).asEagerSingleton();
	}
}
