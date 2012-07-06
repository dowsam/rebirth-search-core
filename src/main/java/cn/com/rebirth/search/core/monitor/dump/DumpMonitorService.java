/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DumpMonitorService.java 2012-7-6 14:30:11 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.dump;

import static cn.com.rebirth.search.core.monitor.dump.heap.HeapDumpContributor.HEAP_DUMP;
import static cn.com.rebirth.search.core.monitor.dump.summary.SummaryDumpContributor.SUMMARY;
import static cn.com.rebirth.search.core.monitor.dump.thread.ThreadDumpContributor.THREAD_DUMP;
import static com.google.common.collect.Maps.newHashMap;

import java.io.File;
import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.monitor.dump.heap.HeapDumpContributor;
import cn.com.rebirth.search.core.monitor.dump.summary.SummaryDumpContributor;
import cn.com.rebirth.search.core.monitor.dump.thread.ThreadDumpContributor;

/**
 * The Class DumpMonitorService.
 *
 * @author l.xue.nong
 */
public class DumpMonitorService extends AbstractComponent {

	/** The dump location. */
	private final String dumpLocation;

	/** The generator. */
	private final DumpGenerator generator;

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The cont settings. */
	private final Map<String, Settings> contSettings;

	/** The contributors. */
	private final Map<String, DumpContributorFactory> contributors;

	/** The work file. */
	private final File workFile;

	/**
	 * Instantiates a new dump monitor service.
	 */
	public DumpMonitorService() {
		this(ImmutableSettings.Builder.EMPTY_SETTINGS, new Environment(ImmutableSettings.Builder.EMPTY_SETTINGS), null,
				null);
	}

	/**
	 * Instantiates a new dump monitor service.
	 *
	 * @param settings the settings
	 * @param environment the environment
	 * @param clusterService the cluster service
	 * @param contributors the contributors
	 */
	@Inject
	public DumpMonitorService(Settings settings, Environment environment, @Nullable ClusterService clusterService,
			@Nullable Map<String, DumpContributorFactory> contributors) {
		super(settings);
		this.clusterService = clusterService;
		this.contributors = contributors;
		contSettings = settings.getGroups("monitor.dump");
		workFile = environment.workWithClusterFile();

		this.dumpLocation = settings.get("dump_location");

		File dumpLocationFile;
		if (dumpLocation != null) {
			dumpLocationFile = new File(dumpLocation);
		} else {
			dumpLocationFile = new File(workFile, "dump");
		}

		Map<String, DumpContributor> contributorMap = newHashMap();
		if (contributors != null) {
			for (Map.Entry<String, DumpContributorFactory> entry : contributors.entrySet()) {
				String contName = entry.getKey();
				DumpContributorFactory dumpContributorFactory = entry.getValue();

				Settings analyzerSettings = contSettings.get(contName);
				if (analyzerSettings == null) {
					analyzerSettings = ImmutableSettings.Builder.EMPTY_SETTINGS;
				}

				DumpContributor analyzerFactory = dumpContributorFactory.create(contName, analyzerSettings);
				contributorMap.put(contName, analyzerFactory);
			}
		}
		if (!contributorMap.containsKey(SUMMARY)) {
			contributorMap.put(SUMMARY, new SummaryDumpContributor(SUMMARY, ImmutableSettings.Builder.EMPTY_SETTINGS));
		}
		if (!contributorMap.containsKey(HEAP_DUMP)) {
			contributorMap.put(HEAP_DUMP, new HeapDumpContributor(HEAP_DUMP, ImmutableSettings.Builder.EMPTY_SETTINGS));
		}
		if (!contributorMap.containsKey(THREAD_DUMP)) {
			contributorMap.put(THREAD_DUMP, new ThreadDumpContributor(THREAD_DUMP,
					ImmutableSettings.Builder.EMPTY_SETTINGS));
		}
		generator = new SimpleDumpGenerator(dumpLocationFile, contributorMap);
	}

	/**
	 * Generate dump.
	 *
	 * @param cause the cause
	 * @param context the context
	 * @return the dump generator. result
	 * @throws DumpGenerationFailedException the dump generation failed exception
	 */
	public DumpGenerator.Result generateDump(String cause, @Nullable Map<String, Object> context)
			throws DumpGenerationFailedException {
		return generator.generateDump(cause, fillContextMap(context));
	}

	/**
	 * Generate dump.
	 *
	 * @param cause the cause
	 * @param context the context
	 * @param contributors the contributors
	 * @return the dump generator. result
	 * @throws DumpGenerationFailedException the dump generation failed exception
	 */
	public DumpGenerator.Result generateDump(String cause, @Nullable Map<String, Object> context,
			String... contributors) throws DumpGenerationFailedException {
		return generator.generateDump(cause, fillContextMap(context), contributors);
	}

	/**
	 * Fill context map.
	 *
	 * @param context the context
	 * @return the map
	 */
	private Map<String, Object> fillContextMap(Map<String, Object> context) {
		if (context == null) {
			context = newHashMap();
		}
		if (clusterService != null) {
			context.put("localNode", clusterService.state().nodes().localNode());
		}
		return context;
	}
}
