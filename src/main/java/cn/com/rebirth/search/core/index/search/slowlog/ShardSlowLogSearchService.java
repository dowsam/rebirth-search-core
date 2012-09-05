/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardSlowLogSearchService.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.search.slowlog;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.xcontent.XContentHelper;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.settings.IndexSettingsService;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class ShardSlowLogSearchService.
 *
 * @author l.xue.nong
 */
public class ShardSlowLogSearchService extends AbstractIndexShardComponent {

	/** The reformat. */
	private boolean reformat;

	/** The query warn threshold. */
	private long queryWarnThreshold;

	/** The query info threshold. */
	private long queryInfoThreshold;

	/** The query debug threshold. */
	private long queryDebugThreshold;

	/** The query trace threshold. */
	private long queryTraceThreshold;

	/** The fetch warn threshold. */
	private long fetchWarnThreshold;

	/** The fetch info threshold. */
	private long fetchInfoThreshold;

	/** The fetch debug threshold. */
	private long fetchDebugThreshold;

	/** The fetch trace threshold. */
	private long fetchTraceThreshold;

	/** The level. */
	private String level;

	static {
		IndexMetaData.addDynamicSettings("index.search.slowlog.threshold.query.warn",
				"index.search.slowlog.threshold.query.info", "index.search.slowlog.threshold.query.debug",
				"index.search.slowlog.threshold.query.trace", "index.search.slowlog.threshold.fetch.warn",
				"index.search.slowlog.threshold.fetch.info", "index.search.slowlog.threshold.fetch.debug",
				"index.search.slowlog.threshold.fetch.trace", "index.search.slowlog.reformat",
				"index.search.slowlog.level");
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
			long queryWarnThreshold = settings.getAsTime("index.search.slowlog.threshold.query.warn",
					TimeValue.timeValueNanos(ShardSlowLogSearchService.this.queryWarnThreshold)).nanos();
			if (queryWarnThreshold != ShardSlowLogSearchService.this.queryWarnThreshold) {
				ShardSlowLogSearchService.this.queryWarnThreshold = queryWarnThreshold;
			}
			long queryInfoThreshold = settings.getAsTime("index.search.slowlog.threshold.query.info",
					TimeValue.timeValueNanos(ShardSlowLogSearchService.this.queryInfoThreshold)).nanos();
			if (queryInfoThreshold != ShardSlowLogSearchService.this.queryInfoThreshold) {
				ShardSlowLogSearchService.this.queryInfoThreshold = queryInfoThreshold;
			}
			long queryDebugThreshold = settings.getAsTime("index.search.slowlog.threshold.query.debug",
					TimeValue.timeValueNanos(ShardSlowLogSearchService.this.queryDebugThreshold)).nanos();
			if (queryDebugThreshold != ShardSlowLogSearchService.this.queryDebugThreshold) {
				ShardSlowLogSearchService.this.queryDebugThreshold = queryDebugThreshold;
			}
			long queryTraceThreshold = settings.getAsTime("index.search.slowlog.threshold.query.trace",
					TimeValue.timeValueNanos(ShardSlowLogSearchService.this.queryTraceThreshold)).nanos();
			if (queryTraceThreshold != ShardSlowLogSearchService.this.queryTraceThreshold) {
				ShardSlowLogSearchService.this.queryTraceThreshold = queryTraceThreshold;
			}

			long fetchWarnThreshold = settings.getAsTime("index.search.slowlog.threshold.fetch.warn",
					TimeValue.timeValueNanos(ShardSlowLogSearchService.this.fetchWarnThreshold)).nanos();
			if (fetchWarnThreshold != ShardSlowLogSearchService.this.fetchWarnThreshold) {
				ShardSlowLogSearchService.this.fetchWarnThreshold = fetchWarnThreshold;
			}
			long fetchInfoThreshold = settings.getAsTime("index.search.slowlog.threshold.fetch.info",
					TimeValue.timeValueNanos(ShardSlowLogSearchService.this.fetchInfoThreshold)).nanos();
			if (fetchInfoThreshold != ShardSlowLogSearchService.this.fetchInfoThreshold) {
				ShardSlowLogSearchService.this.fetchInfoThreshold = fetchInfoThreshold;
			}
			long fetchDebugThreshold = settings.getAsTime("index.search.slowlog.threshold.fetch.debug",
					TimeValue.timeValueNanos(ShardSlowLogSearchService.this.fetchDebugThreshold)).nanos();
			if (fetchDebugThreshold != ShardSlowLogSearchService.this.fetchDebugThreshold) {
				ShardSlowLogSearchService.this.fetchDebugThreshold = fetchDebugThreshold;
			}
			long fetchTraceThreshold = settings.getAsTime("index.search.slowlog.threshold.fetch.trace",
					TimeValue.timeValueNanos(ShardSlowLogSearchService.this.fetchTraceThreshold)).nanos();
			if (fetchTraceThreshold != ShardSlowLogSearchService.this.fetchTraceThreshold) {
				ShardSlowLogSearchService.this.fetchTraceThreshold = fetchTraceThreshold;
			}

			String level = settings.get("index.search.slowlog.level", ShardSlowLogSearchService.this.level);
			if (!level.equals(ShardSlowLogSearchService.this.level)) {
				ShardSlowLogSearchService.this.level = level;
			}

			boolean reformat = settings.getAsBoolean("index.search.slowlog.reformat",
					ShardSlowLogSearchService.this.reformat);
			if (reformat != ShardSlowLogSearchService.this.reformat) {
				ShardSlowLogSearchService.this.reformat = reformat;
			}
		}
	}

	/**
	 * Instantiates a new shard slow log search service.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param indexSettingsService the index settings service
	 */
	@Inject
	public ShardSlowLogSearchService(ShardId shardId, @IndexSettings Settings indexSettings,
			IndexSettingsService indexSettingsService) {
		super(shardId, indexSettings);

		this.reformat = componentSettings.getAsBoolean("reformat", true);

		this.queryWarnThreshold = componentSettings.getAsTime("threshold.query.warn", TimeValue.timeValueNanos(-1))
				.nanos();
		this.queryInfoThreshold = componentSettings.getAsTime("threshold.query.info", TimeValue.timeValueNanos(-1))
				.nanos();
		this.queryDebugThreshold = componentSettings.getAsTime("threshold.query.debug", TimeValue.timeValueNanos(-1))
				.nanos();
		this.queryTraceThreshold = componentSettings.getAsTime("threshold.query.trace", TimeValue.timeValueNanos(-1))
				.nanos();

		this.fetchWarnThreshold = componentSettings.getAsTime("threshold.fetch.warn", TimeValue.timeValueNanos(-1))
				.nanos();
		this.fetchInfoThreshold = componentSettings.getAsTime("threshold.fetch.info", TimeValue.timeValueNanos(-1))
				.nanos();
		this.fetchDebugThreshold = componentSettings.getAsTime("threshold.fetch.debug", TimeValue.timeValueNanos(-1))
				.nanos();
		this.fetchTraceThreshold = componentSettings.getAsTime("threshold.fetch.trace", TimeValue.timeValueNanos(-1))
				.nanos();

		this.level = componentSettings.get("level", "TRACE").toUpperCase();

		indexSettingsService.addListener(new ApplySettings());
	}

	/**
	 * On query phase.
	 *
	 * @param context the context
	 * @param tookInNanos the took in nanos
	 */
	public void onQueryPhase(SearchContext context, long tookInNanos) {
		if (queryWarnThreshold >= 0 && tookInNanos > queryWarnThreshold && logger.isWarnEnabled()) {
			logger.warn("{}", new SlowLogSearchContextPrinter(context, tookInNanos, reformat));
		} else if (queryInfoThreshold >= 0 && tookInNanos > queryInfoThreshold && logger.isInfoEnabled()) {
			logger.info("{}", new SlowLogSearchContextPrinter(context, tookInNanos, reformat));
		} else if (queryDebugThreshold >= 0 && tookInNanos > queryDebugThreshold && logger.isDebugEnabled()) {
			logger.debug("{}", new SlowLogSearchContextPrinter(context, tookInNanos, reformat));
		} else if (queryTraceThreshold >= 0 && tookInNanos > queryTraceThreshold && logger.isTraceEnabled()) {
			logger.trace("{}", new SlowLogSearchContextPrinter(context, tookInNanos, reformat));
		}
	}

	/**
	 * On fetch phase.
	 *
	 * @param context the context
	 * @param tookInNanos the took in nanos
	 */
	public void onFetchPhase(SearchContext context, long tookInNanos) {
		if (fetchWarnThreshold >= 0 && tookInNanos > fetchWarnThreshold && logger.isWarnEnabled()) {
			logger.warn("{}", new SlowLogSearchContextPrinter(context, tookInNanos, reformat));
		} else if (fetchInfoThreshold >= 0 && tookInNanos > fetchInfoThreshold && logger.isInfoEnabled()) {
			logger.info("{}", new SlowLogSearchContextPrinter(context, tookInNanos, reformat));
		} else if (fetchDebugThreshold >= 0 && tookInNanos > fetchDebugThreshold && logger.isDebugEnabled()) {
			logger.debug("{}", new SlowLogSearchContextPrinter(context, tookInNanos, reformat));
		} else if (fetchTraceThreshold >= 0 && tookInNanos > fetchTraceThreshold && logger.isTraceEnabled()) {
			logger.trace("{}", new SlowLogSearchContextPrinter(context, tookInNanos, reformat));
		}
	}

	/**
	 * The Class SlowLogSearchContextPrinter.
	 *
	 * @author l.xue.nong
	 */
	public static class SlowLogSearchContextPrinter {

		/** The context. */
		private final SearchContext context;

		/** The took in nanos. */
		private final long tookInNanos;

		/** The reformat. */
		private final boolean reformat;

		/**
		 * Instantiates a new slow log search context printer.
		 *
		 * @param context the context
		 * @param tookInNanos the took in nanos
		 * @param reformat the reformat
		 */
		public SlowLogSearchContextPrinter(SearchContext context, long tookInNanos, boolean reformat) {
			this.context = context;
			this.tookInNanos = tookInNanos;
			this.reformat = reformat;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("took[").append(TimeValue.timeValueNanos(tookInNanos)).append("], took_millis[")
					.append(TimeUnit.NANOSECONDS.toMillis(tookInNanos)).append("], ");
			sb.append("search_type[").append(context.searchType()).append("], total_shards[")
					.append(context.numberOfShards()).append("], ");
			if (context.request().sourceLength() > 0) {
				try {
					sb.append("source[").append(
							XContentHelper.convertToJson(context.request().source(), context.request().sourceOffset(),
									context.request().sourceLength(), reformat));
				} catch (IOException e) {
					sb.append("source[_failed_to_convert_], ");
				}
			} else {
				sb.append("source[], ");
			}
			if (context.request().extraSourceLength() > 0) {
				try {
					sb.append("extra_source[").append(
							XContentHelper.convertToJson(context.request().extraSource(), context.request()
									.extraSourceOffset(), context.request().extraSourceLength(), reformat));
				} catch (IOException e) {
					sb.append("extra_source[_failed_to_convert_], ");
				}
			} else {
				sb.append("extra_source[], ");
			}
			return sb.toString();
		}
	}
}
