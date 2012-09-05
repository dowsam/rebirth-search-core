/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FsTranslog.java 2012-7-6 14:30:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.translog.fs;

import java.io.File;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jsr166y.ThreadLocalRandom;
import cn.com.rebirth.commons.io.FileSystemUtils;
import cn.com.rebirth.commons.io.stream.BytesStreamOutput;
import cn.com.rebirth.commons.io.stream.CachedStreamOutput;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.env.NodeEnvironment;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.settings.IndexSettingsService;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.translog.Translog;
import cn.com.rebirth.search.core.index.translog.TranslogException;
import cn.com.rebirth.search.core.index.translog.TranslogStreams;

/**
 * The Class FsTranslog.
 *
 * @author l.xue.nong
 */
public class FsTranslog extends AbstractIndexShardComponent implements Translog {

	static {
		IndexMetaData.addDynamicSettings("index.translog.fs.type", "index.translog.fs.buffer_size",
				"index.translog.fs.transient_buffer_size");
	}

	/**
	 * The Class ApplySettings.
	 *
	 * @author l.xue.nong
	 */
	class ApplySettings implements IndexSettingsService.Listener {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.settings.IndexSettingsService.Listener#onRefreshSettings(cn.com.rebirth.commons.settings.Settings)
		 */
		@Override
		public void onRefreshSettings(Settings settings) {
			int bufferSize = (int) settings.getAsBytesSize("index.translog.fs.buffer_size",
					new ByteSizeValue(FsTranslog.this.bufferSize)).bytes();
			if (bufferSize != FsTranslog.this.bufferSize) {
				logger.info("updating buffer_size from [{}] to [{}]", new ByteSizeValue(FsTranslog.this.bufferSize),
						new ByteSizeValue(bufferSize));
				FsTranslog.this.bufferSize = bufferSize;
			}

			int transientBufferSize = (int) settings.getAsBytesSize("index.translog.fs.transient_buffer_size",
					new ByteSizeValue(FsTranslog.this.transientBufferSize)).bytes();
			if (transientBufferSize != FsTranslog.this.transientBufferSize) {
				logger.info("updating transient_buffer_size from [{}] to [{}]", new ByteSizeValue(
						FsTranslog.this.transientBufferSize), new ByteSizeValue(transientBufferSize));
				FsTranslog.this.transientBufferSize = transientBufferSize;
			}

			FsTranslogFile.Type type = FsTranslogFile.Type.fromString(settings.get("index.translog.fs.type",
					FsTranslog.this.type.name()));
			if (type != FsTranslog.this.type) {
				logger.info("updating type from [{}] to [{}]", FsTranslog.this.type, type);
				FsTranslog.this.type = type;
			}
		}
	}

	/** The index settings service. */
	private final IndexSettingsService indexSettingsService;

	/** The rwl. */
	private final ReadWriteLock rwl = new ReentrantReadWriteLock();

	/** The locations. */
	private final File[] locations;

	/** The current. */
	private volatile FsTranslogFile current;

	/** The trans. */
	private volatile FsTranslogFile trans;

	/** The type. */
	private FsTranslogFile.Type type;

	/** The sync on each operation. */
	private boolean syncOnEachOperation = false;

	/** The buffer size. */
	private int bufferSize;

	/** The transient buffer size. */
	private int transientBufferSize;

	/** The apply settings. */
	private final ApplySettings applySettings = new ApplySettings();

	/**
	 * Instantiates a new fs translog.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param indexSettingsService the index settings service
	 * @param nodeEnv the node env
	 */
	@Inject
	public FsTranslog(ShardId shardId, @IndexSettings Settings indexSettings,
			IndexSettingsService indexSettingsService, NodeEnvironment nodeEnv) {
		super(shardId, indexSettings);
		this.indexSettingsService = indexSettingsService;
		File[] shardLocations = nodeEnv.shardLocations(shardId);
		this.locations = new File[shardLocations.length];
		for (int i = 0; i < shardLocations.length; i++) {
			locations[i] = new File(shardLocations[i], "translog");
			FileSystemUtils.mkdirs(locations[i]);
		}

		this.type = FsTranslogFile.Type.fromString(componentSettings.get("type", FsTranslogFile.Type.BUFFERED.name()));
		this.bufferSize = (int) componentSettings.getAsBytesSize("buffer_size",
				ByteSizeValue.parseBytesSizeValue("64k")).bytes();
		this.transientBufferSize = (int) componentSettings.getAsBytesSize("transient_buffer_size",
				ByteSizeValue.parseBytesSizeValue("8k")).bytes();

		indexSettingsService.addListener(applySettings);
	}

