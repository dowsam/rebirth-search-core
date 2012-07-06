/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core LocalNodeIdModule.java 2012-3-29 15:00:50 l.xue.nong$$
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
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(String.class).annotatedWith(LocalNodeId.class).toInstance(localNodeId);
	}
}
