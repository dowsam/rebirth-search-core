/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core LongFieldData.java 2012-3-29 15:02:39 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.longs;

import gnu.trove.list.array.TLongArrayList;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.search.commons.RamUsage;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.NumericFieldData;
import cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader;


/**
 * The Class LongFieldData.
 *
 * @author l.xue.nong
 */
public abstract class LongFieldData extends NumericFieldData<LongDocFieldData> {

    
    /** The Constant EMPTY_LONG_ARRAY. */
    static final long[] EMPTY_LONG_ARRAY = new long[0];
    
    
    /** The Constant EMPTY_DATETIME_ARRAY. */
    static final MutableDateTime[] EMPTY_DATETIME_ARRAY = new MutableDateTime[0];

    
    /** The date time cache. */
    ThreadLocal<ThreadLocals.CleanableValue<MutableDateTime>> dateTimeCache = new ThreadLocal<ThreadLocals.CleanableValue<MutableDateTime>>() {
        @Override
        protected ThreadLocals.CleanableValue<MutableDateTime> initialValue() {
            return new ThreadLocals.CleanableValue<MutableDateTime>(new MutableDateTime(DateTimeZone.UTC));
        }
    };

    
    /** The values. */
    protected final long[] values;

    
    /**
     * Instantiates a new long field data.
     *
     * @param fieldName the field name
     * @param values the values
     */
    protected LongFieldData(String fieldName, long[] values) {
        super(fieldName);
        this.values = values;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.FieldData#computeSizeInBytes()
     */
    @Override
    protected long computeSizeInBytes() {
        return RamUsage.NUM_BYTES_LONG * values.length + RamUsage.NUM_BYTES_ARRAY_HEADER;
    }

    
    /**
     * Values.
     *
     * @return the long[]
     */
    public final long[] values() {
        return this.values;
    }

    
    /**
     * Value.
     *
     * @param docId the doc id
     * @return the long
     */
    abstract public long value(int docId);

    
    /**
     * Values.
     *
     * @param docId the doc id
     * @return the long[]
     */
    abstract public long[] values(int docId);

    
    /**
     * Date.
     *
     * @param docId the doc id
     * @return the mutable date time
     */
    public MutableDateTime date(int docId) {
        MutableDateTime dateTime = dateTimeCache.get().get();
        dateTime.setMillis(value(docId));
        return dateTime;
    }

    
    /**
     * Date.
     *
     * @param docId the doc id
     * @param dateTime the date time
     */
    public void date(int docId, MutableDateTime dateTime) {
        dateTime.setMillis(value(docId));
    }

    
    /**
     * Dates.
     *
     * @param docId the doc id
     * @return the mutable date time[]
     */
    public abstract MutableDateTime[] dates(int docId);

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.NumericFieldData#docFieldData(int)
     */
    @Override
    public LongDocFieldData docFieldData(int docId) {
        return super.docFieldData(docId);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.FieldData#createFieldData()
     */
    @Override
    protected LongDocFieldData createFieldData() {
        return new LongDocFieldData(this);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.FieldData#forEachValue(cn.com.summall.search.core.index.field.data.FieldData.StringValueProc)
     */
    @Override
    public void forEachValue(StringValueProc proc) {
        for (int i = 1; i < values.length; i++) {
            proc.onValue(Long.toString(values[i]));
        }
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.FieldData#stringValue(int)
     */
    @Override
    public String stringValue(int docId) {
        return Long.toString(docId);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.NumericFieldData#byteValue(int)
     */
    @Override
    public byte byteValue(int docId) {
        return (byte) value(docId);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.NumericFieldData#shortValue(int)
     */
    @Override
    public short shortValue(int docId) {
        return (short) value(docId);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.NumericFieldData#intValue(int)
     */
    @Override
    public int intValue(int docId) {
        return (int) value(docId);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.NumericFieldData#longValue(int)
     */
    @Override
    public long longValue(int docId) {
        return value(docId);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.NumericFieldData#floatValue(int)
     */
    @Override
    public float floatValue(int docId) {
        return (float) value(docId);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.NumericFieldData#doubleValue(int)
     */
    @Override
    public double doubleValue(int docId) {
        return (double) value(docId);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.field.data.FieldData#type()
     */
    @Override
    public FieldDataType type() {
        return FieldDataType.DefaultTypes.LONG;
    }

    
    /**
     * For each value.
     *
     * @param proc the proc
     */
    public void forEachValue(ValueProc proc) {
        for (int i = 1; i < values.length; i++) {
            proc.onValue(values[i]);
        }
    }

    
    /**
     * The Interface ValueProc.
     *
     * @author l.xue.nong
     */
    public static interface ValueProc {
        
        
        /**
         * On value.
         *
         * @param value the value
         */
        void onValue(long value);
    }

    
    /**
     * For each value in doc.
     *
     * @param docId the doc id
     * @param proc the proc
     */
    public abstract void forEachValueInDoc(int docId, ValueInDocProc proc);

    
    /**
     * The Interface ValueInDocProc.
     *
     * @author l.xue.nong
     */
    public static interface ValueInDocProc {
        
        
        /**
         * On value.
         *
         * @param docId the doc id
         * @param value the value
         */
        void onValue(int docId, long value);

        
        /**
         * On missing.
         *
         * @param docId the doc id
         */
        void onMissing(int docId);
    }

    
    /**
     * For each value in doc.
     *
     * @param docId the doc id
     * @param proc the proc
     */
    public abstract void forEachValueInDoc(int docId, DateValueInDocProc proc);

    
    /**
     * For each value in doc.
     *
     * @param docId the doc id
     * @param dateTime the date time
     * @param proc the proc
     */
    public abstract void forEachValueInDoc(int docId, MutableDateTime dateTime, DateValueInDocProc proc);

    
    /**
     * The Interface DateValueInDocProc.
     *
     * @author l.xue.nong
     */
    public static interface DateValueInDocProc {
        
        
        /**
         * On value.
         *
         * @param docId the doc id
         * @param dateTime the date time
         */
        void onValue(int docId, MutableDateTime dateTime);
    }

    
    /**
     * Load.
     *
     * @param reader the reader
     * @param field the field
     * @return the long field data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static LongFieldData load(IndexReader reader, String field) throws IOException {
        return FieldDataLoader.load(reader, field, new LongTypeLoader());
    }

    
    /**
     * The Class LongTypeLoader.
     *
     * @author l.xue.nong
     */
    static class LongTypeLoader extends FieldDataLoader.FreqsTypeLoader<LongFieldData> {

        
        /** The terms. */
        private final TLongArrayList terms = new TLongArrayList();

        
        /**
         * Instantiates a new long type loader.
         */
        LongTypeLoader() {
            super();
            
            terms.add(0);
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.core.index.field.data.support.FieldDataLoader.TypeLoader#collectTerm(java.lang.String)
         */
        @Override
        public void collectTerm(String term) {
            terms.add(FieldCache.NUMERIC_UTILS_LONG_PARSER.parseLong(term));
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.core.index.field.data.support.FieldDataLoader.TypeLoader#buildSingleValue(java.lang.String, int[])
         */
        @Override
        public LongFieldData buildSingleValue(String field, int[] ordinals) {
            return new SingleValueLongFieldData(field, ordinals, terms.toArray());
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.core.index.field.data.support.FieldDataLoader.TypeLoader#buildMultiValue(java.lang.String, int[][])
         */
        @Override
        public LongFieldData buildMultiValue(String field, int[][] ordinals) {
            return new MultiValueLongFieldData(field, ordinals, terms.toArray());
        }
    }
}