/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardScoreDoc.java 2012-3-29 15:01:12 l.xue.nong$$
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
        return doc;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.search.controller.ShardDoc#score()
     */
    @Override
    public float score() {
        return score;
    }
}
