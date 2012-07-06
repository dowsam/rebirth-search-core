/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RobinIndexEngine.java 2012-7-6 14:29:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine.robin;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.engine.IndexEngine;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class RobinIndexEngine.
 *
 * @author l.xue.nong
 */
public class RobinIndexEngine extends AbstractIndexComponent implements IndexEngine {

	/**
	 * Instantiates a new robin index engine.
	 *
	 * @param index the index
	 */
	public RobinIndexEngine(Index index) {
		this(index, ImmutableSettings.Builder.EMPTY_SETTINGS);
	}

	/**
	 * Instantiates a new robin index engine.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 */
	@Inject
	public RobinIndexEngine(Index index, @IndexSettings Settings indexSettings) {
		super(index, indexSettings);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.engine.IndexEngine#close()
	 */
	@Override
	public void close() {
	}
}
