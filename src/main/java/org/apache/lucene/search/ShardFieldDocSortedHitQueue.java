/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardFieldDocSortedHitQueue.java 2012-3-29 15:04:17 l.xue.nong$$
 */


package org.apache.lucene.search;

import java.io.IOException;

import org.apache.lucene.util.PriorityQueue;

import cn.com.rebirth.commons.exception.RestartIllegalStateException;
import cn.com.rebirth.search.core.search.controller.ShardFieldDoc;



/**
 * The Class ShardFieldDocSortedHitQueue.
 *
 * @author l.xue.nong
 */
public class ShardFieldDocSortedHitQueue extends PriorityQueue<ShardFieldDoc> {

    
    /** The fields. */
    volatile SortField[] fields = null;
    /** The comparators. */
    @SuppressWarnings("rawtypes")
	FieldComparator[] comparators = null;

    
    /**
     * Instantiates a new shard field doc sorted hit queue.
     *
     * @param fields the fields
     * @param size the size
     */
    public ShardFieldDocSortedHitQueue(SortField[] fields, int size) {
        initialize(size);
        setFields(fields);
    }


    
    /**
     * Sets the fields.
     *
     * @param fields the new fields
     */
    public void setFields(SortField[] fields) {
        this.fields = fields;
        
        try {
            comparators = new FieldComparator[fields.length];
            for (int fieldIDX = 0; fieldIDX < fields.length; fieldIDX++) {
                comparators[fieldIDX] = fields[fieldIDX].getComparator(1, fieldIDX);
            }
        } catch (IOException e) {
            throw new RestartIllegalStateException("failed to get comparator", e);
        }
    }


    
    /**
     * Gets the fields.
     *
     * @return the fields
     */
    SortField[] getFields() {
        return fields;
    }


    
    /* (non-Javadoc)
     * @see org.apache.lucene.util.PriorityQueue#lessThan(java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected final boolean lessThan(final ShardFieldDoc docA, final ShardFieldDoc docB) {
        final int n = fields.length;
        int c = 0;
        for (int i = 0; i < n && c == 0; ++i) {
            final int type = fields[i].getType();
            if (type == SortField.STRING) {
                final String s1 = (String) docA.fields[i];
                final String s2 = (String) docB.fields[i];
                
                
                
                if (s1 == null) {
                    c = (s2 == null) ? 0 : -1;
                } else if (s2 == null) {
                    c = 1;
                } else { 
                    c = s1.compareTo(s2);
                }



            } else {
                c = comparators[i].compareValues(docA.fields[i], docB.fields[i]);
            }
            
            if (fields[i].getReverse()) {
                c = -c;
            }
        }

        
        if (c == 0) {
            
            c = docA.shardTarget().compareTo(docB.shardTarget());
            if (c == 0) {
                return docA.doc > docB.doc;
            }
        }

        return c > 0;
    }
}
