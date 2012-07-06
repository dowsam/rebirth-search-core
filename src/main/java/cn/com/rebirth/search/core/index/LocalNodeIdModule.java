/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LocalNodeIdModule.java 2012-7-6 14:29:31 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index;

import cn.com.rebirth.search.commons.inject.AbstractModule;

/**
 * The Class LocalNodeIdModule.
 *
 * @author l.xue.nong
 */
public class LocalNodeIdModule extends AbstractModule {

	/** The local node id. */
	private final String localNodeId;

	/**
	 * Instantiates a new local node id module.
	 *
	 * @param localNodeId the local node id
	 */
	public LocalNodeIdModule(String localNodeId) {
		this.localNodeId = localNodeId;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(String.class).annotatedWith(LocalNodeId.class).toInstance(localNodeId);
	}
}
