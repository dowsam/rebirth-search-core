/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DummyRiver.java 2012-7-6 14:28:51 l.xue.nong$$
 */

package cn.com.rebirth.search.core.river.dummy;

import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.river.AbstractRiverComponent;
import cn.com.rebirth.search.core.river.River;
import cn.com.rebirth.search.core.river.RiverName;
import cn.com.rebirth.search.core.river.RiverSettings;

/**
 * The Class DummyRiver.
 *
 * @author l.xue.nong
 */
public class DummyRiver extends AbstractRiverComponent implements River {

	/**
	 * Instantiates a new dummy river.
	 *
	 * @param riverName the river name
	 * @param settings the settings
	 */
	@Inject
	public DummyRiver(RiverName riverName, RiverSettings settings) {
		super(riverName, settings);
		logger.info("create");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.river.River#start()
	 */
	@Override
	public void start() {
		logger.info("start");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.river.River#close()
	 */
	@Override
	public void close() {
		logger.info("close");
	}
}
