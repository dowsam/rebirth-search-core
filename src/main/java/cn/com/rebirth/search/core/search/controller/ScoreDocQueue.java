/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ScoreDocQueue.java 2012-3-29 15:02:10 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.controller;

import org.apache.lucene.util.PriorityQueue;


/**
 * The Class ScoreDocQueue.
 *
 * @author l.xue.nong
 */
public class ScoreDocQueue extends PriorityQueue<ShardScoreDoc> {

    
    /**
     * Instantiates a new score doc queue.
     *
     * @param size the size
     */
    public ScoreDocQueue(int size) {
        initialize(size);
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.util.PriorityQueue#lessThan(java.lang.Object, java.lang.Object)
     */
    protected final boolean lessThan(ShardScoreDoc hitA, ShardScoreDoc hitB) {
        if (hitA.score == hitB.score) {
            int c = hitA.shardTarget().compareTo(hitB.shardTarget());
            if (c == 0) {
                return hitA.doc > hitB.doc;
            }
            return c > 0;
        } else {
            return hitA.score < hitB.score;
        }
    }
}
