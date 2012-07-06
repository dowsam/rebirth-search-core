/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportStats.java 2012-3-29 15:01:18 l.xue.nong$$
 */


package cn.com.rebirth.search.core.transport;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;


/**
 * The Class TransportStats.
 *
 * @author l.xue.nong
 */
public class TransportStats implements Streamable, ToXContent {

    
    /** The server open. */
    private long serverOpen;
    
    
    /** The rx count. */
    private long rxCount;
    
    
    /** The rx size. */
    private long rxSize;
    
    
    /** The tx count. */
    private long txCount;
    
    
    /** The tx size. */
    private long txSize;

    
    /**
     * Instantiates a new transport stats.
     */
    TransportStats() {

    }

    
    /**
     * Instantiates a new transport stats.
     *
     * @param serverOpen the server open
     * @param rxCount the rx count
     * @param rxSize the rx size
     * @param txCount the tx count
     * @param txSize the tx size
     */
    public TransportStats(long serverOpen, long rxCount, long rxSize, long txCount, long txSize) {
        this.serverOpen = serverOpen;
        this.rxCount = rxCount;
        this.rxSize = rxSize;
        this.txCount = txCount;
        this.txSize = txSize;
    }

    
    /**
     * Server open.
     *
     * @return the long
     */
    public long serverOpen() {
        return this.serverOpen;
    }

    
    /**
     * Gets the server open.
     *
     * @return the server open
     */
    public long getServerOpen() {
        return serverOpen();
    }

    
    /**
     * Rx count.
     *
     * @return the long
     */
    public long rxCount() {
        return rxCount;
    }

    
    /**
     * Gets the rx count.
     *
     * @return the rx count
     */
    public long getRxCount() {
        return rxCount();
    }

    
    /**
     * Rx size.
     *
     * @return the byte size value
     */
    public ByteSizeValue rxSize() {
        return new ByteSizeValue(rxSize);
    }

    
    /**
     * Gets the rx size.
     *
     * @return the rx size
     */
    public ByteSizeValue getRxSize() {
        return rxSize();
    }

    
    /**
     * Tx count.
     *
     * @return the long
     */
    public long txCount() {
        return txCount;
    }

    
    /**
     * Gets the tx count.
     *
     * @return the tx count
     */
    public long getTxCount() {
        return txCount();
    }

    
    /**
     * Tx size.
     *
     * @return the byte size value
     */
    public ByteSizeValue txSize() {
        return new ByteSizeValue(txSize);
    }

    
    /**
     * Gets the tx size.
     *
     * @return the tx size
     */
    public ByteSizeValue getTxSize() {
        return txSize();
    }

    
    /**
     * Read transport stats.
     *
     * @param in the in
     * @return the transport stats
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static TransportStats readTransportStats(StreamInput in) throws IOException {
        TransportStats stats = new TransportStats();
        stats.readFrom(in);
        return stats;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        serverOpen = in.readVLong();
        rxCount = in.readVLong();
        rxSize = in.readVLong();
        txCount = in.readVLong();
        txSize = in.readVLong();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeVLong(serverOpen);
        out.writeVLong(rxCount);
        out.writeVLong(rxSize);
        out.writeVLong(txCount);
        out.writeVLong(txSize);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
     */
    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(Fields.TRANSPORT);
        builder.field(Fields.SERVER_OPEN, serverOpen);
        builder.field(Fields.RX_COUNT, rxCount);
        builder.field(Fields.RX_SIZE, rxSize().toString());
        builder.field(Fields.RX_SIZE_IN_BYTES, rxSize);
        builder.field(Fields.TX_COUNT, txCount);
        builder.field(Fields.TX_SIZE, txSize().toString());
        builder.field(Fields.TX_SIZE_IN_BYTES, txSize);
        builder.endObject();
        return builder;
    }

    
    /**
     * The Class Fields.
     *
     * @author l.xue.nong
     */
    static final class Fields {
        
        
        /** The Constant TRANSPORT. */
        static final XContentBuilderString TRANSPORT = new XContentBuilderString("transport");
        
        
        /** The Constant SERVER_OPEN. */
        static final XContentBuilderString SERVER_OPEN = new XContentBuilderString("server_open");
        
        
        /** The Constant RX_COUNT. */
        static final XContentBuilderString RX_COUNT = new XContentBuilderString("rx_count");
        
        
        /** The Constant RX_SIZE. */
        static final XContentBuilderString RX_SIZE = new XContentBuilderString("rx_size");
        
        
        /** The Constant RX_SIZE_IN_BYTES. */
        static final XContentBuilderString RX_SIZE_IN_BYTES = new XContentBuilderString("rx_size_in_bytes");
        
        
        /** The Constant TX_COUNT. */
        static final XContentBuilderString TX_COUNT = new XContentBuilderString("tx_count");
        
        
        /** The Constant TX_SIZE. */
        static final XContentBuilderString TX_SIZE = new XContentBuilderString("tx_size");
        
        
        /** The Constant TX_SIZE_IN_BYTES. */
        static final XContentBuilderString TX_SIZE_IN_BYTES = new XContentBuilderString("tx_size_in_bytes");
    }
}