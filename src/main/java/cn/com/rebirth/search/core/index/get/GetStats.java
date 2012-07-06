/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GetStats.java 2012-3-29 15:02:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.get;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;


/**
 * The Class GetStats.
 *
 * @author l.xue.nong
 */
public class GetStats implements Streamable, ToXContent {

    
    /** The exists count. */
    private long existsCount;
    
    
    /** The exists time in millis. */
    private long existsTimeInMillis;
    
    
    /** The missing count. */
    private long missingCount;
    
    
    /** The missing time in millis. */
    private long missingTimeInMillis;
    
    
    /** The current. */
    private long current;

    
    /**
     * Instantiates a new gets the stats.
     */
    public GetStats() {
    }

    
    /**
     * Instantiates a new gets the stats.
     *
     * @param existsCount the exists count
     * @param existsTimeInMillis the exists time in millis
     * @param missingCount the missing count
     * @param missingTimeInMillis the missing time in millis
     * @param current the current
     */
    public GetStats(long existsCount, long existsTimeInMillis, long missingCount, long missingTimeInMillis, long current) {
        this.existsCount = existsCount;
        this.existsTimeInMillis = existsTimeInMillis;
        this.missingCount = missingCount;
        this.missingTimeInMillis = missingTimeInMillis;
        this.current = current;
    }

    
    /**
     * Adds the.
     *
     * @param stats the stats
     */
    public void add(GetStats stats) {
        if (stats == null) {
            return;
        }
        existsCount += stats.existsCount;
        existsTimeInMillis += stats.existsTimeInMillis;
        missingCount += stats.missingCount;
        missingTimeInMillis += stats.missingTimeInMillis;
        current += stats.current;
    }

    
    /**
     * Count.
     *
     * @return the long
     */
    public long count() {
        return existsCount + missingCount;
    }

    
    /**
     * Gets the count.
     *
     * @return the count
     */
    public long getCount() {
        return count();
    }

    
    /**
     * Time in millis.
     *
     * @return the long
     */
    public long timeInMillis() {
        return existsTimeInMillis + missingTimeInMillis;
    }

    
    /**
     * Gets the time in millis.
     *
     * @return the time in millis
     */
    public long getTimeInMillis() {
        return timeInMillis();
    }

    
    /**
     * Time.
     *
     * @return the time value
     */
    public TimeValue time() {
        return new TimeValue(timeInMillis());
    }

    
    /**
     * Gets the time.
     *
     * @return the time
     */
    public TimeValue getTime() {
        return time();
    }

    
    /**
     * Exists count.
     *
     * @return the long
     */
    public long existsCount() {
        return this.existsCount;
    }

    
    /**
     * Gets the exists count.
     *
     * @return the exists count
     */
    public long getExistsCount() {
        return this.existsCount;
    }

    
    /**
     * Exists time in millis.
     *
     * @return the long
     */
    public long existsTimeInMillis() {
        return this.existsTimeInMillis;
    }

    
    /**
     * Gets the exists time in millis.
     *
     * @return the exists time in millis
     */
    public long getExistsTimeInMillis() {
        return this.existsTimeInMillis;
    }

    
    /**
     * Exists time.
     *
     * @return the time value
     */
    public TimeValue existsTime() {
        return new TimeValue(existsTimeInMillis);
    }

    
    /**
     * Gets the exists time.
     *
     * @return the exists time
     */
    public TimeValue getExistsTime() {
        return existsTime();
    }

    
    /**
     * Missing count.
     *
     * @return the long
     */
    public long missingCount() {
        return this.missingCount;
    }

    
    /**
     * Gets the missing count.
     *
     * @return the missing count
     */
    public long getMissingCount() {
        return this.missingCount;
    }

    
    /**
     * Missing time in millis.
     *
     * @return the long
     */
    public long missingTimeInMillis() {
        return this.missingTimeInMillis;
    }

    
    /**
     * Gets the missing time in millis.
     *
     * @return the missing time in millis
     */
    public long getMissingTimeInMillis() {
        return this.missingTimeInMillis;
    }

    
    /**
     * Missing time.
     *
     * @return the time value
     */
    public TimeValue missingTime() {
        return new TimeValue(missingTimeInMillis);
    }

    
    /**
     * Gets the missing time.
     *
     * @return the missing time
     */
    public TimeValue getMissingTime() {
        return missingTime();
    }

    
    /**
     * Current.
     *
     * @return the long
     */
    public long current() {
        return this.current;
    }

    
    /**
     * Gets the current.
     *
     * @return the current
     */
    public long getCurrent() {
        return this.current;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
     */
    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(Fields.GET);
        builder.field(Fields.TOTAL, count());
        builder.field(Fields.TIME, time().toString());
        builder.field(Fields.TIME_IN_MILLIS, timeInMillis());
        builder.field(Fields.EXISTS_TOTAL, existsCount);
        builder.field(Fields.EXISTS_TIME, existsTime().toString());
        builder.field(Fields.EXISTS_TIME_IN_MILLIS, existsTimeInMillis);
        builder.field(Fields.MISSING_TOTAL, missingCount);
        builder.field(Fields.MISSING_TIME, missingTime().toString());
        builder.field(Fields.MISSING_TIME_IN_MILLIS, missingTimeInMillis);
        builder.field(Fields.CURRENT, current);
        builder.endObject();
        return builder;
    }

    
    /**
     * The Class Fields.
     *
     * @author l.xue.nong
     */
    static final class Fields {
        
        
        /** The Constant GET. */
        static final XContentBuilderString GET = new XContentBuilderString("get");
        
        
        /** The Constant TOTAL. */
        static final XContentBuilderString TOTAL = new XContentBuilderString("total");
        
        
        /** The Constant TIME. */
        static final XContentBuilderString TIME = new XContentBuilderString("time");
        
        
        /** The Constant TIME_IN_MILLIS. */
        static final XContentBuilderString TIME_IN_MILLIS = new XContentBuilderString("time_in_millis");
        
        
        /** The Constant EXISTS_TOTAL. */
        static final XContentBuilderString EXISTS_TOTAL = new XContentBuilderString("exists_total");
        
        
        /** The Constant EXISTS_TIME. */
        static final XContentBuilderString EXISTS_TIME = new XContentBuilderString("exists_time");
        
        
        /** The Constant EXISTS_TIME_IN_MILLIS. */
        static final XContentBuilderString EXISTS_TIME_IN_MILLIS = new XContentBuilderString("exists_time_in_millis");
        
        
        /** The Constant MISSING_TOTAL. */
        static final XContentBuilderString MISSING_TOTAL = new XContentBuilderString("missing_total");
        
        
        /** The Constant MISSING_TIME. */
        static final XContentBuilderString MISSING_TIME = new XContentBuilderString("missing_time");
        
        
        /** The Constant MISSING_TIME_IN_MILLIS. */
        static final XContentBuilderString MISSING_TIME_IN_MILLIS = new XContentBuilderString("missing_time_in_millis");
        
        
        /** The Constant CURRENT. */
        static final XContentBuilderString CURRENT = new XContentBuilderString("current");
    }

    
    /**
     * Read get stats.
     *
     * @param in the in
     * @return the gets the stats
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static GetStats readGetStats(StreamInput in) throws IOException {
        GetStats stats = new GetStats();
        stats.readFrom(in);
        return stats;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        existsCount = in.readVLong();
        existsTimeInMillis = in.readVLong();
        missingCount = in.readVLong();
        missingTimeInMillis = in.readVLong();
        current = in.readVLong();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeVLong(existsCount);
        out.writeVLong(existsTimeInMillis);
        out.writeVLong(missingCount);
        out.writeVLong(missingTimeInMillis);
        out.writeVLong(current);
    }
}
