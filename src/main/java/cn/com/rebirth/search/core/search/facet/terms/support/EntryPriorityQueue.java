/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core EntryPriorityQueue.java 2012-3-29 15:01:15 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.terms.support;

import java.util.Comparator;

import org.apache.lucene.util.PriorityQueue;

import cn.com.rebirth.search.core.search.facet.terms.TermsFacet;


/**
 * The Class EntryPriorityQueue.
 *
 * @author l.xue.nong
 */
public class EntryPriorityQueue extends PriorityQueue<TermsFacet.Entry> {

	
	/** The Constant LIMIT. */
	public static final int LIMIT = 5000;

	
	/** The comparator. */
	private final Comparator<TermsFacet.Entry> comparator;

	
	/**
	 * Instantiates a new entry priority queue.
	 *
	 * @param size the size
	 * @param comparator the comparator
	 */
	public EntryPriorityQueue(int size, Comparator<TermsFacet.Entry> comparator) {
		initialize(size);
		this.comparator = comparator;
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.util.PriorityQueue#lessThan(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected boolean lessThan(TermsFacet.Entry a, TermsFacet.Entry b) {
		return comparator.compare(a, b) > 0; 
	}
}
