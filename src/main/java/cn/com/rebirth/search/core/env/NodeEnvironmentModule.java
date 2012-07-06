/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodeEnvironmentModule.java 2012-7-6 14:30:41 l.xue.nong$$
 */

package cn.com.rebirth.search.core.env;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.commons.inject.AbstractModule;

/**
 * The Class NodeEnvironmentModule.
 *
 * @author l.xue.nong
 */
public class NodeEnvironmentModule extends AbstractModule {

	/** The node environment. */
	private final NodeEnvironment nodeEnvironment;

	/**
	 * Instantiates a new node environment module.
	 */
	public NodeEnvironmentModule() {
		this(null);
	}

	/**
	 * Instantiates a new node environment module.
	 *
	 * @param nodeEnvironment the node environment
	 */
	public NodeEnvironmentModule(@Nullable NodeEnvironment nodeEnvironment) {
		this.nodeEnvironment = nodeEnvironment;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		if (nodeEnvironment != null) {
			bind(NodeEnvironment.class).toInstance(nodeEnvironment);
		} else {
			bind(NodeEnvironment.class).asEagerSingleton();
		}
	}
}