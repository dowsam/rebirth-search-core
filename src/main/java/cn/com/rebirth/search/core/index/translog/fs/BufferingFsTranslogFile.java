/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BufferingFsTranslogFile.java 2012-3-29 15:02:37 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.translog.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.translog.Translog;
import cn.com.rebirth.search.core.index.translog.TranslogException;


/**
 * The Class BufferingFsTranslogFile.
 *
 * @author l.xue.nong
 */
public class BufferingFsTranslogFile implements FsTranslogFile {

	
	/** The id. */
	private final long id;

	
	/** The shard id. */
	private final ShardId shardId;

	
	/** The raf. */
	private final RafReference raf;

	
	/** The rwl. */
	private final ReadWriteLock rwl = new ReentrantReadWriteLock();

	
	/** The operation counter. */
	private volatile int operationCounter;

	
	/** The last position. */
	private long lastPosition;

	
	/** The last written position. */
	private volatile long lastWrittenPosition;

	
	/** The last sync position. */
	private volatile long lastSyncPosition = 0;

	
	/** The buffer. */
	private byte[] buffer;

	
	/** The buffer count. */
	private int bufferCount;

	
	/**
	 * Instantiates a new buffering fs translog file.
	 *
	 * @param shardId the shard id
	 * @param id the id
	 * @param raf the raf
	 * @param bufferSize the buffer size
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public BufferingFsTranslogFile(ShardId shardId, long id, RafReference raf, int bufferSize) throws IOException {
		this.shardId = shardId;
		this.id = id;
		this.raf = raf;
		this.buffer = new byte[bufferSize];
		raf.raf().setLength(0);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#id()
	 */
	public long id() {
		return this.id;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#estimatedNumberOfOperations()
	 */
	public int estimatedNumberOfOperations() {
		return operationCounter;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#translogSizeInBytes()
	 */
	public long translogSizeInBytes() {
		return lastWrittenPosition;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#add(byte[], int, int)
	 */
	@Override
	public Translog.Location add(byte[] data, int from, int size) throws IOException {
		rwl.writeLock().lock();
		try {
			operationCounter++;
			long position = lastPosition;
			if (size >= buffer.length) {
				flushBuffer();
				raf.raf().write(data, from, size);
				lastWrittenPosition += size;
				lastPosition += size;
				return new Translog.Location(id, position, size);
			}
			if (size > buffer.length - bufferCount) {
				flushBuffer();
			}
			System.arraycopy(data, from, buffer, bufferCount, size);
			bufferCount += size;
			lastPosition += size;
			return new Translog.Location(id, position, size);
		} finally {
			rwl.writeLock().unlock();
		}
	}

	
	/**
	 * Flush buffer.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void flushBuffer() throws IOException {
		if (bufferCount > 0) {
			raf.raf().write(buffer, 0, bufferCount);
			lastWrittenPosition += bufferCount;
			bufferCount = 0;
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#read(cn.com.summall.search.core.index.translog.Translog.Location)
	 */
	@Override
	public byte[] read(Translog.Location location) throws IOException {
		rwl.readLock().lock();
		try {
			if (location.translogLocation >= lastWrittenPosition) {
				byte[] data = new byte[location.size];
				System.arraycopy(buffer, (int) (location.translogLocation - lastWrittenPosition), data, 0,
						location.size);
				return data;
			}
		} finally {
			rwl.readLock().unlock();
		}
		ByteBuffer buffer = ByteBuffer.allocate(location.size);
		raf.channel().read(buffer, location.translogLocation);
		return buffer.array();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#snapshot()
	 */
	@Override
	public FsChannelSnapshot snapshot() throws TranslogException {
		rwl.writeLock().lock();
		try {
			flushBuffer();
			if (!raf.increaseRefCount()) {
				return null;
			}
			return new FsChannelSnapshot(this.id, raf, lastWrittenPosition, operationCounter);
		} catch (IOException e) {
			throw new TranslogException(shardId, "failed to flush", e);
		} finally {
			rwl.writeLock().unlock();
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#syncNeeded()
	 */
	@Override
	public boolean syncNeeded() {
		return lastPosition != lastSyncPosition;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#sync()
	 */
	@Override
	public void sync() {
		try {
			
			long last = lastPosition;
			if (last == lastSyncPosition) {
				return;
			}
			lastSyncPosition = last;
			rwl.writeLock().lock();
			try {
				flushBuffer();
			} finally {
				rwl.writeLock().unlock();
			}
			raf.channel().force(false);
		} catch (Exception e) {
			
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#close(boolean)
	 */
	@Override
	public void close(boolean delete) {
		if (!delete) {
			rwl.writeLock().lock();
			try {
				flushBuffer();
				sync();
			} catch (IOException e) {
				throw new TranslogException(shardId, "failed to close", e);
			} finally {
				rwl.writeLock().unlock();
			}
		}
		raf.decreaseRefCount(delete);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#reuse(cn.com.summall.search.core.index.translog.fs.FsTranslogFile)
	 */
	@Override
	public void reuse(FsTranslogFile other) {
		if (!(other instanceof BufferingFsTranslogFile)) {
			return;
		}
		rwl.writeLock().lock();
		try {
			flushBuffer();
			this.buffer = ((BufferingFsTranslogFile) other).buffer;
		} catch (IOException e) {
			throw new TranslogException(shardId, "failed to flush", e);
		} finally {
			rwl.writeLock().unlock();
		}
	}
}
