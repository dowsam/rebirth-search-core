/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardFieldDoc.java 2012-3-29 15:01:13 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.controller;

import org.apache.lucene.search.FieldDoc;

import cn.com.rebirth.search.core.search.SearchShardTarget;


/**
 * The Class ShardFieldDoc.
 *
 * @author l.xue.nong
 */
public class ShardFieldDoc extends FieldDoc implements ShardDoc {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1244670897734214036L;
	
	/** The shard target. */
	private final SearchShardTarget shardTarget;

	
	/**
	 * Instantiates a new shard field doc.
	 *
	 * @param shardTarget the shard target
	 * @param doc the doc
	 * @param score the score
	 */
	public ShardFieldDoc(SearchShardTarget shardTarget, int doc, float score) {
		super(doc, score);
		this.shardTarget = shardTarget;
	}

	
	/**
	 * Instantiates a new shard field doc.
	 *
	 * @param shardTarget the shard target
	 * @param doc the doc
	 * @param score the score
	 * @param fields the fields
	 */
	public ShardFieldDoc(SearchShardTarget shardTarget, int doc, float score, Object[] fields) {
		super(doc, score, fields);
		this.shardTarget = shardTarget;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.controller.ShardDoc#shardTarget()
	 */
	@Override
	public SearchShardTarget shardTarget() {
		return this.shardTarget;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.controller.ShardDoc#docId()
	 */
	@Override
	public int docId() {
		return this.doc;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.controller.ShardDoc#score()
	 */
	@Override
	public float score() {
		return score;
	}
}
