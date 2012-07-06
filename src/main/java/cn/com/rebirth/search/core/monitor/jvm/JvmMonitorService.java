/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core JvmMonitorService.java 2012-7-6 14:30:02 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.jvm;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.monitor.dump.DumpGenerator;
import cn.com.rebirth.search.core.monitor.dump.DumpMonitorService;
import cn.com.rebirth.search.core.monitor.dump.summary.SummaryDumpContributor;
import cn.com.rebirth.search.core.monitor.dump.thread.ThreadDumpContributor;
import cn.com.rebirth.search.core.monitor.jvm.JvmStats.GarbageCollector;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * The Class JvmMonitorService.
 *
 * @author l.xue.nong
 */
public class JvmMonitorService extends AbstractLifecycleComponent<JvmMonitorService> {

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The dump monitor service. */
	private final DumpMonitorService dumpMonitorService;

	/** The enabled. */
	private final boolean enabled;

	/** The interval. */
	private final TimeValue interval;

	/** The gc thresholds. */
	private final ImmutableMap<String, GcThreshold> gcThresholds;

	/** The scheduled future. */
	private volatile ScheduledFuture scheduledFuture;

	/**
	 * The Class GcThreshold.
	 *
	 * @author l.xue.nong
	 */
	static class GcThreshold {

		/** The name. */
		public final String name;

		/** The warn threshold. */
		public final long warnThreshold;

		/** The info threshold. */
		public final long infoThreshold;

		/** The debug threshold. */
		public final long debugThreshold;

		/**
		 * Instantiates a new gc threshold.
		 *
		 * @param name the name
		 * @param warnThreshold the warn threshold
		 * @param infoThreshold the info threshold
		 * @param debugThreshold the debug threshold
		 */
		GcThreshold(String name, long warnThreshold, long infoThreshold, long debugThreshold) {
			this.name = name;
			this.warnThreshold = warnThreshold;
			this.infoThreshold = infoThreshold;
			this.debugThreshold = debugThreshold;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "GcThreshold{" + "name='" + name + '\'' + ", warnThreshold=" + warnThreshold + ", infoThreshold="
					+ infoThreshold + ", debugThreshold=" + debugThreshold + '}';
		}
	}

	/**
	 * Instantiates a new jvm monitor service.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param dumpMonitorService the dump monitor service
	 */
	@Inject
	public JvmMonitorService(Settings settings, ThreadPool threadPool, DumpMonitorService dumpMonitorService) {
		super(settings);
		this.threadPool = threadPool;
		this.dumpMonitorService = dumpMonitorService;

		this.enabled = componentSettings.getAsBoolean("enabled", true);
		this.interval = componentSettings.getAsTime("interval", TimeValue.timeValueSeconds(1));

		MapBuilder<String, GcThreshold> gcThresholds = MapBuilder.newMapBuilder();
		Map<String, Settings> gcThresholdGroups = componentSettings.getGroups("gc");
		for (Map.Entry<String, Settings> entry : gcThresholdGroups.entrySet()) {
			String name = entry.getKey();
			TimeValue warn = entry.getValue().getAsTime("warn", null);
			TimeValue info = entry.getValue().getAsTime("info", null);
			TimeValue debug = entry.getValue().getAsTime("debug", null);
			if (warn == null || info == null || debug == null) {
				logger.warn("ignoring gc_threshold for [{}], missing warn/info/debug values", name);
			} else {
				gcThresholds.put(name, new GcThreshold(name, warn.millis(), info.millis(), debug.millis()));
			}
		}
		if (!gcThresholds.containsKey("ParNew")) {
			gcThresholds.put("ParNew", new GcThreshold("ParNew", 1000, 700, 400));
		}
		if (!gcThresholds.containsKey("ConcurrentMarkSweep")) {
			gcThresholds.put("ConcurrentMarkSweep", new GcThreshold("ConcurrentMarkSweep", 10000, 5000, 2000));
		}
		if (!gcThresholds.containsKey("default")) {
			gcThresholds.put("default", new GcThreshold("default", 10000, 5000, 2000));
		}

		this.gcThresholds = gcThresholds.immutableMap();

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RebirthException {
		if (!enabled) {
			return;
		}
		scheduledFuture = threadPool.scheduleWithFixedDelay(new JvmMonitor(), interval);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RebirthException {
		if (!enabled) {
			return;
		}
		scheduledFuture.cancel(true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RebirthException {
	}

	/**
	 * The Class JvmMonitor.
	 *
	 * @author l.xue.nong
	 */
	private class JvmMonitor implements Runnable {

		/** The last jvm stats. */
		private JvmStats lastJvmStats = JvmStats.jvmStats();

		/** The seq. */
		private long seq = 0;

		/** The last seen deadlocks. */
		private final Set<DeadlockAnalyzer.Deadlock> lastSeenDeadlocks = new HashSet<DeadlockAnalyzer.Deadlock>();

		/**
		 * Instantiates a new jvm monitor.
		 */
		public JvmMonitor() {
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {

			monitorLongGc();
		}

		/**
		 * Monitor long gc.
		 */
		private synchronized void monitorLongGc() {
			seq++;
			JvmStats currentJvmStats = JvmStats.jvmStats();

			for (int i = 0; i < currentJvmStats.gc().collectors().length; i++) {
				GarbageCollector gc = currentJvmStats.gc().collectors()[i];
				GarbageCollector prevGc = lastJvmStats.gc.collectors[i];

				long collections = gc.collectionCount - prevGc.collectionCount;
				if (collections == 0) {
					continue;
				}
				long collectionTime = gc.collectionTime - prevGc.collectionTime;
				if (collectionTime == 0) {
					continue;
				}

				GcThreshold gcThreshold = gcThresholds.get(gc.name());
				if (gcThreshold == null) {
					gcThreshold = gcThresholds.get("default");
				}

				if (gc.lastGc() != null && prevGc.lastGc() != null) {
					GarbageCollector.LastGc lastGc = gc.lastGc();
					if (lastGc.startTime == prevGc.lastGc().startTime()) {

						continue;
					}

					if (lastGc.duration().hoursFrac() > 1) {
						continue;
					}
				}

			}
			lastJvmStats = currentJvmStats;
		}

		/**
		 * Monitor deadlock.
		 */
		private void monitorDeadlock() {
			DeadlockAnalyzer.Deadlock[] deadlocks = DeadlockAnalyzer.deadlockAnalyzer().findDeadlocks();
			if (deadlocks != null && deadlocks.length > 0) {
				ImmutableSet<DeadlockAnalyzer.Deadlock> asSet = new ImmutableSet.Builder<DeadlockAnalyzer.Deadlock>()
						.add(deadlocks).build();
				if (!asSet.equals(lastSeenDeadlocks)) {
					DumpGenerator.Result genResult = dumpMonitorService.generateDump("deadlock", null,
							SummaryDumpContributor.SUMMARY, ThreadDumpContributor.THREAD_DUMP);
					StringBuilder sb = new StringBuilder("Detected Deadlock(s)");
					for (DeadlockAnalyzer.Deadlock deadlock : asSet) {
						sb.append("\n   ----> ").append(deadlock);
					}
					sb.append("\nDump generated [").append(genResult.location()).append("]");
					logger.error(sb.toString());
					lastSeenDeadlocks.clear();
					lastSeenDeadlocks.addAll(asSet);
				}
			} else {
				lastSeenDeadlocks.clear();
			}
		}
	}
}
