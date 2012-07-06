/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestModule.java 2012-7-6 14:30:37 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest;

import java.util.List;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.rest.action.RestActionModule;

import com.google.common.collect.Lists;

/**
 * The Class RestModule.
 *
 * @author l.xue.nong
 */
public class RestModule extends AbstractModule {

	/** The settings. */
	private final Settings settings;

	/** The rest plugins actions. */
	private List<Class<? extends BaseRestHandler>> restPluginsActions = Lists.newArrayList();

	/**
	 * Adds the rest action.
	 *
	 * @param restAction the rest action
	 */
	public void addRestAction(Class<? extends BaseRestHandler> restAction) {
		restPluginsActions.add(restAction);
	}

	/**
	 * Instantiates a new rest module.
	 *
	 * @param settings the settings
	 */
	public RestModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(RestController.class).asEagerSingleton();
		new RestActionModule(restPluginsActions).configure(binder());
	}
}
