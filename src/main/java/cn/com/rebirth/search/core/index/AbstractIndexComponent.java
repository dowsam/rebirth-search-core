/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AbstractIndexComponent.java 2012-3-29 15:02:41 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.jmx.ManagedGroupName;


/**
 * The Class AbstractIndexComponent.
 *
 * @author l.xue.nong
 */
public abstract class AbstractIndexComponent implements IndexComponent {

	
	/** The logger. */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	
	/** The index. */
	protected final Index index;

	
	/** The index settings. */
	protected final Settings indexSettings;

	
	/** The component settings. */
	protected final Settings componentSettings;

	
	/**
	 * Instantiates a new abstract index component.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 */
	protected AbstractIndexComponent(Index index, @IndexSettings Settings indexSettings) {
		this.index = index;
		this.indexSettings = indexSettings;
		this.componentSettings = indexSettings.getComponentSettings(getClass());
	}

	
	/**
	 * Instantiates a new abstract index component.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param prefixSettings the prefix settings
	 */
	protected AbstractIndexComponent(Index index, @IndexSettings Settings indexSettings, String prefixSettings) {
		this.index = index;
		this.indexSettings = indexSettings;
		this.componentSettings = indexSettings.getComponentSettings(prefixSettings, getClass());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.IndexComponent#index()
	 */
	@Override
	public Index index() {
		return this.index;
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
	@SuppressWarnings("unused")
	@ManagedGroupName
	private String managementGroupName() {
		return IndexServiceManagement.buildIndexGroupName(index);
	}
}