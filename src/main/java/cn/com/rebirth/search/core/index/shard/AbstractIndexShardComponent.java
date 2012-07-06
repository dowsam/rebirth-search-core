/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AbstractIndexShardComponent.java 2012-7-6 14:29:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.shard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.jmx.ManagedGroupName;

/**
 * The Class AbstractIndexShardComponent.
 *
 * @author l.xue.nong
 */
public abstract class AbstractIndexShardComponent implements IndexShardComponent {

	/** The logger. */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/** The shard id. */
	protected final ShardId shardId;

	/** The index settings. */
	protected final Settings indexSettings;

	/** The component settings. */
	protected final Settings componentSettings;

	/**
	 * Instantiates a new abstract index shard component.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 */
	protected AbstractIndexShardComponent(ShardId shardId, @IndexSettings Settings indexSettings) {
		this.shardId = shardId;
		this.indexSettings = indexSettings;
		this.componentSettings = indexSettings.getComponentSettings(getClass());
	}

	/**
	 * Instantiates a new abstract index shard component.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param prefixSettings the prefix settings
	 */
	protected AbstractIndexShardComponent(ShardId shardId, @IndexSettings Settings indexSettings, String prefixSettings) {
		this.shardId = shardId;
		this.indexSettings = indexSettings;
		this.componentSettings = indexSettings.getComponentSettings(prefixSettings, getClass());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.IndexShardComponent#shardId()
	 */
	@Override
	public ShardId shardId() {
		return this.shardId;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.shard.IndexShardComponent#indexSettings()
	 */
	@Override
	public Settings indexSettings() {
		return this.indexSettings;
	}

	/**
	 * Node name.
	 *
	 * @return the string
	 */
	public String nodeName() {
		return indexSettings.get("name", "");
	}

	/**
	 * Management group name.
	 *
	 * @return the string
	 */
	@ManagedGroupName
	public String managementGroupName() {
		return IndexShardManagement.buildShardGroupName(shardId);
	}
}
