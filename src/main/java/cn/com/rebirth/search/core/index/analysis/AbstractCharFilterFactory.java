/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AbstractCharFilterFactory.java 2012-3-29 15:01:44 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * A factory for creating AbstractCharFilter objects.
 */
public abstract class AbstractCharFilterFactory extends AbstractIndexComponent implements CharFilterFactory {

	
	/** The name. */
	private final String name;

	
	/**
	 * Instantiates a new abstract char filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 */
	public AbstractCharFilterFactory(Index index, @IndexSettings Settings indexSettings, String name) {
		super(index, indexSettings);
		this.name = name;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.CharFilterFactory#name()
	 */
	@Override
	public String name() {
		return this.name;
	}
}
