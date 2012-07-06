/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodeClosedException.java 2012-3-29 15:02:19 l.xue.nong$$
 */


package cn.com.rebirth.search.core.node;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;


/**
 * The Class NodeClosedException.
 *
 * @author l.xue.nong
 */
public class NodeClosedException extends RestartException {
	
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3611865037540089354L;

	
    /**
     * Instantiates a new node closed exception.
     *
     * @param node the node
     */
    public NodeClosedException(DiscoveryNode node) {
        super("node closed " + node);
    }
}
