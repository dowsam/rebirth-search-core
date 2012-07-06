/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FieldDataCache.java 2012-3-29 15:01:22 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.cache.field.data;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.search.commons.component.CloseableComponent;
import cn.com.rebirth.search.core.index.IndexComponent;
import cn.com.rebirth.search.core.index.field.data.FieldData;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;


/**
 * The Interface FieldDataCache.
 *
 * @author l.xue.nong
 */
public interface FieldDataCache extends IndexComponent, CloseableComponent {

    
    /**
     * Cache.
     *
     * @param type the type
     * @param reader the reader
     * @param fieldName the field name
     * @return the field data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    FieldData cache(FieldDataType type, IndexReader reader, String fieldName) throws IOException;

    
    /**
     * Type.
     *
     * @return the string
     */
    String type();

    
    /**
     * Clear.
     *
     * @param fieldName the field name
     */
    void clear(String fieldName);

    
    /**
     * Clear.
     */
    void clear();

    
    /**
     * Clear.
     *
     * @param reader the reader
     */
    void clear(IndexReader reader);

    
    /**
     * Evictions.
     *
     * @return the long
     */
    long evictions();

    
    /**
     * Size in bytes.
     *
     * @return the long
     */
    long sizeInBytes();

    
    /**
     * Size in bytes.
     *
     * @param fieldName the field name
     * @return the long
     */
    long sizeInBytes(String fieldName);
}
