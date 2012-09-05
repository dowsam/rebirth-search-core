/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SimpleBloomCache.java 2012-7-6 14:29:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.bloom.simple;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.util.StringHelper;
import org.apache.lucene.util.UnicodeUtil;

import cn.com.rebirth.commons.Unicode;
import cn.com.rebirth.commons.concurrent.ConcurrentCollections;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.SizeUnit;
import cn.com.rebirth.commons.unit.SizeValue;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.commons.bloom.BloomFilter;
import cn.com.rebirth.search.commons.bloom.BloomFilterFactory;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.cache.bloom.BloomCache;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class SimpleBloomCache.
 *
 * @author l.xue.nong
 */
public class SimpleBloomCache extends AbstractIndexComponent implements BloomCache, IndexReader.ReaderFinishedListener {

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The max size. */
	private final long maxSize;

	/** The cache. */
	private final ConcurrentMap<Object, ConcurrentMap<String, BloomFilterEntry>> cache;

	/** The creation mutex. */
	private final Object creationMutex = new Object();

	/**
	 * Instantiates a new simple bloom cache.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param threadPool the thread pool
	 */
	@Inject
	public SimpleBloomCache(Index index, @IndexSettings Settings indexSettings, ThreadPool threadPool) {
		super(index, indexSettings);
		this.threadPool = threadPool;

		this.maxSize = indexSettings.getAsSize("index.cache.bloom.max_size", new SizeValue(500, SizeUnit.MEGA))
				.singles();
		this.cache = ConcurrentCollections.newConcurrentMap();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.CloseableComponent#close()
	 */
	@Override
	public void close() throws RebirthException {
		clear();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.bloom.BloomCache#clear()
	 */
	@Override
	public void clear() {
		cache.clear();
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.index.IndexReader.ReaderFinishedListener#finished(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public void finished(IndexReader reader) {
		clear(reader);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.bloom.BloomCache#clear(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public void clear(IndexReader reader) {
		ConcurrentMap<String, BloomFilterEntry> map = cache.remove(reader.getCoreCacheKey());

		if (map != null) {
			map.clear();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.bloom.BloomCache#sizeInBytes()
	 */
	@Override
	public long sizeInBytes() {

		long sizeInBytes = 0;
		for (ConcurrentMap<String, BloomFilterEntry> map : cache.values()) {
			for (BloomFilterEntry filter : map.values()) {
				sizeInBytes += filter.filter.sizeInBytes();
			}
		}
		return sizeInBytes;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.bloom.BloomCache#sizeInBytes(java.lang.String)
	 */
	@Override
	public long sizeInBytes(String fieldName) {
		long sizeInBytes = 0;
		for (ConcurrentMap<String, BloomFilterEntry> map : cache.values()) {
			BloomFilterEntry filter = map.get(fieldName);
			if (filter != null) {
				sizeInBytes += filter.filter.sizeInBytes();
			}
		}
		return sizeInBytes;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.bloom.BloomCache#filter(org.apache.lucene.index.IndexReader, java.lang.String, boolean)
	 */
	@Override
	public BloomFilter filter(IndexReader reader, String fieldName, boolean asyncLoad) {
		int currentNumDocs = reader.numDocs();
		if (currentNumDocs == 0) {
			return BloomFilter.EMPTY;
		}
		ConcurrentMap<String, BloomFilterEntry> fieldCache = cache.get(reader.getCoreCacheKey());
		if (fieldCache == null) {
			synchronized (creationMutex) {
				fieldCache = cache.get(reader.getCoreCacheKey());
				if (fieldCache == null) {
					reader.addReaderFinishedListener(this);
					fieldCache = ConcurrentCollections.newConcurrentMap();
					cache.put(reader.getCoreCacheKey(), fieldCache);
				}
			}
		}
		BloomFilterEntry filter = fieldCache.get(fieldName);
		if (filter == null) {
			synchronized (fieldCache) {
				filter = fieldCache.get(fieldName);
				if (filter == null) {
					filter = new BloomFilterEntry(currentNumDocs, BloomFilter.NONE);
					fieldCache.put(fieldName, filter);

					if (currentNumDocs < maxSize) {
						filter.loading.set(true);
						BloomFilterLoader loader = new BloomFilterLoader(reader, fieldName);
						if (asyncLoad) {
							threadPool.executor(ThreadPool.Names.CACHE).execute(loader);
						} else {
							loader.run();
							filter = fieldCache.get(fieldName);
						}
					}
				}
			}
		}

		if (filter.numDocs > 1000 && filter.numDocs < maxSize && (currentNumDocs / filter.numDocs) < 0.6) {
			if (filter.loading.compareAndSet(false, true)) {

				BloomFilterLoader loader = new BloomFilterLoader(reader, fieldName);
				if (asyncLoad) {
					threadPool.executor(ThreadPool.Names.CACHE).execute(loader);
				} else {
					loader.run();
					filter = fieldCache.get(fieldName);
				}
			}
		}
		return filter.filter;
	}

	/**
	 * The Class BloomFilterLoader.
	 *
	 * @author l.xue.nong
	 */
	class BloomFilterLoader implements Runnable {

		/** The reader. */
		private final IndexReader reader;

		/** The field. */
		private final String field;

		/**
		 * Instantiates a new bloom filter loader.
		 *
		 * @param reader the reader
		 * @param field the field
		 */
		BloomFilterLoader(IndexReader reader, String field) {
			this.reader = reader;
			this.field = StringHelper.intern(field);
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@SuppressWarnings({ "StringEquality" })
		@Override
		public void run() {
			TermDocs termDocs = null;
			TermEnum termEnum = null;
			try {
				BloomFilter filter = BloomFilterFactory.getFilter(reader.numDocs(), 15);
				termDocs = reader.termDocs();
				termEnum = reader.terms(new Term(field));
				do {
					Term term = termEnum.term();
					if (term == null || term.field() != field)
						break;

					UnicodeUtil.UTF8Result utf8Result = Unicode.fromStringAsUtf8(term.text());
					termDocs.seek(termEnum);
					while (termDocs.next()) {

						if (!reader.isDeleted(termDocs.doc())) {
							filter.add(utf8Result.result, 0, utf8Result.length);
						}
					}
				} while (termEnum.next());
				ConcurrentMap<String, BloomFilterEntry> fieldCache = cache.get(reader.getCoreCacheKey());
				if (fieldCache != null) {
					if (fieldCache.containsKey(field)) {
						BloomFilterEntry filterEntry = new BloomFilterEntry(reader.numDocs(), filter);
						filterEntry.loading.set(false);
						fieldCache.put(field, filterEntry);
					}
				}
			} catch (AlreadyClosedException e) {

			} catch (ClosedChannelException e) {

			} catch (Exception e) {

				if (reader.getRefCount() > 0) {
					logger.warn("failed to load bloom filter for [{}]", e, field);
				}
			} finally {
				try {
					if (termDocs != null) {
						termDocs.close();
					}
				} catch (Exception e) {

				}
				try {
					if (termEnum != null) {
						termEnum.close();
					}
				} catch (Exception e) {

				}
			}
		}
	}

	/**
	 * The Class BloomFilterEntry.
	 *
	 * @author l.xue.nong
	 */
	static class BloomFilterEntry {

		/** The num docs. */
		final int numDocs;

		/** The filter. */
		final BloomFilter filter;

		/** The loading. */
		final AtomicBoolean loading = new AtomicBoolean();

		/**
		 * Instantiates a new bloom filter entry.
		 *
		 * @param numDocs the num docs
		 * @param filter the filter
		 */
		public BloomFilterEntry(int numDocs, BloomFilter filter) {
			this.numDocs = numDocs;
			this.filter = filter;
		}
	}
}