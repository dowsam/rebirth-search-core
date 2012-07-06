/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodeModule.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.node.internal;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.core.node.Node;
import cn.com.rebirth.search.core.node.service.NodeService;
import cn.com.rebirth.search.core.node.settings.NodeSettingsService;

/**
 * The Class NodeModule.
 *
 * @author l.xue.nong
 */
public class NodeModule extends AbstractModule {

	/** The node. */
	private final Node node;

	/**
	 * Instantiates a new node module.
	 *
	 * @param node the node
	 */
	public NodeModule(Node node) {
		this.node = node;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(Node.class).toInstance(node);
		bind(NodeSettingsService.class).asEagerSingleton();
		bind(NodeService.class).asEagerSingleton();
	}
}
