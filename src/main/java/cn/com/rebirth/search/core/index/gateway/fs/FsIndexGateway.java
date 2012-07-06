/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FsIndexGateway.java 2012-3-29 15:00:44 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.gateway.fs;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.gateway.Gateway;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.gateway.IndexShardGateway;
import cn.com.rebirth.search.core.index.gateway.blobstore.BlobStoreIndexGateway;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * The Class FsIndexGateway.
 *
 * @author l.xue.nong
 */
public class FsIndexGateway extends BlobStoreIndexGateway {

    
    /**
     * Instantiates a new fs index gateway.
     *
     * @param index the index
     * @param indexSettings the index settings
     * @param gateway the gateway
     */
    @Inject
    public FsIndexGateway(Index index, @IndexSettings Settings indexSettings, Gateway gateway) {
        super(index, indexSettings, gateway);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.gateway.IndexGateway#type()
     */
    @Override
    public String type() {
        return "fs";
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.gateway.IndexGateway#shardGatewayClass()
     */
    @Override
    public Class<? extends IndexShardGateway> shardGatewayClass() {
        return FsIndexShardGateway.class;
    }
}
