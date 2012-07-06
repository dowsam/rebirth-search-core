/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RobinEngine.java 2012-3-29 15:02:09 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.engine.robin;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.lucene.index.ExtendedIndexSearcher;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.util.UnicodeUtil;

import cn.com.rebirth.commons.Unicode;
import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeUnit;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.Preconditions;
import cn.com.rebirth.search.commons.bloom.BloomFilter;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.commons.lucene.manager.SearcherFactory;
import cn.com.rebirth.search.commons.lucene.manager.SearcherManager;
import cn.com.rebirth.search.commons.lucene.uid.UidField;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.index.VersionType;
import cn.com.rebirth.search.core.index.analysis.AnalysisService;
import cn.com.rebirth.search.core.index.cache.bloom.BloomCache;
import cn.com.rebirth.search.core.index.deletionpolicy.SnapshotDeletionPolicy;
import cn.com.rebirth.search.core.index.deletionpolicy.SnapshotIndexCommit;
import cn.com.rebirth.search.core.index.engine.CreateFailedEngineException;
import cn.com.rebirth.search.core.index.engine.DeleteByQueryFailedEngineException;
import cn.com.rebirth.search.core.index.engine.DeleteFailedEngineException;
import cn.com.rebirth.search.core.index.engine.DocumentAlreadyExistsException;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.engine.EngineAlreadyStartedException;
import cn.com.rebirth.search.core.index.engine.EngineClosedException;
import cn.com.rebirth.search.core.index.engine.EngineCreationFailureException;
import cn.com.rebirth.search.core.index.engine.EngineException;
import cn.com.rebirth.search.core.index.engine.FlushFailedEngineException;
import cn.com.rebirth.search.core.index.engine.FlushNotAllowedEngineException;
import cn.com.rebirth.search.core.index.engine.IndexFailedEngineException;
import cn.com.rebirth.search.core.index.engine.OptimizeFailedEngineException;
import cn.com.rebirth.search.core.index.engine.RecoveryEngineException;
import cn.com.rebirth.search.core.index.engine.RefreshFailedEngineException;
import cn.com.rebirth.search.core.index.engine.Segment;
import cn.com.rebirth.search.core.index.engine.SnapshotFailedEngineException;
import cn.com.rebirth.search.core.index.engine.VersionConflictEngineException;
import cn.com.rebirth.search.core.index.mapper.internal.UidFieldMapper;
import cn.com.rebirth.search.core.index.merge.policy.EnableMergePolicy;
import cn.com.rebirth.search.core.index.merge.policy.MergePolicyProvider;
import cn.com.rebirth.search.core.index.merge.scheduler.MergeSchedulerProvider;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.settings.IndexSettingsService;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.similarity.SimilarityService;
import cn.com.rebirth.search.core.index.store.Store;
import cn.com.rebirth.search.core.index.translog.Translog;
import cn.com.rebirth.search.core.index.translog.TranslogStreams;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

/**
 * The Class RobinEngine.
 *
 * @author l.xue.nong
 */
public class RobinEngine extends AbstractIndexShardComponent implements Engine {

	/** The indexing buffer size. */
	private volatile ByteSizeValue indexingBufferSize;

	/** The term index interval. */
	private volatile int termIndexInterval;

	/** The term index divisor. */
	private volatile int termIndexDivisor;

	/** The index concurrency. */
	private volatile int indexConcurrency;

	/** The rwl. */
	private final ReadWriteLock rwl = new ReentrantReadWriteLock();

	/** The optimize mutex. */
	private final AtomicBoolean optimizeMutex = new AtomicBoolean();

	/** The gc deletes in millis. */
	private long gcDeletesInMillis;

	/** The enable gc deletes. */
	private volatile boolean enableGcDeletes = true;

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The index settings service. */
	private final IndexSettingsService indexSettingsService;

	/** The store. */
	private final Store store;

	/** The deletion policy. */
	private final SnapshotDeletionPolicy deletionPolicy;

	/** The translog. */
	private final Translog translog;

	/** The merge policy provider. */
	private final MergePolicyProvider mergePolicyProvider;

	/** The merge scheduler. */
	private final MergeSchedulerProvider mergeScheduler;

	/** The analysis service. */
	private final AnalysisService analysisService;

	/** The similarity service. */
	private final SimilarityService similarityService;

	/** The bloom cache. */
	private final BloomCache bloomCache;

	/** The async load bloom filter. */
	private final boolean asyncLoadBloomFilter;

	/** The index writer. */
	private volatile IndexWriter indexWriter;

	/** The searcher factory. */
	private final SearcherFactory searcherFactory = new RobinSearchFactory();

	/** The searcher manager. */
	private volatile SearcherManager searcherManager;

	/** The closed. */
	private volatile boolean closed = false;

	/** The dirty. */
	private volatile boolean dirty = false;

	/** The possible merge needed. */
	private volatile boolean possibleMergeNeeded = false;

	/** The flush needed. */
	private volatile boolean flushNeeded = false;

	/** The disable flush counter. */
	private volatile int disableFlushCounter = 0;

	/** The flushing. */
	private final AtomicBoolean flushing = new AtomicBoolean();

	/** The version map. */
	private final ConcurrentMap<String, VersionValue> versionMap;

	/** The dirty locks. */
	private final Object[] dirtyLocks;

	/** The refresh mutex. */
	private final Object refreshMutex = new Object();

	/** The apply settings. */
	private final ApplySettings applySettings = new ApplySettings();

	/** The failed engine. */
	private Throwable failedEngine = null;

	/** The failed engine mutex. */
	private final Object failedEngineMutex = new Object();

	/** The failed engine listeners. */
	private final CopyOnWriteArrayList<FailedEngineListener> failedEngineListeners = new CopyOnWriteArrayList<FailedEngineListener>();

	/** The translog id generator. */
	private final AtomicLong translogIdGenerator = new AtomicLong();

	/** The last committed segment infos. */
	private SegmentInfos lastCommittedSegmentInfos;

