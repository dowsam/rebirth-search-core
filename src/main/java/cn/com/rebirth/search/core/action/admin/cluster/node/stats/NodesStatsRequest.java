/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodesStatsRequest.java 2012-3-29 15:02:06 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.node.stats;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest;


/**
 * The Class NodesStatsRequest.
 *
 * @author l.xue.nong
 */
public class NodesStatsRequest extends NodesOperationRequest {

    
    /** The indices. */
    private boolean indices = true;
    
    
    /** The os. */
    private boolean os;
    
    
    /** The process. */
    private boolean process;
    
    
    /** The jvm. */
    private boolean jvm;
    
    
    /** The thread pool. */
    private boolean threadPool;
    
    
    /** The network. */
    private boolean network;
    
    
    /** The fs. */
    private boolean fs;
    
    
    /** The transport. */
    private boolean transport;
    
    
    /** The http. */
    private boolean http;

    
    /**
     * Instantiates a new nodes stats request.
     */
    protected NodesStatsRequest() {
    }

    
    /**
     * Instantiates a new nodes stats request.
     *
     * @param nodesIds the nodes ids
     */
    public NodesStatsRequest(String... nodesIds) {
        super(nodesIds);
    }

    
    /**
     * All.
     *
     * @return the nodes stats request
     */
    public NodesStatsRequest all() {
        this.indices = true;
        this.os = true;
        this.process = true;
        this.jvm = true;
        this.threadPool = true;
        this.network = true;
        this.fs = true;
        this.transport = true;
        this.http = true;
        return this;
    }

    
    /**
     * Clear.
     *
     * @return the nodes stats request
     */
    public NodesStatsRequest clear() {
        this.indices = false;
        this.os = false;
        this.process = false;
        this.jvm = false;
        this.threadPool = false;
        this.network = false;
        this.fs = false;
        this.transport = false;
        this.http = false;
        return this;
    }

    
    /**
     * Indices.
     *
     * @return true, if successful
     */
    public boolean indices() {
        return this.indices;
    }

    
    /**
     * Indices.
     *
     * @param indices the indices
     * @return the nodes stats request
     */
    public NodesStatsRequest indices(boolean indices) {
        this.indices = indices;
        return this;
    }

    
    /**
     * Os.
     *
     * @return true, if successful
     */
    public boolean os() {
        return this.os;
    }

    
    /**
     * Os.
     *
     * @param os the os
     * @return the nodes stats request
     */
    public NodesStatsRequest os(boolean os) {
        this.os = os;
        return this;
    }

    
    /**
     * Process.
     *
     * @return true, if successful
     */
    public boolean process() {
        return this.process;
    }

    
    /**
     * Process.
     *
     * @param process the process
     * @return the nodes stats request
     */
    public NodesStatsRequest process(boolean process) {
        this.process = process;
        return this;
    }

    
    /**
     * Jvm.
     *
     * @return true, if successful
     */
    public boolean jvm() {
        return this.jvm;
    }

    
    /**
     * Jvm.
     *
     * @param jvm the jvm
     * @return the nodes stats request
     */
    public NodesStatsRequest jvm(boolean jvm) {
        this.jvm = jvm;
        return this;
    }

    
    /**
     * Thread pool.
     *
     * @return true, if successful
     */
    public boolean threadPool() {
        return this.threadPool;
    }

    
    /**
     * Thread pool.
     *
     * @param threadPool the thread pool
     * @return the nodes stats request
     */
    public NodesStatsRequest threadPool(boolean threadPool) {
        this.threadPool = threadPool;
        return this;
    }

    
    /**
     * Network.
     *
     * @return true, if successful
     */
    public boolean network() {
        return this.network;
    }

    
    /**
     * Network.
     *
     * @param network the network
     * @return the nodes stats request
     */
    public NodesStatsRequest network(boolean network) {
        this.network = network;
        return this;
    }

    
    /**
     * Fs.
     *
     * @return true, if successful
     */
    public boolean fs() {
        return this.fs;
    }

    
    /**
     * Fs.
     *
     * @param fs the fs
     * @return the nodes stats request
     */
    public NodesStatsRequest fs(boolean fs) {
        this.fs = fs;
        return this;
    }

    
    /**
     * Transport.
     *
     * @return true, if successful
     */
    public boolean transport() {
        return this.transport;
    }

    
    /**
     * Transport.
     *
     * @param transport the transport
     * @return the nodes stats request
     */
    public NodesStatsRequest transport(boolean transport) {
        this.transport = transport;
        return this;
    }

    
    /**
     * Http.
     *
     * @return true, if successful
     */
    public boolean http() {
        return this.http;
    }

    
    /**
     * Http.
     *
     * @param http the http
     * @return the nodes stats request
     */
    public NodesStatsRequest http(boolean http) {
        this.http = http;
        return this;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.nodes.NodesOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        indices = in.readBoolean();
        os = in.readBoolean();
        process = in.readBoolean();
        jvm = in.readBoolean();
        threadPool = in.readBoolean();
        network = in.readBoolean();
        fs = in.readBoolean();
        transport = in.readBoolean();
        http = in.readBoolean();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.nodes.NodesOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeBoolean(indices);
        out.writeBoolean(os);
        out.writeBoolean(process);
        out.writeBoolean(jvm);
        out.writeBoolean(threadPool);
        out.writeBoolean(network);
        out.writeBoolean(fs);
        out.writeBoolean(transport);
        out.writeBoolean(http);
    }

}