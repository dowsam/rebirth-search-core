/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RiverNameModule.java 2012-3-29 15:01:20 l.xue.nong$$
 */


package cn.com.rebirth.search.core.river;

import cn.com.rebirth.search.commons.inject.AbstractModule;


/**
 * The Class RiverNameModule.
 *
 * @author l.xue.nong
 */
public class RiverNameModule extends AbstractModule {

	
	/** The river name. */
	private final RiverName riverName;

	
	/**
	 * Instantiates a new river name module.
	 *
	 * @param riverName the river name
	 */
	public RiverNameModule(RiverName riverName) {
		this.riverName = riverName;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(RiverName.class).toInstance(riverName);
	}
}
