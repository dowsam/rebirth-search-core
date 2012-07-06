/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ByteBufferIndexOutput.java 2012-7-6 14:29:26 l.xue.nong$$
 */

package org.apache.lucene.store.bytebuffer;

import org.apache.lucene.store.IndexOutput;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The Class ByteBufferIndexOutput.
 *
 * @author l.xue.nong
 */
public class ByteBufferIndexOutput extends IndexOutput {

	/** The Constant EMPTY_BUFFER. */
	private final static ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0).asReadOnlyBuffer();

	/** The dir. */
	private final ByteBufferDirectory dir;

	/** The name. */
	private final String name;

	/** The allocator. */
	private final ByteBufferAllocator allocator;

	/** The allocator type. */
	private final ByteBufferAllocator.Type allocatorType;

	/** The buffer size. */
	private final int BUFFER_SIZE;

	/** The file. */
	private final ByteBufferFileOutput file;

	/** The current buffer. */
	private ByteBuffer currentBuffer;

	/** The current buffer index. */
	private int currentBufferIndex;

	/** The buffer start. */
	private long bufferStart;

	/**
	 * Instantiates a new byte buffer index output.
	 *
	 * @param dir the dir
	 * @param name the name
	 * @param allocator the allocator
	 * @param allocatorType the allocator type
	 * @param file the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ByteBufferIndexOutput(ByteBufferDirectory dir, String name, ByteBufferAllocator allocator,
			ByteBufferAllocator.Type allocatorType, ByteBufferFileOutput file) throws IOException {
		this.dir = dir;
		this.name = name;
		this.allocator = allocator;
		this.allocatorType = allocatorType;
		this.BUFFER_SIZE = file.bufferSize;
		this.file = file;

		currentBufferIndex = -1;
		currentBuffer = EMPTY_BUFFER;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.IndexOutput#close()
	 */
	@Override
	public void close() throws IOException {
		flush();
		dir.closeOutput(name, file);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.IndexOutput#seek(long)
	 */
	@Override
	public void seek(long pos) throws IOException {

		setFileLength();
		if (pos < bufferStart || pos >= bufferStart + BUFFER_SIZE) {
			currentBufferIndex = (int) (pos / BUFFER_SIZE);
			switchCurrentBuffer();
		}
		currentBuffer.position((int) (pos % BUFFER_SIZE));
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.IndexOutput#length()
	 */
	@Override
	public long length() {
		return file.getLength();
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.DataOutput#writeByte(byte)
	 */
	@Override
	public void writeByte(byte b) throws IOException {
		if (!currentBuffer.hasRemaining()) {
			currentBufferIndex++;
			switchCurrentBuffer();
		}
		currentBuffer.put(b);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.DataOutput#writeBytes(byte[], int, int)
	 */
	@Override
	public void writeBytes(byte[] b, int offset, int len) throws IOException {
		while (len > 0) {
			if (!currentBuffer.hasRemaining()) {
				currentBufferIndex++;
				switchCurrentBuffer();
			}

			int remainInBuffer = currentBuffer.remaining();
			int bytesToCopy = len < remainInBuffer ? len : remainInBuffer;
			currentBuffer.put(b, offset, bytesToCopy);
			offset += bytesToCopy;
			len -= bytesToCopy;
		}
	}

	/**
	 * Switch current buffer.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void switchCurrentBuffer() throws IOException {
		if (currentBufferIndex == file.numBuffers()) {
			currentBuffer = allocator.allocate(allocatorType);
			file.addBuffer(currentBuffer);
		} else {
			currentBuffer = file.getBuffer(currentBufferIndex);
		}
		currentBuffer.position(0);
		bufferStart = (long) BUFFER_SIZE * (long) currentBufferIndex;
	}

	/**
	 * Sets the file length.
	 */
	private void setFileLength() {
		long pointer = bufferStart + currentBuffer.position();
		if (pointer > file.getLength()) {
			file.setLength(pointer);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.IndexOutput#flush()
	 */
	@Override
	public void flush() throws IOException {
		file.setLastModified(System.currentTimeMillis());
		setFileLength();
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.IndexOutput#getFilePointer()
	 */
	@Override
	public long getFilePointer() {
		return currentBufferIndex < 0 ? 0 : bufferStart + currentBuffer.position();
	}
}
