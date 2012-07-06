/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndicesExistsAction.java 2012-3-29 15:01:54 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.exists;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class IndicesExistsAction.
 *
 * @author l.xue.nong
 */
public class IndicesExistsAction extends IndicesAction<IndicesExistsRequest, IndicesExistsResponse, IndicesExistsRequestBuilder> {

    
    /** The Constant INSTANCE. */
    public static final IndicesExistsAction INSTANCE = new IndicesExistsAction();
    
    
    /** The Constant NAME. */
    public static final String NAME = "indices/exists";

    
    /**
     * Instantiates a new indices exists action.
     */
    private IndicesExistsAction() {
        super(NAME);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.GenericAction#newResponse()
     */
    @Override
    public IndicesExistsResponse newResponse() {
        return new IndicesExistsResponse();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.summall.search.core.client.IndicesAdminClient)
     */
    @Override
    public IndicesExistsRequestBuilder newRequestBuilder(IndicesAdminClient client) {
        return new IndicesExistsRequestBuilder(client);
    }
}
