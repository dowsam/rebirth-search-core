/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FloatFieldDataComparator.java 2012-3-29 15:02:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.floats;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.support.NumericFieldDataComparator;



/**
 * The Class FloatFieldDataComparator.
 *
 * @author l.xue.nong
 */
public class FloatFieldDataComparator extends NumericFieldDataComparator {

    
    /** The values. */
    private final float[] values;
    
    
    /** The bottom. */
    private float bottom;

    
    /**
     * Instantiates a new float field data comparator.
     *
     * @param numHits the num hits
     * @param fieldName the field name
     * @param fieldDataCache the field data cache
     */
    public FloatFieldDataComparator(int numHits, String fieldName, FieldDataCache fieldDataCache) {
        super(fieldName, fieldDataCache);
        values = new float[numHits];
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.support.NumericFieldDataComparator#fieldDataType()
     */
    @Override
    public FieldDataType fieldDataType() {
        return FieldDataType.DefaultTypes.FLOAT;
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.FieldComparator#compare(int, int)
     */
    @Override
    public int compare(int slot1, int slot2) {
        
        
        final float v1 = values[slot1];
        final float v2 = values[slot2];
        if (v1 > v2) {
            return 1;
        } else if (v1 < v2) {
            return -1;
        } else {
            return 0;
        }
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.FieldComparator#compareBottom(int)
     */
    @Override
    public int compareBottom(int doc) {
        
        
        final float v2 = currentFieldData.floatValue(doc);
        if (bottom > v2) {
            return 1;
        } else if (bottom < v2) {
            return -1;
        } else {
            return 0;
        }
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.FieldComparator#copy(int, int)
     */
    @Override
    public void copy(int slot, int doc) {
        values[slot] = currentFieldData.floatValue(doc);
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.FieldComparator#setBottom(int)
     */
    @Override
    public void setBottom(final int bottom) {
        this.bottom = values[bottom];
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.FieldComparator#value(int)
     */
    @Override
    public Comparable value(int slot) {
        return Float.valueOf(values[slot]);
    }
}
