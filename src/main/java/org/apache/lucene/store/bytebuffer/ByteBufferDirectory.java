/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ByteBufferDirectory.java 2012-3-29 15:04:16 l.xue.nong$$
 */
package org.apache.lucene.store.bytebuffer;



import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.SingleInstanceLockFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


/**
 * The Class ByteBufferDirectory.
 *
 * @author l.xue.nong
 */
public class ByteBufferDirectory extends Directory {

    /** The files. */
    protected final Map<String, ByteBufferFile> files = new ConcurrentHashMap<String, ByteBufferFile>();

    /** The allocator. */
    private final ByteBufferAllocator allocator;

    /** The internal allocator. */
    private final boolean internalAllocator;

    /** The size in bytes. */
    final AtomicLong sizeInBytes = new AtomicLong();

    
    /**
     * Instantiates a new byte buffer directory.
     */
    public ByteBufferDirectory() {
        this.allocator = new PlainByteBufferAllocator(false, 1024, 1024 * 10);
        this.internalAllocator = true;
        try {
            setLockFactory(new SingleInstanceLockFactory());
        } catch (IOException e) {
            
        }
    }

    
    /**
     * Instantiates a new byte buffer directory.
     *
     * @param allocator the allocator
     */
    public ByteBufferDirectory(ByteBufferAllocator allocator) {
        this.allocator = allocator;
        this.internalAllocator = false;
        try {
            setLockFactory(new SingleInstanceLockFactory());
        } catch (IOException e) {
            
        }
    }

    
    /**
     * Size in bytes.
     *
     * @return the long
     */
    public long sizeInBytes() {
        return sizeInBytes.get();
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.store.Directory#sync(java.util.Collection)
     */
    public void sync(Collection<String> names) throws IOException {
        
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.store.Directory#listAll()
     */
    @Override
    public String[] listAll() throws IOException {
        return files.keySet().toArray(new String[0]);
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.store.Directory#fileExists(java.lang.String)
     */
    @Override
    public boolean fileExists(String name) throws IOException {
        return files.containsKey(name);
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.store.Directory#fileModified(java.lang.String)
     */
    @Override
    public long fileModified(String name) throws IOException {
        ByteBufferFile file = files.get(name);
        if (file == null)
            throw new FileNotFoundException(name);
        return file.getLastModified();
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.store.Directory#touchFile(java.lang.String)
     */
    @Override
    public void touchFile(String name) throws IOException {
        ByteBufferFile file = files.get(name);
        if (file == null)
            throw new FileNotFoundException(name);

        long ts2, ts1 = System.currentTimeMillis();
        do {
            try {
                Thread.sleep(0, 1);
            } catch (java.lang.InterruptedException ie) {
                
                
                Thread.currentThread().interrupt();
                throw new RuntimeException(ie);
            }
            ts2 = System.currentTimeMillis();
        } while (ts1 == ts2);

        file.setLastModified(ts2);
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.store.Directory#deleteFile(java.lang.String)
     */
    @Override
    public void deleteFile(String name) throws IOException {
        ByteBufferFile file = files.remove(name);
        if (file == null)
            throw new FileNotFoundException(name);
        sizeInBytes.addAndGet(-file.sizeInBytes());
        file.delete();
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.store.Directory#fileLength(java.lang.String)
     */
    @Override
    public long fileLength(String name) throws IOException {
        ByteBufferFile file = files.get(name);
        if (file == null)
            throw new FileNotFoundException(name);
        return file.getLength();
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.store.Directory#createOutput(java.lang.String)
     */
    @Override
    public IndexOutput createOutput(String name) throws IOException {
        ByteBufferAllocator.Type allocatorType = ByteBufferAllocator.Type.LARGE;
        if (name.contains("segments") || name.endsWith(".del")) {
            allocatorType = ByteBufferAllocator.Type.SMALL;
        }
        ByteBufferFileOutput file = new ByteBufferFileOutput(this, allocator.sizeInBytes(allocatorType));
        ByteBufferFile existing = files.put(name, file);
        if (existing != null) {
            sizeInBytes.addAndGet(-existing.sizeInBytes());
            existing.delete();
        }
        return new ByteBufferIndexOutput(this, name, allocator, allocatorType, file);
    }

    /**
     * Close output.
     *
     * @param name the name
     * @param file the file
     */
    void closeOutput(String name, ByteBufferFileOutput file) {
        
        files.put(name, new ByteBufferFile(file));
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.store.Directory#openInput(java.lang.String)
     */
    @Override
    public IndexInput openInput(String name) throws IOException {
        ByteBufferFile file = files.get(name);
        if (file == null)
            throw new FileNotFoundException(name);
        return new ByteBufferIndexInput(name, file);
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.store.Directory#close()
     */
    @Override
    public void close() throws IOException {
        String[] files = listAll();
        for (String file : files) {
            deleteFile(file);
        }
        if (internalAllocator) {
            allocator.close();
        }
    }

    /**
     * Release buffer.
     *
     * @param byteBuffer the byte buffer
     */
    void releaseBuffer(ByteBuffer byteBuffer) {
        allocator.release(byteBuffer);
    }
}
