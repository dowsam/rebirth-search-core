/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NodeCache.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cache;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cache.memory.ByteBufferCache;
import cn.com.rebirth.search.core.cluster.ClusterChangedEvent;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterStateListener;

/**
 * The Class NodeCache.
 *
 * @author l.xue.nong
 */
public class NodeCache extends AbstractComponent implements ClusterStateListener {

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The byte buffer cache. */
	private final ByteBufferCache byteBufferCache;

	/**
	 * Instantiates a new node cache.
	 *
	 * @param settings the settings
	 * @param byteBufferCache the byte buffer cache
	 * @param clusterService the cluster service
	 */
	@Inject
	public NodeCache(Settings settings, ByteBufferCache byteBufferCache, ClusterService clusterService) {
		super(settings);
		this.clusterService = clusterService;
		this.byteBufferCache = byteBufferCache;
		clusterService.add(this);
	}

	/**
	 * Close.
	 */
	public void close() {
		clusterService.remove(this);
		byteBufferCache.close();
	}

	/**
	 * Byte buffer.
	 *
	 * @return the byte buffer cache
	 */
	public ByteBufferCache byteBuffer() {
		return byteBufferCache;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.ClusterStateListener#clusterChanged(cn.com.rebirth.search.core.cluster.ClusterChangedEvent)
	 */
	@Override
	public void clusterChanged(ClusterChangedEvent event) {
	}
}
