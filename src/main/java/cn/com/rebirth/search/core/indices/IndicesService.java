/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndicesService.java 2012-3-29 15:02:47 l.xue.nong$$
 */


package cn.com.rebirth.search.core.indices;

import java.util.Set;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.LifecycleComponent;
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
	 * @throws SumMallSearchException the sum mall search exception
	 */
	IndexService createIndex(String index, Settings settings, String localNodeId) throws RestartException;

	
	/**
	 * Delete index.
	 *
	 * @param index the index
	 * @param reason the reason
	 * @throws SumMallSearchException the sum mall search exception
	 */
	void deleteIndex(String index, String reason) throws RestartException;

	
	/**
	 * Clean index.
	 *
	 * @param index the index
	 * @param reason the reason
	 * @throws SumMallSearchException the sum mall search exception
	 */
	void cleanIndex(String index, String reason) throws RestartException;
}
