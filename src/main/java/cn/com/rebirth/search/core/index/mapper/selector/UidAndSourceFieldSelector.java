/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core UidAndSourceFieldSelector.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.selector;

import org.apache.lucene.document.FieldSelectorResult;

import cn.com.rebirth.search.commons.lucene.document.ResetFieldSelector;
import cn.com.rebirth.search.core.index.mapper.internal.SourceFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.UidFieldMapper;

/**
 * The Class UidAndSourceFieldSelector.
 *
 * @author l.xue.nong
 */
public class UidAndSourceFieldSelector implements ResetFieldSelector {

	/** The match. */
	private int match = 0;

	/* (non-Javadoc)
	 * @see org.apache.lucene.document.FieldSelector#accept(java.lang.String)
	 */
	@Override
	public FieldSelectorResult accept(String fieldName) {
		if (UidFieldMapper.NAME.equals(fieldName)) {
			if (++match == 2) {
				return FieldSelectorResult.LOAD_AND_BREAK;
			}
			return FieldSelectorResult.LOAD;
		}
		if (SourceFieldMapper.NAME.equals(fieldName)) {
			if (++match == 2) {
				return FieldSelectorResult.LOAD_AND_BREAK;
			}
			return FieldSelectorResult.LOAD;
		}
		return FieldSelectorResult.NO_LOAD;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.lucene.document.ResetFieldSelector#reset()
	 */
	@Override
	public void reset() {
		match = 0;
	}
}
