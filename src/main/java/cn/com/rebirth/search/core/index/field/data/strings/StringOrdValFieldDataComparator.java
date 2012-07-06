/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core StringOrdValFieldDataComparator.java 2012-3-29 15:02:19 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.strings;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldData;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;



/**
 * The Class StringOrdValFieldDataComparator.
 *
 * @author l.xue.nong
 */
public class StringOrdValFieldDataComparator extends FieldComparator {

    
    /** The field data cache. */
    private final FieldDataCache fieldDataCache;

    
    /** The ords. */
    private final int[] ords;
    
    
    /** The values. */
    private final String[] values;
    
    
    /** The reader gen. */
    private final int[] readerGen;

    
    /** The current reader gen. */
    private int currentReaderGen = -1;
    
    
    /** The lookup. */
    private String[] lookup;
    
    
    /** The order. */
    private int[] order;
    
    
    /** The field. */
    private final String field;

    
    /** The bottom slot. */
    private int bottomSlot = -1;
    
    
    /** The bottom ord. */
    private int bottomOrd;
    
    
    /** The bottom same reader. */
    private boolean bottomSameReader;
    
    
    /** The bottom value. */
    private String bottomValue;

    
    /**
     * Instantiates a new string ord val field data comparator.
     *
     * @param numHits the num hits
     * @param field the field
     * @param sortPos the sort pos
     * @param reversed the reversed
     * @param fieldDataCache the field data cache
     */
    public StringOrdValFieldDataComparator(int numHits, String field, int sortPos, boolean reversed, FieldDataCache fieldDataCache) {
        this.fieldDataCache = fieldDataCache;
        ords = new int[numHits];
        values = new String[numHits];
        readerGen = new int[numHits];
        this.field = field;
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.FieldComparator#compare(int, int)
     */
    @Override
    public int compare(int slot1, int slot2) {
        if (readerGen[slot1] == readerGen[slot2]) {
            return ords[slot1] - ords[slot2];
        }

        final String val1 = values[slot1];
        final String val2 = values[slot2];
        if (val1 == null) {
            if (val2 == null) {
                return 0;
            }
            return -1;
        } else if (val2 == null) {
            return 1;
        }
        return val1.compareTo(val2);
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.FieldComparator#compareBottom(int)
     */
    @Override
    public int compareBottom(int doc) {
        assert bottomSlot != -1;
        if (bottomSameReader) {
            
            return bottomOrd - this.order[doc];
        } else {
            
            
            
            final int order = this.order[doc];
            final int cmp = bottomOrd - order;
            if (cmp != 0) {
                return cmp;
            }

            final String val2 = lookup[order];
            if (bottomValue == null) {
                if (val2 == null) {
                    return 0;
                }
                
                return -1;
            } else if (val2 == null) {
                
                return 1;
            }
            return bottomValue.compareTo(val2);
        }
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.FieldComparator#copy(int, int)
     */
    @Override
    public void copy(int slot, int doc) {
        final int ord = order[doc];
        ords[slot] = ord;
        assert ord >= 0;
        values[slot] = lookup[ord];
        readerGen[slot] = currentReaderGen;
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.FieldComparator#setNextReader(org.apache.lucene.index.IndexReader, int)
     */
    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
        FieldData cleanFieldData = fieldDataCache.cache(FieldDataType.DefaultTypes.STRING, reader, field);
        if (cleanFieldData instanceof MultiValueStringFieldData) {
            throw new IOException("Can't sort on string types with more than one value per doc, or more than one token per field");
        }
        SingleValueStringFieldData fieldData = (SingleValueStringFieldData) cleanFieldData;
        currentReaderGen++;
        order = fieldData.ordinals();
        lookup = fieldData.values();
        assert lookup.length > 0;
        if (bottomSlot != -1) {
            setBottom(bottomSlot);
        }
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.FieldComparator#setBottom(int)
     */
    @Override
    public void setBottom(final int bottom) {
        bottomSlot = bottom;

        bottomValue = values[bottomSlot];
        if (currentReaderGen == readerGen[bottomSlot]) {
            bottomOrd = ords[bottomSlot];
            bottomSameReader = true;
        } else {
            if (bottomValue == null) {
                ords[bottomSlot] = 0;
                bottomOrd = 0;
                bottomSameReader = true;
                readerGen[bottomSlot] = currentReaderGen;
            } else {
                final int index = binarySearch(lookup, bottomValue);
                if (index < 0) {
                    bottomOrd = -index - 2;
                    bottomSameReader = false;
                } else {
                    bottomOrd = index;
                    
                    bottomSameReader = true;
                    readerGen[bottomSlot] = currentReaderGen;
                    ords[bottomSlot] = bottomOrd;
                }
            }
        }
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.FieldComparator#value(int)
     */
    @Override
    public Comparable value(int slot) {
        return values[slot];
    }

    
    /**
     * Gets the values.
     *
     * @return the values
     */
    public String[] getValues() {
        return values;
    }

    
    /**
     * Gets the bottom slot.
     *
     * @return the bottom slot
     */
    public int getBottomSlot() {
        return bottomSlot;
    }

    
    /**
     * Gets the field.
     *
     * @return the field
     */
    public String getField() {
        return field;
    }

}
