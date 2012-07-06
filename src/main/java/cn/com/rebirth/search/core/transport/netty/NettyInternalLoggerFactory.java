/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NettyInternalLoggerFactory.java 2012-7-6 14:29:46 l.xue.nong$$
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
