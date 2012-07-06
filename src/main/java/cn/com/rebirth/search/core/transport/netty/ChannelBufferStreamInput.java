/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ChannelBufferStreamInput.java 2012-3-29 15:02:29 l.xue.nong$$
 */


package cn.com.rebirth.search.core.transport.netty;

import java.io.EOFException;
import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.io.stream.StreamInput;


/**
 * The Class ChannelBufferStreamInput.
 *
 * @author l.xue.nong
 */
public class ChannelBufferStreamInput extends StreamInput {

	
	/** The buffer. */
	private final ChannelBuffer buffer;

	
	/** The start index. */
	private final int startIndex;

	
	/** The end index. */
	private final int endIndex;

	
	/**
	 * Instantiates a new channel buffer stream input.
	 *
	 * @param buffer the buffer
	 * @param length the length
	 */
	public ChannelBufferStreamInput(ChannelBuffer buffer, int length) {
		if (length > buffer.readableBytes()) {
			throw new IndexOutOfBoundsException();
		}
		this.buffer = buffer;
		startIndex = buffer.readerIndex();
		endIndex = startIndex + length;
		buffer.markReaderIndex();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.StreamInput#readBytesReference()
	 */
	@Override
	public BytesHolder readBytesReference() throws IOException {
		
		
		
		if (!buffer.hasArray()) {
			return super.readBytesReference();
		}
		int size = readVInt();
		BytesHolder bytes = new BytesHolder(buffer.array(), buffer.arrayOffset() + buffer.readerIndex(), size);
		buffer.skipBytes(size);
		return bytes;
	}

	
	/* (non-Javadoc)
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		return endIndex - buffer.readerIndex();
	}

	
	/* (non-Javadoc)
	 * @see java.io.InputStream#mark(int)
	 */
	@Override
	public void mark(int readlimit) {
		buffer.markReaderIndex();
	}

	
	/* (non-Javadoc)
	 * @see java.io.InputStream#markSupported()
	 */
	@Override
	public boolean markSupported() {
		return true;
	}

	
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		if (available() == 0) {
			return -1;
		}
		return buffer.readByte() & 0xff;
	}

	
	/* (non-Javadoc)
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (len == 0) {
			return 0;
		}
		int available = available();
		if (available == 0) {
			return -1;
		}

		len = Math.min(available, len);
		buffer.readBytes(b, off, len);
		return len;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.StreamInput#reset()
	 */
	@Override
	public void reset() throws IOException {
		buffer.resetReaderIndex();
	}

	
	/* (non-Javadoc)
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip(long n) throws IOException {
		if (n > Integer.MAX_VALUE) {
			return skipBytes(Integer.MAX_VALUE);
		} else {
			return skipBytes((int) n);
		}
	}

	
	/**
	 * Skip bytes.
	 *
	 * @param n the n
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public int skipBytes(int n) throws IOException {
		int nBytes = Math.min(available(), n);
		buffer.skipBytes(nBytes);
		return nBytes;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.StreamInput#readByte()
	 */
	@Override
	public byte readByte() throws IOException {
		return buffer.readByte();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.StreamInput#readBytes(byte[], int, int)
	 */
	@Override
	public void readBytes(byte[] b, int offset, int len) throws IOException {
		int read = read(b, offset, len);
		if (read < len) {
			throw new EOFException();
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.StreamInput#close()
	 */
	@Override
	public void close() throws IOException {
		
	}
}
