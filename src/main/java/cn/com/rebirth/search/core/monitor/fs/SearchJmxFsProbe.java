/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchJmxFsProbe.java 2012-7-23 16:52:21 l.xue.nong$$
 */
package cn.com.rebirth.search.core.monitor.fs;

import java.io.File;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.monitor.fs.JmxFsProbe;
import cn.com.rebirth.search.core.env.NodeEnvironment;

/**
 * The Class SearchJmxFsProbe.
 *
 * @author l.xue.nong
 */
public class SearchJmxFsProbe extends JmxFsProbe {

	/**
	 * Instantiates a new search jmx fs probe.
	 *
	 * @param settings the settings
	 * @param nodeEnvironment the node environment
	 */
	@Inject
	public SearchJmxFsProbe(Settings settings, NodeEnvironment nodeEnvironment) {
		this(settings, nodeEnvironment.nodeDataLocations(), nodeEnvironment.hasNodeFile());
	}

	/**
	 * Instantiates a new search jmx fs probe.
	 *
	 * @param settings the settings
	 * @param dataLocations the data locations
	 * @param hasNodeFile the has node file
	 */
	public SearchJmxFsProbe(Settings settings, File[] dataLocations, boolean hasNodeFile) {
		super(settings, dataLocations, hasNodeFile);
	}

}
