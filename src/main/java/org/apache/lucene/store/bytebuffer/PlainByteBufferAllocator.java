/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PlainByteBufferAllocator.java 2012-7-6 14:29:48 l.xue.nong$$
 */

package org.apache.lucene.store.bytebuffer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The Class PlainByteBufferAllocator.
 *
 * @author l.xue.nong
 */
public class PlainByteBufferAllocator implements ByteBufferAllocator {

	/** The direct. */
	protected final boolean direct;

	/** The small buffer size in bytes. */
	protected final int smallBufferSizeInBytes;

	/** The large buffer size in bytes. */
	protected final int largeBufferSizeInBytes;

	/**
	 * Instantiates a new plain byte buffer allocator.
	 *
	 * @param direct the direct
	 * @param smallBufferSizeInBytes the small buffer size in bytes
	 * @param largeBufferSizeInBytes the large buffer size in bytes
	 */
	public PlainByteBufferAllocator(boolean direct, int smallBufferSizeInBytes, int largeBufferSizeInBytes) {
		this.direct = direct;
		this.smallBufferSizeInBytes = smallBufferSizeInBytes;
		this.largeBufferSizeInBytes = largeBufferSizeInBytes;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.bytebuffer.ByteBufferAllocator#sizeInBytes(org.apache.lucene.store.bytebuffer.ByteBufferAllocator.Type)
	 */
	public int sizeInBytes(Type type) {
		return type == Type.SMALL ? smallBufferSizeInBytes : largeBufferSizeInBytes;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.bytebuffer.ByteBufferAllocator#allocate(org.apache.lucene.store.bytebuffer.ByteBufferAllocator.Type)
	 */
	public ByteBuffer allocate(Type type) throws IOException {
		int sizeToAllocate = type == Type.SMALL ? smallBufferSizeInBytes : largeBufferSizeInBytes;
		if (direct) {
			return ByteBuffer.allocateDirect(sizeToAllocate);
		}
		return ByteBuffer.allocate(sizeToAllocate);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.bytebuffer.ByteBufferAllocator#release(java.nio.ByteBuffer)
	 */
	public void release(ByteBuffer buffer) {
		Cleaner.clean(buffer);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.bytebuffer.ByteBufferAllocator#close()
	 */
	public void close() {

	}
}
