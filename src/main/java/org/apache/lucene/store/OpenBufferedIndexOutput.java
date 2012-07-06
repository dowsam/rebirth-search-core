/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core OpenBufferedIndexOutput.java 2012-3-29 15:04:16 l.xue.nong$$
 */
package org.apache.lucene.store;

import java.io.IOException;



/**
 * The Class OpenBufferedIndexOutput.
 *
 * @author l.xue.nong
 */
public abstract class OpenBufferedIndexOutput extends IndexOutput {

    /** The Constant DEFAULT_BUFFER_SIZE. */
    public static final int DEFAULT_BUFFER_SIZE = BufferedIndexOutput.BUFFER_SIZE;

    /** The BUFFE r_ size. */
    final int BUFFER_SIZE;

    /** The buffer. */
    private final byte[] buffer;
    
    /** The buffer start. */
    private long bufferStart = 0;           
    
    /** The buffer position. */
    private int bufferPosition = 0;         

    /**
     * Instantiates a new open buffered index output.
     *
     * @param BUFFER_SIZE the bUFFE r_ size
     */
    protected OpenBufferedIndexOutput(int BUFFER_SIZE) {
        this.BUFFER_SIZE = BUFFER_SIZE;
        this.buffer = new byte[BUFFER_SIZE];
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.store.DataOutput#writeByte(byte)
     */
    @Override
    public void writeByte(byte b) throws IOException {
        if (bufferPosition >= BUFFER_SIZE)
            flush();
        buffer[bufferPosition++] = b;
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.store.DataOutput#writeBytes(byte[], int, int)
     */
    @Override
    public void writeBytes(byte[] b, int offset, int length) throws IOException {
        int bytesLeft = BUFFER_SIZE - bufferPosition;
        
        if (bytesLeft >= length) {
            
            System.arraycopy(b, offset, buffer, bufferPosition, length);
            bufferPosition += length;
            
            if (BUFFER_SIZE - bufferPosition == 0)
                flush();
        } else {
            
            if (length > BUFFER_SIZE) {
                
                if (bufferPosition > 0)
                    flush();
                
                flushBuffer(b, offset, length);
                bufferStart += length;
            } else {
                
                int pos = 0; 
                int pieceLength;
                while (pos < length) {
                    pieceLength = (length - pos < bytesLeft) ? length - pos : bytesLeft;
                    System.arraycopy(b, pos + offset, buffer, bufferPosition, pieceLength);
                    pos += pieceLength;
                    bufferPosition += pieceLength;
                    
                    bytesLeft = BUFFER_SIZE - bufferPosition;
                    if (bytesLeft == 0) {
                        flush();
                        bytesLeft = BUFFER_SIZE;
                    }
                }
            }
        }
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.store.IndexOutput#flush()
     */
    @Override
    public void flush() throws IOException {
        flushBuffer(buffer, bufferPosition);
        bufferStart += bufferPosition;
        bufferPosition = 0;
    }

    
    /**
     * Flush buffer.
     *
     * @param b the b
     * @param len the len
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void flushBuffer(byte[] b, int len) throws IOException {
        flushBuffer(b, 0, len);
    }

    
    /**
     * Flush buffer.
     *
     * @param b the b
     * @param offset the offset
     * @param len the len
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected abstract void flushBuffer(byte[] b, int offset, int len) throws IOException;

    
    /* (non-Javadoc)
     * @see org.apache.lucene.store.IndexOutput#close()
     */
    @Override
    public void close() throws IOException {
        flush();
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.store.IndexOutput#getFilePointer()
     */
    @Override
    public long getFilePointer() {
        return bufferStart + bufferPosition;
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.store.IndexOutput#seek(long)
     */
    @Override
    public void seek(long pos) throws IOException {
        flush();
        bufferStart = pos;
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.store.IndexOutput#length()
     */
    @Override
    public abstract long length() throws IOException;


}
