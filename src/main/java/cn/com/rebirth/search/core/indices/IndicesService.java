/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesService.java 2012-7-6 14:30:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices;

import java.util.Set;

import cn.com.rebirth.commons.component.LifecycleComponent;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.core.index.service.IndexService;

/**
 * The Interface IndicesService.
 *
 * @author l.xue.nong
 */
public interface IndicesService extends Iterable<IndexService>, LifecycleComponent<IndicesService> {

	/**
	 * Changes allowed.
	 *
	 * @return true, if successful
	 */
	public boolean changesAllowed();

	/**
	 * Stats.
	 *
	 * @param includePrevious the include previous
	 * @return the node indices stats
	 */
	NodeIndicesStats stats(boolean includePrevious);

	/**
	 * Checks for index.
	 *
	 * @param index the index
	 * @return true, if successful
	 */
	boolean hasIndex(String index);

	/**
	 * Indices lifecycle.
	 *
	 * @return the indices lifecycle
	 */
	IndicesLifecycle indicesLifecycle();

	/**
	 * Indices.
	 *
	 * @return the sets the
	 */
	Set<String> indices();

	/**
	 * Index service.
	 *
	 * @param index the index
	 * @return the index service
	 */
	IndexService indexService(String index);

	/**
	 * Index service safe.
	 *
	 * @param index the index
	 * @return the index service
	 * @throws IndexMissingException the index missing exception
	 */
	IndexService indexServiceSafe(String index) throws IndexMissingException;

	/**
	 * Creates the index.
	 *
	 * @param index the index
	 * @param settings the settings
	 * @param localNodeId the local node id
	 * @return the index service
	 * @throws RebirthException the rebirth exception
	 */
	IndexService createIndex(String index, Settings settings, String localNodeId) throws RebirthException;

	/**
	 * Delete index.
	 *
	 * @param index the index
	 * @param reason the reason
	 * @throws RebirthException the rebirth exception
	 */
	void deleteIndex(String index, String reason) throws RebirthException;

	/**
	 * Clean index.
	 *
	 * @param index the index
	 * @param reason the reason
	 * @throws RebirthException the rebirth exception
	 */
	void cleanIndex(String index, String reason) throws RebirthException;
}
