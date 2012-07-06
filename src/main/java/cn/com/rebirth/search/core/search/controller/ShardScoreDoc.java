/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardScoreDoc.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.controller;

import org.apache.lucene.search.ScoreDoc;

import cn.com.rebirth.search.core.search.SearchShardTarget;

/**
 * The Class ShardScoreDoc.
 *
 * @author l.xue.nong
 */
public class ShardScoreDoc extends ScoreDoc implements ShardDoc {

	/** The shard target. */
	private final SearchShardTarget shardTarget;

	/**
	 * Instantiates a new shard score doc.
	 *
	 * @param shardTarget the shard target
	 * @param doc the doc
	 * @param score the score
	 */
	public ShardScoreDoc(SearchShardTarget shardTarget, int doc, float score) {
		super(doc, score);
		this.shardTarget = shardTarget;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.controller.ShardDoc#shardTarget()
	 */
	@Override
	public SearchShardTarget shardTarget() {
		return this.shardTarget;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.controller.ShardDoc#docId()
	 */
	@Override
	public int docId() {
		return doc;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.controller.ShardDoc#score()
	 */
	@Override
	public float score() {
		return score;
	}
}
