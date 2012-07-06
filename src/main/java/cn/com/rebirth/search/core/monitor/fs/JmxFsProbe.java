/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core JmxFsProbe.java 2012-7-6 14:29:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.fs;

import java.io.File;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.env.NodeEnvironment;

/**
 * The Class JmxFsProbe.
 *
 * @author l.xue.nong
 */
public class JmxFsProbe extends AbstractComponent implements FsProbe {

	/** The node env. */
	private final NodeEnvironment nodeEnv;

	/**
	 * Instantiates a new jmx fs probe.
	 *
	 * @param settings the settings
	 * @param nodeEnv the node env
	 */
	@Inject
	public JmxFsProbe(Settings settings, NodeEnvironment nodeEnv) {
		super(settings);
		this.nodeEnv = nodeEnv;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.monitor.fs.FsProbe#stats()
	 */
	@Override
	public FsStats stats() {
		if (!nodeEnv.hasNodeFile()) {
			return new FsStats(System.currentTimeMillis(), new FsStats.Info[0]);
		}
		File[] dataLocations = nodeEnv.nodeDataLocations();
		FsStats.Info[] infos = new FsStats.Info[dataLocations.length];
		for (int i = 0; i < dataLocations.length; i++) {
			File dataLocation = dataLocations[i];
			FsStats.Info info = new FsStats.Info();
			info.path = dataLocation.getAbsolutePath();
			info.total = dataLocation.getTotalSpace();
			info.free = dataLocation.getFreeSpace();
			info.available = dataLocation.getUsableSpace();
			infos[i] = info;
		}
		return new FsStats(System.currentTimeMillis(), infos);
	}
}