	/**
	 * Instantiates a new robin engine.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param threadPool the thread pool
	 * @param indexSettingsService the index settings service
	 * @param store the store
	 * @param deletionPolicy the deletion policy
	 * @param translog the translog
	 * @param mergePolicyProvider the merge policy provider
	 * @param mergeScheduler the merge scheduler
	 * @param analysisService the analysis service
	 * @param similarityService the similarity service
	 * @param bloomCache the bloom cache
	 * @throws EngineException the engine exception
	 */
	@Inject
	public RobinEngine(ShardId shardId, @IndexSettings Settings indexSettings, ThreadPool threadPool,
			IndexSettingsService indexSettingsService, Store store, SnapshotDeletionPolicy deletionPolicy,
			Translog translog, MergePolicyProvider mergePolicyProvider, MergeSchedulerProvider mergeScheduler,
			AnalysisService analysisService, SimilarityService similarityService, BloomCache bloomCache)
			throws EngineException {
		super(shardId, indexSettings);
		Preconditions.checkNotNull(store, "Store must be provided to the engine");
		Preconditions.checkNotNull(deletionPolicy, "Snapshot deletion policy must be provided to the engine");
		Preconditions.checkNotNull(translog, "Translog must be provided to the engine");

		this.gcDeletesInMillis = indexSettings.getAsTime("index.gc_deletes", TimeValue.timeValueSeconds(60)).millis();
		this.indexingBufferSize = componentSettings.getAsBytesSize("index_buffer_size", new ByteSizeValue(64,
				ByteSizeUnit.MB));
		this.termIndexInterval = indexSettings.getAsInt("index.term_index_interval",
				IndexWriterConfig.DEFAULT_TERM_INDEX_INTERVAL);
		this.termIndexDivisor = indexSettings.getAsInt("index.term_index_divisor", 1);
		this.asyncLoadBloomFilter = componentSettings.getAsBoolean("async_load_bloom", true);

		this.threadPool = threadPool;
		this.indexSettingsService = indexSettingsService;
		this.store = store;
		this.deletionPolicy = deletionPolicy;
		this.translog = translog;
		this.mergePolicyProvider = mergePolicyProvider;
		this.mergeScheduler = mergeScheduler;
		this.analysisService = analysisService;
		this.similarityService = similarityService;
		this.bloomCache = bloomCache;

		this.indexConcurrency = indexSettings.getAsInt("index.index_concurrency",
				IndexWriterConfig.DEFAULT_MAX_THREAD_STATES);
		this.versionMap = new ConcurrentHashMap<String, VersionValue>();
		this.dirtyLocks = new Object[indexConcurrency * 10];
		for (int i = 0; i < dirtyLocks.length; i++) {
			dirtyLocks[i] = new Object();
		}

		this.indexSettingsService.addListener(applySettings);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#updateIndexingBufferSize(cn.com.summall.search.commons.unit.ByteSizeValue)
	 */
	@Override
	public void updateIndexingBufferSize(ByteSizeValue indexingBufferSize) {
		ByteSizeValue preValue = this.indexingBufferSize;
		rwl.readLock().lock();
		try {

			if (indexingBufferSize.mbFrac() > 2048.0) {
				this.indexingBufferSize = new ByteSizeValue(2048, ByteSizeUnit.MB);
			} else {
				this.indexingBufferSize = indexingBufferSize;
			}
			IndexWriter indexWriter = this.indexWriter;
			if (indexWriter != null) {
				indexWriter.getConfig().setRAMBufferSizeMB(this.indexingBufferSize.mbFrac());
			}
		} finally {
			rwl.readLock().unlock();
		}

		if (indexingBufferSize == Engine.INACTIVE_SHARD_INDEXING_BUFFER
				&& preValue != Engine.INACTIVE_SHARD_INDEXING_BUFFER) {
			try {
				flush(new Flush().full(true));
			} catch (Exception e) {
				logger.warn("failed to flush after setting shard to inactive", e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#addFailedEngineListener(cn.com.summall.search.core.index.engine.Engine.FailedEngineListener)
	 */
	@Override
	public void addFailedEngineListener(FailedEngineListener listener) {
		failedEngineListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#start()
	 */
	@Override
	public void start() throws EngineException {
		rwl.writeLock().lock();
		try {
			if (indexWriter != null) {
				throw new EngineAlreadyStartedException(shardId);
			}
			if (closed) {
				throw new EngineClosedException(shardId);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Starting engine");
			}
			try {
				this.indexWriter = createWriter();
			} catch (IOException e) {
				throw new EngineCreationFailureException(shardId, "Failed to create engine", e);
			}

			try {

				if (IndexReader.indexExists(store.directory())) {
					Map<String, String> commitUserData = IndexReader.getCommitUserData(store.directory());
					if (commitUserData.containsKey(Translog.TRANSLOG_ID_KEY)) {
						translogIdGenerator.set(Long.parseLong(commitUserData.get(Translog.TRANSLOG_ID_KEY)));
					} else {
						translogIdGenerator.set(System.currentTimeMillis());
						indexWriter.commit(MapBuilder.<String, String> newMapBuilder()
								.put(Translog.TRANSLOG_ID_KEY, Long.toString(translogIdGenerator.get())).map());
					}
				} else {
					translogIdGenerator.set(System.currentTimeMillis());
					indexWriter.commit(MapBuilder.<String, String> newMapBuilder()
							.put(Translog.TRANSLOG_ID_KEY, Long.toString(translogIdGenerator.get())).map());
				}
				translog.newTranslog(translogIdGenerator.get());
				this.searcherManager = buildSearchManager(indexWriter);
				SegmentInfos infos = new SegmentInfos();
				infos.read(store.directory());
				lastCommittedSegmentInfos = infos;
			} catch (IOException e) {
				try {
					indexWriter.rollback();
				} catch (IOException e1) {

				} finally {
					try {
						indexWriter.close();
					} catch (IOException e1) {

					}
				}
				throw new EngineCreationFailureException(shardId, "Failed to open reader on writer", e);
			}
		} finally {
			rwl.writeLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#defaultRefreshInterval()
	 */
	@Override
	public TimeValue defaultRefreshInterval() {
		return new TimeValue(1, TimeUnit.SECONDS);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#enableGcDeletes(boolean)
	 */
	@Override
	public void enableGcDeletes(boolean enableGcDeletes) {
		this.enableGcDeletes = enableGcDeletes;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#get(cn.com.summall.search.core.index.engine.Engine.Get)
	 */
	public GetResult get(Get get) throws EngineException {
		rwl.readLock().lock();
		try {
			if (get.realtime()) {
				VersionValue versionValue = versionMap.get(get.uid().text());
				if (versionValue != null) {
					if (versionValue.delete()) {
						return GetResult.NOT_EXISTS;
					}
					if (!get.loadSource()) {
						return new GetResult(true, versionValue.version(), null);
					}
					byte[] data = translog.read(versionValue.translogLocation());
					if (data != null) {
						try {
							Translog.Source source = TranslogStreams.readSource(data);
							return new GetResult(true, versionValue.version(), source);
						} catch (IOException e) {

						}
					}
				}
			}

			Searcher searcher = searcher();
			try {
				UnicodeUtil.UTF8Result utf8 = Unicode.fromStringAsUtf8(get.uid().text());
				for (IndexReader reader : searcher.searcher().subReaders()) {
					BloomFilter filter = bloomCache.filter(reader, UidFieldMapper.NAME, asyncLoadBloomFilter);

					if (!filter.isPresent(utf8.result, 0, utf8.length)) {
						continue;
					}
					UidField.DocIdAndVersion docIdAndVersion = UidField.loadDocIdAndVersion(reader, get.uid());
					if (docIdAndVersion != null && docIdAndVersion.docId != Lucene.NO_DOC) {
						return new GetResult(searcher, docIdAndVersion);
					}
				}
			} catch (Exception e) {
				searcher.release();

				throw new EngineException(shardId(), "failed to load document", e);
			}
			searcher.release();
			return GetResult.NOT_EXISTS;
		} finally {
			rwl.readLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#create(cn.com.summall.search.core.index.engine.Engine.Create)
	 */
	@Override
	public void create(Create create) throws EngineException {
		rwl.readLock().lock();
		try {
			IndexWriter writer = this.indexWriter;
			if (writer == null) {
				throw new EngineClosedException(shardId, failedEngine);
			}
			innerCreate(create, writer);
			dirty = true;
			possibleMergeNeeded = true;
			flushNeeded = true;
		} catch (IOException e) {
			throw new CreateFailedEngineException(shardId, create, e);
		} catch (OutOfMemoryError e) {
			failEngine(e);
			throw new CreateFailedEngineException(shardId, create, e);
		} catch (IllegalStateException e) {
			if (e.getMessage().contains("OutOfMemoryError")) {
				failEngine(e);
			}
			throw new CreateFailedEngineException(shardId, create, e);
		} finally {
			rwl.readLock().unlock();
		}
	}

	/**
	 * Inner create.
	 *
	 * @param create the create
	 * @param writer the writer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void innerCreate(Create create, IndexWriter writer) throws IOException {
		synchronized (dirtyLock(create.uid())) {
			UidField uidField = create.uidField();
			final long currentVersion;
			VersionValue versionValue = versionMap.get(create.uid().text());
			if (versionValue == null) {
				currentVersion = loadCurrentVersionFromIndex(create.uid());
			} else {
				if (enableGcDeletes && versionValue.delete()
						&& (threadPool.estimatedTimeInMillis() - versionValue.time()) > gcDeletesInMillis) {
					currentVersion = -1;
				} else {
					currentVersion = versionValue.version();
				}
			}

			long updatedVersion;
			if (create.origin() == Operation.Origin.PRIMARY) {
				if (create.versionType() == VersionType.INTERNAL) {
					long expectedVersion = create.version();
					if (expectedVersion != 0 && currentVersion != -2) {

						if (currentVersion == -1) {
							throw new VersionConflictEngineException(shardId, create.type(), create.id(), -1,
									expectedVersion);
						} else if (expectedVersion != currentVersion) {
							throw new VersionConflictEngineException(shardId, create.type(), create.id(),
									currentVersion, expectedVersion);
						}
					}
					updatedVersion = currentVersion < 0 ? 1 : currentVersion + 1;
				} else {

					if (currentVersion >= 0) {
						if (currentVersion >= create.version()) {
							throw new VersionConflictEngineException(shardId, create.type(), create.id(),
									currentVersion, create.version());
						}
					}
					updatedVersion = create.version();
				}
			} else {
				long expectedVersion = create.version();
				if (currentVersion != -2) {

					if (!(currentVersion == -1 && create.version() == 1)) {

						if (expectedVersion <= currentVersion) {
							if (create.origin() == Operation.Origin.RECOVERY) {
								return;
							} else {
								throw new VersionConflictEngineException(shardId, create.type(), create.id(),
										currentVersion, expectedVersion);
							}
						}
					}
				}

				updatedVersion = create.version();
			}

			if (versionValue != null) {
				if (!versionValue.delete()) {
					if (create.origin() == Operation.Origin.RECOVERY) {
						return;
					} else {
						throw new DocumentAlreadyExistsException(shardId, create.type(), create.id());
					}
				}
			} else if (currentVersion != -1) {

				if (create.origin() == Operation.Origin.RECOVERY) {
					return;
				} else {
					throw new DocumentAlreadyExistsException(shardId, create.type(), create.id());
				}
			}

			uidField.version(updatedVersion);
			create.version(updatedVersion);

			if (create.docs().size() > 1) {
				writer.addDocuments(create.docs(), create.analyzer());
			} else {
				writer.addDocument(create.docs().get(0), create.analyzer());
			}
			Translog.Location translogLocation = translog.add(new Translog.Create(create));

			versionMap.put(create.uid().text(),
					new VersionValue(updatedVersion, false, threadPool.estimatedTimeInMillis(), translogLocation));
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#index(cn.com.summall.search.core.index.engine.Engine.Index)
	 */
	@Override
	public void index(Index index) throws EngineException {
		rwl.readLock().lock();
		try {
			IndexWriter writer = this.indexWriter;
			if (writer == null) {
				throw new EngineClosedException(shardId, failedEngine);
			}

			innerIndex(index, writer);
			dirty = true;
			possibleMergeNeeded = true;
			flushNeeded = true;
		} catch (IOException e) {
			throw new IndexFailedEngineException(shardId, index, e);
		} catch (OutOfMemoryError e) {
			failEngine(e);
			throw new IndexFailedEngineException(shardId, index, e);
		} catch (IllegalStateException e) {
			if (e.getMessage().contains("OutOfMemoryError")) {
				failEngine(e);
			}
			throw new IndexFailedEngineException(shardId, index, e);
		} finally {
			rwl.readLock().unlock();
		}
	}

	/**
	 * Inner index.
	 *
	 * @param index the index
	 * @param writer the writer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void innerIndex(Index index, IndexWriter writer) throws IOException {
		synchronized (dirtyLock(index.uid())) {
			UidField uidField = index.uidField();
			final long currentVersion;
			VersionValue versionValue = versionMap.get(index.uid().text());
			if (versionValue == null) {
				currentVersion = loadCurrentVersionFromIndex(index.uid());
			} else {
				if (enableGcDeletes && versionValue.delete()
						&& (threadPool.estimatedTimeInMillis() - versionValue.time()) > gcDeletesInMillis) {
					currentVersion = -1;
				} else {
					currentVersion = versionValue.version();
				}
			}

			long updatedVersion;
			if (index.origin() == Operation.Origin.PRIMARY) {
				if (index.versionType() == VersionType.INTERNAL) {
					long expectedVersion = index.version();
					if (expectedVersion != 0 && currentVersion != -2) {

						if (currentVersion == -1) {
							throw new VersionConflictEngineException(shardId, index.type(), index.id(), -1,
									expectedVersion);
						} else if (expectedVersion != currentVersion) {
							throw new VersionConflictEngineException(shardId, index.type(), index.id(), currentVersion,
									expectedVersion);
						}
					}
					updatedVersion = currentVersion < 0 ? 1 : currentVersion + 1;
				} else {

					if (currentVersion >= 0) {
						if (currentVersion >= index.version()) {
							throw new VersionConflictEngineException(shardId, index.type(), index.id(), currentVersion,
									index.version());
						}
					}
					updatedVersion = index.version();
				}
			} else {
				long expectedVersion = index.version();
				if (currentVersion != -2) {

					if (!(currentVersion == -1 && index.version() == 1)) {

						if (expectedVersion <= currentVersion) {
							if (index.origin() == Operation.Origin.RECOVERY) {
								return;
							} else {
								throw new VersionConflictEngineException(shardId, index.type(), index.id(),
										currentVersion, expectedVersion);
							}
						}
					}
				}

				updatedVersion = index.version();
			}

			uidField.version(updatedVersion);
			index.version(updatedVersion);

			if (currentVersion == -1) {

				if (index.docs().size() > 1) {
					writer.addDocuments(index.docs(), index.analyzer());
				} else {
					writer.addDocument(index.docs().get(0), index.analyzer());
				}
			} else {
				if (index.docs().size() > 1) {
					writer.updateDocuments(index.uid(), index.docs(), index.analyzer());
				} else {
					writer.updateDocument(index.uid(), index.docs().get(0), index.analyzer());
				}
			}
			Translog.Location translogLocation = translog.add(new Translog.Index(index));

			versionMap.put(index.uid().text(),
					new VersionValue(updatedVersion, false, threadPool.estimatedTimeInMillis(), translogLocation));
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#delete(cn.com.summall.search.core.index.engine.Engine.Delete)
	 */
	@Override
	public void delete(Delete delete) throws EngineException {
		rwl.readLock().lock();
		try {
			IndexWriter writer = this.indexWriter;
			if (writer == null) {
				throw new EngineClosedException(shardId, failedEngine);
			}
			innerDelete(delete, writer);
			dirty = true;
			possibleMergeNeeded = true;
			flushNeeded = true;
		} catch (IOException e) {
			throw new DeleteFailedEngineException(shardId, delete, e);
		} catch (OutOfMemoryError e) {
			failEngine(e);
			throw new DeleteFailedEngineException(shardId, delete, e);
		} catch (IllegalStateException e) {
			if (e.getMessage().contains("OutOfMemoryError")) {
				failEngine(e);
			}
			throw new DeleteFailedEngineException(shardId, delete, e);
		} finally {
			rwl.readLock().unlock();
		}
	}

	/**
	 * Inner delete.
	 *
	 * @param delete the delete
	 * @param writer the writer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void innerDelete(Delete delete, IndexWriter writer) throws IOException {
		synchronized (dirtyLock(delete.uid())) {
			final long currentVersion;
			VersionValue versionValue = versionMap.get(delete.uid().text());
			if (versionValue == null) {
				currentVersion = loadCurrentVersionFromIndex(delete.uid());
			} else {
				if (enableGcDeletes && versionValue.delete()
						&& (threadPool.estimatedTimeInMillis() - versionValue.time()) > gcDeletesInMillis) {
					currentVersion = -1;
				} else {
					currentVersion = versionValue.version();
				}
			}

			long updatedVersion;
			if (delete.origin() == Operation.Origin.PRIMARY) {
				if (delete.versionType() == VersionType.INTERNAL) {
					if (delete.version() != 0 && currentVersion != -2) {

						if (currentVersion == -1) {
							throw new VersionConflictEngineException(shardId, delete.type(), delete.id(), -1,
									delete.version());
						} else if (delete.version() != currentVersion) {
							throw new VersionConflictEngineException(shardId, delete.type(), delete.id(),
									currentVersion, delete.version());
						}
					}
					updatedVersion = currentVersion < 0 ? 1 : currentVersion + 1;
				} else {
					if (currentVersion == -1) {

					} else if (currentVersion >= delete.version()) {
						throw new VersionConflictEngineException(shardId, delete.type(), delete.id(), currentVersion,
								delete.version());
					}
					updatedVersion = delete.version();
				}
			} else {

				if (currentVersion != -2) {

					if (currentVersion != -1) {

						if (delete.version() <= currentVersion) {
							if (delete.origin() == Operation.Origin.RECOVERY) {
								return;
							} else {
								throw new VersionConflictEngineException(shardId, delete.type(), delete.id(),
										currentVersion - 1, delete.version());
							}
						}
					}
				}

				updatedVersion = delete.version();
			}

			if (currentVersion == -1) {

				delete.version(updatedVersion).notFound(true);
				Translog.Location translogLocation = translog.add(new Translog.Delete(delete));
				versionMap.put(delete.uid().text(),
						new VersionValue(updatedVersion, true, threadPool.estimatedTimeInMillis(), translogLocation));
			} else if (versionValue != null && versionValue.delete()) {

				delete.version(updatedVersion).notFound(true);
				Translog.Location translogLocation = translog.add(new Translog.Delete(delete));
				versionMap.put(delete.uid().text(),
						new VersionValue(updatedVersion, true, threadPool.estimatedTimeInMillis(), translogLocation));
			} else {
				delete.version(updatedVersion);
				writer.deleteDocuments(delete.uid());
				Translog.Location translogLocation = translog.add(new Translog.Delete(delete));
				versionMap.put(delete.uid().text(),
						new VersionValue(updatedVersion, true, threadPool.estimatedTimeInMillis(), translogLocation));
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#delete(cn.com.summall.search.core.index.engine.Engine.DeleteByQuery)
	 */
	@Override
	public void delete(DeleteByQuery delete) throws EngineException {
		rwl.readLock().lock();
		try {
			IndexWriter writer = this.indexWriter;
			if (writer == null) {
				throw new EngineClosedException(shardId);
			}
			Query query;
			if (delete.aliasFilter() == null) {
				query = delete.query();
			} else {
				query = new FilteredQuery(delete.query(), delete.aliasFilter());
			}
			writer.deleteDocuments(query);
			translog.add(new Translog.DeleteByQuery(delete));
			dirty = true;
			possibleMergeNeeded = true;
			flushNeeded = true;
		} catch (IOException e) {
			throw new DeleteByQueryFailedEngineException(shardId, delete, e);
		} finally {
			rwl.readLock().unlock();
		}

		refreshVersioningTable(System.currentTimeMillis());
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#searcher()
	 */
	@Override
	public Searcher searcher() throws EngineException {
		SearcherManager manager = this.searcherManager;
		IndexSearcher searcher = manager.acquire();
		return new RobinSearchResult(searcher, manager);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#refreshNeeded()
	 */
	@Override
	public boolean refreshNeeded() {
		return dirty;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#possibleMergeNeeded()
	 */
	@Override
	public boolean possibleMergeNeeded() {
		return this.possibleMergeNeeded;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#refresh(cn.com.summall.search.core.index.engine.Engine.Refresh)
	 */
	@Override
	public void refresh(Refresh refresh) throws EngineException {
		if (indexWriter == null) {
			throw new EngineClosedException(shardId);
		}

		rwl.readLock().lock();
		try {

			IndexWriter currentWriter = indexWriter;
			if (currentWriter == null) {
				throw new EngineClosedException(shardId, failedEngine);
			}
			try {

				synchronized (refreshMutex) {
					if (dirty || refresh.force()) {
						dirty = false;
						searcherManager.maybeRefresh();
					}
				}
			} catch (AlreadyClosedException e) {

			} catch (OutOfMemoryError e) {
				failEngine(e);
				throw new RefreshFailedEngineException(shardId, e);
			} catch (IllegalStateException e) {
				if (e.getMessage().contains("OutOfMemoryError")) {
					failEngine(e);
				}
				throw new RefreshFailedEngineException(shardId, e);
			} catch (Exception e) {
				if (indexWriter == null) {
					throw new EngineClosedException(shardId, failedEngine);
				} else if (currentWriter != indexWriter) {

				} else {
					throw new RefreshFailedEngineException(shardId, e);
				}
			}
		} finally {
			rwl.readLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#flush(cn.com.summall.search.core.index.engine.Engine.Flush)
	 */
	@Override
	public void flush(Flush flush) throws EngineException {
		if (indexWriter == null) {
			throw new EngineClosedException(shardId, failedEngine);
		}

		if (disableFlushCounter > 0) {
			throw new FlushNotAllowedEngineException(shardId, "Recovery is in progress, flush is not allowed");
		}

		if (!flushing.compareAndSet(false, true)) {
			throw new FlushNotAllowedEngineException(shardId, "Already flushing...");
		}

		try {
			boolean makeTransientCurrent = false;
			if (flush.full()) {
				rwl.writeLock().lock();
				try {
					if (indexWriter == null) {
						throw new EngineClosedException(shardId, failedEngine);
					}
					if (disableFlushCounter > 0) {
						throw new FlushNotAllowedEngineException(shardId,
								"Recovery is in progress, flush is not allowed");
					}

					dirty = false;
					try {

						indexWriter.close(false);
						indexWriter = createWriter();

						if (flushNeeded || flush.force()) {
							flushNeeded = false;
							long translogId = translogIdGenerator.incrementAndGet();
							indexWriter.commit(MapBuilder.<String, String> newMapBuilder()
									.put(Translog.TRANSLOG_ID_KEY, Long.toString(translogId)).map());
							translog.newTranslog(translogId);
						}

						SearcherManager current = this.searcherManager;
						this.searcherManager = buildSearchManager(indexWriter);
						current.close();

						refreshVersioningTable(threadPool.estimatedTimeInMillis());
					} catch (OutOfMemoryError e) {
						failEngine(e);
						throw new FlushFailedEngineException(shardId, e);
					} catch (IllegalStateException e) {
						if (e.getMessage().contains("OutOfMemoryError")) {
							failEngine(e);
						}
						throw new FlushFailedEngineException(shardId, e);
					} catch (Exception e) {
						throw new FlushFailedEngineException(shardId, e);
					}
				} finally {
					rwl.writeLock().unlock();
				}
			} else {
				rwl.readLock().lock();
				try {
					if (indexWriter == null) {
						throw new EngineClosedException(shardId, failedEngine);
					}
					if (disableFlushCounter > 0) {
						throw new FlushNotAllowedEngineException(shardId,
								"Recovery is in progress, flush is not allowed");
					}

					if (flushNeeded || flush.force()) {
						flushNeeded = false;
						try {
							long translogId = translogIdGenerator.incrementAndGet();
							translog.newTransientTranslog(translogId);
							indexWriter.commit(MapBuilder.<String, String> newMapBuilder()
									.put(Translog.TRANSLOG_ID_KEY, Long.toString(translogId)).map());
							if (flush.force()) {

								Map<String, String> commitUserData = IndexReader.getCommitUserData(store.directory());
								long committedTranslogId = Long.parseLong(commitUserData.get(Translog.TRANSLOG_ID_KEY));
								if (committedTranslogId != translogId) {

									translog.revertTransient();
								} else {
									makeTransientCurrent = true;
								}
							} else {
								makeTransientCurrent = true;
							}
							if (makeTransientCurrent) {
								refreshVersioningTable(threadPool.estimatedTimeInMillis());

								translog.makeTransientCurrent();
							}
						} catch (OutOfMemoryError e) {
							translog.revertTransient();
							failEngine(e);
							throw new FlushFailedEngineException(shardId, e);
						} catch (IllegalStateException e) {
							if (e.getMessage().contains("OutOfMemoryError")) {
								failEngine(e);
							}
							throw new FlushFailedEngineException(shardId, e);
						} catch (Exception e) {
							translog.revertTransient();
							throw new FlushFailedEngineException(shardId, e);
						}
					}
				} finally {
					rwl.readLock().unlock();
				}
			}
			try {
				SegmentInfos infos = new SegmentInfos();
				infos.read(store.directory());
				lastCommittedSegmentInfos = infos;
			} catch (Exception e) {
				if (!closed) {
					logger.warn("failed to read latest segment infos on flush", e);
				}
			}
		} finally {
			flushing.set(false);
		}
	}

	/**
	 * Refresh versioning table.
	 *
	 * @param time the time
	 */
	private void refreshVersioningTable(long time) {

		refresh(new Refresh(true).force(true));
		for (Map.Entry<String, VersionValue> entry : versionMap.entrySet()) {
			String id = entry.getKey();
			synchronized (dirtyLock(id)) {
				VersionValue versionValue = versionMap.get(id);
				if (versionValue == null) {
					continue;
				}
				if (time - versionValue.time() <= 0) {
					continue;
				}
				if (versionValue.delete()) {
					if (enableGcDeletes && (time - versionValue.time()) > gcDeletesInMillis) {
						versionMap.remove(id);
					}
				} else {
					versionMap.remove(id);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#maybeMerge()
	 */
	@Override
	public void maybeMerge() throws EngineException {
		if (!possibleMergeNeeded) {
			return;
		}
		possibleMergeNeeded = false;
		rwl.readLock().lock();
		try {
			if (indexWriter == null) {
				throw new EngineClosedException(shardId, failedEngine);
			}
			if (indexWriter.getConfig().getMergePolicy() instanceof EnableMergePolicy) {
				((EnableMergePolicy) indexWriter.getConfig().getMergePolicy()).enableMerge();
			}
			indexWriter.maybeMerge();
		} catch (OutOfMemoryError e) {
			failEngine(e);
			throw new OptimizeFailedEngineException(shardId, e);
		} catch (IllegalStateException e) {
			if (e.getMessage().contains("OutOfMemoryError")) {
				failEngine(e);
			}
			throw new OptimizeFailedEngineException(shardId, e);
		} catch (Exception e) {
			throw new OptimizeFailedEngineException(shardId, e);
		} finally {
			rwl.readLock().unlock();
			if (indexWriter != null && indexWriter.getConfig().getMergePolicy() instanceof EnableMergePolicy) {
				((EnableMergePolicy) indexWriter.getConfig().getMergePolicy()).disableMerge();
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#optimize(cn.com.summall.search.core.index.engine.Engine.Optimize)
	 */
	@Override
	public void optimize(Optimize optimize) throws EngineException {
		if (optimize.flush()) {
			flush(new Flush().force(true));
		}
		if (optimizeMutex.compareAndSet(false, true)) {
			rwl.readLock().lock();
			try {
				if (indexWriter == null) {
					throw new EngineClosedException(shardId, failedEngine);
				}
				if (indexWriter.getConfig().getMergePolicy() instanceof EnableMergePolicy) {
					((EnableMergePolicy) indexWriter.getConfig().getMergePolicy()).enableMerge();
				}
				if (optimize.onlyExpungeDeletes()) {
					indexWriter.expungeDeletes(false);
				} else if (optimize.maxNumSegments() <= 0) {
					indexWriter.maybeMerge();
					possibleMergeNeeded = false;
				} else {
					indexWriter.forceMerge(optimize.maxNumSegments(), false);
				}
			} catch (OutOfMemoryError e) {
				failEngine(e);
				throw new OptimizeFailedEngineException(shardId, e);
			} catch (IllegalStateException e) {
				if (e.getMessage().contains("OutOfMemoryError")) {
					failEngine(e);
				}
				throw new OptimizeFailedEngineException(shardId, e);
			} catch (Exception e) {
				throw new OptimizeFailedEngineException(shardId, e);
			} finally {
				rwl.readLock().unlock();
				if (indexWriter != null && indexWriter.getConfig().getMergePolicy() instanceof EnableMergePolicy) {
					((EnableMergePolicy) indexWriter.getConfig().getMergePolicy()).disableMerge();
				}
				optimizeMutex.set(false);
			}
		}

		if (optimize.waitForMerge()) {
			indexWriter.waitForMerges();
		}
		if (optimize.flush()) {
			flush(new Flush().force(true));
		}
		if (optimize.refresh()) {
			refresh(new Refresh(false).force(true));
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#snapshot(cn.com.summall.search.core.index.engine.Engine.SnapshotHandler)
	 */
	@Override
	public <T> T snapshot(SnapshotHandler<T> snapshotHandler) throws EngineException {
		SnapshotIndexCommit snapshotIndexCommit = null;
		Translog.Snapshot traslogSnapshot = null;
		rwl.readLock().lock();
		try {
			snapshotIndexCommit = deletionPolicy.snapshot();
			traslogSnapshot = translog.snapshot();
		} catch (Exception e) {
			if (snapshotIndexCommit != null)
				snapshotIndexCommit.release();
			throw new SnapshotFailedEngineException(shardId, e);
		} finally {
			rwl.readLock().unlock();
		}

		try {
			return snapshotHandler.snapshot(snapshotIndexCommit, traslogSnapshot);
		} finally {
			snapshotIndexCommit.release();
			traslogSnapshot.release();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#recover(cn.com.summall.search.core.index.engine.Engine.RecoveryHandler)
	 */
	@Override
	public void recover(RecoveryHandler recoveryHandler) throws EngineException {

		rwl.writeLock().lock();
		try {
			disableFlushCounter++;
		} finally {
			rwl.writeLock().unlock();
		}

		SnapshotIndexCommit phase1Snapshot;
		try {
			phase1Snapshot = deletionPolicy.snapshot();
		} catch (Exception e) {
			--disableFlushCounter;
			throw new RecoveryEngineException(shardId, 1, "Snapshot failed", e);
		}

		try {
			recoveryHandler.phase1(phase1Snapshot);
		} catch (Exception e) {
			--disableFlushCounter;
			phase1Snapshot.release();
			if (closed) {
				e = new EngineClosedException(shardId, e);
			}
			throw new RecoveryEngineException(shardId, 1, "Execution failed", e);
		}

		Translog.Snapshot phase2Snapshot;
		try {
			phase2Snapshot = translog.snapshot();
		} catch (Exception e) {
			--disableFlushCounter;
			phase1Snapshot.release();
			if (closed) {
				e = new EngineClosedException(shardId, e);
			}
			throw new RecoveryEngineException(shardId, 2, "Snapshot failed", e);
		}

		try {
			recoveryHandler.phase2(phase2Snapshot);
		} catch (Exception e) {
			--disableFlushCounter;
			phase1Snapshot.release();
			phase2Snapshot.release();
			if (closed) {
				e = new EngineClosedException(shardId, e);
			}
			throw new RecoveryEngineException(shardId, 2, "Execution failed", e);
		}

		rwl.writeLock().lock();
		Translog.Snapshot phase3Snapshot = null;
		try {
			phase3Snapshot = translog.snapshot(phase2Snapshot);
			recoveryHandler.phase3(phase3Snapshot);
		} catch (Exception e) {
			throw new RecoveryEngineException(shardId, 3, "Execution failed", e);
		} finally {
			--disableFlushCounter;
			rwl.writeLock().unlock();
			phase1Snapshot.release();
			phase2Snapshot.release();
			if (phase3Snapshot != null) {
				phase3Snapshot.release();
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.engine.Engine#segments()
	 */
	@Override
	public List<Segment> segments() {
		rwl.readLock().lock();
		try {
			IndexWriter indexWriter = this.indexWriter;
			if (indexWriter == null) {
				throw new EngineClosedException(shardId, failedEngine);
			}
			Map<String, Segment> segments = new HashMap<String, Segment>();

			Searcher searcher = searcher();
			try {
				IndexReader[] readers = searcher.reader().getSequentialSubReaders();
				for (IndexReader reader : readers) {
					assert reader instanceof SegmentReader;
					SegmentInfo info = Lucene.getSegmentInfo((SegmentReader) reader);
					assert !segments.containsKey(info.name);
					Segment segment = new Segment(info.name);
					segment.search = true;
					segment.docCount = reader.numDocs();
					segment.delDocCount = reader.numDeletedDocs();
					try {
						segment.sizeInBytes = info.sizeInBytes(true);
					} catch (IOException e) {
						logger.trace("failed to get size for [{}]", e, info.name);
					}
					segments.put(info.name, segment);
				}
			} finally {
				searcher.release();
			}

			if (lastCommittedSegmentInfos != null) {
				SegmentInfos infos = lastCommittedSegmentInfos;
				for (SegmentInfo info : infos) {
					Segment segment = segments.get(info.name);
					if (segment == null) {
						segment = new Segment(info.name);
						segment.search = false;
						segment.committed = true;
						segment.docCount = info.docCount;
						try {
							segment.delDocCount = indexWriter.numDeletedDocs(info);
						} catch (IOException e) {
							logger.trace("failed to get deleted docs for committed segment", e);
						}
						try {
							segment.sizeInBytes = info.sizeInBytes(true);
						} catch (IOException e) {
							logger.trace("failed to get size for [{}]", e, info.name);
						}
						segments.put(info.name, segment);
					} else {
						segment.committed = true;
					}
				}
			}

			Segment[] segmentsArr = segments.values().toArray(new Segment[segments.values().size()]);
			Arrays.sort(segmentsArr, new Comparator<Segment>() {
				@Override
				public int compare(Segment o1, Segment o2) {
					return (int) (o1.generation() - o2.generation());
				}
			});

			return Arrays.asList(segmentsArr);
		} finally {
			rwl.readLock().unlock();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.CloseableComponent#close()
	 */
	@Override
	public void close() throws RestartException {
		rwl.writeLock().lock();
		try {
			innerClose();
		} finally {
			rwl.writeLock().unlock();
		}
	}

	/**
	 * Fail engine.
	 *
	 * @param failure the failure
	 */
	private void failEngine(Throwable failure) {
		synchronized (failedEngineMutex) {
			if (failedEngine != null) {
				return;
			}
			logger.warn("failed engine", failure);
			failedEngine = failure;
			for (FailedEngineListener listener : failedEngineListeners) {
				listener.onFailedEngine(shardId, failure);
			}
			innerClose();
		}
	}

	/**
	 * Inner close.
	 */
	private void innerClose() {
		if (closed) {
			return;
		}
		indexSettingsService.removeListener(applySettings);
		closed = true;
		this.versionMap.clear();
		this.failedEngineListeners.clear();
		try {
			if (searcherManager != null) {
				searcherManager.close();
			}

			if (indexWriter != null) {
				try {
					indexWriter.rollback();
				} catch (AlreadyClosedException e) {

				}
			}
		} catch (Exception e) {
			logger.debug("failed to rollback writer on close", e);
		} finally {
			indexWriter = null;
		}
	}

	/**
	 * Dirty lock.
	 *
	 * @param id the id
	 * @return the object
	 */
	private Object dirtyLock(String id) {
		int hash = id.hashCode();

		if (hash == Integer.MIN_VALUE) {
			hash = 0;
		}
		return dirtyLocks[Math.abs(hash) % dirtyLocks.length];
	}

	/**
	 * Dirty lock.
	 *
	 * @param uid the uid
	 * @return the object
	 */
	private Object dirtyLock(Term uid) {
		return dirtyLock(uid.text());
	}

	/**
	 * Load current version from index.
	 *
	 * @param uid the uid
	 * @return the long
	 */
	private long loadCurrentVersionFromIndex(Term uid) {
		UnicodeUtil.UTF8Result utf8 = Unicode.fromStringAsUtf8(uid.text());
		Searcher searcher = searcher();
		try {
			for (IndexReader reader : searcher.searcher().subReaders()) {
				BloomFilter filter = bloomCache.filter(reader, UidFieldMapper.NAME, asyncLoadBloomFilter);

				if (!filter.isPresent(utf8.result, 0, utf8.length)) {
					continue;
				}
				long version = UidField.loadVersion(reader, uid);

				if (version != -1) {
					return version;
				}
			}
			return -1;
		} finally {
			searcher.release();
		}
	}

	/**
	 * Creates the writer.
	 *
	 * @return the index writer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private IndexWriter createWriter() throws IOException {
		IndexWriter indexWriter = null;
		try {

			if (IndexWriter.isLocked(store.directory())) {
				logger.warn("shard is locked, releasing lock");
				IndexWriter.unlock(store.directory());
			}
			boolean create = !IndexReader.indexExists(store.directory());
			IndexWriterConfig config = new IndexWriterConfig(Lucene.VERSION, analysisService.defaultIndexAnalyzer());
			config.setOpenMode(create ? IndexWriterConfig.OpenMode.CREATE : IndexWriterConfig.OpenMode.APPEND);
			config.setIndexDeletionPolicy(deletionPolicy);
			config.setMergeScheduler(mergeScheduler.newMergeScheduler());
			config.setMergePolicy(mergePolicyProvider.newMergePolicy());
			config.setSimilarity(similarityService.defaultIndexSimilarity());
			config.setRAMBufferSizeMB(indexingBufferSize.mbFrac());
			config.setTermIndexInterval(termIndexInterval);
			config.setReaderTermsIndexDivisor(termIndexDivisor);
			config.setMaxThreadStates(indexConcurrency);

			indexWriter = new IndexWriter(store.directory(), config);
		} catch (IOException e) {
			Lucene.safeClose(indexWriter);
			throw e;
		}
		return indexWriter;
	}

	static {
		IndexMetaData.addDynamicSettings("index.term_index_interval", "index.term_index_divisor",
				"index.index_concurrency", "index.gc_deletes");
	}

	/**
	 * The Class ApplySettings.
	 *
	 * @author l.xue.nong
	 */
	class ApplySettings implements IndexSettingsService.Listener {

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.settings.IndexSettingsService.Listener#onRefreshSettings(cn.com.summall.search.commons.settings.Settings)
		 */
		@Override
		public void onRefreshSettings(Settings settings) {
			long gcDeletesInMillis = indexSettings.getAsTime("index.gc_deletes",
					TimeValue.timeValueMillis(RobinEngine.this.gcDeletesInMillis)).millis();
			if (gcDeletesInMillis != RobinEngine.this.gcDeletesInMillis) {
				logger.info("updating index.gc_deletes from [{}] to [{}]",
						TimeValue.timeValueMillis(RobinEngine.this.gcDeletesInMillis),
						TimeValue.timeValueMillis(gcDeletesInMillis));
				RobinEngine.this.gcDeletesInMillis = gcDeletesInMillis;
			}

			int termIndexInterval = settings.getAsInt("index.term_index_interval", RobinEngine.this.termIndexInterval);
			int termIndexDivisor = settings.getAsInt("index.term_index_divisor", RobinEngine.this.termIndexDivisor);
			int indexConcurrency = settings.getAsInt("index.index_concurrency", RobinEngine.this.indexConcurrency);
			boolean requiresFlushing = false;
			if (termIndexInterval != RobinEngine.this.termIndexInterval
					|| termIndexDivisor != RobinEngine.this.termIndexDivisor) {
				rwl.readLock().lock();
				try {
					if (termIndexInterval != RobinEngine.this.termIndexInterval) {
						logger.info("updating index.term_index_interval from [{}] to [{}]",
								RobinEngine.this.termIndexInterval, termIndexInterval);
						RobinEngine.this.termIndexInterval = termIndexInterval;
						indexWriter.getConfig().setTermIndexInterval(termIndexInterval);
					}
					if (termIndexDivisor != RobinEngine.this.termIndexDivisor) {
						logger.info("updating index.term_index_divisor from [{}] to [{}]",
								RobinEngine.this.termIndexDivisor, termIndexDivisor);
						RobinEngine.this.termIndexDivisor = termIndexDivisor;
						indexWriter.getConfig().setReaderTermsIndexDivisor(termIndexDivisor);

						requiresFlushing = true;
					}
					if (indexConcurrency != RobinEngine.this.indexConcurrency) {
						logger.info("updating index.index_concurrency from [{}] to [{}]",
								RobinEngine.this.indexConcurrency, indexConcurrency);
						RobinEngine.this.indexConcurrency = indexConcurrency;

						requiresFlushing = true;
					}
				} finally {
					rwl.readLock().unlock();
				}
				if (requiresFlushing) {
					flush(new Flush().full(true));
				}
			}
		}
	}

	/**
	 * Builds the search manager.
	 *
	 * @param indexWriter the index writer
	 * @return the searcher manager
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private SearcherManager buildSearchManager(IndexWriter indexWriter) throws IOException {
		return new SearcherManager(indexWriter, true, searcherFactory);
	}

	/**
	 * The Class RobinSearchResult.
	 *
	 * @author l.xue.nong
	 */
	static class RobinSearchResult implements Searcher {

		/** The searcher. */
		private final IndexSearcher searcher;

		/** The manager. */
		private final SearcherManager manager;

		/**
		 * Instantiates a new robin search result.
		 *
		 * @param searcher the searcher
		 * @param manager the manager
		 */
		private RobinSearchResult(IndexSearcher searcher, SearcherManager manager) {
			this.searcher = searcher;
			this.manager = manager;
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.engine.Engine.Searcher#reader()
		 */
		@Override
		public IndexReader reader() {
			return searcher.getIndexReader();
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.engine.Engine.Searcher#searcher()
		 */
		@Override
		public ExtendedIndexSearcher searcher() {
			return (ExtendedIndexSearcher) searcher;
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.lease.Releasable#release()
		 */
		@Override
		public boolean release() throws RestartException {
			try {
				manager.release(searcher);
				return true;
			} catch (IOException e) {
				return false;
			}
		}
	}

	/**
	 * The Class VersionValue.
	 *
	 * @author l.xue.nong
	 */
	static class VersionValue {

		/** The version. */
		private final long version;

		/** The delete. */
		private final boolean delete;

		/** The time. */
		private final long time;

		/** The translog location. */
		private final Translog.Location translogLocation;

		/**
		 * Instantiates a new version value.
		 *
		 * @param version the version
		 * @param delete the delete
		 * @param time the time
		 * @param translogLocation the translog location
		 */
		VersionValue(long version, boolean delete, long time, Translog.Location translogLocation) {
			this.version = version;
			this.delete = delete;
			this.time = time;
			this.translogLocation = translogLocation;
		}

		/**
		 * Time.
		 *
		 * @return the long
		 */
		public long time() {
			return this.time;
		}

		/**
		 * Version.
		 *
		 * @return the long
		 */
		public long version() {
			return version;
		}

		/**
		 * Delete.
		 *
		 * @return true, if successful
		 */
		public boolean delete() {
			return delete;
		}

		/**
		 * Translog location.
		 *
		 * @return the translog. location
		 */
		public Translog.Location translogLocation() {
			return this.translogLocation;
		}
	}

	/**
	 * A factory for creating RobinSearch objects.
	 */
	class RobinSearchFactory extends SearcherFactory {

		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.lucene.manager.SearcherFactory#newSearcher(org.apache.lucene.index.IndexReader)
		 */
		@Override
		public IndexSearcher newSearcher(IndexReader reader) throws IOException {
			ExtendedIndexSearcher searcher = new ExtendedIndexSearcher(reader);
			searcher.setSimilarity(similarityService.defaultSearchSimilarity());
			return searcher;
		}
	}
}
