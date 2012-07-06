/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RecoveryFailedException.java 2012-3-29 15:01:19 l.xue.nong$$
 */


package cn.com.rebirth.search.core.indices.recovery;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Class RecoveryFailedException.
 *
 * @author l.xue.nong
 */
public class RecoveryFailedException extends RestartException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6104912072806696482L;

	
	/**
	 * Instantiates a new recovery failed exception.
	 *
	 * @param request the request
	 * @param cause the cause
	 */
	public RecoveryFailedException(StartRecoveryRequest request, Throwable cause) {
		this(request.shardId(), request.sourceNode(), request.targetNode(), cause);
	}

	
	/**
	 * Instantiates a new recovery failed exception.
	 *
	 * @param shardId the shard id
	 * @param sourceNode the source node
	 * @param targetNode the target node
	 * @param cause the cause
	 */
	public RecoveryFailedException(ShardId shardId, DiscoveryNode sourceNode, DiscoveryNode targetNode, Throwable cause) {
		super(shardId + ": Recovery failed from " + sourceNode + " into " + targetNode, cause);
	}
}
