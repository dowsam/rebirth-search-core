/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TransportServiceManagement.java 2012-3-29 15:00:56 l.xue.nong$$
 */


package cn.com.rebirth.search.core.transport;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.jmx.MBean;
import cn.com.rebirth.search.core.jmx.ManagedAttribute;


/**
 * The Class TransportServiceManagement.
 *
 * @author l.xue.nong
 */
@MBean(objectName = "service=transport", description = "Transport")
public class TransportServiceManagement {

    
    /** The transport service. */
    private final TransportService transportService;

    
    /**
     * Instantiates a new transport service management.
     *
     * @param transportService the transport service
     */
    @Inject
    public TransportServiceManagement(TransportService transportService) {
        this.transportService = transportService;
    }

    
    /**
     * Gets the publish address.
     *
     * @return the publish address
     */
    @ManagedAttribute(description = "Transport address published to other nodes")
    public String getPublishAddress() {
        return transportService.boundAddress().publishAddress().toString();
    }

    
    /**
     * Gets the bound address.
     *
     * @return the bound address
     */
    @ManagedAttribute(description = "Transport address bounded on")
    public String getBoundAddress() {
        return transportService.boundAddress().boundAddress().toString();
    }

    
    /**
     * Gets the total number of requests.
     *
     * @return the total number of requests
     */
    @ManagedAttribute(description = "Total number of transport requests sent")
    public long getTotalNumberOfRequests() {
        return transportService.requestIds.get();
    }
}
