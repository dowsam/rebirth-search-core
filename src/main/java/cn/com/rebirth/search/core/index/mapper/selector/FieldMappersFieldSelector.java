/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FieldMappersFieldSelector.java 2012-3-29 15:02:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.selector;

import java.util.HashSet;

import org.apache.lucene.document.FieldSelectorResult;

import cn.com.rebirth.search.commons.lucene.document.ResetFieldSelector;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.FieldMappers;

/**
 * The Class FieldMappersFieldSelector.
 *
 * @author l.xue.nong
 */
public class FieldMappersFieldSelector implements ResetFieldSelector {

	private static final long serialVersionUID = 8527280328157772313L;
	/** The names. */
	private final HashSet<String> names = new HashSet<String>();

	/**
	 * Adds the.
	 *
	 * @param fieldName the field name
	 */
	public void add(String fieldName) {
		names.add(fieldName);
	}

	/**
	 * Adds the.
	 *
	 * @param fieldMappers the field mappers
	 */
	public void add(FieldMappers fieldMappers) {
		for (FieldMapper fieldMapper : fieldMappers) {
			names.add(fieldMapper.names().indexName());
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.document.FieldSelector#accept(java.lang.String)
	 */
	@Override
	public FieldSelectorResult accept(String fieldName) {
		if (names.contains(fieldName)) {
			return FieldSelectorResult.LOAD;
		}
		return FieldSelectorResult.NO_LOAD;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.lucene.document.ResetFieldSelector#reset()
	 */
	@Override
	public void reset() {
	}
}