	/**
	 * Instantiates a new fs translog.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param location the location
	 */
	public FsTranslog(ShardId shardId, @IndexSettings Settings indexSettings, File location) {
		super(shardId, indexSettings);
		this.indexSettingsService = null;
		this.locations = new File[] { location };
		FileSystemUtils.mkdirs(location);

		this.type = FsTranslogFile.Type.fromString(componentSettings.get("type", FsTranslogFile.Type.BUFFERED.name()));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog#close(boolean)
	 */
	@Override
	public void close(boolean delete) {
		if (indexSettingsService != null) {
			indexSettingsService.removeListener(applySettings);
		}
		rwl.writeLock().lock();
		try {
			FsTranslogFile current1 = this.current;
			if (current1 != null) {
				current1.close(delete);
			}
			current1 = this.trans;
			if (current1 != null) {
				current1.close(delete);
			}
		} finally {
			rwl.writeLock().unlock();
		}
	}

	/**
	 * Locations.
	 *
	 * @return the file[]
	 */
	public File[] locations() {
		return locations;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog#currentId()
	 */
	@Override
	public long currentId() {
		FsTranslogFile current1 = this.current;
		if (current1 == null) {
			return -1;
		}
		return current1.id();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog#estimatedNumberOfOperations()
	 */
	@Override
	public int estimatedNumberOfOperations() {
		FsTranslogFile current1 = this.current;
		if (current1 == null) {
			return 0;
		}
		return current1.estimatedNumberOfOperations();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog#memorySizeInBytes()
	 */
	@Override
	public long memorySizeInBytes() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog#translogSizeInBytes()
	 */
	@Override
	public long translogSizeInBytes() {
		FsTranslogFile current1 = this.current;
		if (current1 == null) {
			return 0;
		}
		return current1.translogSizeInBytes();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog#clearUnreferenced()
	 */
	@Override
	public void clearUnreferenced() {
		rwl.writeLock().lock();
		try {
			for (File location : locations) {
				File[] files = location.listFiles();
				if (files != null) {
					for (File file : files) {
						if (file.getName().equals("translog-" + current.id())) {
							continue;
						}
						if (trans != null && file.getName().equals("translog-" + trans.id())) {
							continue;
						}
						try {
							file.delete();
						} catch (Exception e) {

						}
					}
				}
			}
		} finally {
			rwl.writeLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog#newTranslog(long)
	 */
	@Override
	public void newTranslog(long id) throws TranslogException {
		rwl.writeLock().lock();
		try {
			FsTranslogFile newFile;
			long size = Long.MAX_VALUE;
			File location = null;
			for (File file : locations) {
				long currentFree = file.getFreeSpace();
				if (currentFree < size) {
					size = currentFree;
					location = file;
				} else if (currentFree == size && ThreadLocalRandom.current().nextBoolean()) {
					location = file;
				}
			}
			try {
				newFile = type.create(shardId, id, new RafReference(new File(location, "translog-" + id)), bufferSize);
			} catch (IOException e) {
				throw new TranslogException(shardId, "failed to create new translog file", e);
			}
			FsTranslogFile old = current;
			current = newFile;
			if (old != null) {

				boolean delete = true;
				if (old.id() == id) {
					delete = false;
				}
				old.close(delete);
			}
		} finally {
			rwl.writeLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog#newTransientTranslog(long)
	 */
	@Override
	public void newTransientTranslog(long id) throws TranslogException {
		rwl.writeLock().lock();
		try {
			assert this.trans == null;
			long size = Long.MAX_VALUE;
			File location = null;
			for (File file : locations) {
				long currentFree = file.getFreeSpace();
				if (currentFree < size) {
					size = currentFree;
					location = file;
				} else if (currentFree == size && ThreadLocalRandom.current().nextBoolean()) {
					location = file;
				}
			}
			this.trans = type.create(shardId, id, new RafReference(new File(location, "translog-" + id)),
					transientBufferSize);
		} catch (IOException e) {
			throw new TranslogException(shardId, "failed to create new translog file", e);
		} finally {
			rwl.writeLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog#makeTransientCurrent()
	 */
	@Override
	public void makeTransientCurrent() {
		FsTranslogFile old;
		rwl.writeLock().lock();
		try {
			assert this.trans != null;
			old = current;
			this.current = this.trans;
			this.trans = null;
		} finally {
			rwl.writeLock().unlock();
		}
		old.close(true);
		current.reuse(old);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog#revertTransient()
	 */
	@Override
	public void revertTransient() {
		FsTranslogFile old;
		rwl.writeLock().lock();
		try {
			old = trans;
			this.trans = null;
		} finally {
			rwl.writeLock().unlock();
		}
		old.close(true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog#read(cn.com.rebirth.search.core.index.translog.Translog.Location)
	 */
	public byte[] read(Location location) {
		rwl.readLock().lock();
		try {
			FsTranslogFile trans = this.trans;
			if (trans != null && trans.id() == location.translogId) {
				try {
					return trans.read(location);
				} catch (Exception e) {

				}
			}
			if (current.id() == location.translogId) {
				try {
					return current.read(location);
				} catch (Exception e) {

				}
			}
			return null;
		} finally {
			rwl.readLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog#add(cn.com.rebirth.search.core.index.translog.Translog.Operation)
	 */
	@Override
	public Location add(Operation operation) throws TranslogException {
		CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
		rwl.readLock().lock();
		try {
			BytesStreamOutput out = cachedEntry.cachedBytes();
			out.writeInt(0);
			TranslogStreams.writeTranslogOperation(out, operation);
			out.flush();

			int size = out.size();
			out.seek(0);
			out.writeInt(size - 4);

			Location location = current.add(out.underlyingBytes(), 0, size);
			if (syncOnEachOperation) {
				current.sync();
			}
			FsTranslogFile trans = this.trans;
			if (trans != null) {
				try {
					location = trans.add(out.underlyingBytes(), 0, size);
				} catch (ClosedChannelException e) {

				}
			}
			return location;
		} catch (Exception e) {
			throw new TranslogException(shardId, "Failed to write operation [" + operation + "]", e);
		} finally {
			rwl.readLock().unlock();
			CachedStreamOutput.pushEntry(cachedEntry);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog#snapshot()
	 */
	@Override
	public FsChannelSnapshot snapshot() throws TranslogException {
		while (true) {
			FsChannelSnapshot snapshot = current.snapshot();
			if (snapshot != null) {
				return snapshot;
			}
			Thread.yield();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog#snapshot(cn.com.rebirth.search.core.index.translog.Translog.Snapshot)
	 */
	@Override
	public Snapshot snapshot(Snapshot snapshot) {
		FsChannelSnapshot snap = snapshot();
		if (snap.translogId() == snapshot.translogId()) {
			snap.seekForward(snapshot.position());
		}
		return snap;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog#sync()
	 */
	@Override
	public void sync() {
		FsTranslogFile current1 = this.current;
		if (current1 == null) {
			return;
		}
		current1.sync();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog#syncNeeded()
	 */
	@Override
	public boolean syncNeeded() {
		FsTranslogFile current1 = this.current;
		return current1 != null && current1.syncNeeded();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.translog.Translog#syncOnEachOperation(boolean)
	 */
	@Override
	public void syncOnEachOperation(boolean syncOnEachOperation) {
		this.syncOnEachOperation = syncOnEachOperation;
		if (syncOnEachOperation) {
			type = FsTranslogFile.Type.SIMPLE;
		} else {
			type = FsTranslogFile.Type.BUFFERED;
		}
	}
}
