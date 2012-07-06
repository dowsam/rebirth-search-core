/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ByteBufferIndexInput.java 2012-7-6 14:29:21 l.xue.nong$$
 */

package org.apache.lucene.store.bytebuffer;

import org.apache.lucene.store.IndexInput;

import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * The Class ByteBufferIndexInput.
 *
 * @author l.xue.nong
 */
public class ByteBufferIndexInput extends IndexInput {

	/** The Constant EMPTY_BUFFER. */
	private final static ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0).asReadOnlyBuffer();

	/** The file. */
	private final ByteBufferFile file;

	/** The length. */
	private final long length;

	/** The current buffer. */
	private ByteBuffer currentBuffer;

	/** The current buffer index. */
	private int currentBufferIndex;

	/** The buffer start. */
	private long bufferStart;

	/** The buffer size. */
	private final int BUFFER_SIZE;

	/** The closed. */
	private volatile boolean closed = false;

	/**
	 * Instantiates a new byte buffer index input.
	 *
	 * @param name the name
	 * @param file the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ByteBufferIndexInput(String name, ByteBufferFile file) throws IOException {
		super("BBIndexInput(name=" + name + ")");
		this.file = file;
		this.file.incRef();
		this.length = file.getLength();
		this.BUFFER_SIZE = file.bufferSize;

		currentBufferIndex = -1;
		currentBuffer = EMPTY_BUFFER;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.IndexInput#close()
	 */
	@Override
	public void close() {

		if (closed) {
			return;
		}
		closed = true;
		file.decRef();
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.IndexInput#length()
	 */
	@Override
	public long length() {
		return length;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.DataInput#readShort()
	 */
	@Override
	public short readShort() throws IOException {
		try {
			currentBuffer.mark();
			return currentBuffer.getShort();
		} catch (BufferUnderflowException e) {
			currentBuffer.reset();
			return super.readShort();
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.DataInput#readInt()
	 */
	@Override
	public int readInt() throws IOException {
		try {
			currentBuffer.mark();
			return currentBuffer.getInt();
		} catch (BufferUnderflowException e) {
			currentBuffer.reset();
			return super.readInt();
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.DataInput#readLong()
	 */
	@Override
	public long readLong() throws IOException {
		try {
			currentBuffer.mark();
			return currentBuffer.getLong();
		} catch (BufferUnderflowException e) {
			currentBuffer.reset();
			return super.readLong();
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.DataInput#readByte()
	 */
	@Override
	public byte readByte() throws IOException {
		if (!currentBuffer.hasRemaining()) {
			currentBufferIndex++;
			switchCurrentBuffer(true);
		}
		return currentBuffer.get();
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.DataInput#readBytes(byte[], int, int)
	 */
	@Override
	public void readBytes(byte[] b, int offset, int len) throws IOException {
		while (len > 0) {
			if (!currentBuffer.hasRemaining()) {
				currentBufferIndex++;
				switchCurrentBuffer(true);
			}

			int remainInBuffer = currentBuffer.remaining();
			int bytesToCopy = len < remainInBuffer ? len : remainInBuffer;
			currentBuffer.get(b, offset, bytesToCopy);
			offset += bytesToCopy;
			len -= bytesToCopy;
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.IndexInput#getFilePointer()
	 */
	@Override
	public long getFilePointer() {
		return currentBufferIndex < 0 ? 0 : bufferStart + currentBuffer.position();
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.IndexInput#seek(long)
	 */
	@Override
	public void seek(long pos) throws IOException {
		if (currentBuffer == EMPTY_BUFFER || pos < bufferStart || pos >= bufferStart + BUFFER_SIZE) {
			currentBufferIndex = (int) (pos / BUFFER_SIZE);
			switchCurrentBuffer(false);
		}
		try {
			currentBuffer.position((int) (pos % BUFFER_SIZE));

		} catch (IllegalArgumentException e) {
			IOException ioException = new IOException("seeking past position");
			ioException.initCause(e);
			throw ioException;
		}
	}

	/**
	 * Switch current buffer.
	 *
	 * @param enforceEOF the enforce eof
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void switchCurrentBuffer(boolean enforceEOF) throws IOException {
		if (currentBufferIndex >= file.numBuffers()) {

			if (enforceEOF) {
				throw new EOFException("Read past EOF (resource: " + this + ")");
			} else {

				currentBufferIndex--;
				currentBuffer.position(currentBuffer.limit());
			}
		} else {
			ByteBuffer buffer = file.getBuffer(currentBufferIndex);

			currentBuffer = buffer.asReadOnlyBuffer();
			currentBuffer.position(0);
			bufferStart = (long) BUFFER_SIZE * (long) currentBufferIndex;

			long buflen = length - bufferStart;
			if (buflen < BUFFER_SIZE) {
				currentBuffer.limit((int) buflen);
			}

			if (!currentBuffer.hasRemaining()) {
				if (enforceEOF) {
					throw new EOFException("Read past EOF (resource: " + this + ")");
				} else {

					currentBufferIndex--;
					currentBuffer.position(currentBuffer.limit());
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.DataInput#clone()
	 */
	@Override
	public Object clone() {
		ByteBufferIndexInput cloned = (ByteBufferIndexInput) super.clone();
		cloned.file.incRef();
		if (currentBuffer != EMPTY_BUFFER) {
			cloned.currentBuffer = currentBuffer.asReadOnlyBuffer();
			cloned.currentBuffer.position(currentBuffer.position());
		}
		return cloned;
	}
}
