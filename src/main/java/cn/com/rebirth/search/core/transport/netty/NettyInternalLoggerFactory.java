/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NettyInternalLoggerFactory.java 2012-3-29 15:01:49 l.xue.nong$$
 */


package cn.com.rebirth.search.core.transport.netty;

import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.slf4j.LoggerFactory;


/**
 * A factory for creating NettyInternalLogger objects.
 */
public class NettyInternalLoggerFactory extends InternalLoggerFactory {

	
	/* (non-Javadoc)
	 * @see org.jboss.netty.logging.InternalLoggerFactory#newInstance(java.lang.String)
	 */
	@Override
	public InternalLogger newInstance(String name) {
		return new NettyInternalLogger(LoggerFactory.getLogger((name)));
	}
}
