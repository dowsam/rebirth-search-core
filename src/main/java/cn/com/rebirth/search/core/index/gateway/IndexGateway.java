/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexGateway.java 2012-3-29 15:02:21 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.gateway;

import cn.com.rebirth.search.core.index.CloseableIndexComponent;
import cn.com.rebirth.search.core.index.IndexComponent;


/**
 * The Interface IndexGateway.
 *
 * @author l.xue.nong
 */
public interface IndexGateway extends IndexComponent, CloseableIndexComponent {

	
	/**
	 * Type.
	 *
	 * @return the string
	 */
	String type();

	
	/**
	 * Shard gateway class.
	 *
	 * @return the class<? extends index shard gateway>
	 */
	Class<? extends IndexShardGateway> shardGatewayClass();

}
