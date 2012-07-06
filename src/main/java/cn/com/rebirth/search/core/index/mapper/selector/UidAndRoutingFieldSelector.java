/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core UidAndRoutingFieldSelector.java 2012-3-29 15:02:45 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper.selector;

import org.apache.lucene.document.FieldSelectorResult;

import cn.com.rebirth.search.commons.lucene.document.ResetFieldSelector;
import cn.com.rebirth.search.core.index.mapper.internal.RoutingFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.UidFieldMapper;


/**
 * The Class UidAndRoutingFieldSelector.
 *
 * @author l.xue.nong
 */
public class UidAndRoutingFieldSelector implements ResetFieldSelector {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5266034642645060611L;

	
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
		if (RoutingFieldMapper.NAME.equals(fieldName)) {
			if (++match == 2) {
				return FieldSelectorResult.LOAD_AND_BREAK;
			}
			return FieldSelectorResult.LOAD;
		}
		return FieldSelectorResult.NO_LOAD;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.lucene.document.ResetFieldSelector#reset()
	 */
	@Override
	public void reset() {
		match = 0;
	}
}
