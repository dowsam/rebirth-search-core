/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexMissingException.java 2012-3-29 15:02:14 l.xue.nong$$
 */


package cn.com.rebirth.search.core.indices;

import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.IndexException;


/**
 * The Class IndexMissingException.
 *
 * @author l.xue.nong
 */
public class IndexMissingException extends IndexException {

    
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1007256881642021231L;

	
    /**
     * Instantiates a new index missing exception.
     *
     * @param index the index
     */
    public IndexMissingException(Index index) {
        super(index, "missing");
    }

}