/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core Store.java 2012-7-6 14:30:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import jsr166y.ThreadLocalRandom;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.OpenBufferedIndexOutput;
import org.apache.lucene.store.SimpleFSDirectory;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.lucene.Directories;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.index.store.support.ForceSyncDirectory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * The Class Store.
 *
 * @author l.xue.nong
 */
public class Store extends AbstractIndexShardComponent {

	/** The Constant CHECKSUMS_PREFIX. */
	static final String CHECKSUMS_PREFIX = "_checksums-";

	/**
	 * Checks if is checksum.
	 *
	 * @param name the name
	 * @return true, if is checksum
	 */
	public static final boolean isChecksum(String name) {
		return name.startsWith(CHECKSUMS_PREFIX);
	}

	/** The index store. */
	private final IndexStore indexStore;

	/** The directory service. */
	private final DirectoryService directoryService;

	/** The directory. */
	private final StoreDirectory directory;

	/** The files metadata. */
	private volatile ImmutableMap<String, StoreFileMetaData> filesMetadata = ImmutableMap.of();

	/** The files. */
	private volatile String[] files = Strings.EMPTY_ARRAY;

	/** The mutex. */
	private final Object mutex = new Object();

	/** The sync. */
	private final boolean sync;

	/**
	 * Instantiates a new store.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param indexStore the index store
	 * @param directoryService the directory service
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Inject
	public Store(ShardId shardId, @IndexSettings Settings indexSettings, IndexStore indexStore,
			DirectoryService directoryService) throws IOException {
		super(shardId, indexSettings);
		this.indexStore = indexStore;
		this.directoryService = directoryService;
		this.sync = componentSettings.getAsBoolean("sync", true);
		this.directory = new StoreDirectory(directoryService.build());
	}

	/**
	 * Directory.
	 *
	 * @return the directory
	 */
	public Directory directory() {
		return directory;
	}

