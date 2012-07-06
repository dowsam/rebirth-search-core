/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InternalQueryFacet.java 2012-3-29 15:01:39 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.query;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.InternalFacet;


/**
 * The Class InternalQueryFacet.
 *
 * @author l.xue.nong
 */
public class InternalQueryFacet implements QueryFacet, InternalFacet {

    
    /** The Constant STREAM_TYPE. */
    private static final String STREAM_TYPE = "query";

    
    /**
     * Register streams.
     */
    public static void registerStreams() {
        Streams.registerStream(STREAM, STREAM_TYPE);
    }

    
    /** The STREAM. */
    static Stream STREAM = new Stream() {
        @Override
        public Facet readFacet(String type, StreamInput in) throws IOException {
            return readQueryFacet(in);
        }
    };

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.search.facet.InternalFacet#streamType()
     */
    @Override
    public String streamType() {
        return STREAM_TYPE;
    }

    
    /** The name. */
    private String name;

    
    /** The count. */
    private long count;

    
    /**
     * Instantiates a new internal query facet.
     */
    private InternalQueryFacet() {

    }

    
    /**
     * Instantiates a new internal query facet.
     *
     * @param name the name
     * @param count the count
     */
    public InternalQueryFacet(String name, long count) {
        this.name = name;
        this.count = count;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.search.facet.Facet#type()
     */
    @Override
    public String type() {
        return TYPE;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.search.facet.Facet#getType()
     */
    @Override
    public String getType() {
        return TYPE;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.search.facet.Facet#name()
     */
    public String name() {
        return name;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.search.facet.Facet#getName()
     */
    @Override
    public String getName() {
        return name();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.search.facet.query.QueryFacet#count()
     */
    public long count() {
        return count;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.search.facet.query.QueryFacet#getCount()
     */
    public long getCount() {
        return count;
    }

    
    /**
     * The Class Fields.
     *
     * @author l.xue.nong
     */
    static final class Fields {
        
        
        /** The Constant _TYPE. */
        static final XContentBuilderString _TYPE = new XContentBuilderString("_type");
        
        
        /** The Constant COUNT. */
        static final XContentBuilderString COUNT = new XContentBuilderString("count");
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
     */
    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(name);
        builder.field(Fields._TYPE, QueryFacet.TYPE);
        builder.field(Fields.COUNT, count);
        builder.endObject();
        return builder;
    }

    
    /**
     * Read query facet.
     *
     * @param in the in
     * @return the query facet
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static QueryFacet readQueryFacet(StreamInput in) throws IOException {
        InternalQueryFacet result = new InternalQueryFacet();
        result.readFrom(in);
        return result;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        name = in.readUTF();
        count = in.readVLong();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeUTF(name);
        out.writeVLong(count);
    }
}