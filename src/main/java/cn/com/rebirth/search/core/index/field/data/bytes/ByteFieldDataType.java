/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ByteFieldDataType.java 2012-3-29 15:02:05 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.bytes;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.SortField;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.index.field.data.ExtendedFieldComparatorSource;


/**
 * The Class ByteFieldDataType.
 *
 * @author l.xue.nong
 */
public class ByteFieldDataType implements FieldDataType<ByteFieldData> {

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.FieldDataType#newFieldComparatorSource(cn.com.summall.search.core.index.cache.field.data.FieldDataCache, java.lang.String)
     */
    @Override
    public ExtendedFieldComparatorSource newFieldComparatorSource(final FieldDataCache cache, final String missing) {
        if (missing == null) {
            return new ExtendedFieldComparatorSource() {
                @Override
                public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed) throws IOException {
                    return new ByteFieldDataComparator(numHits, fieldname, cache);
                }

                @Override
                public int reducedType() {
                    return SortField.BYTE;
                }
            };
        }
        if (missing.equals("_last")) {
            return new ExtendedFieldComparatorSource() {
                @Override
                public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed) throws IOException {
                    return new ByteFieldDataMissingComparator(numHits, fieldname, cache, reversed ? Byte.MIN_VALUE : Byte.MAX_VALUE);
                }

                @Override
                public int reducedType() {
                    return SortField.BYTE;
                }
            };
        }
        if (missing.equals("_first")) {
            return new ExtendedFieldComparatorSource() {
                @Override
                public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed) throws IOException {
                    return new ByteFieldDataMissingComparator(numHits, fieldname, cache, reversed ? Byte.MAX_VALUE : Byte.MIN_VALUE);
                }

                @Override
                public int reducedType() {
                    return SortField.BYTE;
                }
            };
        }
        return new ExtendedFieldComparatorSource() {
            @Override
            public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed) throws IOException {
                return new ByteFieldDataMissingComparator(numHits, fieldname, cache, Byte.parseByte(missing));
            }

            @Override
            public int reducedType() {
                return SortField.BYTE;
            }
        };
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.FieldDataType#load(org.apache.lucene.index.IndexReader, java.lang.String)
     */
    @Override
    public ByteFieldData load(IndexReader reader, String fieldName) throws IOException {
        return ByteFieldData.load(reader, fieldName);
    }
}
