/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FsDirectoryService.java 2012-3-29 15:01:41 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.store.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.store.SimpleFSLockFactory;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.io.FileSystemUtils;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.store.DirectoryService;
import cn.com.rebirth.search.core.index.store.IndexStore;


/**
 * The Class FsDirectoryService.
 *
 * @author l.xue.nong
 */
public abstract class FsDirectoryService extends AbstractIndexShardComponent implements DirectoryService {

	
	/** The index store. */
	protected final FsIndexStore indexStore;

	
	/**
	 * Instantiates a new fs directory service.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param indexStore the index store
	 */
	public FsDirectoryService(ShardId shardId, @IndexSettings Settings indexSettings, IndexStore indexStore) {
		super(shardId, indexSettings);
		this.indexStore = (FsIndexStore) indexStore;
	}

	
	/**
	 * Builds the lock factory.
	 *
	 * @return the lock factory
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected LockFactory buildLockFactory() throws IOException {
		String fsLock = componentSettings.get("lock", componentSettings.get("fs_lock", "native"));
		LockFactory lockFactory = NoLockFactory.getNoLockFactory();
		if (fsLock.equals("native")) {
			
			lockFactory = new NativeFSLockFactory();
		} else if (fsLock.equals("simple")) {
			lockFactory = new SimpleFSLockFactory();
		} else if (fsLock.equals("none")) {
			lockFactory = NoLockFactory.getNoLockFactory();
		}
		return lockFactory;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.store.DirectoryService#renameFile(org.apache.lucene.store.Directory, java.lang.String, java.lang.String)
	 */
	@Override
	public void renameFile(Directory dir, String from, String to) throws IOException {
		File directory = ((FSDirectory) dir).getDirectory();
		File old = new File(directory, from);
		File nu = new File(directory, to);
		if (nu.exists())
			if (!nu.delete())
				throw new IOException("Cannot delete " + nu);

		if (!old.exists()) {
			throw new FileNotFoundException("Can't rename from [" + from + "] to [" + to + "], from does not exists");
		}

		boolean renamed = false;
		for (int i = 0; i < 3; i++) {
			if (old.renameTo(nu)) {
				renamed = true;
				break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new InterruptedIOException(e.getMessage());
			}
		}
		if (!renamed) {
			throw new IOException("Failed to rename, from [" + from + "], to [" + to + "]");
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.store.DirectoryService#fullDelete(org.apache.lucene.store.Directory)
	 */
	@Override
	public void fullDelete(Directory dir) throws IOException {
		FSDirectory fsDirectory = (FSDirectory) dir;
		FileSystemUtils.deleteRecursively(fsDirectory.getDirectory());
		
		String[] list = fsDirectory.getDirectory().getParentFile().list();
		if (list == null || list.length == 0) {
			FileSystemUtils.deleteRecursively(fsDirectory.getDirectory().getParentFile());
		}
	}
}