	/**
	 * List.
	 *
	 * @return the immutable map
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ImmutableMap<String, StoreFileMetaData> list() throws IOException {
		ImmutableMap.Builder<String, StoreFileMetaData> builder = ImmutableMap.builder();
		for (String name : files) {
			StoreFileMetaData md = metaData(name);
			if (md != null) {
				builder.put(md.name(), md);
			}
		}
		return builder.build();
	}

	/**
	 * Meta data.
	 *
	 * @param name the name
	 * @return the store file meta data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public StoreFileMetaData metaData(String name) throws IOException {
		StoreFileMetaData md = filesMetadata.get(name);
		if (md == null) {
			return null;
		}

		if (md.lastModified() == -1 || md.length() == -1) {
			return null;
		}
		return md;
	}

	/**
	 * Delete content.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void deleteContent() throws IOException {
		String[] files = directory.listAll();
		IOException lastException = null;
		for (String file : files) {
			if (isChecksum(file)) {
				try {
					directory.deleteFileChecksum(file);
				} catch (IOException e) {
					lastException = e;
				}
			} else {
				try {
					directory.deleteFile(file);
				} catch (FileNotFoundException e) {

				} catch (IOException e) {
					lastException = e;
				}
			}
		}
		if (lastException != null) {
			throw lastException;
		}
	}

	/**
	 * Full delete.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void fullDelete() throws IOException {
		deleteContent();
		for (Directory delegate : directory.delegates()) {
			directoryService.fullDelete(delegate);
		}
	}

	/**
	 * Stats.
	 *
	 * @return the store stats
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public StoreStats stats() throws IOException {
		return new StoreStats(Directories.estimateSize(directory));
	}

	/**
	 * Estimate size.
	 *
	 * @return the byte size value
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ByteSizeValue estimateSize() throws IOException {
		return new ByteSizeValue(Directories.estimateSize(directory));
	}

	/**
	 * Rename file.
	 *
	 * @param from the from
	 * @param to the to
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void renameFile(String from, String to) throws IOException {
		synchronized (mutex) {
			StoreFileMetaData fromMetaData = filesMetadata.get(from);
			if (fromMetaData == null) {
				throw new FileNotFoundException(from);
			}
			directoryService.renameFile(fromMetaData.directory(), from, to);
			StoreFileMetaData toMetaData = new StoreFileMetaData(to, fromMetaData.length(),
					fromMetaData.lastModified(), fromMetaData.checksum(), fromMetaData.directory());
			filesMetadata = MapBuilder.newMapBuilder(filesMetadata).remove(from).put(to, toMetaData).immutableMap();
			files = filesMetadata.keySet().toArray(new String[filesMetadata.size()]);
		}
	}

	/**
	 * Read checksums.
	 *
	 * @param locations the locations
	 * @return the map
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Map<String, String> readChecksums(File[] locations) throws IOException {
		Directory[] dirs = new Directory[locations.length];
		try {
			for (int i = 0; i < locations.length; i++) {
				dirs[i] = new SimpleFSDirectory(locations[i]);
			}
			return readChecksums(dirs, null);
		} finally {
			for (Directory dir : dirs) {
				if (dir != null) {
					try {
						dir.close();
					} catch (IOException e) {

					}
				}
			}
		}
	}

	/**
	 * Read checksums.
	 *
	 * @param dirs the dirs
	 * @param defaultValue the default value
	 * @return the map
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	static Map<String, String> readChecksums(Directory[] dirs, Map<String, String> defaultValue) throws IOException {
		long lastFound = -1;
		Directory lastDir = null;
		for (Directory dir : dirs) {
			for (String name : dir.listAll()) {
				if (!isChecksum(name)) {
					continue;
				}
				long current = Long.parseLong(name.substring(CHECKSUMS_PREFIX.length()));
				if (current > lastFound) {
					lastFound = current;
					lastDir = dir;
				}
			}
		}
		if (lastFound == -1) {
			return defaultValue;
		}
		IndexInput indexInput = lastDir.openInput(CHECKSUMS_PREFIX + lastFound);
		try {
			indexInput.readInt();
			return indexInput.readStringStringMap();
		} catch (Exception e) {

			return defaultValue;
		} finally {
			indexInput.close();
		}
	}

	/**
	 * Write checksums.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeChecksums() throws IOException {
		String checksumName = CHECKSUMS_PREFIX + System.currentTimeMillis();
		ImmutableMap<String, StoreFileMetaData> files = list();
		synchronized (mutex) {
			Map<String, String> checksums = new HashMap<String, String>();
			for (StoreFileMetaData metaData : files.values()) {
				if (metaData.checksum() != null) {
					checksums.put(metaData.name(), metaData.checksum());
				}
			}
			IndexOutput output = directory.createOutput(checksumName, false);
			output.writeInt(0);
			output.writeStringStringMap(checksums);
			output.close();
		}
		for (StoreFileMetaData metaData : files.values()) {
			if (metaData.name().startsWith(CHECKSUMS_PREFIX) && !checksumName.equals(metaData.name())) {
				try {
					directory.deleteFileChecksum(metaData.name());
				} catch (Exception e) {

				}
			}
		}
	}

	/**
	 * Suggest use compound file.
	 *
	 * @return true, if successful
	 */
	public boolean suggestUseCompoundFile() {
		return false;
	}

