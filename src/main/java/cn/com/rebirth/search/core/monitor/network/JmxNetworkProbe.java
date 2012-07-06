/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core JmxNetworkProbe.java 2012-3-29 15:02:05 l.xue.nong$$
 */


package cn.com.rebirth.search.core.monitor.network;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;


/**
 * The Class JmxNetworkProbe.
 *
 * @author l.xue.nong
 */
public class JmxNetworkProbe extends AbstractComponent implements NetworkProbe {

	
	/**
	 * Instantiates a new jmx network probe.
	 *
	 * @param settings the settings
	 */
	@Inject
	public JmxNetworkProbe(Settings settings) {
		super(settings);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.monitor.network.NetworkProbe#networkInfo()
	 */
	@Override
	public NetworkInfo networkInfo() {
		NetworkInfo info = new NetworkInfo();
		return info;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.monitor.network.NetworkProbe#networkStats()
	 */
	@Override
	public NetworkStats networkStats() {
		NetworkStats stats = new NetworkStats();
		stats.timestamp = System.currentTimeMillis();
		return stats;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.monitor.network.NetworkProbe#ifconfig()
	 */
	@Override
	public String ifconfig() {
		return "NA";
	}
}
