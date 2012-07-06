/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core UidFieldSelector.java 2012-3-29 15:01:10 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper.selector;

import org.apache.lucene.document.FieldSelectorResult;

import cn.com.rebirth.search.commons.lucene.document.ResetFieldSelector;
import cn.com.rebirth.search.core.index.mapper.internal.UidFieldMapper;


/**
 * The Class UidFieldSelector.
 *
 * @author l.xue.nong
 */
public class UidFieldSelector implements ResetFieldSelector {

	
	/** The Constant INSTANCE. */
	public static final UidFieldSelector INSTANCE = new UidFieldSelector();

	
	/**
	 * Instantiates a new uid field selector.
	 */
	private UidFieldSelector() {

	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.document.FieldSelector#accept(java.lang.String)
	 */
	@Override
	public FieldSelectorResult accept(String fieldName) {
		if (UidFieldMapper.NAME.equals(fieldName)) {
			return FieldSelectorResult.LOAD_AND_BREAK;
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