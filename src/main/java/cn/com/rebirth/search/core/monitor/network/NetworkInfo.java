/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NetworkInfo.java 2012-3-29 15:01:17 l.xue.nong$$
 */


package cn.com.rebirth.search.core.monitor.network;

import java.io.IOException;
import java.io.Serializable;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;


/**
 * The Class NetworkInfo.
 *
 * @author l.xue.nong
 */
public class NetworkInfo implements Streamable, Serializable, ToXContent {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 9137207806709002671L;

	
    /** The Constant NA_INTERFACE. */
    public static final Interface NA_INTERFACE = new Interface();

    
    /** The refresh interval. */
    long refreshInterval;

    
    /** The primary. */
    Interface primary = NA_INTERFACE;

    
    /**
     * Refresh interval.
     *
     * @return the long
     */
    public long refreshInterval() {
        return this.refreshInterval;
    }

    
    /**
     * Gets the refresh interval.
     *
     * @return the refresh interval
     */
    public long getRefreshInterval() {
        return this.refreshInterval;
    }

    
    /**
     * Primary interface.
     *
     * @return the interface
     */
    public Interface primaryInterface() {
        return primary;
    }

    
    /**
     * Gets the primary interface.
     *
     * @return the primary interface
     */
    public Interface getPrimaryInterface() {
        return primaryInterface();
    }

    
    /**
     * The Class Fields.
     *
     * @author l.xue.nong
     */
    static final class Fields {
        
        
        /** The Constant NETWORK. */
        static final XContentBuilderString NETWORK = new XContentBuilderString("network");
        
        
        /** The Constant REFRESH_INTERVAL. */
        static final XContentBuilderString REFRESH_INTERVAL = new XContentBuilderString("refresh_interval");
        
        
        /** The Constant PRIMARY_INTERFACE. */
        static final XContentBuilderString PRIMARY_INTERFACE = new XContentBuilderString("primary_interface");
        
        
        /** The Constant ADDRESS. */
        static final XContentBuilderString ADDRESS = new XContentBuilderString("address");
        
        
        /** The Constant NAME. */
        static final XContentBuilderString NAME = new XContentBuilderString("name");
        
        
        /** The Constant MAC_ADDRESS. */
        static final XContentBuilderString MAC_ADDRESS = new XContentBuilderString("mac_address");
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
     */
    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(Fields.NETWORK);
        builder.field(Fields.REFRESH_INTERVAL, refreshInterval);
        if (primary != NA_INTERFACE) {
            builder.startObject(Fields.PRIMARY_INTERFACE);
            builder.field(Fields.ADDRESS, primary.address());
            builder.field(Fields.NAME, primary.name());
            builder.field(Fields.MAC_ADDRESS, primary.macAddress());
            builder.endObject();
        }
        builder.endObject();
        return builder;
    }

    
    /**
     * Read network info.
     *
     * @param in the in
     * @return the network info
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static NetworkInfo readNetworkInfo(StreamInput in) throws IOException {
        NetworkInfo info = new NetworkInfo();
        info.readFrom(in);
        return info;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        refreshInterval = in.readLong();
        primary = Interface.readNetworkInterface(in);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeLong(refreshInterval);
        primary.writeTo(out);
    }

    
    /**
     * The Class Interface.
     *
     * @author l.xue.nong
     */
    public static class Interface implements Streamable, Serializable {

        
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 5376694971717307976L;

		
        /** The name. */
        private String name = "";
        
        
        /** The address. */
        private String address = "";
        
        
        /** The mac address. */
        private String macAddress = "";

        
        /**
         * Instantiates a new interface.
         */
        private Interface() {
        }

        
        /**
         * Instantiates a new interface.
         *
         * @param name the name
         * @param address the address
         * @param macAddress the mac address
         */
        public Interface(String name, String address, String macAddress) {
            this.name = name;
            this.address = address;
            this.macAddress = macAddress;
        }

        
        /**
         * Name.
         *
         * @return the string
         */
        public String name() {
            return name;
        }

        
        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return name();
        }

        
        /**
         * Address.
         *
         * @return the string
         */
        public String address() {
            return address;
        }

        
        /**
         * Gets the address.
         *
         * @return the address
         */
        public String getAddress() {
            return address();
        }

        
        /**
         * Mac address.
         *
         * @return the string
         */
        public String macAddress() {
            return macAddress;
        }

        
        /**
         * Gets the mac address.
         *
         * @return the mac address
         */
        public String getMacAddress() {
            return macAddress();
        }

        
        /**
         * Read network interface.
         *
         * @param in the in
         * @return the interface
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Interface readNetworkInterface(StreamInput in) throws IOException {
            Interface inf = new Interface();
            inf.readFrom(in);
            return inf;
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
         */
        @Override
        public void readFrom(StreamInput in) throws IOException {
            name = in.readUTF();
            address = in.readUTF();
            macAddress = in.readUTF();
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
         */
        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeUTF(name);
            out.writeUTF(address);
            out.writeUTF(macAddress);
        }

    }
}
