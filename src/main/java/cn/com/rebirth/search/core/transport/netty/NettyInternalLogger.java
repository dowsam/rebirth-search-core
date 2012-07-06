/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NettyInternalLogger.java 2012-3-29 15:02:38 l.xue.nong$$
 */


package cn.com.rebirth.search.core.transport.netty;

import org.jboss.netty.logging.AbstractInternalLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class NettyInternalLogger.
 *
 * @author l.xue.nong
 */
public class NettyInternalLogger extends AbstractInternalLogger {

	
	/** The logger. */
	private final Logger logger;

	/**
	 * Instantiates a new netty internal logger.
	 *
	 * @param logger the logger
	 */
	public NettyInternalLogger(Logger logger) {
		super();
		this.logger = logger;
	}

	
	/**
	 * Instantiates a new netty internal logger.
	 */
	public NettyInternalLogger() {
		super();
		this.logger = LoggerFactory.getLogger(getClass());
	}

	
	/* (non-Javadoc)
	 * @see org.jboss.netty.logging.InternalLogger#isDebugEnabled()
	 */
	@Override
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	
	/* (non-Javadoc)
	 * @see org.jboss.netty.logging.InternalLogger#isInfoEnabled()
	 */
	@Override
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	
	/* (non-Javadoc)
	 * @see org.jboss.netty.logging.InternalLogger#isWarnEnabled()
	 */
	@Override
	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
	}

	
	/* (non-Javadoc)
	 * @see org.jboss.netty.logging.InternalLogger#isErrorEnabled()
	 */
	@Override
	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
	}

	
	/* (non-Javadoc)
	 * @see org.jboss.netty.logging.InternalLogger#debug(java.lang.String)
	 */
	@Override
	public void debug(String msg) {
		logger.debug(msg);
	}

	
	/* (non-Javadoc)
	 * @see org.jboss.netty.logging.InternalLogger#debug(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void debug(String msg, Throwable cause) {
		logger.debug(msg, cause);
	}

	
	/* (non-Javadoc)
	 * @see org.jboss.netty.logging.InternalLogger#info(java.lang.String)
	 */
	@Override
	public void info(String msg) {
		logger.info(msg);
	}

	
	/* (non-Javadoc)
	 * @see org.jboss.netty.logging.InternalLogger#info(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void info(String msg, Throwable cause) {
		logger.info(msg, cause);
	}

	
	/* (non-Javadoc)
	 * @see org.jboss.netty.logging.InternalLogger#warn(java.lang.String)
	 */
	@Override
	public void warn(String msg) {
		logger.warn(msg);
	}

	
	/* (non-Javadoc)
	 * @see org.jboss.netty.logging.InternalLogger#warn(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void warn(String msg, Throwable cause) {
		logger.warn(msg, cause);
	}

	
	/* (non-Javadoc)
	 * @see org.jboss.netty.logging.InternalLogger#error(java.lang.String)
	 */
	@Override
	public void error(String msg) {
		logger.error(msg);
	}

	
	/* (non-Javadoc)
	 * @see org.jboss.netty.logging.InternalLogger#error(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void error(String msg, Throwable cause) {
		logger.error(msg, cause);
	}
}
