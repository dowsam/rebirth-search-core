/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ByteBufferFile.java 2012-7-6 14:29:50 l.xue.nong$$
 */

package org.apache.lucene.store.bytebuffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Class ByteBufferFile.
 *
 * @author l.xue.nong
 */
public class ByteBufferFile {

	/** The dir. */
	final ByteBufferDirectory dir;

	/** The buffer size. */
	final int bufferSize;

	/** The buffers. */
	final List<ByteBuffer> buffers;

	/** The length. */
	long length;

	/** The last modified. */
	volatile long lastModified = System.currentTimeMillis();

	/** The ref count. */
	final AtomicInteger refCount;

	/** The size in bytes. */
	long sizeInBytes;

	/**
	 * Instantiates a new byte buffer file.
	 *
	 * @param dir the dir
	 * @param bufferSize the buffer size
	 */
	public ByteBufferFile(ByteBufferDirectory dir, int bufferSize) {
		this.dir = dir;
		this.bufferSize = bufferSize;
		this.buffers = new ArrayList<ByteBuffer>();
		this.refCount = new AtomicInteger(1);
	}

	/**
	 * Instantiates a new byte buffer file.
	 *
	 * @param file the file
	 */
	ByteBufferFile(ByteBufferFile file) {
		this.dir = file.dir;
		this.bufferSize = file.bufferSize;
		this.buffers = file.buffers;
		this.length = file.length;
		this.lastModified = file.lastModified;
		this.refCount = file.refCount;
		this.sizeInBytes = file.sizeInBytes;
	}

	/**
	 * Gets the length.
	 *
	 * @return the length
	 */
	public long getLength() {
		return length;
	}

	/**
	 * Gets the last modified.
	 *
	 * @return the last modified
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * Sets the last modified.
	 *
	 * @param lastModified the new last modified
	 */
	void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * Size in bytes.
	 *
	 * @return the long
	 */
	long sizeInBytes() {
		return sizeInBytes;
	}

	/**
	 * Gets the buffer.
	 *
	 * @param index the index
	 * @return the buffer
	 */
	ByteBuffer getBuffer(int index) {
		return buffers.get(index);
	}

	/**
	 * Num buffers.
	 *
	 * @return the int
	 */
	int numBuffers() {
		return buffers.size();
	}

	/**
	 * Delete.
	 */
	void delete() {
		decRef();
	}

	/**
	 * Inc ref.
	 */
	void incRef() {
		refCount.incrementAndGet();
	}

	/**
	 * Dec ref.
	 */
	void decRef() {
		if (refCount.decrementAndGet() == 0) {
			length = 0;
			for (ByteBuffer buffer : buffers) {
				dir.releaseBuffer(buffer);
			}
			buffers.clear();
			sizeInBytes = 0;
		}
	}
}
