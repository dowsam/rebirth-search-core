/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SourceFieldSelector.java 2012-7-6 14:30:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.internal;

import org.apache.lucene.document.FieldSelectorResult;

import cn.com.rebirth.search.commons.lucene.document.ResetFieldSelector;

/**
 * The Class SourceFieldSelector.
 *
 * @author l.xue.nong
 */
public class SourceFieldSelector implements ResetFieldSelector {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6329639956526526842L;

	/** The Constant INSTANCE. */
	public static final SourceFieldSelector INSTANCE = new SourceFieldSelector();

	/**
	 * Instantiates a new source field selector.
	 */
	private SourceFieldSelector() {

	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.document.FieldSelector#accept(java.lang.String)
	 */
	@Override
	public FieldSelectorResult accept(String fieldName) {
		if (SourceFieldMapper.NAME.equals(fieldName)) {
			return FieldSelectorResult.LOAD_AND_BREAK;
		}
		return FieldSelectorResult.NO_LOAD;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.lucene.document.ResetFieldSelector#reset()
	 */
	@Override
	public void reset() {
	}
}