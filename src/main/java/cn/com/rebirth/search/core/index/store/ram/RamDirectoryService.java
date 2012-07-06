/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RamDirectoryService.java 2012-3-29 15:02:03 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.store.ram;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.RAMFile;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.store.DirectoryService;


/**
 * The Class RamDirectoryService.
 *
 * @author l.xue.nong
 */
public class RamDirectoryService extends AbstractIndexShardComponent implements DirectoryService {

	
	/**
	 * Instantiates a new ram directory service.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 */
	@Inject
	public RamDirectoryService(ShardId shardId, @IndexSettings Settings indexSettings) {
		super(shardId, indexSettings);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.store.DirectoryService#build()
	 */
	@Override
	public Directory[] build() {
		return new Directory[] { new CustomRAMDirectory() };
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.store.DirectoryService#renameFile(org.apache.lucene.store.Directory, java.lang.String, java.lang.String)
	 */
	@Override
	public void renameFile(Directory dir, String from, String to) throws IOException {
		((CustomRAMDirectory) dir).renameTo(from, to);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.store.DirectoryService#fullDelete(org.apache.lucene.store.Directory)
	 */
	@Override
	public void fullDelete(Directory dir) {
	}

	
	/**
	 * The Class CustomRAMDirectory.
	 *
	 * @author l.xue.nong
	 */
	static class CustomRAMDirectory extends RAMDirectory {

		
		/**
		 * Rename to.
		 *
		 * @param from the from
		 * @param to the to
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public synchronized void renameTo(String from, String to) throws IOException {
			RAMFile fromFile = fileMap.get(from);
			if (fromFile == null)
				throw new FileNotFoundException(from);
			RAMFile toFile = fileMap.get(to);
			if (toFile != null) {
				sizeInBytes.addAndGet(-fileLength(from));
				fileMap.remove(from);
			}
			fileMap.put(to, fromFile);
		}
	}
}
