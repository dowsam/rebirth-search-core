/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FsChannelSnapshot.java 2012-7-6 14:29:24 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.translog.fs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.search.commons.io.FileChannelInputStream;
import cn.com.rebirth.search.commons.io.stream.BytesStreamInput;
import cn.com.rebirth.search.core.index.translog.Translog;
import cn.com.rebirth.search.core.index.translog.TranslogStreams;

/**
 * The Class FsChannelSnapshot.
 *
 * @author l.xue.nong
 */
public class FsChannelSnapshot implements Translog.Snapshot {

	/** The id. */
	private final long id;

	/** The total operations. */
	private final int totalOperations;

	/** The raf. */
	private final RafReference raf;

	/** The channel. */
	private final FileChannel channel;

	/** The length. */
	private final long length;

	/** The last operation read. */
	private Translog.Operation lastOperationRead = null;

	/** The position. */
	private int position = 0;

	/** The cache buffer. */
	private ByteBuffer cacheBuffer;

	/**
	 * Instantiates a new fs channel snapshot.
	 *
	 * @param id the id
	 * @param raf the raf
	 * @param length the length
	 * @param totalOperations the total operations
	 * @throws FileNotFoundException the file not found exception
	 */
	public FsChannelSnapshot(long id, RafReference raf, long length, int totalOperations) throws FileNotFoundException {
		this.id = id;
		this.raf = raf;
		this.channel = raf.raf().getChannel();
		this.length = length;
		this.totalOperations = totalOperations;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog.Snapshot#translogId()
	 */
	@Override
	public long translogId() {
		return this.id;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog.Snapshot#position()
	 */
	@Override
	public long position() {
		return this.position;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog.Snapshot#length()
	 */
	@Override
	public long length() {
		return this.length;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog.Snapshot#estimatedTotalOperations()
	 */
	@Override
	public int estimatedTotalOperations() {
		return this.totalOperations;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog.Snapshot#stream()
	 */
	@Override
	public InputStream stream() throws IOException {
		return new FileChannelInputStream(channel, position, lengthInBytes());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog.Snapshot#lengthInBytes()
	 */
	@Override
	public long lengthInBytes() {
		return length - position;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog.Snapshot#hasNext()
	 */
	@Override
	public boolean hasNext() {
		try {
			if (position > length) {
				return false;
			}
			if (cacheBuffer == null) {
				cacheBuffer = ByteBuffer.allocate(1024);
			}
			cacheBuffer.limit(4);
			int bytesRead = channel.read(cacheBuffer, position);
			if (bytesRead < 4) {
				return false;
			}
			cacheBuffer.flip();
			int opSize = cacheBuffer.getInt();
			position += 4;
			if ((position + opSize) > length) {

				position -= 4;
				return false;
			}
			if (cacheBuffer.capacity() < opSize) {
				cacheBuffer = ByteBuffer.allocate(opSize);
			}
			cacheBuffer.clear();
			cacheBuffer.limit(opSize);
			channel.read(cacheBuffer, position);
			cacheBuffer.flip();
			position += opSize;
			lastOperationRead = TranslogStreams.readTranslogOperation(new BytesStreamInput(cacheBuffer.array(), 0,
					opSize, true));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog.Snapshot#next()
	 */
	@Override
	public Translog.Operation next() {
		return this.lastOperationRead;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog.Snapshot#seekForward(long)
	 */
	@Override
	public void seekForward(long length) {
		this.position += length;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.lease.Releasable#release()
	 */
	@Override
	public boolean release() throws RebirthException {
		raf.decreaseRefCount(true);
		return true;
	}
}
