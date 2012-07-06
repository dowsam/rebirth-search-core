/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexStore.java 2012-3-29 15:02:12 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.store;

import java.io.IOException;

import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.core.index.IndexComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;


/**
 * The Interface IndexStore.
 *
 * @author l.xue.nong
 */
public interface IndexStore extends IndexComponent {

    
    /**
     * Persistent.
     *
     * @return true, if successful
     */
    boolean persistent();

    
    /**
     * Shard directory.
     *
     * @return the class<? extends directory service>
     */
    Class<? extends DirectoryService> shardDirectory();

    
    /**
     * Backing store total space.
     *
     * @return the byte size value
     */
    ByteSizeValue backingStoreTotalSpace();

    
    /**
     * Backing store free space.
     *
     * @return the byte size value
     */
    ByteSizeValue backingStoreFreeSpace();

    
    /**
     * Can delete unallocated.
     *
     * @param shardId the shard id
     * @return true, if successful
     */
    boolean canDeleteUnallocated(ShardId shardId);

    
    /**
     * Delete unallocated.
     *
     * @param shardId the shard id
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void deleteUnallocated(ShardId shardId) throws IOException;
}
