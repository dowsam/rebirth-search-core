/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RefreshStats.java 2012-3-29 15:02:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.refresh;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;


/**
 * The Class RefreshStats.
 *
 * @author l.xue.nong
 */
public class RefreshStats implements Streamable, ToXContent {

    
    /** The total. */
    private long total;

    
    /** The total time in millis. */
    private long totalTimeInMillis;

    
    /**
     * Instantiates a new refresh stats.
     */
    public RefreshStats() {

    }

    
    /**
     * Instantiates a new refresh stats.
     *
     * @param total the total
     * @param totalTimeInMillis the total time in millis
     */
    public RefreshStats(long total, long totalTimeInMillis) {
        this.total = total;
        this.totalTimeInMillis = totalTimeInMillis;
    }

    
    /**
     * Adds the.
     *
     * @param total the total
     * @param totalTimeInMillis the total time in millis
     */
    public void add(long total, long totalTimeInMillis) {
        this.total += total;
        this.totalTimeInMillis += totalTimeInMillis;
    }

    
    /**
     * Adds the.
     *
     * @param refreshStats the refresh stats
     */
    public void add(RefreshStats refreshStats) {
        if (refreshStats == null) {
            return;
        }
        this.total += refreshStats.total;
        this.totalTimeInMillis += refreshStats.totalTimeInMillis;
    }

    
    /**
     * Total.
     *
     * @return the long
     */
    public long total() {
        return this.total;
    }

    
    /**
     * Total time in millis.
     *
     * @return the long
     */
    public long totalTimeInMillis() {
        return this.totalTimeInMillis;
    }

    
    /**
     * Total time.
     *
     * @return the time value
     */
    public TimeValue totalTime() {
        return new TimeValue(totalTimeInMillis);
    }

    
    /**
     * Read refresh stats.
     *
     * @param in the in
     * @return the refresh stats
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static RefreshStats readRefreshStats(StreamInput in) throws IOException {
        RefreshStats refreshStats = new RefreshStats();
        refreshStats.readFrom(in);
        return refreshStats;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
     */
    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(Fields.REFRESH);
        builder.field(Fields.TOTAL, total);
        builder.field(Fields.TOTAL_TIME, totalTime().toString());
        builder.field(Fields.TOTAL_TIME_IN_MILLIS, totalTimeInMillis);
        builder.endObject();
        return builder;
    }

    
    /**
     * The Class Fields.
     *
     * @author l.xue.nong
     */
    static final class Fields {
        
        
        /** The Constant REFRESH. */
        static final XContentBuilderString REFRESH = new XContentBuilderString("refresh");
        
        
        /** The Constant TOTAL. */
        static final XContentBuilderString TOTAL = new XContentBuilderString("total");
        
        
        /** The Constant TOTAL_TIME. */
        static final XContentBuilderString TOTAL_TIME = new XContentBuilderString("total_time");
        
        
        /** The Constant TOTAL_TIME_IN_MILLIS. */
        static final XContentBuilderString TOTAL_TIME_IN_MILLIS = new XContentBuilderString("total_time_in_millis");
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        total = in.readVLong();
        totalTimeInMillis = in.readVLong();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeVLong(total);
        out.writeVLong(totalTimeInMillis);
    }
}