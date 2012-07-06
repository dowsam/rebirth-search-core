/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core QueryBuilder.java 2012-3-29 15:01:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import cn.com.rebirth.search.commons.io.BytesStream;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentType;


/**
 * The Interface QueryBuilder.
 *
 * @author l.xue.nong
 */
public interface QueryBuilder extends ToXContent {

	
	/**
	 * Builds the as bytes.
	 *
	 * @return the bytes stream
	 * @throws QueryBuilderException the query builder exception
	 */
	BytesStream buildAsBytes() throws QueryBuilderException;

	
	/**
	 * Builds the as bytes.
	 *
	 * @param contentType the content type
	 * @return the bytes stream
	 * @throws QueryBuilderException the query builder exception
	 */
	BytesStream buildAsBytes(XContentType contentType) throws QueryBuilderException;
}
