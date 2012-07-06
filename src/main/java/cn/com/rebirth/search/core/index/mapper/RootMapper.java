/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RootMapper.java 2012-7-6 14:30:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper;

import java.io.IOException;

/**
 * The Interface RootMapper.
 *
 * @author l.xue.nong
 */
public interface RootMapper extends Mapper {

	/**
	 * Pre parse.
	 *
	 * @param context the context
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void preParse(ParseContext context) throws IOException;

	/**
	 * Post parse.
	 *
	 * @param context the context
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void postParse(ParseContext context) throws IOException;

	/**
	 * Validate.
	 *
	 * @param context the context
	 * @throws MapperParsingException the mapper parsing exception
	 */
	void validate(ParseContext context) throws MapperParsingException;

	/**
	 * Include in object.
	 *
	 * @return true, if successful
	 */
	boolean includeInObject();
}
