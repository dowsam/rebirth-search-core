/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core JvmService.java 2012-3-29 15:02:08 l.xue.nong$$
 */


package cn.com.rebirth.search.core.monitor.jvm;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;


/**
 * The Class JvmService.
 *
 * @author l.xue.nong
 */
public class JvmService extends AbstractComponent {

	
	/** The jvm info. */
	private final JvmInfo jvmInfo;

	
	/** The refresh interval. */
	private final TimeValue refreshInterval;

	
	/** The jvm stats. */
	private JvmStats jvmStats;

	
	/**
	 * Instantiates a new jvm service.
	 *
	 * @param settings the settings
	 */
	@Inject
	public JvmService(Settings settings) {
		super(settings);
		this.jvmInfo = JvmInfo.jvmInfo();
		this.jvmStats = JvmStats.jvmStats();

		this.refreshInterval = componentSettings.getAsTime("refresh_interval", TimeValue.timeValueSeconds(1));

		logger.debug("Using refresh_interval [{}]", refreshInterval);
	}

	
	/**
	 * Info.
	 *
	 * @return the jvm info
	 */
	public JvmInfo info() {
		return this.jvmInfo;
	}

	
	/**
	 * Stats.
	 *
	 * @return the jvm stats
	 */
	public synchronized JvmStats stats() {
		if ((System.currentTimeMillis() - jvmStats.timestamp()) > refreshInterval.millis()) {
			jvmStats = JvmStats.jvmStats();
		}
		return jvmStats;
	}
}
