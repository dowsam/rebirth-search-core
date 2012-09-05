/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core EnvironmentModule.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.env;

import cn.com.rebirth.core.inject.AbstractModule;

/**
 * The Class EnvironmentModule.
 *
 * @author l.xue.nong
 */
public class EnvironmentModule extends AbstractModule {

	/** The environment. */
	private final Environment environment;

	/**
	 * Instantiates a new environment module.
	 *
	 * @param environment the environment
	 */
	public EnvironmentModule(Environment environment) {
		this.environment = environment;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(Environment.class).toInstance(environment);
	}
}
