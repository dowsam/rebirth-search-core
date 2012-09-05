/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RiverNameModule.java 2012-7-6 14:30:23 l.xue.nong$$
 */

package cn.com.rebirth.search.core.river;

import cn.com.rebirth.core.inject.AbstractModule;

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
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(RiverName.class).toInstance(riverName);
	}
}
