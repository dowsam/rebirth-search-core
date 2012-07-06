/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MergePolicyModule.java 2012-7-6 14:29:11 l.xue.nong$$
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
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(MergePolicyProvider.class).to(
				settings.getAsClass("index.merge.policy.type", TieredMergePolicyProvider.class,
						"cn.com.rebirth.search.core.index.merge.policy.", "MergePolicyProvider")).asEagerSingleton();
	}
}
