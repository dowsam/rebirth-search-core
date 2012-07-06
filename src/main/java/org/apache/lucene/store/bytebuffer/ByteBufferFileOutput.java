/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ByteBufferFileOutput.java 2012-3-29 15:04:16 l.xue.nong$$
 */
package org.apache.lucene.store.bytebuffer;




import java.nio.ByteBuffer;


/**
 * The Class ByteBufferFileOutput.
 *
 * @author l.xue.nong
 */
public class ByteBufferFileOutput extends ByteBufferFile {

    /**
     * Instantiates a new byte buffer file output.
     *
     * @param dir the dir
     * @param bufferSize the buffer size
     */
    public ByteBufferFileOutput(ByteBufferDirectory dir, int bufferSize) {
        super(dir, bufferSize);
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.store.bytebuffer.ByteBufferFile#getLength()
     */
    @Override
    public synchronized long getLength() {
        return super.getLength();
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.store.bytebuffer.ByteBufferFile#getLastModified()
     */
    @Override
    public synchronized long getLastModified() {
        return super.getLastModified();
    }

    /**
     * Sets the length.
     *
     * @param length the new length
     */
    synchronized void setLength(long length) {
        this.length = length;
    }

    /**
     * Adds the buffer.
     *
     * @param buffer the buffer
     */
    synchronized final void addBuffer(ByteBuffer buffer) {
        buffers.add(buffer);
        sizeInBytes += buffer.remaining();
        dir.sizeInBytes.addAndGet(buffer.remaining());
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.store.bytebuffer.ByteBufferFile#getBuffer(int)
     */
    @Override
    synchronized ByteBuffer getBuffer(int index) {
        return super.getBuffer(index);
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.store.bytebuffer.ByteBufferFile#numBuffers()
     */
    @Override
    synchronized int numBuffers() {
        return super.numBuffers();
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.store.bytebuffer.ByteBufferFile#sizeInBytes()
     */
    @Override
    synchronized long sizeInBytes() {
        return super.sizeInBytes();
    }
}
