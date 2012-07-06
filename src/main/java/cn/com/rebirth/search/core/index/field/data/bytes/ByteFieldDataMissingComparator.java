/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ByteFieldDataMissingComparator.java 2012-3-29 15:01:24 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.bytes;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.support.NumericFieldDataComparator;


/**
 * The Class ByteFieldDataMissingComparator.
 *
 * @author l.xue.nong
 */
public class ByteFieldDataMissingComparator extends NumericFieldDataComparator {

    
    /** The values. */
    private final byte[] values;
    
    
    /** The bottom. */
    private short bottom;
    
    
    /** The missing value. */
    private final byte missingValue;

    
    /**
     * Instantiates a new byte field data missing comparator.
     *
     * @param numHits the num hits
     * @param fieldName the field name
     * @param fieldDataCache the field data cache
     * @param missingValue the missing value
     */
    public ByteFieldDataMissingComparator(int numHits, String fieldName, FieldDataCache fieldDataCache, byte missingValue) {
        super(fieldName, fieldDataCache);
        values = new byte[numHits];
        this.missingValue = missingValue;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.support.NumericFieldDataComparator#fieldDataType()
     */
    @Override
    public FieldDataType fieldDataType() {
        return FieldDataType.DefaultTypes.BYTE;
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
        byte value = missingValue;
        if (currentFieldData.hasValue(doc)) {
            value = currentFieldData.byteValue(doc);
        }
        return bottom - value;
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.FieldComparator#copy(int, int)
     */
    @Override
    public void copy(int slot, int doc) {
        byte value = missingValue;
        if (currentFieldData.hasValue(doc)) {
            value = currentFieldData.byteValue(doc);
        }
        values[slot] = value;
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
        return Byte.valueOf(values[slot]);
    }
}
