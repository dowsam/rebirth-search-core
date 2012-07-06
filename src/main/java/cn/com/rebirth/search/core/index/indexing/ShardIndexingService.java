/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardIndexingService.java 2012-3-29 15:02:45 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.indexing;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.metrics.CounterMetric;
import cn.com.rebirth.search.commons.metrics.MeanMetric;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;

import com.google.common.collect.ImmutableMap;


/**
 * The Class ShardIndexingService.
 *
 * @author l.xue.nong
 */
public class ShardIndexingService extends AbstractIndexShardComponent {

	
	/** The total stats. */
	private final StatsHolder totalStats = new StatsHolder();

	
	/** The types stats. */
	private volatile Map<String, StatsHolder> typesStats = ImmutableMap.of();

	
	/** The listeners. */
	private CopyOnWriteArrayList<IndexingOperationListener> listeners = null;

	
	/**
	 * Instantiates a new shard indexing service.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 */
	@Inject
	public ShardIndexingService(ShardId shardId, @IndexSettings Settings indexSettings) {
		super(shardId, indexSettings);
	}

	
	/**
	 * Stats.
	 *
	 * @param types the types
	 * @return the indexing stats
	 */
	public IndexingStats stats(String... types) {
		IndexingStats.Stats total = totalStats.stats();
		Map<String, IndexingStats.Stats> typesSt = null;
		if (types != null && types.length > 0) {
			if (types.length == 1 && types[0].equals("_all")) {
				typesSt = new HashMap<String, IndexingStats.Stats>(typesStats.size());
				for (Map.Entry<String, StatsHolder> entry : typesStats.entrySet()) {
					typesSt.put(entry.getKey(), entry.getValue().stats());
				}
			} else {
				typesSt = new HashMap<String, IndexingStats.Stats>(types.length);
				for (String type : types) {
					StatsHolder statsHolder = typesStats.get(type);
					if (statsHolder != null) {
						typesSt.put(type, statsHolder.stats());
					}
				}
			}
		}
		return new IndexingStats(total, typesSt);
	}

	
	/**
	 * Adds the listener.
	 *
	 * @param listener the listener
	 */
	public synchronized void addListener(IndexingOperationListener listener) {
		if (listeners == null) {
			listeners = new CopyOnWriteArrayList<IndexingOperationListener>();
		}
		listeners.add(listener);
	}

	
	/**
	 * Removes the listener.
	 *
	 * @param listener the listener
	 */
	public synchronized void removeListener(IndexingOperationListener listener) {
		if (listeners == null) {
			return;
		}
		listeners.remove(listener);
		if (listeners.isEmpty()) {
			listeners = null;
		}
	}

	
	/**
	 * Pre create.
	 *
	 * @param create the create
	 * @return the engine. create
	 */
	public Engine.Create preCreate(Engine.Create create) {
		if (listeners != null) {
			for (IndexingOperationListener listener : listeners) {
				create = listener.preCreate(create);
			}
		}
		return create;
	}

	
	/**
	 * Post create.
	 *
	 * @param create the create
	 */
	public void postCreate(Engine.Create create) {
		long took = create.endTime() - create.startTime();
		totalStats.indexMetric.inc(took);
		typeStats(create.type()).indexMetric.inc(took);
		if (listeners != null) {
			for (IndexingOperationListener listener : listeners) {
				listener.postCreate(create);
			}
		}
	}

	
	/**
	 * Pre index.
	 *
	 * @param index the index
	 * @return the engine. index
	 */
	public Engine.Index preIndex(Engine.Index index) {
		totalStats.indexCurrent.inc();
		typeStats(index.type()).indexCurrent.inc();
		if (listeners != null) {
			for (IndexingOperationListener listener : listeners) {
				index = listener.preIndex(index);
			}
		}
		return index;
	}

	
	/**
	 * Post index.
	 *
	 * @param index the index
	 */
	public void postIndex(Engine.Index index) {
		long took = index.endTime() - index.startTime();
		totalStats.indexMetric.inc(took);
		totalStats.indexCurrent.dec();
		StatsHolder typeStats = typeStats(index.type());
		typeStats.indexMetric.inc(took);
		typeStats.indexCurrent.dec();
		if (listeners != null) {
			for (IndexingOperationListener listener : listeners) {
				listener.postIndex(index);
			}
		}
	}

	
	/**
	 * Failed index.
	 *
	 * @param index the index
	 */
	public void failedIndex(Engine.Index index) {
		totalStats.indexCurrent.dec();
		typeStats(index.type()).indexCurrent.dec();
	}

	
	/**
	 * Pre delete.
	 *
	 * @param delete the delete
	 * @return the engine. delete
	 */
	public Engine.Delete preDelete(Engine.Delete delete) {
		totalStats.deleteCurrent.inc();
		typeStats(delete.type()).deleteCurrent.inc();
		if (listeners != null) {
			for (IndexingOperationListener listener : listeners) {
				delete = listener.preDelete(delete);
			}
		}
		return delete;
	}

	
	/**
	 * Post delete.
	 *
	 * @param delete the delete
	 */
	public void postDelete(Engine.Delete delete) {
		long took = delete.endTime() - delete.startTime();
		totalStats.deleteMetric.inc(took);
		totalStats.deleteCurrent.dec();
		StatsHolder typeStats = typeStats(delete.type());
		typeStats.deleteMetric.inc(took);
		typeStats.deleteCurrent.dec();
		if (listeners != null) {
			for (IndexingOperationListener listener : listeners) {
				listener.postDelete(delete);
			}
		}
	}

	
	/**
	 * Failed delete.
	 *
	 * @param delete the delete
	 */
	public void failedDelete(Engine.Delete delete) {
		totalStats.deleteCurrent.dec();
		typeStats(delete.type()).deleteCurrent.dec();
	}

	
	/**
	 * Pre delete by query.
	 *
	 * @param deleteByQuery the delete by query
	 * @return the engine. delete by query
	 */
	public Engine.DeleteByQuery preDeleteByQuery(Engine.DeleteByQuery deleteByQuery) {
		if (listeners != null) {
			for (IndexingOperationListener listener : listeners) {
				deleteByQuery = listener.preDeleteByQuery(deleteByQuery);
			}
		}
		return deleteByQuery;
	}

	
	/**
	 * Post delete by query.
	 *
	 * @param deleteByQuery the delete by query
	 */
	public void postDeleteByQuery(Engine.DeleteByQuery deleteByQuery) {
		if (listeners != null) {
			for (IndexingOperationListener listener : listeners) {
				listener.postDeleteByQuery(deleteByQuery);
			}
		}
	}

	
	/**
	 * Clear.
	 */
	public void clear() {
		totalStats.clear();
		synchronized (this) {
			if (!typesStats.isEmpty()) {
				MapBuilder<String, StatsHolder> typesStatsBuilder = MapBuilder.newMapBuilder();
				for (Map.Entry<String, StatsHolder> typeStats : typesStats.entrySet()) {
					if (typeStats.getValue().totalCurrent() > 0) {
						typeStats.getValue().clear();
						typesStatsBuilder.put(typeStats.getKey(), typeStats.getValue());
					}
				}
				typesStats = typesStatsBuilder.immutableMap();
			}
		}
	}

	
	/**
	 * Type stats.
	 *
	 * @param type the type
	 * @return the stats holder
	 */
	private StatsHolder typeStats(String type) {
		StatsHolder stats = typesStats.get(type);
		if (stats == null) {
			synchronized (this) {
				stats = typesStats.get(type);
				if (stats == null) {
					stats = new StatsHolder();
					typesStats = MapBuilder.newMapBuilder(typesStats).put(type, stats).immutableMap();
				}
			}
		}
		return stats;
	}

	
	/**
	 * The Class StatsHolder.
	 *
	 * @author l.xue.nong
	 */
	static class StatsHolder {

		
		/** The index metric. */
		public final MeanMetric indexMetric = new MeanMetric();

		
		/** The delete metric. */
		public final MeanMetric deleteMetric = new MeanMetric();

		
		/** The index current. */
		public final CounterMetric indexCurrent = new CounterMetric();

		
		/** The delete current. */
		public final CounterMetric deleteCurrent = new CounterMetric();

		
		/**
		 * Stats.
		 *
		 * @return the indexing stats. stats
		 */
		public IndexingStats.Stats stats() {
			return new IndexingStats.Stats(indexMetric.count(), TimeUnit.NANOSECONDS.toMillis(indexMetric.sum()),
					indexCurrent.count(), deleteMetric.count(), TimeUnit.NANOSECONDS.toMillis(deleteMetric.sum()),
					deleteCurrent.count());
		}

		
		/**
		 * Total current.
		 *
		 * @return the long
		 */
		public long totalCurrent() {
			return indexCurrent.count() + deleteMetric.count();
		}

		
		/**
		 * Clear.
		 */
		public void clear() {
			indexMetric.clear();
			deleteMetric.clear();
		}
	}
}
