/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AllButSourceFieldSelector.java 2012-7-6 14:29:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.selector;

import org.apache.lucene.document.FieldSelectorResult;

import cn.com.rebirth.search.commons.lucene.document.ResetFieldSelector;
import cn.com.rebirth.search.core.index.mapper.internal.SourceFieldMapper;

/**
 * The Class AllButSourceFieldSelector.
 *
 * @author l.xue.nong
 */
public class AllButSourceFieldSelector implements ResetFieldSelector {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4822954158569997877L;

	/** The Constant INSTANCE. */
	public static final AllButSourceFieldSelector INSTANCE = new AllButSourceFieldSelector();

	/* (non-Javadoc)
	 * @see org.apache.lucene.document.FieldSelector#accept(java.lang.String)
	 */
	@Override
	public FieldSelectorResult accept(String fieldName) {
		if (SourceFieldMapper.NAME.equals(fieldName)) {
			return FieldSelectorResult.NO_LOAD;
		}
		return FieldSelectorResult.LOAD;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.lucene.document.ResetFieldSelector#reset()
	 */
	@Override
	public void reset() {
	}
}
