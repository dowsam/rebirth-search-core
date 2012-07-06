/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core WeightedFilterCache.java 2012-7-6 14:30:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.filter.weighted;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;

import cn.com.rebirth.commons.concurrent.ConcurrentCollections;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.docset.DocSet;
import cn.com.rebirth.search.commons.lucene.search.NoCacheFilter;
import cn.com.rebirth.search.commons.metrics.CounterMetric;
import cn.com.rebirth.search.commons.metrics.MeanMetric;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.cache.filter.FilterCache;
import cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter;
import cn.com.rebirth.search.core.index.cache.filter.support.FilterCacheValue;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.indices.cache.filter.IndicesFilterCache;

import com.google.common.cache.Cache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.cache.Weigher;

/**
 * The Class WeightedFilterCache.
 *
 * @author l.xue.nong
 */
public class WeightedFilterCache extends AbstractIndexComponent implements FilterCache,
		IndexReader.ReaderFinishedListener,
		RemovalListener<WeightedFilterCache.FilterCacheKey, FilterCacheValue<DocSet>> {

	/** The indices filter cache. */
	final IndicesFilterCache indicesFilterCache;

	/** The seen readers. */
	final ConcurrentMap<Object, Boolean> seenReaders = ConcurrentCollections.newConcurrentMap();

	/** The seen readers count. */
	final CounterMetric seenReadersCount = new CounterMetric();

	/** The evictions metric. */
	final CounterMetric evictionsMetric = new CounterMetric();

	/** The total metric. */
	final MeanMetric totalMetric = new MeanMetric();

	/**
	 * Instantiates a new weighted filter cache.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param indicesFilterCache the indices filter cache
	 */
	@Inject
	public WeightedFilterCache(Index index, @IndexSettings Settings indexSettings, IndicesFilterCache indicesFilterCache) {
		super(index, indexSettings);
		this.indicesFilterCache = indicesFilterCache;
		indicesFilterCache.addRemovalListener(index.name(), this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.filter.FilterCache#type()
	 */
	@Override
	public String type() {
		return "weighted";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.CloseableComponent#close()
	 */
	@Override
	public void close() throws RebirthException {
		clear();
		indicesFilterCache.removeRemovalListener(index.name());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.filter.FilterCache#clear()
	 */
	@Override
	public void clear() {
		for (Object readerKey : seenReaders.keySet()) {
			Boolean removed = seenReaders.remove(readerKey);
			if (removed == null) {
				return;
			}
			seenReadersCount.dec();
			for (FilterCacheKey key : indicesFilterCache.cache().asMap().keySet()) {
				if (key.readerKey() == readerKey) {

					indicesFilterCache.cache().invalidate(key);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.index.IndexReader.ReaderFinishedListener#finished(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public void finished(IndexReader reader) {
		clear(reader);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.filter.FilterCache#clear(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public void clear(IndexReader reader) {

		Boolean removed = seenReaders.remove(reader.getCoreCacheKey());
		if (removed == null) {
			return;
		}
		seenReadersCount.dec();
		Cache<FilterCacheKey, FilterCacheValue<DocSet>> cache = indicesFilterCache.cache();
		for (FilterCacheKey key : cache.asMap().keySet()) {
			if (key.readerKey() == reader.getCoreCacheKey()) {

				cache.invalidate(key);
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.filter.FilterCache#entriesStats()
	 */
	@Override
	public EntriesStats entriesStats() {
		long seenReadersCount = this.seenReadersCount.count();
		return new EntriesStats(totalMetric.sum(), seenReadersCount == 0 ? 0 : totalMetric.count() / seenReadersCount);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.filter.FilterCache#evictions()
	 */
	@Override
	public long evictions() {
		return evictionsMetric.count();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.filter.FilterCache#cache(org.apache.lucene.search.Filter)
	 */
	@Override
	public Filter cache(Filter filterToCache) {
		if (filterToCache instanceof NoCacheFilter) {
			return filterToCache;
		}
		if (isCached(filterToCache)) {
			return filterToCache;
		}
		return new FilterCacheFilterWrapper(filterToCache, this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.filter.FilterCache#isCached(org.apache.lucene.search.Filter)
	 */
	@Override
	public boolean isCached(Filter filter) {
		return filter instanceof FilterCacheFilterWrapper;
	}

	/**
	 * The Class FilterCacheFilterWrapper.
	 *
	 * @author l.xue.nong
	 */
	static class FilterCacheFilterWrapper extends Filter {

		/** The filter. */
		private final Filter filter;

		/** The cache. */
		private final WeightedFilterCache cache;

		/**
		 * Instantiates a new filter cache filter wrapper.
		 *
		 * @param filter the filter
		 * @param cache the cache
		 */
		FilterCacheFilterWrapper(Filter filter, WeightedFilterCache cache) {
			this.filter = filter;
			this.cache = cache;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Filter#getDocIdSet(org.apache.lucene.index.IndexReader)
		 */
		@Override
		public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
			Object filterKey = filter;
			if (filter instanceof CacheKeyFilter) {
				filterKey = ((CacheKeyFilter) filter).cacheKey();
			}
			FilterCacheKey cacheKey = new FilterCacheKey(cache.index().name(), reader.getCoreCacheKey(), filterKey);
			Cache<FilterCacheKey, FilterCacheValue<DocSet>> innerCache = cache.indicesFilterCache.cache();

			FilterCacheValue<DocSet> cacheValue = innerCache.getIfPresent(cacheKey);
			if (cacheValue == null) {
				if (!cache.seenReaders.containsKey(reader.getCoreCacheKey())) {
					Boolean previous = cache.seenReaders.putIfAbsent(reader.getCoreCacheKey(), Boolean.TRUE);
					if (previous == null) {
						reader.addReaderFinishedListener(cache);
						cache.seenReadersCount.inc();
					}
				}

				DocIdSet docIdSet = filter.getDocIdSet(reader);
				DocSet docSet = FilterCacheValue.cacheable(reader, docIdSet);
				cacheValue = new FilterCacheValue<DocSet>(docSet);

				cache.totalMetric.inc(cacheValue.value().sizeInBytes());
				innerCache.put(cacheKey, cacheValue);
			}

			return cacheValue.value() == DocSet.EMPTY_DOC_SET ? null : cacheValue.value();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "cache(" + filter + ")";
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object o) {
			if (!(o instanceof FilterCacheFilterWrapper))
				return false;
			return this.filter.equals(((FilterCacheFilterWrapper) o).filter);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return filter.hashCode() ^ 0x1117BF25;
		}
	}

	/**
	 * The Class FilterCacheValueWeigher.
	 *
	 * @author l.xue.nong
	 */
	public static class FilterCacheValueWeigher implements
			Weigher<WeightedFilterCache.FilterCacheKey, FilterCacheValue<DocSet>> {

		/* (non-Javadoc)
		 * @see com.google.common.cache.Weigher#weigh(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int weigh(FilterCacheKey key, FilterCacheValue<DocSet> value) {
			int weight = (int) Math.min(value.value().sizeInBytes(), Integer.MAX_VALUE);
			return weight == 0 ? 1 : weight;
		}
	}

	/* (non-Javadoc)
	 * @see com.google.common.cache.RemovalListener#onRemoval(com.google.common.cache.RemovalNotification)
	 */
	@Override
	public void onRemoval(RemovalNotification<FilterCacheKey, FilterCacheValue<DocSet>> removalNotification) {
		if (removalNotification.wasEvicted()) {
			evictionsMetric.inc();
		}
		if (removalNotification.getValue() != null) {
			totalMetric.dec(removalNotification.getValue().value().sizeInBytes());
		}
	}

	/**
	 * The Class FilterCacheKey.
	 *
	 * @author l.xue.nong
	 */
	public static class FilterCacheKey {

		/** The index. */
		private final String index;

		/** The reader key. */
		private final Object readerKey;

		/** The filter key. */
		private final Object filterKey;

		/**
		 * Instantiates a new filter cache key.
		 *
		 * @param index the index
		 * @param readerKey the reader key
		 * @param filterKey the filter key
		 */
		public FilterCacheKey(String index, Object readerKey, Object filterKey) {
			this.index = index;
			this.readerKey = readerKey;
			this.filterKey = filterKey;
		}

		/**
		 * Index.
		 *
		 * @return the string
		 */
		public String index() {
			return index;
		}

		/**
		 * Reader key.
		 *
		 * @return the object
		 */
		public Object readerKey() {
			return readerKey;
		}

		/**
		 * Filter key.
		 *
		 * @return the object
		 */
		public Object filterKey() {
			return filterKey;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;

			FilterCacheKey that = (FilterCacheKey) o;
			return (readerKey == that.readerKey && filterKey.equals(that.filterKey));
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return readerKey.hashCode() + 31 * filterKey.hashCode();
		}
	}
}