	/**
	 * Close.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void close() throws IOException {
		directory.close();
	}

	/**
	 * Creates the output with no checksum.
	 *
	 * @param name the name
	 * @return the index output
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public IndexOutput createOutputWithNoChecksum(String name) throws IOException {
		return directory.createOutput(name, false);
	}

	/**
	 * Write checksum.
	 *
	 * @param name the name
	 * @param checksum the checksum
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeChecksum(String name, String checksum) throws IOException {

		synchronized (mutex) {
			StoreFileMetaData metaData = filesMetadata.get(name);
			metaData = new StoreFileMetaData(metaData.name(), metaData.length(), metaData.lastModified(), checksum,
					metaData.directory());
			filesMetadata = MapBuilder.newMapBuilder(filesMetadata).put(name, metaData).immutableMap();
			writeChecksums();
		}
	}

	/**
	 * Write checksums.
	 *
	 * @param checksums the checksums
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeChecksums(Map<String, String> checksums) throws IOException {

		synchronized (mutex) {
			for (Map.Entry<String, String> entry : checksums.entrySet()) {
				StoreFileMetaData metaData = filesMetadata.get(entry.getKey());
				metaData = new StoreFileMetaData(metaData.name(), metaData.length(), metaData.lastModified(),
						entry.getValue(), metaData.directory());
				filesMetadata = MapBuilder.newMapBuilder(filesMetadata).put(entry.getKey(), metaData).immutableMap();
			}
			writeChecksums();
		}
	}

	/**
	 * The Class StoreDirectory.
	 *
	 * @author l.xue.nong
	 */
	class StoreDirectory extends Directory implements ForceSyncDirectory {

		/** The delegates. */
		private final Directory[] delegates;

