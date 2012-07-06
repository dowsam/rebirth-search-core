/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NumericRangeFieldDataFilter.java 2012-3-29 15:02:47 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.search;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.NumericUtils;

import cn.com.rebirth.search.commons.lucene.docset.DocSet;
import cn.com.rebirth.search.commons.lucene.docset.GetDocSet;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.bytes.ByteFieldData;
import cn.com.rebirth.search.core.index.field.data.doubles.DoubleFieldData;
import cn.com.rebirth.search.core.index.field.data.floats.FloatFieldData;
import cn.com.rebirth.search.core.index.field.data.ints.IntFieldData;
import cn.com.rebirth.search.core.index.field.data.longs.LongFieldData;
import cn.com.rebirth.search.core.index.field.data.shorts.ShortFieldData;


/**
 * The Class NumericRangeFieldDataFilter.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public abstract class NumericRangeFieldDataFilter<T> extends Filter {

	private static final long serialVersionUID = -5232935646467936725L;


	/** The field data cache. */
    final FieldDataCache fieldDataCache;
    
    
    /** The field. */
    final String field;
    
    
    /** The lower val. */
    final T lowerVal;
    
    
    /** The upper val. */
    final T upperVal;
    
    
    /** The include lower. */
    final boolean includeLower;
    
    
    /** The include upper. */
    final boolean includeUpper;

    
    /**
     * Gets the field.
     *
     * @return the field
     */
    public String getField() {
        return field;
    }

    
    /**
     * Gets the lower val.
     *
     * @return the lower val
     */
    public T getLowerVal() {
        return lowerVal;
    }

    
    /**
     * Gets the upper val.
     *
     * @return the upper val
     */
    public T getUpperVal() {
        return upperVal;
    }

    
    /**
     * Checks if is include lower.
     *
     * @return true, if is include lower
     */
    public boolean isIncludeLower() {
        return includeLower;
    }

    
    /**
     * Checks if is include upper.
     *
     * @return true, if is include upper
     */
    public boolean isIncludeUpper() {
        return includeUpper;
    }

    
    /**
     * Instantiates a new numeric range field data filter.
     *
     * @param fieldDataCache the field data cache
     * @param field the field
     * @param lowerVal the lower val
     * @param upperVal the upper val
     * @param includeLower the include lower
     * @param includeUpper the include upper
     */
    protected NumericRangeFieldDataFilter(FieldDataCache fieldDataCache, String field, T lowerVal, T upperVal, boolean includeLower, boolean includeUpper) {
        this.fieldDataCache = fieldDataCache;
        this.field = field;
        this.lowerVal = lowerVal;
        this.upperVal = upperVal;
        this.includeLower = includeLower;
        this.includeUpper = includeUpper;
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder(field).append(":");
        return sb.append(includeLower ? '[' : '{')
                .append((lowerVal == null) ? "*" : lowerVal.toString())
                .append(" TO ")
                .append((upperVal == null) ? "*" : upperVal.toString())
                .append(includeUpper ? ']' : '}')
                .toString();
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumericRangeFieldDataFilter)) return false;
        NumericRangeFieldDataFilter other = (NumericRangeFieldDataFilter) o;

        if (!this.field.equals(other.field)
                || this.includeLower != other.includeLower
                || this.includeUpper != other.includeUpper
                ) {
            return false;
        }
        if (this.lowerVal != null ? !this.lowerVal.equals(other.lowerVal) : other.lowerVal != null) return false;
        if (this.upperVal != null ? !this.upperVal.equals(other.upperVal) : other.upperVal != null) return false;
        return true;
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public final int hashCode() {
        int h = field.hashCode();
        h ^= (lowerVal != null) ? lowerVal.hashCode() : 550356204;
        h = (h << 1) | (h >>> 31);  
        h ^= (upperVal != null) ? upperVal.hashCode() : -1674416163;
        h ^= (includeLower ? 1549299360 : -365038026) ^ (includeUpper ? 1721088258 : 1948649653);
        return h;
    }

    
    /**
     * New byte range.
     *
     * @param fieldDataCache the field data cache
     * @param field the field
     * @param lowerVal the lower val
     * @param upperVal the upper val
     * @param includeLower the include lower
     * @param includeUpper the include upper
     * @return the numeric range field data filter
     */
    public static NumericRangeFieldDataFilter<Byte> newByteRange(FieldDataCache fieldDataCache, String field, Byte lowerVal, Byte upperVal, boolean includeLower, boolean includeUpper) {
        return new NumericRangeFieldDataFilter<Byte>(fieldDataCache, field, lowerVal, upperVal, includeLower, includeUpper) {
            @Override
            public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
                final byte inclusiveLowerPoint, inclusiveUpperPoint;
                if (lowerVal != null) {
                    byte i = lowerVal.byteValue();
                    if (!includeLower && i == Byte.MAX_VALUE)
                        return DocSet.EMPTY_DOC_SET;
                    inclusiveLowerPoint = (byte) (includeLower ? i : (i + 1));
                } else {
                    inclusiveLowerPoint = Byte.MIN_VALUE;
                }
                if (upperVal != null) {
                    byte i = upperVal.byteValue();
                    if (!includeUpper && i == Byte.MIN_VALUE)
                        return DocSet.EMPTY_DOC_SET;
                    inclusiveUpperPoint = (byte) (includeUpper ? i : (i - 1));
                } else {
                    inclusiveUpperPoint = Byte.MAX_VALUE;
                }

                if (inclusiveLowerPoint > inclusiveUpperPoint)
                    return DocSet.EMPTY_DOC_SET;

                final ByteFieldData fieldData = (ByteFieldData) this.fieldDataCache.cache(FieldDataType.DefaultTypes.BYTE, reader, field);
                return new GetDocSet(reader.maxDoc()) {

                    @Override
                    public boolean isCacheable() {
                        
                        
                        
                        return false;
                    }

                    @Override
                    public boolean get(int doc) {
                        if (!fieldData.hasValue(doc)) {
                            return false;
                        }
                        if (fieldData.multiValued()) {
                            byte[] values = fieldData.values(doc);
                            for (byte value : values) {
                                if (value >= inclusiveLowerPoint && value <= inclusiveUpperPoint) {
                                    return true;
                                }
                            }
                            return false;
                        } else {
                            byte value = fieldData.value(doc);
                            return value >= inclusiveLowerPoint && value <= inclusiveUpperPoint;
                        }
                    }
                };
            }
        };
    }


    
    /**
     * New short range.
     *
     * @param fieldDataCache the field data cache
     * @param field the field
     * @param lowerVal the lower val
     * @param upperVal the upper val
     * @param includeLower the include lower
     * @param includeUpper the include upper
     * @return the numeric range field data filter
     */
    public static NumericRangeFieldDataFilter<Short> newShortRange(FieldDataCache fieldDataCache, String field, Short lowerVal, Short upperVal, boolean includeLower, boolean includeUpper) {
        return new NumericRangeFieldDataFilter<Short>(fieldDataCache, field, lowerVal, upperVal, includeLower, includeUpper) {
            @Override
            public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
                final short inclusiveLowerPoint, inclusiveUpperPoint;
                if (lowerVal != null) {
                    short i = lowerVal.shortValue();
                    if (!includeLower && i == Short.MAX_VALUE)
                        return DocSet.EMPTY_DOC_SET;
                    inclusiveLowerPoint = (short) (includeLower ? i : (i + 1));
                } else {
                    inclusiveLowerPoint = Short.MIN_VALUE;
                }
                if (upperVal != null) {
                    short i = upperVal.shortValue();
                    if (!includeUpper && i == Short.MIN_VALUE)
                        return DocSet.EMPTY_DOC_SET;
                    inclusiveUpperPoint = (short) (includeUpper ? i : (i - 1));
                } else {
                    inclusiveUpperPoint = Short.MAX_VALUE;
                }

                if (inclusiveLowerPoint > inclusiveUpperPoint)
                    return DocSet.EMPTY_DOC_SET;

                final ShortFieldData fieldData = (ShortFieldData) this.fieldDataCache.cache(FieldDataType.DefaultTypes.SHORT, reader, field);
                return new GetDocSet(reader.maxDoc()) {

                    @Override
                    public boolean isCacheable() {
                        
                        
                        
                        return false;
                    }

                    @Override
                    public boolean get(int doc) {
                        if (!fieldData.hasValue(doc)) {
                            return false;
                        }
                        if (fieldData.multiValued()) {
                            short[] values = fieldData.values(doc);
                            for (short value : values) {
                                if (value >= inclusiveLowerPoint && value <= inclusiveUpperPoint) {
                                    return true;
                                }
                            }
                            return false;
                        } else {
                            short value = fieldData.value(doc);
                            return value >= inclusiveLowerPoint && value <= inclusiveUpperPoint;
                        }
                    }
                };
            }
        };
    }

    
    /**
     * New int range.
     *
     * @param fieldDataCache the field data cache
     * @param field the field
     * @param lowerVal the lower val
     * @param upperVal the upper val
     * @param includeLower the include lower
     * @param includeUpper the include upper
     * @return the numeric range field data filter
     */
    public static NumericRangeFieldDataFilter<Integer> newIntRange(FieldDataCache fieldDataCache, String field, Integer lowerVal, Integer upperVal, boolean includeLower, boolean includeUpper) {
        return new NumericRangeFieldDataFilter<Integer>(fieldDataCache, field, lowerVal, upperVal, includeLower, includeUpper) {
            @Override
            public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
                final int inclusiveLowerPoint, inclusiveUpperPoint;
                if (lowerVal != null) {
                    int i = lowerVal.intValue();
                    if (!includeLower && i == Integer.MAX_VALUE)
                        return DocSet.EMPTY_DOC_SET;
                    inclusiveLowerPoint = includeLower ? i : (i + 1);
                } else {
                    inclusiveLowerPoint = Integer.MIN_VALUE;
                }
                if (upperVal != null) {
                    int i = upperVal.intValue();
                    if (!includeUpper && i == Integer.MIN_VALUE)
                        return DocSet.EMPTY_DOC_SET;
                    inclusiveUpperPoint = includeUpper ? i : (i - 1);
                } else {
                    inclusiveUpperPoint = Integer.MAX_VALUE;
                }

                if (inclusiveLowerPoint > inclusiveUpperPoint)
                    return DocSet.EMPTY_DOC_SET;

                final IntFieldData fieldData = (IntFieldData) this.fieldDataCache.cache(FieldDataType.DefaultTypes.INT, reader, field);
                return new GetDocSet(reader.maxDoc()) {

                    @Override
                    public boolean isCacheable() {
                        
                        
                        
                        return false;
                    }

                    @Override
                    public boolean get(int doc) {
                        if (!fieldData.hasValue(doc)) {
                            return false;
                        }
                        if (fieldData.multiValued()) {
                            int[] values = fieldData.values(doc);
                            for (int value : values) {
                                if (value >= inclusiveLowerPoint && value <= inclusiveUpperPoint) {
                                    return true;
                                }
                            }
                            return false;
                        } else {
                            int value = fieldData.value(doc);
                            return value >= inclusiveLowerPoint && value <= inclusiveUpperPoint;
                        }
                    }
                };
            }
        };
    }

    
    /**
     * New long range.
     *
     * @param fieldDataCache the field data cache
     * @param field the field
     * @param lowerVal the lower val
     * @param upperVal the upper val
     * @param includeLower the include lower
     * @param includeUpper the include upper
     * @return the numeric range field data filter
     */
    public static NumericRangeFieldDataFilter<Long> newLongRange(FieldDataCache fieldDataCache, String field, Long lowerVal, Long upperVal, boolean includeLower, boolean includeUpper) {
        return new NumericRangeFieldDataFilter<Long>(fieldDataCache, field, lowerVal, upperVal, includeLower, includeUpper) {
            @Override
            public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
                final long inclusiveLowerPoint, inclusiveUpperPoint;
                if (lowerVal != null) {
                    long i = lowerVal.longValue();
                    if (!includeLower && i == Long.MAX_VALUE)
                        return DocSet.EMPTY_DOC_SET;
                    inclusiveLowerPoint = includeLower ? i : (i + 1l);
                } else {
                    inclusiveLowerPoint = Long.MIN_VALUE;
                }
                if (upperVal != null) {
                    long i = upperVal.longValue();
                    if (!includeUpper && i == Long.MIN_VALUE)
                        return DocSet.EMPTY_DOC_SET;
                    inclusiveUpperPoint = includeUpper ? i : (i - 1l);
                } else {
                    inclusiveUpperPoint = Long.MAX_VALUE;
                }

                if (inclusiveLowerPoint > inclusiveUpperPoint)
                    return DocSet.EMPTY_DOC_SET;

                final LongFieldData fieldData = (LongFieldData) this.fieldDataCache.cache(FieldDataType.DefaultTypes.LONG, reader, field);
                return new GetDocSet(reader.maxDoc()) {

                    @Override
                    public boolean isCacheable() {
                        
                        
                        
                        return false;
                    }

                    @Override
                    public boolean get(int doc) {
                        if (!fieldData.hasValue(doc)) {
                            return false;
                        }
                        if (fieldData.multiValued()) {
                            long[] values = fieldData.values(doc);
                            for (long value : values) {
                                if (value >= inclusiveLowerPoint && value <= inclusiveUpperPoint) {
                                    return true;
                                }
                            }
                            return false;
                        } else {
                            long value = fieldData.value(doc);
                            return value >= inclusiveLowerPoint && value <= inclusiveUpperPoint;
                        }
                    }
                };
            }
        };
    }

    
    /**
     * New float range.
     *
     * @param fieldDataCache the field data cache
     * @param field the field
     * @param lowerVal the lower val
     * @param upperVal the upper val
     * @param includeLower the include lower
     * @param includeUpper the include upper
     * @return the numeric range field data filter
     */
    public static NumericRangeFieldDataFilter<Float> newFloatRange(FieldDataCache fieldDataCache, String field, Float lowerVal, Float upperVal, boolean includeLower, boolean includeUpper) {
        return new NumericRangeFieldDataFilter<Float>(fieldDataCache, field, lowerVal, upperVal, includeLower, includeUpper) {
            @Override
            public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
                
                
                final float inclusiveLowerPoint, inclusiveUpperPoint;
                if (lowerVal != null) {
                    float f = lowerVal.floatValue();
                    if (!includeUpper && f > 0.0f && Float.isInfinite(f))
                        return DocSet.EMPTY_DOC_SET;
                    int i = NumericUtils.floatToSortableInt(f);
                    inclusiveLowerPoint = NumericUtils.sortableIntToFloat(includeLower ? i : (i + 1));
                } else {
                    inclusiveLowerPoint = Float.NEGATIVE_INFINITY;
                }
                if (upperVal != null) {
                    float f = upperVal.floatValue();
                    if (!includeUpper && f < 0.0f && Float.isInfinite(f))
                        return DocSet.EMPTY_DOC_SET;
                    int i = NumericUtils.floatToSortableInt(f);
                    inclusiveUpperPoint = NumericUtils.sortableIntToFloat(includeUpper ? i : (i - 1));
                } else {
                    inclusiveUpperPoint = Float.POSITIVE_INFINITY;
                }

                if (inclusiveLowerPoint > inclusiveUpperPoint)
                    return DocSet.EMPTY_DOC_SET;

                final FloatFieldData fieldData = (FloatFieldData) this.fieldDataCache.cache(FieldDataType.DefaultTypes.FLOAT, reader, field);
                return new GetDocSet(reader.maxDoc()) {

                    @Override
                    public boolean isCacheable() {
                        
                        
                        
                        return false;
                    }

                    @Override
                    public boolean get(int doc) {
                        if (!fieldData.hasValue(doc)) {
                            return false;
                        }
                        if (fieldData.multiValued()) {
                            float[] values = fieldData.values(doc);
                            for (float value : values) {
                                if (value >= inclusiveLowerPoint && value <= inclusiveUpperPoint) {
                                    return true;
                                }
                            }
                            return false;
                        } else {
                            float value = fieldData.value(doc);
                            return value >= inclusiveLowerPoint && value <= inclusiveUpperPoint;
                        }
                    }
                };
            }
        };
    }

    
    /**
     * New double range.
     *
     * @param fieldDataCache the field data cache
     * @param field the field
     * @param lowerVal the lower val
     * @param upperVal the upper val
     * @param includeLower the include lower
     * @param includeUpper the include upper
     * @return the numeric range field data filter
     */
    public static NumericRangeFieldDataFilter<Double> newDoubleRange(FieldDataCache fieldDataCache, String field, Double lowerVal, Double upperVal, boolean includeLower, boolean includeUpper) {
        return new NumericRangeFieldDataFilter<Double>(fieldDataCache, field, lowerVal, upperVal, includeLower, includeUpper) {
            @Override
            public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
                
                
                final double inclusiveLowerPoint, inclusiveUpperPoint;
                if (lowerVal != null) {
                    double f = lowerVal.doubleValue();
                    if (!includeUpper && f > 0.0 && Double.isInfinite(f))
                        return DocSet.EMPTY_DOC_SET;
                    long i = NumericUtils.doubleToSortableLong(f);
                    inclusiveLowerPoint = NumericUtils.sortableLongToDouble(includeLower ? i : (i + 1L));
                } else {
                    inclusiveLowerPoint = Double.NEGATIVE_INFINITY;
                }
                if (upperVal != null) {
                    double f = upperVal.doubleValue();
                    if (!includeUpper && f < 0.0 && Double.isInfinite(f))
                        return DocSet.EMPTY_DOC_SET;
                    long i = NumericUtils.doubleToSortableLong(f);
                    inclusiveUpperPoint = NumericUtils.sortableLongToDouble(includeUpper ? i : (i - 1L));
                } else {
                    inclusiveUpperPoint = Double.POSITIVE_INFINITY;
                }

                if (inclusiveLowerPoint > inclusiveUpperPoint)
                    return DocSet.EMPTY_DOC_SET;

                final DoubleFieldData fieldData = (DoubleFieldData) this.fieldDataCache.cache(FieldDataType.DefaultTypes.DOUBLE, reader, field);
                return new GetDocSet(reader.maxDoc()) {

                    @Override
                    public boolean isCacheable() {
                        
                        
                        
                        return false;
                    }

                    @Override
                    public boolean get(int doc) {
                        if (!fieldData.hasValue(doc)) {
                            return false;
                        }
                        if (fieldData.multiValued()) {
                            double[] values = fieldData.values(doc);
                            for (double value : values) {
                                if (value >= inclusiveLowerPoint && value <= inclusiveUpperPoint) {
                                    return true;
                                }
                            }
                            return false;
                        } else {
                            double value = fieldData.value(doc);
                            return value >= inclusiveLowerPoint && value <= inclusiveUpperPoint;
                        }
                    }
                };
            }
        };
    }
}
