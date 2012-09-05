/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchSigarFsProbe.java 2012-7-23 16:49:30 l.xue.nong$$
 */
package cn.com.rebirth.search.core.monitor.fs;

import java.io.File;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.monitor.fs.SigarFsProbe;
import cn.com.rebirth.core.monitor.sigar.SigarService;
import cn.com.rebirth.search.core.env.NodeEnvironment;

/**
 * The Class SearchSigarFsProbe.
 *
 * @author l.xue.nong
 */
public class SearchSigarFsProbe extends SigarFsProbe {

	/**
	 * Instantiates a new search sigar fs probe.
	 *
	 * @param settings the settings
	 * @param sigarService the sigar service
	 * @param nodeEnvironment the node environment
	 */
	@Inject
	public SearchSigarFsProbe(Settings settings, SigarService sigarService, NodeEnvironment nodeEnvironment) {
		this(settings, sigarService, nodeEnvironment.nodeDataLocations(), nodeEnvironment.hasNodeFile());
	}

	/**
	 * Instantiates a new search sigar fs probe.
	 *
	 * @param settings the settings
	 * @param sigarService the sigar service
	 * @param dataLocations the data locations
	 * @param hasNodeFile the has node file
	 */
	public SearchSigarFsProbe(Settings settings, SigarService sigarService, File[] dataLocations, boolean hasNodeFile) {
		super(settings, sigarService, dataLocations, hasNodeFile);
	}

}
