/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FsTranslogFile.java 2012-3-29 15:02:32 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.translog.fs;

import java.io.IOException;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.translog.Translog;
import cn.com.rebirth.search.core.index.translog.TranslogException;


/**
 * The Interface FsTranslogFile.
 *
 * @author l.xue.nong
 */
public interface FsTranslogFile {

	
	/**
	 * The Enum Type.
	 *
	 * @author l.xue.nong
	 */
	public static enum Type {

		
		/** The SIMPLE. */
		SIMPLE() {
			@Override
			public FsTranslogFile create(ShardId shardId, long id, RafReference raf, int bufferSize) throws IOException {
				return new SimpleFsTranslogFile(shardId, id, raf);
			}
		},

		
		/** The BUFFERED. */
		BUFFERED() {
			@Override
			public FsTranslogFile create(ShardId shardId, long id, RafReference raf, int bufferSize) throws IOException {
				return new BufferingFsTranslogFile(shardId, id, raf, bufferSize);
			}
		};

		
		/**
		 * Creates the.
		 *
		 * @param shardId the shard id
		 * @param id the id
		 * @param raf the raf
		 * @param bufferSize the buffer size
		 * @return the fs translog file
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public abstract FsTranslogFile create(ShardId shardId, long id, RafReference raf, int bufferSize)
				throws IOException;

		
		/**
		 * From string.
		 *
		 * @param type the type
		 * @return the type
		 * @throws SumMallSearchIllegalArgumentException the sum mall search illegal argument exception
		 */
		public static Type fromString(String type) throws RestartIllegalArgumentException {
			if (SIMPLE.name().equalsIgnoreCase(type)) {
				return SIMPLE;
			} else if (BUFFERED.name().equalsIgnoreCase(type)) {
				return BUFFERED;
			}
			throw new RestartIllegalArgumentException("No translog fs type [" + type + "]");
		}
	}

	
	/**
	 * Id.
	 *
	 * @return the long
	 */
	long id();

	
	/**
	 * Estimated number of operations.
	 *
	 * @return the int
	 */
	int estimatedNumberOfOperations();

	
	/**
	 * Translog size in bytes.
	 *
	 * @return the long
	 */
	long translogSizeInBytes();

	
	/**
	 * Adds the.
	 *
	 * @param data the data
	 * @param from the from
	 * @param size the size
	 * @return the translog. location
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	Translog.Location add(byte[] data, int from, int size) throws IOException;

	
	/**
	 * Read.
	 *
	 * @param location the location
	 * @return the byte[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	byte[] read(Translog.Location location) throws IOException;

	
	/**
	 * Close.
	 *
	 * @param delete the delete
	 * @throws TranslogException the translog exception
	 */
	void close(boolean delete) throws TranslogException;

	
	/**
	 * Snapshot.
	 *
	 * @return the fs channel snapshot
	 * @throws TranslogException the translog exception
	 */
	FsChannelSnapshot snapshot() throws TranslogException;

	
	/**
	 * Reuse.
	 *
	 * @param other the other
	 * @throws TranslogException the translog exception
	 */
	void reuse(FsTranslogFile other) throws TranslogException;

	
	/**
	 * Sync.
	 */
	void sync();

	
	/**
	 * Sync needed.
	 *
	 * @return true, if successful
	 */
	boolean syncNeeded();
}
