/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FloatFieldDataType.java 2012-3-29 15:01:50 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.floats;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.SortField;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.index.field.data.ExtendedFieldComparatorSource;


/**
 * The Class FloatFieldDataType.
 *
 * @author l.xue.nong
 */
public class FloatFieldDataType implements FieldDataType<FloatFieldData> {

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.FieldDataType#newFieldComparatorSource(cn.com.summall.search.core.index.cache.field.data.FieldDataCache, java.lang.String)
     */
    @Override
    public ExtendedFieldComparatorSource newFieldComparatorSource(final FieldDataCache cache, final String missing) {
        if (missing == null) {
            return new ExtendedFieldComparatorSource() {
                @Override
                public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed) throws IOException {
                    return new FloatFieldDataComparator(numHits, fieldname, cache);
                }

                @Override
                public int reducedType() {
                    return SortField.FLOAT;
                }
            };
        }
        if (missing.equals("_last")) {
            return new ExtendedFieldComparatorSource() {
                @Override
                public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed) throws IOException {
                    return new FloatFieldDataMissingComparator(numHits, fieldname, cache, reversed ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY);
                }

                @Override
                public int reducedType() {
                    return SortField.FLOAT;
                }
            };
        }
        if (missing.equals("_first")) {
            return new ExtendedFieldComparatorSource() {
                @Override
                public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed) throws IOException {
                    return new FloatFieldDataMissingComparator(numHits, fieldname, cache, reversed ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY);
                }

                @Override
                public int reducedType() {
                    return SortField.FLOAT;
                }
            };
        }
        return new ExtendedFieldComparatorSource() {
            @Override
            public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed) throws IOException {
                return new FloatFieldDataMissingComparator(numHits, fieldname, cache, Float.parseFloat(missing));
            }

            @Override
            public int reducedType() {
                return SortField.FLOAT;
            }
        };
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.FieldDataType#load(org.apache.lucene.index.IndexReader, java.lang.String)
     */
    @Override
    public FloatFieldData load(IndexReader reader, String fieldName) throws IOException {
        return FloatFieldData.load(reader, fieldName);
    }
}
