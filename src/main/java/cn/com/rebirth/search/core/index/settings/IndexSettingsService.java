/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexSettingsService.java 2012-7-6 14:29:26 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.settings;

import java.util.concurrent.CopyOnWriteArrayList;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;

/**
 * The Class IndexSettingsService.
 *
 * @author l.xue.nong
 */
public class IndexSettingsService extends AbstractIndexComponent {

	/** The settings. */
	private volatile Settings settings;

	/** The listeners. */
	private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();

	/**
	 * Instantiates a new index settings service.
	 *
	 * @param index the index
	 * @param settings the settings
	 */
	@Inject
	public IndexSettingsService(Index index, Settings settings) {
		super(index, settings);
		this.settings = settings;
	}

	/**
	 * Refresh settings.
	 *
	 * @param settings the settings
	 */
	public synchronized void refreshSettings(Settings settings) {

		if (this.settings.getByPrefix("index.").getAsMap().equals(settings.getByPrefix("index.").getAsMap())) {

			return;
		}
		this.settings = ImmutableSettings.settingsBuilder().put(this.settings).put(settings).build();
		for (Listener listener : listeners) {
			try {
				listener.onRefreshSettings(settings);
			} catch (Exception e) {
				logger.warn("failed to refresh settings for [{}]", e, listener);
			}
		}
	}

	/**
	 * Gets the settings.
	 *
	 * @return the settings
	 */
	public Settings getSettings() {
		return this.settings;
	}

	/**
	 * Adds the listener.
	 *
	 * @param listener the listener
	 */
	public void addListener(Listener listener) {
		this.listeners.add(listener);
	}

	/**
	 * Removes the listener.
	 *
	 * @param listener the listener
	 */
	public void removeListener(Listener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * The Interface Listener.
	 *
	 * @author l.xue.nong
	 */
	public static interface Listener {

		/**
		 * On refresh settings.
		 *
		 * @param settings the settings
		 */
		void onRefreshSettings(Settings settings);
	}
}