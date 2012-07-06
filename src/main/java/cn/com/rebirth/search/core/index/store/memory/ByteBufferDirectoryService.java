/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ByteBufferDirectoryService.java 2012-3-29 15:02:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.store.memory;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.bytebuffer.ByteBufferAllocator;
import org.apache.lucene.store.bytebuffer.ByteBufferDirectory;
import org.apache.lucene.store.bytebuffer.ByteBufferFile;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cache.memory.ByteBufferCache;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.store.DirectoryService;
import cn.com.rebirth.search.core.index.store.IndexStore;


/**
 * The Class ByteBufferDirectoryService.
 *
 * @author l.xue.nong
 */
public class ByteBufferDirectoryService extends AbstractIndexShardComponent implements DirectoryService {

	
	/** The byte buffer cache. */
	private final ByteBufferCache byteBufferCache;

	
	/**
	 * Instantiates a new byte buffer directory service.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param indexStore the index store
	 * @param byteBufferCache the byte buffer cache
	 */
	@Inject
	public ByteBufferDirectoryService(ShardId shardId, @IndexSettings Settings indexSettings, IndexStore indexStore,
			ByteBufferCache byteBufferCache) {
		super(shardId, indexSettings);
		this.byteBufferCache = byteBufferCache;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.store.DirectoryService#build()
	 */
	@Override
	public Directory[] build() {
		return new Directory[] { new CustomByteBufferDirectory(byteBufferCache) };
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.store.DirectoryService#renameFile(org.apache.lucene.store.Directory, java.lang.String, java.lang.String)
	 */
	@Override
	public void renameFile(Directory dir, String from, String to) throws IOException {
		((CustomByteBufferDirectory) dir).renameTo(from, to);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.store.DirectoryService#fullDelete(org.apache.lucene.store.Directory)
	 */
	@Override
	public void fullDelete(Directory dir) {
	}

	
	/**
	 * The Class CustomByteBufferDirectory.
	 *
	 * @author l.xue.nong
	 */
	static class CustomByteBufferDirectory extends ByteBufferDirectory {

		
		/**
		 * Instantiates a new custom byte buffer directory.
		 */
		CustomByteBufferDirectory() {
		}

		
		/**
		 * Instantiates a new custom byte buffer directory.
		 *
		 * @param allocator the allocator
		 */
		CustomByteBufferDirectory(ByteBufferAllocator allocator) {
			super(allocator);
		}

		
		/**
		 * Rename to.
		 *
		 * @param from the from
		 * @param to the to
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public void renameTo(String from, String to) throws IOException {
			ByteBufferFile fromFile = files.get(from);
			if (fromFile == null)
				throw new FileNotFoundException(from);
			ByteBufferFile toFile = files.get(to);
			if (toFile != null) {
				files.remove(from);
			}
			files.put(to, fromFile);
		}
	}
}
