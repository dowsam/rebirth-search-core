/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SimpleFsTranslogFile.java 2012-3-29 15:02:04 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.translog.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.translog.Translog;
import cn.com.rebirth.search.core.index.translog.TranslogException;


/**
 * The Class SimpleFsTranslogFile.
 *
 * @author l.xue.nong
 */
public class SimpleFsTranslogFile implements FsTranslogFile {

	
	/** The id. */
	private final long id;

	
	/** The shard id. */
	private final ShardId shardId;

	
	/** The raf. */
	private final RafReference raf;

	
	/** The operation counter. */
	private final AtomicInteger operationCounter = new AtomicInteger();

	
	/** The last position. */
	private final AtomicLong lastPosition = new AtomicLong(0);

	
	/** The last written position. */
	private final AtomicLong lastWrittenPosition = new AtomicLong(0);

	
	/** The last sync position. */
	private volatile long lastSyncPosition = 0;

	
	/**
	 * Instantiates a new simple fs translog file.
	 *
	 * @param shardId the shard id
	 * @param id the id
	 * @param raf the raf
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public SimpleFsTranslogFile(ShardId shardId, long id, RafReference raf) throws IOException {
		this.shardId = shardId;
		this.id = id;
		this.raf = raf;
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
		return operationCounter.get();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#translogSizeInBytes()
	 */
	public long translogSizeInBytes() {
		return lastWrittenPosition.get();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#add(byte[], int, int)
	 */
	public Translog.Location add(byte[] data, int from, int size) throws IOException {
		long position = lastPosition.getAndAdd(size);
		raf.channel().write(ByteBuffer.wrap(data, from, size), position);
		lastWrittenPosition.getAndAdd(size);
		operationCounter.incrementAndGet();
		return new Translog.Location(id, position, size);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#read(cn.com.summall.search.core.index.translog.Translog.Location)
	 */
	public byte[] read(Translog.Location location) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(location.size);
		raf.channel().read(buffer, location.translogLocation);
		return buffer.array();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#close(boolean)
	 */
	public void close(boolean delete) {
		sync();
		raf.decreaseRefCount(delete);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#snapshot()
	 */
	public FsChannelSnapshot snapshot() throws TranslogException {
		try {
			if (!raf.increaseRefCount()) {
				return null;
			}
			return new FsChannelSnapshot(this.id, raf, lastWrittenPosition.get(), operationCounter.get());
		} catch (Exception e) {
			throw new TranslogException(shardId, "Failed to snapshot", e);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#syncNeeded()
	 */
	@Override
	public boolean syncNeeded() {
		return lastWrittenPosition.get() != lastSyncPosition;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#sync()
	 */
	public void sync() {
		try {
			
			long last = lastWrittenPosition.get();
			if (last == lastSyncPosition) {
				return;
			}
			lastSyncPosition = last;
			raf.channel().force(false);
		} catch (Exception e) {
			
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.translog.fs.FsTranslogFile#reuse(cn.com.summall.search.core.index.translog.fs.FsTranslogFile)
	 */
	@Override
	public void reuse(FsTranslogFile other) {
		
	}
}
