/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SigarService.java 2012-3-29 15:02:06 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.sigar;

import org.hyperic.sigar.Sigar;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;

/**
 * The Class SigarService.
 *
 * @author l.xue.nong
 */
public class SigarService extends AbstractComponent {

	/** The sigar. */
	private final Sigar sigar;

	/**
	 * Instantiates a new sigar service.
	 *
	 * @param settings the settings
	 */
	@Inject
	public SigarService(Settings settings) {
		super(settings);
		Sigar sigar = null;
		try {
			sigar = new Sigar();
			sigar.getPid();
		} catch (Throwable t) {
			logger.trace("Failed to load sigar", t);
			if (sigar != null) {
				try {
					sigar.close();
				} catch (Throwable t1) {

				} finally {
					sigar = null;
				}
			}
		}
		this.sigar = sigar;
	}

	/**
	 * Sigar available.
	 *
	 * @return true, if successful
	 */
	public boolean sigarAvailable() {
		return sigar != null;
	}

	/**
	 * Sigar.
	 *
	 * @return the sigar
	 */
	public Sigar sigar() {
		return this.sigar;
	}
}
