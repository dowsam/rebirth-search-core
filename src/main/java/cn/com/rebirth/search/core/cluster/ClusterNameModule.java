/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterNameModule.java 2012-7-6 14:30:11 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;

/**
 * The Class ClusterNameModule.
 *
 * @author l.xue.nong
 */
public class ClusterNameModule extends AbstractModule {

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new cluster name module.
	 *
	 * @param settings the settings
	 */
	public ClusterNameModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(ClusterName.class).toInstance(ClusterName.clusterNameFromSettings(settings));
	}
}
