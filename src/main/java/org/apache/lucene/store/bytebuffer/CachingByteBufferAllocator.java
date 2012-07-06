/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CachingByteBufferAllocator.java 2012-3-29 15:04:17 l.xue.nong$$
 */
package org.apache.lucene.store.bytebuffer;



import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;


/**
 * The Class CachingByteBufferAllocator.
 *
 * @author l.xue.nong
 */
public class CachingByteBufferAllocator extends PlainByteBufferAllocator {

    /** The small cache. */
    private final ArrayBlockingQueue<ByteBuffer> smallCache;
    
    /** The large cache. */
    private final ArrayBlockingQueue<ByteBuffer> largeCache;

    
    /**
     * Instantiates a new caching byte buffer allocator.
     *
     * @param direct the direct
     * @param smallBufferSizeInBytes the small buffer size in bytes
     * @param largeBufferSizeInBytes the large buffer size in bytes
     * @param smallCacheSizeInBytes the small cache size in bytes
     * @param largeCacheSizeInBytes the large cache size in bytes
     */
    public CachingByteBufferAllocator(boolean direct, int smallBufferSizeInBytes, int largeBufferSizeInBytes,
                                      int smallCacheSizeInBytes, int largeCacheSizeInBytes) {
        super(direct, smallBufferSizeInBytes, largeBufferSizeInBytes);
        this.smallCache = new ArrayBlockingQueue<ByteBuffer>(smallCacheSizeInBytes / smallBufferSizeInBytes);
        this.largeCache = new ArrayBlockingQueue<ByteBuffer>(largeCacheSizeInBytes / largeBufferSizeInBytes);
    }


    /* (non-Javadoc)
     * @see org.apache.lucene.store.bytebuffer.PlainByteBufferAllocator#allocate(org.apache.lucene.store.bytebuffer.ByteBufferAllocator.Type)
     */
    public ByteBuffer allocate(Type type) throws IOException {
        ByteBuffer buffer = type == Type.SMALL ? smallCache.poll() : largeCache.poll();
        if (buffer == null) {
            buffer = super.allocate(type);
        }
        return buffer;
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.store.bytebuffer.PlainByteBufferAllocator#release(java.nio.ByteBuffer)
     */
    public void release(ByteBuffer buffer) {
        if (buffer.capacity() == smallBufferSizeInBytes) {
            boolean success = smallCache.offer(buffer);
            if (!success) {
                super.release(buffer);
            }
        } else if (buffer.capacity() == largeBufferSizeInBytes) {
            boolean success = largeCache.offer(buffer);
            if (!success) {
                super.release(buffer);
            }
        }
        
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.store.bytebuffer.PlainByteBufferAllocator#close()
     */
    public void close() {
        for (ByteBuffer buffer : smallCache) {
            super.release(buffer);
        }
        smallCache.clear();
        for (ByteBuffer buffer : largeCache) {
            super.release(buffer);
        }
        largeCache.clear();
    }
}
