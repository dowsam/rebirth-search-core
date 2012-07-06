/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexNameModule.java 2012-3-29 15:02:39 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index;

import cn.com.rebirth.search.commons.inject.AbstractModule;


/**
 * The Class IndexNameModule.
 *
 * @author l.xue.nong
 */
public class IndexNameModule extends AbstractModule {

	
	/** The index. */
	private final Index index;

	
	/**
	 * Instantiates a new index name module.
	 *
	 * @param index the index
	 */
	public IndexNameModule(Index index) {
		this.index = index;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(Index.class).toInstance(index);
	}
}
