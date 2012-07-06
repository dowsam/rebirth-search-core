/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SimpleDumpGenerator.java 2012-3-29 15:01:15 l.xue.nong$$
 */


package cn.com.rebirth.search.core.monitor.dump;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.commons.io.FileSystemUtils;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;

import com.google.common.collect.ImmutableMap;


/**
 * The Class SimpleDumpGenerator.
 *
 * @author l.xue.nong
 */
public class SimpleDumpGenerator implements DumpGenerator {

	
	/** The dump location. */
	private final File dumpLocation;

	
	/** The contributors. */
	private final ImmutableMap<String, DumpContributor> contributors;

	
	/**
	 * Instantiates a new simple dump generator.
	 *
	 * @param dumpLocation the dump location
	 * @param contributors the contributors
	 */
	public SimpleDumpGenerator(File dumpLocation, Map<String, DumpContributor> contributors) {
		this.dumpLocation = dumpLocation;
		this.contributors = ImmutableMap.copyOf(contributors);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.monitor.dump.DumpGenerator#generateDump(java.lang.String, java.util.Map)
	 */
	public Result generateDump(String cause, @Nullable Map<String, Object> context)
			throws DumpGenerationFailedException {
		return generateDump(cause, context, contributors.keySet().toArray(new String[contributors.size()]));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.monitor.dump.DumpGenerator#generateDump(java.lang.String, java.util.Map, java.lang.String[])
	 */
	public Result generateDump(String cause, @Nullable Map<String, Object> context, String... contributors)
			throws DumpGenerationFailedException {
		long timestamp = System.currentTimeMillis();
		String fileName = "";
		if (context.containsKey("localNode")) {
			DiscoveryNode localNode = (DiscoveryNode) context.get("localNode");
			if (localNode.name() != null) {
				fileName += localNode.name() + "-";
			}
			fileName += localNode.id() + "-";
		}
		File file = new File(dumpLocation, fileName + cause + "-" + timestamp);
		FileSystemUtils.mkdirs(file);
		SimpleDump dump;
		try {
			dump = new SimpleDump(System.currentTimeMillis(), cause, context, file);
		} catch (FileNotFoundException e) {
			throw new DumpGenerationFailedException("Failed to generate dump", e);
		}
		ArrayList<DumpContributionFailedException> failedContributors = new ArrayList<DumpContributionFailedException>();
		for (String name : contributors) {
			DumpContributor contributor = this.contributors.get(name);
			if (contributor == null) {
				failedContributors.add(new DumpContributionFailedException(name, "No contributor"));
				continue;
			}
			try {
				contributor.contribute(dump);
			} catch (DumpContributionFailedException e) {
				failedContributors.add(e);
			} catch (Exception e) {
				failedContributors.add(new DumpContributionFailedException(contributor.getName(), "Failed", e));
			}
		}
		dump.finish();
		return new Result(file, failedContributors);
	}
}
