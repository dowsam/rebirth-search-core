/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DeadlockAnalyzer.java 2012-7-6 14:29:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.jvm;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * The Class DeadlockAnalyzer.
 *
 * @author l.xue.nong
 */
public class DeadlockAnalyzer {

	/** The Constant NULL_RESULT. */
	private static final Deadlock NULL_RESULT[] = new Deadlock[0];

	/** The thread bean. */
	private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

	/** The instance. */
	private static DeadlockAnalyzer INSTANCE = new DeadlockAnalyzer();

	/**
	 * Deadlock analyzer.
	 *
	 * @return the deadlock analyzer
	 */
	public static DeadlockAnalyzer deadlockAnalyzer() {
		return INSTANCE;
	}

	/**
	 * Instantiates a new deadlock analyzer.
	 */
	private DeadlockAnalyzer() {

	}

	/**
	 * Find deadlocks.
	 *
	 * @return the deadlock[]
	 */
	public Deadlock[] findDeadlocks() {
		long deadlockedThreads[] = threadBean.findMonitorDeadlockedThreads();
		if (deadlockedThreads == null || deadlockedThreads.length == 0) {
			return NULL_RESULT;
		}
		ImmutableMap<Long, ThreadInfo> threadInfoMap = createThreadInfoMap(deadlockedThreads);
		Set<LinkedHashSet<ThreadInfo>> cycles = calculateCycles(threadInfoMap);
		Set<LinkedHashSet<ThreadInfo>> chains = calculateCycleDeadlockChains(threadInfoMap, cycles);
		cycles.addAll(chains);
		return createDeadlockDescriptions(cycles);
	}

	/**
	 * Creates the deadlock descriptions.
	 *
	 * @param cycles the cycles
	 * @return the deadlock[]
	 */
	private Deadlock[] createDeadlockDescriptions(Set<LinkedHashSet<ThreadInfo>> cycles) {
		Deadlock result[] = new Deadlock[cycles.size()];
		int count = 0;
		for (LinkedHashSet<ThreadInfo> cycle : cycles) {
			ThreadInfo asArray[] = cycle.toArray(new ThreadInfo[cycle.size()]);
			Deadlock d = new Deadlock(asArray);
			result[count++] = d;
		}
		return result;
	}

	/**
	 * Calculate cycles.
	 *
	 * @param threadInfoMap the thread info map
	 * @return the sets the
	 */
	private Set<LinkedHashSet<ThreadInfo>> calculateCycles(ImmutableMap<Long, ThreadInfo> threadInfoMap) {
		Set<LinkedHashSet<ThreadInfo>> cycles = new HashSet<LinkedHashSet<ThreadInfo>>();
		for (Map.Entry<Long, ThreadInfo> entry : threadInfoMap.entrySet()) {
			LinkedHashSet<ThreadInfo> cycle = new LinkedHashSet<ThreadInfo>();
			for (ThreadInfo t = entry.getValue(); !cycle.contains(t); t = threadInfoMap.get(Long.valueOf(t
					.getLockOwnerId())))
				cycle.add(t);

			if (!cycles.contains(cycle))
				cycles.add(cycle);
		}
		return cycles;
	}

	/**
	 * Calculate cycle deadlock chains.
	 *
	 * @param threadInfoMap the thread info map
	 * @param cycles the cycles
	 * @return the sets the
	 */
	private Set<LinkedHashSet<ThreadInfo>> calculateCycleDeadlockChains(ImmutableMap<Long, ThreadInfo> threadInfoMap,
			Set<LinkedHashSet<ThreadInfo>> cycles) {
		ThreadInfo allThreads[] = threadBean.getThreadInfo(threadBean.getAllThreadIds());
		Set<LinkedHashSet<ThreadInfo>> deadlockChain = new HashSet<LinkedHashSet<ThreadInfo>>();
		Set<Long> knownDeadlockedThreads = threadInfoMap.keySet();
		for (ThreadInfo threadInfo : allThreads) {
			Thread.State state = threadInfo.getThreadState();
			if (state == Thread.State.BLOCKED && !knownDeadlockedThreads.contains(threadInfo.getThreadId())) {
				for (LinkedHashSet cycle : cycles) {
					if (cycle.contains(threadInfoMap.get(Long.valueOf(threadInfo.getLockOwnerId())))) {
						LinkedHashSet<ThreadInfo> chain = new LinkedHashSet<ThreadInfo>();
						for (ThreadInfo node = threadInfo; !chain.contains(node); node = threadInfoMap.get(Long
								.valueOf(node.getLockOwnerId())))
							chain.add(node);

						deadlockChain.add(chain);
					}
				}

			}
		}

		return deadlockChain;
	}

	/**
	 * Creates the thread info map.
	 *
	 * @param threadIds the thread ids
	 * @return the immutable map
	 */
	private ImmutableMap<Long, ThreadInfo> createThreadInfoMap(long threadIds[]) {
		ThreadInfo threadInfos[] = threadBean.getThreadInfo(threadIds);
		ImmutableMap.Builder<Long, ThreadInfo> threadInfoMap = ImmutableMap.builder();
		for (ThreadInfo threadInfo : threadInfos) {
			threadInfoMap.put(threadInfo.getThreadId(), threadInfo);
		}
		return threadInfoMap.build();
	}

	/**
	 * The Class Deadlock.
	 *
	 * @author l.xue.nong
	 */
	public static class Deadlock {

		/** The members. */
		private final ThreadInfo members[];

		/** The description. */
		private final String description;

		/** The member ids. */
		private final ImmutableSet<Long> memberIds;

		/**
		 * Instantiates a new deadlock.
		 *
		 * @param members the members
		 */
		public Deadlock(ThreadInfo[] members) {
			this.members = members;

			ImmutableSet.Builder<Long> builder = ImmutableSet.builder();
			StringBuilder sb = new StringBuilder();
			for (int x = 0; x < members.length; x++) {
				ThreadInfo ti = members[x];
				sb.append(ti.getThreadName());
				if (x < members.length)
					sb.append(" > ");
				if (x == members.length - 1)
					sb.append(ti.getLockOwnerName());
				builder.add(ti.getThreadId());
			}
			this.description = sb.toString();
			this.memberIds = builder.build();
		}

		/**
		 * Members.
		 *
		 * @return the thread info[]
		 */
		public ThreadInfo[] members() {
			return members;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			Deadlock deadlock = (Deadlock) o;

			if (memberIds != null ? !memberIds.equals(deadlock.memberIds) : deadlock.memberIds != null)
				return false;

			return true;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			int result = members != null ? Arrays.hashCode(members) : 0;
			result = 31 * result + (description != null ? description.hashCode() : 0);
			result = 31 * result + (memberIds != null ? memberIds.hashCode() : 0);
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return description;
		}
	}
}