		/**
		 * Instantiates a new store directory.
		 *
		 * @param delegates the delegates
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		StoreDirectory(Directory[] delegates) throws IOException {
			this.delegates = delegates;
			synchronized (mutex) {
				MapBuilder<String, StoreFileMetaData> builder = MapBuilder.newMapBuilder();
				Map<String, String> checksums = readChecksums(delegates, new HashMap<String, String>());
				for (Directory delegate : delegates) {
					for (String file : delegate.listAll()) {
						String checksum = checksums.get(file);
						builder.put(file,
								new StoreFileMetaData(file, delegate.fileLength(file), delegate.fileModified(file),
										checksum, delegate));
					}
				}
				filesMetadata = builder.immutableMap();
				files = filesMetadata.keySet().toArray(new String[filesMetadata.size()]);
			}
		}

		/**
		 * Delegates.
		 *
		 * @return the directory[]
		 */
		public Directory[] delegates() {
			return delegates;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.Directory#listAll()
		 */
		@Override
		public String[] listAll() throws IOException {
			return files;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.Directory#fileExists(java.lang.String)
		 */
		@Override
		public boolean fileExists(String name) throws IOException {
			return filesMetadata.containsKey(name);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.Directory#fileModified(java.lang.String)
		 */
		@Override
		public long fileModified(String name) throws IOException {
			StoreFileMetaData metaData = filesMetadata.get(name);
			if (metaData == null) {
				throw new FileNotFoundException(name);
			}

			if (metaData.lastModified() != -1) {
				return metaData.lastModified();
			}
			return metaData.directory().fileModified(name);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.Directory#touchFile(java.lang.String)
		 */
		@Override
		public void touchFile(String name) throws IOException {
			synchronized (mutex) {
				StoreFileMetaData metaData = filesMetadata.get(name);
				if (metaData != null) {
					metaData.directory().touchFile(name);
					metaData = new StoreFileMetaData(metaData.name(), metaData.length(), metaData.directory()
							.fileModified(name), metaData.checksum(), metaData.directory());
					filesMetadata = MapBuilder.newMapBuilder(filesMetadata).put(name, metaData).immutableMap();
				}
			}
		}

		/**
		 * Delete file checksum.
		 *
		 * @param name the name
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public void deleteFileChecksum(String name) throws IOException {
			StoreFileMetaData metaData = filesMetadata.get(name);
			if (metaData != null) {
				try {
					metaData.directory().deleteFile(name);
				} catch (IOException e) {
					if (metaData.directory().fileExists(name)) {
						throw e;
					}
				}
			}
			synchronized (mutex) {
				filesMetadata = MapBuilder.newMapBuilder(filesMetadata).remove(name).immutableMap();
				files = filesMetadata.keySet().toArray(new String[filesMetadata.size()]);
			}
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.Directory#deleteFile(java.lang.String)
		 */
		@Override
		public void deleteFile(String name) throws IOException {

			if (isChecksum(name)) {
				return;
			}
			StoreFileMetaData metaData = filesMetadata.get(name);
			if (metaData != null) {
				try {
					metaData.directory().deleteFile(name);
				} catch (IOException e) {
					if (metaData.directory().fileExists(name)) {
						throw e;
					}
				}
			}
			synchronized (mutex) {
				filesMetadata = MapBuilder.newMapBuilder(filesMetadata).remove(name).immutableMap();
				files = filesMetadata.keySet().toArray(new String[filesMetadata.size()]);
			}
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.Directory#fileLength(java.lang.String)
		 */
		@Override
		public long fileLength(String name) throws IOException {
			StoreFileMetaData metaData = filesMetadata.get(name);
			if (metaData == null) {
				throw new FileNotFoundException(name);
			}

			if (metaData.length() != -1) {
				return metaData.length();
			}
			return metaData.directory().fileLength(name);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.Directory#createOutput(java.lang.String)
		 */
		@Override
		public IndexOutput createOutput(String name) throws IOException {
			return createOutput(name, true);
		}

		/**
		 * Creates the output.
		 *
		 * @param name the name
		 * @param computeChecksum the compute checksum
		 * @return the index output
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public IndexOutput createOutput(String name, boolean computeChecksum) throws IOException {
			Directory directory = null;
			if (isChecksum(name)) {
				directory = delegates[0];
			} else {
				if (delegates.length == 1) {
					directory = delegates[0];
				} else {
					long size = Long.MIN_VALUE;
					for (Directory delegate : delegates) {
						if (delegate instanceof FSDirectory) {
							long currentSize = ((FSDirectory) delegate).getDirectory().getUsableSpace();
							if (currentSize > size) {
								size = currentSize;
								directory = delegate;
							} else if (currentSize == size && ThreadLocalRandom.current().nextBoolean()) {
								directory = delegate;
							} else {
							}
						} else {
							directory = delegate;
						}
					}
				}
			}
			IndexOutput out = directory.createOutput(name);
			synchronized (mutex) {
				StoreFileMetaData metaData = new StoreFileMetaData(name, -1, -1, null, directory);
				filesMetadata = MapBuilder.newMapBuilder(filesMetadata).put(name, metaData).immutableMap();
				files = filesMetadata.keySet().toArray(new String[filesMetadata.size()]);
				return new StoreIndexOutput(metaData, out, name, computeChecksum);
			}
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.Directory#openInput(java.lang.String)
		 */
		@Override
		public IndexInput openInput(String name) throws IOException {
			StoreFileMetaData metaData = filesMetadata.get(name);
			if (metaData == null) {
				throw new FileNotFoundException(name);
			}
			return metaData.directory().openInput(name);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.Directory#close()
		 */
		@Override
		public void close() throws IOException {
			for (Directory delegate : delegates) {
				delegate.close();
			}
			synchronized (mutex) {
				filesMetadata = ImmutableMap.of();
				files = Strings.EMPTY_ARRAY;
			}
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.Directory#makeLock(java.lang.String)
		 */
		@Override
		public Lock makeLock(String name) {
			return delegates[0].makeLock(name);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.Directory#openInput(java.lang.String, int)
		 */
		@Override
		public IndexInput openInput(String name, int bufferSize) throws IOException {
			StoreFileMetaData metaData = filesMetadata.get(name);
			if (metaData == null) {
				throw new FileNotFoundException(name);
			}
			return metaData.directory().openInput(name, bufferSize);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.Directory#clearLock(java.lang.String)
		 */
		@Override
		public void clearLock(String name) throws IOException {
			delegates[0].clearLock(name);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.Directory#setLockFactory(org.apache.lucene.store.LockFactory)
		 */
		@Override
		public void setLockFactory(LockFactory lockFactory) throws IOException {
			delegates[0].setLockFactory(lockFactory);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.Directory#getLockFactory()
		 */
		@Override
		public LockFactory getLockFactory() {
			return delegates[0].getLockFactory();
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.Directory#getLockID()
		 */
		@Override
		public String getLockID() {
			return delegates[0].getLockID();
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.Directory#sync(java.util.Collection)
		 */
		@Override
		public void sync(Collection<String> names) throws IOException {
			if (sync) {
				Map<Directory, Collection<String>> map = Maps.newHashMap();
				for (String name : names) {
					StoreFileMetaData metaData = filesMetadata.get(name);
					if (metaData == null) {
						throw new FileNotFoundException(name);
					}
					Collection<String> dirNames = map.get(metaData.directory());
					if (dirNames == null) {
						dirNames = new ArrayList<String>();
						map.put(metaData.directory(), dirNames);
					}
					dirNames.add(name);
				}
				for (Map.Entry<Directory, Collection<String>> entry : map.entrySet()) {
					entry.getKey().sync(entry.getValue());
				}
			}
			for (String name : names) {

				if (!name.equals("segments.gen") && name.startsWith("segments")) {
					writeChecksums();
					break;
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.Directory#sync(java.lang.String)
		 */
		@Override
		public void sync(String name) throws IOException {
			if (sync) {
				sync(ImmutableList.of(name));
			}

			if (!name.equals("segments.gen") && name.startsWith("segments")) {
				writeChecksums();
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.index.store.support.ForceSyncDirectory#forceSync(java.lang.String)
		 */
		@Override
		public void forceSync(String name) throws IOException {
			sync(ImmutableList.of(name));
		}
	}

	/**
	 * The Class StoreIndexOutput.
	 *
	 * @author l.xue.nong
	 */
	class StoreIndexOutput extends OpenBufferedIndexOutput {

		/** The meta data. */
		private final StoreFileMetaData metaData;

		/** The delegate. */
		private final IndexOutput delegate;

		/** The name. */
		private final String name;

		/** The digest. */
		private final Checksum digest;

		/**
		 * Instantiates a new store index output.
		 *
		 * @param metaData the meta data
		 * @param delegate the delegate
		 * @param name the name
		 * @param computeChecksum the compute checksum
		 */
		StoreIndexOutput(StoreFileMetaData metaData, IndexOutput delegate, String name, boolean computeChecksum) {

			super(OpenBufferedIndexOutput.DEFAULT_BUFFER_SIZE + 64);
			this.metaData = metaData;
			this.delegate = delegate;
			this.name = name;
			if (computeChecksum) {
				if ("segments.gen".equals(name)) {

					this.digest = null;
				} else if (name.startsWith("segments")) {

					this.digest = null;
				} else {

					this.digest = new Adler32();
				}
			} else {
				this.digest = null;
			}
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.OpenBufferedIndexOutput#close()
		 */
		@Override
		public void close() throws IOException {
			super.close();
			delegate.close();
			String checksum = null;
			if (digest != null) {
				checksum = Long.toString(digest.getValue(), Character.MAX_RADIX);
			}
			synchronized (mutex) {
				StoreFileMetaData md = new StoreFileMetaData(name, metaData.directory().fileLength(name), metaData
						.directory().fileModified(name), checksum, metaData.directory());
				filesMetadata = MapBuilder.newMapBuilder(filesMetadata).put(name, md).immutableMap();
				files = filesMetadata.keySet().toArray(new String[filesMetadata.size()]);
			}
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.OpenBufferedIndexOutput#flushBuffer(byte[], int, int)
		 */
		@Override
		protected void flushBuffer(byte[] b, int offset, int len) throws IOException {
			delegate.writeBytes(b, offset, len);
			if (digest != null) {
				digest.update(b, offset, len);
			}
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.OpenBufferedIndexOutput#flush()
		 */
		@Override
		public void flush() throws IOException {
			super.flush();
			delegate.flush();
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.OpenBufferedIndexOutput#seek(long)
		 */
		@Override
		public void seek(long pos) throws IOException {

			super.seek(pos);
			delegate.seek(pos);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.OpenBufferedIndexOutput#length()
		 */
		@Override
		public long length() throws IOException {
			return delegate.length();
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.store.IndexOutput#setLength(long)
		 */
		@Override
		public void setLength(long length) throws IOException {
			delegate.setLength(length);
		}
	}
}
