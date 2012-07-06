/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MergeMappingException.java 2012-7-6 14:29:24 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper;

import java.util.Arrays;

/**
 * The Class MergeMappingException.
 *
 * @author l.xue.nong
 */
public class MergeMappingException extends MapperException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4188805280080087948L;

	/** The failures. */
	private final String[] failures;

	/**
	 * Instantiates a new merge mapping exception.
	 *
	 * @param failures the failures
	 */
	public MergeMappingException(String[] failures) {
		super("Merge failed with failures {" + Arrays.toString(failures) + "}");
		this.failures = failures;
	}

	/**
	 * Failures.
	 *
	 * @return the string[]
	 */
	public String[] failures() {
		return failures;
	}

}
