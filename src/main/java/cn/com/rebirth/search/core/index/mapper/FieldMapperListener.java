/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FieldMapperListener.java 2012-7-6 14:29:23 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper;

/**
 * The listener interface for receiving fieldMapper events.
 * The class that is interested in processing a fieldMapper
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addFieldMapperListener<code> method. When
 * the fieldMapper event occurs, that object's appropriate
 * method is invoked.
 *
 * @see FieldMapperEvent
 */
public interface FieldMapperListener {

	/**
	 * Field mapper.
	 *
	 * @param fieldMapper the field mapper
	 */
	void fieldMapper(FieldMapper fieldMapper);
}
