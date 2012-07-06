/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ObjectMapperListener.java 2012-3-29 15:02:40 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper;

import cn.com.rebirth.search.core.index.mapper.object.ObjectMapper;


/**
 * The listener interface for receiving objectMapper events.
 * The class that is interested in processing a objectMapper
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addObjectMapperListener<code> method. When
 * the objectMapper event occurs, that object's appropriate
 * method is invoked.
 *
 * @see ObjectMapperEvent
 */
public interface ObjectMapperListener {

	
	/**
	 * Object mapper.
	 *
	 * @param objectMapper the object mapper
	 */
	void objectMapper(ObjectMapper objectMapper);
}
