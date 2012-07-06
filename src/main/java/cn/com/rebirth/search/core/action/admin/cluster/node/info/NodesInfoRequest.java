/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NodesInfoRequest.java 2012-3-29 15:01:36 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.node.info;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.nodes.NodesOperationRequest;


/**
 * The Class NodesInfoRequest.
 *
 * @author l.xue.nong
 */
public class NodesInfoRequest extends NodesOperationRequest {

    
    /** The settings. */
    private boolean settings = false;
    
    
    /** The os. */
    private boolean os = false;
    
    
    /** The process. */
    private boolean process = false;
    
    
    /** The jvm. */
    private boolean jvm = false;
    
    
    /** The thread pool. */
    private boolean threadPool = false;
    
    
    /** The network. */
    private boolean network = false;
    
    
    /** The transport. */
    private boolean transport = false;
    
    
    /** The http. */
    private boolean http = false;

    
    /**
     * Instantiates a new nodes info request.
     */
    public NodesInfoRequest() {
    }

    
    /**
     * Instantiates a new nodes info request.
     *
     * @param nodesIds the nodes ids
     */
    public NodesInfoRequest(String... nodesIds) {
        super(nodesIds);
    }

    
    /**
     * Clear.
     *
     * @return the nodes info request
     */
    public NodesInfoRequest clear() {
        settings = false;
        os = false;
        process = false;
        jvm = false;
        threadPool = false;
        network = false;
        transport = false;
        http = false;
        return this;
    }

    
    /**
     * All.
     *
     * @return the nodes info request
     */
    public NodesInfoRequest all() {
        settings = true;
        os = true;
        process = true;
        jvm = true;
        threadPool = true;
        network = true;
        transport = true;
        http = true;
        return this;
    }

    
    /**
     * Settings.
     *
     * @return true, if successful
     */
    public boolean settings() {
        return this.settings;
    }

    
    /**
     * Settings.
     *
     * @param settings the settings
     * @return the nodes info request
     */
    public NodesInfoRequest settings(boolean settings) {
        this.settings = settings;
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
     * @return the nodes info request
     */
    public NodesInfoRequest os(boolean os) {
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
     * @return the nodes info request
     */
    public NodesInfoRequest process(boolean process) {
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
     * @return the nodes info request
     */
    public NodesInfoRequest jvm(boolean jvm) {
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
     * @return the nodes info request
     */
    public NodesInfoRequest threadPool(boolean threadPool) {
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
     * @return the nodes info request
     */
    public NodesInfoRequest network(boolean network) {
        this.network = network;
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
     * @return the nodes info request
     */
    public NodesInfoRequest transport(boolean transport) {
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
     * @return the nodes info request
     */
    public NodesInfoRequest http(boolean http) {
        this.http = http;
        return this;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.nodes.NodesOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        settings = in.readBoolean();
        os = in.readBoolean();
        process = in.readBoolean();
        jvm = in.readBoolean();
        threadPool = in.readBoolean();
        network = in.readBoolean();
        transport = in.readBoolean();
        http = in.readBoolean();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.nodes.NodesOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeBoolean(settings);
        out.writeBoolean(os);
        out.writeBoolean(process);
        out.writeBoolean(jvm);
        out.writeBoolean(threadPool);
        out.writeBoolean(network);
        out.writeBoolean(transport);
        out.writeBoolean(http);
    }
}
