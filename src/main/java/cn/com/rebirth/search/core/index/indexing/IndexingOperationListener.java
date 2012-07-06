/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexingOperationListener.java 2012-3-29 15:01:32 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.indexing;

import cn.com.rebirth.search.core.index.engine.Engine;


/**
 * The listener interface for receiving indexingOperation events.
 * The class that is interested in processing a indexingOperation
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addIndexingOperationListener<code> method. When
 * the indexingOperation event occurs, that object's appropriate
 * method is invoked.
 *
 * @see IndexingOperationEvent
 */
public abstract class IndexingOperationListener {

	
	/**
	 * Pre create.
	 *
	 * @param create the create
	 * @return the engine. create
	 */
	public Engine.Create preCreate(Engine.Create create) {
		return create;
	}

	
	/**
	 * Post create.
	 *
	 * @param create the create
	 */
	public void postCreate(Engine.Create create) {

	}

	
	/**
	 * Pre index.
	 *
	 * @param index the index
	 * @return the engine. index
	 */
	public Engine.Index preIndex(Engine.Index index) {
		return index;
	}

	
	/**
	 * Post index.
	 *
	 * @param index the index
	 */
	public void postIndex(Engine.Index index) {

	}

	
	/**
	 * Pre delete.
	 *
	 * @param delete the delete
	 * @return the engine. delete
	 */
	public Engine.Delete preDelete(Engine.Delete delete) {
		return delete;
	}

	
	/**
	 * Post delete.
	 *
	 * @param delete the delete
	 */
	public void postDelete(Engine.Delete delete) {

	}

	
	/**
	 * Pre delete by query.
	 *
	 * @param deleteByQuery the delete by query
	 * @return the engine. delete by query
	 */
	public Engine.DeleteByQuery preDeleteByQuery(Engine.DeleteByQuery deleteByQuery) {
		return deleteByQuery;
	}

	
	/**
	 * Post delete by query.
	 *
	 * @param deleteByQuery the delete by query
	 */
	public void postDeleteByQuery(Engine.DeleteByQuery deleteByQuery) {

	}
}
