/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShortFieldDataComparator.java 2012-3-29 15:02:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.shorts;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.support.NumericFieldDataComparator;



/**
 * The Class ShortFieldDataComparator.
 *
 * @author l.xue.nong
 */
public class ShortFieldDataComparator extends NumericFieldDataComparator {

    
    /** The values. */
    private final short[] values;
    
    
    /** The bottom. */
    private short bottom;

    
    /**
     * Instantiates a new short field data comparator.
     *
     * @param numHits the num hits
     * @param fieldName the field name
     * @param fieldDataCache the field data cache
     */
    public ShortFieldDataComparator(int numHits, String fieldName, FieldDataCache fieldDataCache) {
        super(fieldName, fieldDataCache);
        values = new short[numHits];
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.support.NumericFieldDataComparator#fieldDataType()
     */
    @Override
    public FieldDataType fieldDataType() {
        return FieldDataType.DefaultTypes.SHORT;
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.FieldComparator#compare(int, int)
     */
    @Override
    public int compare(int slot1, int slot2) {
        return values[slot1] - values[slot2];
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.FieldComparator#compareBottom(int)
     */
    @Override
    public int compareBottom(int doc) {
        return bottom - currentFieldData.shortValue(doc);
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.FieldComparator#copy(int, int)
     */
    @Override
    public void copy(int slot, int doc) {
        values[slot] = currentFieldData.shortValue(doc);
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
        return Short.valueOf(values[slot]);
    }
}
