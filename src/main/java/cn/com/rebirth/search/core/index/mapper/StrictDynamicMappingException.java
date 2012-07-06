/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StrictDynamicMappingException.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper;

/**
 * The Class StrictDynamicMappingException.
 *
 * @author l.xue.nong
 */
public class StrictDynamicMappingException extends MapperException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4099635702297475335L;

	/**
	 * Instantiates a new strict dynamic mapping exception.
	 *
	 * @param path the path
	 * @param fieldName the field name
	 */
	public StrictDynamicMappingException(String path, String fieldName) {
		super("mapping set to strict, dynamic introduction of [" + fieldName + "] within [" + path + "] is not allowed");
	}

}