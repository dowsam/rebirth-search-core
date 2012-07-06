/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodeSettingsService.java 2012-3-29 15:01:02 l.xue.nong$$
 */


package cn.com.rebirth.search.core.node.settings;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.ClusterChangedEvent;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterStateListener;


/**
 * The Class NodeSettingsService.
 *
 * @author l.xue.nong
 */
public class NodeSettingsService extends AbstractComponent implements ClusterStateListener {

	
	/** The last settings applied. */
	private volatile Settings lastSettingsApplied;

	
	/** The listeners. */
	private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();

	
	/**
	 * Instantiates a new node settings service.
	 *
	 * @param settings the settings
	 */
	@Inject
	public NodeSettingsService(Settings settings) {
		super(settings);
	}

	
	
	/**
	 * Sets the cluster service.
	 *
	 * @param clusterService the new cluster service
	 */
	public void setClusterService(ClusterService clusterService) {
		clusterService.add(this);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.cluster.ClusterStateListener#clusterChanged(cn.com.summall.search.core.cluster.ClusterChangedEvent)
	 */
	@Override
	public void clusterChanged(ClusterChangedEvent event) {
		
		if (event.state().blocks().disableStatePersistence()) {
			return;
		}

		if (!event.metaDataChanged()) {
			
			return;
		}

		if (lastSettingsApplied != null && event.state().metaData().settings().equals(lastSettingsApplied)) {
			
			return;
		}

		for (Listener listener : listeners) {
			try {
				listener.onRefreshSettings(event.state().metaData().settings());
			} catch (Exception e) {
				logger.warn("failed to refresh settings for [{}]", e, listener);
			}
		}

		try {
			for (Map.Entry<String, String> entry : event.state().metaData().settings().getAsMap().entrySet()) {
				if (entry.getKey().startsWith("logger.")) {
					String component = entry.getKey().substring("logger.".length());
					logger.info(component + entry.getValue());
				}
			}
		} catch (Exception e) {
			logger.warn("failed to refresh settings for [{}]", e, "logger");
		}

		lastSettingsApplied = event.state().metaData().settings();
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
