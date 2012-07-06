/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardSearchService.java 2012-3-29 15:01:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.search.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.metrics.CounterMetric;
import cn.com.rebirth.search.commons.metrics.MeanMetric;
import cn.com.rebirth.search.core.index.search.slowlog.ShardSlowLogSearchService;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.ImmutableMap;


/**
 * The Class ShardSearchService.
 *
 * @author l.xue.nong
 */
public class ShardSearchService extends AbstractIndexShardComponent {

	
	/** The slow log search service. */
	private final ShardSlowLogSearchService slowLogSearchService;

	
	/** The total stats. */
	private final StatsHolder totalStats = new StatsHolder();

	
	/** The groups stats. */
	private volatile Map<String, StatsHolder> groupsStats = ImmutableMap.of();

	
	/**
	 * Instantiates a new shard search service.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param slowLogSearchService the slow log search service
	 */
	@Inject
	public ShardSearchService(ShardId shardId, @IndexSettings Settings indexSettings,
			ShardSlowLogSearchService slowLogSearchService) {
		super(shardId, indexSettings);
		this.slowLogSearchService = slowLogSearchService;
	}

	
	/**
	 * Stats.
	 *
	 * @param groups the groups
	 * @return the search stats
	 */
	public SearchStats stats(String... groups) {
		SearchStats.Stats total = totalStats.stats();
		Map<String, SearchStats.Stats> groupsSt = null;
		if (groups != null && groups.length > 0) {
			if (groups.length == 1 && groups[0].equals("_all")) {
				groupsSt = new HashMap<String, SearchStats.Stats>(groupsStats.size());
				for (Map.Entry<String, StatsHolder> entry : groupsStats.entrySet()) {
					groupsSt.put(entry.getKey(), entry.getValue().stats());
				}
			} else {
				groupsSt = new HashMap<String, SearchStats.Stats>(groups.length);
				for (String group : groups) {
					StatsHolder statsHolder = groupsStats.get(group);
					if (statsHolder != null) {
						groupsSt.put(group, statsHolder.stats());
					}
				}
			}
		}
		return new SearchStats(total, groupsSt);
	}

	
	/**
	 * On pre query phase.
	 *
	 * @param searchContext the search context
	 */
	public void onPreQueryPhase(SearchContext searchContext) {
		totalStats.queryCurrent.inc();
		if (searchContext.groupStats() != null) {
			for (int i = 0; i < searchContext.groupStats().size(); i++) {
				groupStats(searchContext.groupStats().get(i)).queryCurrent.inc();
			}
		}
	}

	
	/**
	 * On failed query phase.
	 *
	 * @param searchContext the search context
	 */
	public void onFailedQueryPhase(SearchContext searchContext) {
		totalStats.queryCurrent.dec();
		if (searchContext.groupStats() != null) {
			for (int i = 0; i < searchContext.groupStats().size(); i++) {
				groupStats(searchContext.groupStats().get(i)).queryCurrent.dec();
			}
		}
	}

	
	/**
	 * On query phase.
	 *
	 * @param searchContext the search context
	 * @param tookInNanos the took in nanos
	 */
	public void onQueryPhase(SearchContext searchContext, long tookInNanos) {
		totalStats.queryMetric.inc(tookInNanos);
		totalStats.queryCurrent.dec();
		if (searchContext.groupStats() != null) {
			for (int i = 0; i < searchContext.groupStats().size(); i++) {
				StatsHolder statsHolder = groupStats(searchContext.groupStats().get(i));
				statsHolder.queryMetric.inc(tookInNanos);
				statsHolder.queryCurrent.dec();
			}
		}
		slowLogSearchService.onQueryPhase(searchContext, tookInNanos);
	}

	
	/**
	 * On pre fetch phase.
	 *
	 * @param searchContext the search context
	 */
	public void onPreFetchPhase(SearchContext searchContext) {
		totalStats.fetchCurrent.inc();
		if (searchContext.groupStats() != null) {
			for (int i = 0; i < searchContext.groupStats().size(); i++) {
				groupStats(searchContext.groupStats().get(i)).fetchCurrent.inc();
			}
		}
	}

	
	/**
	 * On failed fetch phase.
	 *
	 * @param searchContext the search context
	 */
	public void onFailedFetchPhase(SearchContext searchContext) {
		totalStats.fetchCurrent.dec();
		if (searchContext.groupStats() != null) {
			for (int i = 0; i < searchContext.groupStats().size(); i++) {
				groupStats(searchContext.groupStats().get(i)).fetchCurrent.dec();
			}
		}
	}

	
	/**
	 * On fetch phase.
	 *
	 * @param searchContext the search context
	 * @param tookInNanos the took in nanos
	 */
	public void onFetchPhase(SearchContext searchContext, long tookInNanos) {
		totalStats.fetchMetric.inc(tookInNanos);
		totalStats.fetchCurrent.dec();
		if (searchContext.groupStats() != null) {
			for (int i = 0; i < searchContext.groupStats().size(); i++) {
				StatsHolder statsHolder = groupStats(searchContext.groupStats().get(i));
				statsHolder.fetchMetric.inc(tookInNanos);
				statsHolder.fetchCurrent.dec();
			}
		}
		slowLogSearchService.onFetchPhase(searchContext, tookInNanos);
	}

	
	/**
	 * Clear.
	 */
	public void clear() {
		totalStats.clear();
		synchronized (this) {
			if (!groupsStats.isEmpty()) {
				MapBuilder<String, StatsHolder> typesStatsBuilder = MapBuilder.newMapBuilder();
				for (Map.Entry<String, StatsHolder> typeStats : groupsStats.entrySet()) {
					if (typeStats.getValue().totalCurrent() > 0) {
						typeStats.getValue().clear();
						typesStatsBuilder.put(typeStats.getKey(), typeStats.getValue());
					}
				}
				groupsStats = typesStatsBuilder.immutableMap();
			}
		}
	}

	
	/**
	 * Group stats.
	 *
	 * @param group the group
	 * @return the stats holder
	 */
	private StatsHolder groupStats(String group) {
		StatsHolder stats = groupsStats.get(group);
		if (stats == null) {
			synchronized (this) {
				stats = groupsStats.get(group);
				if (stats == null) {
					stats = new StatsHolder();
					groupsStats = MapBuilder.newMapBuilder(groupsStats).put(group, stats).immutableMap();
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

		
		/** The query metric. */
		public final MeanMetric queryMetric = new MeanMetric();

		
		/** The fetch metric. */
		public final MeanMetric fetchMetric = new MeanMetric();

		
		/** The query current. */
		public final CounterMetric queryCurrent = new CounterMetric();

		
		/** The fetch current. */
		public final CounterMetric fetchCurrent = new CounterMetric();

		
		/**
		 * Stats.
		 *
		 * @return the search stats. stats
		 */
		public SearchStats.Stats stats() {
			return new SearchStats.Stats(queryMetric.count(), TimeUnit.NANOSECONDS.toMillis(queryMetric.sum()),
					queryCurrent.count(), fetchMetric.count(), TimeUnit.NANOSECONDS.toMillis(fetchMetric.sum()),
					fetchCurrent.count());
		}

		
		/**
		 * Total current.
		 *
		 * @return the long
		 */
		public long totalCurrent() {
			return queryCurrent.count() + fetchCurrent.count();
		}

		
		/**
		 * Clear.
		 */
		public void clear() {
			queryMetric.clear();
			fetchMetric.clear();
		}
	}
}
