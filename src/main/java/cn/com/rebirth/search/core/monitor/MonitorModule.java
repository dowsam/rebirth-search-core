/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MonitorModule.java 2012-7-6 14:30:42 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor;

import static cn.com.rebirth.search.core.monitor.dump.cluster.ClusterDumpContributor.CLUSTER;
import static cn.com.rebirth.search.core.monitor.dump.heap.HeapDumpContributor.HEAP_DUMP;
import static cn.com.rebirth.search.core.monitor.dump.summary.SummaryDumpContributor.SUMMARY;
import static cn.com.rebirth.search.core.monitor.dump.thread.ThreadDumpContributor.THREAD_DUMP;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Scopes;
import cn.com.rebirth.search.commons.inject.assistedinject.FactoryProvider;
import cn.com.rebirth.search.commons.inject.multibindings.MapBinder;
import cn.com.rebirth.search.core.monitor.dump.DumpContributorFactory;
import cn.com.rebirth.search.core.monitor.dump.DumpMonitorService;
import cn.com.rebirth.search.core.monitor.dump.cluster.ClusterDumpContributor;
import cn.com.rebirth.search.core.monitor.dump.heap.HeapDumpContributor;
import cn.com.rebirth.search.core.monitor.dump.summary.SummaryDumpContributor;
import cn.com.rebirth.search.core.monitor.dump.thread.ThreadDumpContributor;
import cn.com.rebirth.search.core.monitor.fs.FsProbe;
import cn.com.rebirth.search.core.monitor.fs.FsService;
import cn.com.rebirth.search.core.monitor.fs.JmxFsProbe;
import cn.com.rebirth.search.core.monitor.fs.SigarFsProbe;
import cn.com.rebirth.search.core.monitor.jvm.JvmMonitorService;
import cn.com.rebirth.search.core.monitor.jvm.JvmService;
import cn.com.rebirth.search.core.monitor.network.JmxNetworkProbe;
import cn.com.rebirth.search.core.monitor.network.NetworkProbe;
import cn.com.rebirth.search.core.monitor.network.NetworkService;
import cn.com.rebirth.search.core.monitor.network.SigarNetworkProbe;
import cn.com.rebirth.search.core.monitor.os.JmxOsProbe;
import cn.com.rebirth.search.core.monitor.os.OsProbe;
import cn.com.rebirth.search.core.monitor.os.OsService;
import cn.com.rebirth.search.core.monitor.os.SigarOsProbe;
import cn.com.rebirth.search.core.monitor.process.JmxProcessProbe;
import cn.com.rebirth.search.core.monitor.process.ProcessProbe;
import cn.com.rebirth.search.core.monitor.process.ProcessService;
import cn.com.rebirth.search.core.monitor.process.SigarProcessProbe;
import cn.com.rebirth.search.core.monitor.sigar.SigarService;

/**
 * The Class MonitorModule.
 *
 * @author l.xue.nong
 */
public class MonitorModule extends AbstractModule {

	/** The logger. */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * The Class MonitorSettings.
	 *
	 * @author l.xue.nong
	 */
	public static final class MonitorSettings {

		/** The Constant MEMORY_MANAGER_TYPE. */
		public static final String MEMORY_MANAGER_TYPE = "monitor.memory.type";
	}

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new monitor module.
	 *
	 * @param settings the settings
	 */
	public MonitorModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		boolean sigarLoaded = false;
		try {
			settings.getClassLoader().loadClass("org.hyperic.sigar.Sigar");
			SigarService sigarService = new SigarService(settings);
			if (sigarService.sigarAvailable()) {
				bind(SigarService.class).toInstance(sigarService);
				bind(ProcessProbe.class).to(SigarProcessProbe.class).asEagerSingleton();
				bind(OsProbe.class).to(SigarOsProbe.class).asEagerSingleton();
				bind(NetworkProbe.class).to(SigarNetworkProbe.class).asEagerSingleton();
				bind(FsProbe.class).to(SigarFsProbe.class).asEagerSingleton();
				sigarLoaded = true;
			}
		} catch (Throwable e) {

			logger.trace("failed to load sigar", e);
		}
		if (!sigarLoaded) {

			bind(ProcessProbe.class).to(JmxProcessProbe.class).asEagerSingleton();
			bind(OsProbe.class).to(JmxOsProbe.class).asEagerSingleton();
			bind(NetworkProbe.class).to(JmxNetworkProbe.class).asEagerSingleton();
			bind(FsProbe.class).to(JmxFsProbe.class).asEagerSingleton();
		}

		bind(ProcessService.class).asEagerSingleton();
		bind(OsService.class).asEagerSingleton();
		bind(NetworkService.class).asEagerSingleton();
		bind(JvmService.class).asEagerSingleton();
		bind(FsService.class).asEagerSingleton();

		bind(JvmMonitorService.class).asEagerSingleton();

		MapBinder<String, DumpContributorFactory> tokenFilterBinder = MapBinder.newMapBinder(binder(), String.class,
				DumpContributorFactory.class);

		Map<String, Settings> dumpContSettings = settings.getGroups("monitor.dump");
		for (Map.Entry<String, Settings> entry : dumpContSettings.entrySet()) {
			String dumpContributorName = entry.getKey();
			Settings dumpContributorSettings = entry.getValue();

			Class<? extends DumpContributorFactory> type = dumpContributorSettings.getAsClass("type", null,
					"cn.com.rebirth.search.core.monitor.dump." + dumpContributorName + ".", "DumpContributor");
			if (type == null) {
				throw new IllegalArgumentException("Dump Contributor [" + dumpContributorName
						+ "] must have a type associated with it");
			}
			tokenFilterBinder.addBinding(dumpContributorName)
					.toProvider(FactoryProvider.newFactory(DumpContributorFactory.class, type)).in(Scopes.SINGLETON);
		}

		if (!dumpContSettings.containsKey(SUMMARY)) {
			tokenFilterBinder.addBinding(SUMMARY)
					.toProvider(FactoryProvider.newFactory(DumpContributorFactory.class, SummaryDumpContributor.class))
					.in(Scopes.SINGLETON);
		}
		if (!dumpContSettings.containsKey(THREAD_DUMP)) {
			tokenFilterBinder.addBinding(THREAD_DUMP)
					.toProvider(FactoryProvider.newFactory(DumpContributorFactory.class, ThreadDumpContributor.class))
					.in(Scopes.SINGLETON);
		}
		if (!dumpContSettings.containsKey(HEAP_DUMP)) {
			tokenFilterBinder.addBinding(HEAP_DUMP)
					.toProvider(FactoryProvider.newFactory(DumpContributorFactory.class, HeapDumpContributor.class))
					.in(Scopes.SINGLETON);
		}
		if (!dumpContSettings.containsKey(CLUSTER)) {
			tokenFilterBinder.addBinding(CLUSTER)
					.toProvider(FactoryProvider.newFactory(DumpContributorFactory.class, ClusterDumpContributor.class))
					.in(Scopes.SINGLETON);
		}

		bind(DumpMonitorService.class).asEagerSingleton();
	}
}
