/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StoreManagement.java 2012-7-6 14:30:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.store;

import java.io.IOException;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.jmx.MBean;
import cn.com.rebirth.search.core.jmx.ManagedAttribute;

/**
 * The Class StoreManagement.
 *
 * @author l.xue.nong
 */
@MBean(objectName = "shardType=store", description = "The storage of the index shard")
public class StoreManagement extends AbstractIndexShardComponent {

	/** The store. */
	private final Store store;

	/**
	 * Instantiates a new store management.
	 *
	 * @param store the store
	 */
	@Inject
	public StoreManagement(Store store) {
		super(store.shardId(), store.indexSettings());
		this.store = store;
	}

	/**
	 * Gets the size in bytes.
	 *
	 * @return the size in bytes
	 */
	@ManagedAttribute(description = "Size in bytes")
	public long getSizeInBytes() {
		try {
			return store.estimateSize().bytes();
		} catch (IOException e) {
			return -1;
		}
	}

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	@ManagedAttribute(description = "Size")
	public String getSize() {
		try {
			return store.estimateSize().toString();
		} catch (IOException e) {
			return "NA";
		}
	}
}
