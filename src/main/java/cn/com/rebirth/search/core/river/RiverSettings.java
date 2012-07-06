/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RiverSettings.java 2012-7-6 14:29:27 l.xue.nong$$
 */

package cn.com.rebirth.search.core.river;

import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;

/**
 * The Class RiverSettings.
 *
 * @author l.xue.nong
 */
public class RiverSettings {

	/** The global settings. */
	private final Settings globalSettings;

	/** The settings. */
	private final Map<String, Object> settings;

	/**
	 * Instantiates a new river settings.
	 *
	 * @param globalSettings the global settings
	 * @param settings the settings
	 */
	public RiverSettings(Settings globalSettings, Map<String, Object> settings) {
		this.globalSettings = globalSettings;
		this.settings = settings;
	}

	/**
	 * Global settings.
	 *
	 * @return the settings
	 */
	public Settings globalSettings() {
		return globalSettings;
	}

	/**
	 * Settings.
	 *
	 * @return the map
	 */
	public Map<String, Object> settings() {
		return settings;
	}
}
