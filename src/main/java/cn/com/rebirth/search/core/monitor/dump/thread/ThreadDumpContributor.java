/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ThreadDumpContributor.java 2012-7-6 14:30:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.dump.thread;

import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.monitor.dump.Dump;
import cn.com.rebirth.search.core.monitor.dump.DumpContributionFailedException;
import cn.com.rebirth.search.core.monitor.dump.DumpContributor;

/**
 * The Class ThreadDumpContributor.
 *
 * @author l.xue.nong
 */
public class ThreadDumpContributor implements DumpContributor {

	/** The Constant threadBean. */
	private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

	/** The Constant THREAD_DUMP. */
	public static final String THREAD_DUMP = "thread";

	/** The name. */
	private final String name;

	/**
	 * Instantiates a new thread dump contributor.
	 *
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public ThreadDumpContributor(@Assisted String name, @Assisted Settings settings) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.monitor.dump.DumpContributor#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.monitor.dump.DumpContributor#contribute(cn.com.rebirth.search.core.monitor.dump.Dump)
	 */
	@Override
	public void contribute(Dump dump) throws DumpContributionFailedException {
		PrintWriter writer = new PrintWriter(dump.createFileWriter("threads.txt"));
		try {
			processDeadlocks(writer);
			processAllThreads(writer);
		} catch (Exception e) {
			throw new DumpContributionFailedException(getName(), "Failed to generate", e);
		} finally {
			try {
				writer.close();
			} catch (Exception e) {

			}
		}
	}

	/**
	 * Process deadlocks.
	 *
	 * @param dump the dump
	 */
	private void processDeadlocks(PrintWriter dump) {
		dump.println("=====  Deadlocked Threads =====");
		long deadlockedThreadIds[] = findDeadlockedThreads();
		if (deadlockedThreadIds != null)
			dumpThreads(dump, getThreadInfo(deadlockedThreadIds));
	}

	/**
	 * Process all threads.
	 *
	 * @param dump the dump
	 */
	private void processAllThreads(PrintWriter dump) {
		dump.println();
		dump.println("===== All Threads =====");
		dumpThreads(dump, dumpAllThreads());
	}

	/**
	 * Dump threads.
	 *
	 * @param dump the dump
	 * @param infos the infos
	 */
	private void dumpThreads(PrintWriter dump, ThreadInfo infos[]) {
		for (ThreadInfo info : infos) {
			dump.println();
			write(info, dump);
		}
	}

	/**
	 * Dump all threads.
	 *
	 * @return the thread info[]
	 */
	private ThreadInfo[] dumpAllThreads() {
		return threadBean.dumpAllThreads(true, true);
	}

	/**
	 * Find deadlocked threads.
	 *
	 * @return the long[]
	 */
	public long[] findDeadlockedThreads() {
		return threadBean.findDeadlockedThreads();
	}

	/**
	 * Gets the thread info.
	 *
	 * @param threadIds the thread ids
	 * @return the thread info
	 */
	public ThreadInfo[] getThreadInfo(long[] threadIds) {
		return threadBean.getThreadInfo(threadIds, true, true);
	}

	/**
	 * Write.
	 *
	 * @param threadInfo the thread info
	 * @param writer the writer
	 */
	private void write(ThreadInfo threadInfo, PrintWriter writer) {
		writer.print(String.format("\"%s\" Id=%s %s", threadInfo.getThreadName(), threadInfo.getThreadId(),
				threadInfo.getThreadState()));
		if (threadInfo.getLockName() != null) {
			writer.print(String.format(" on %s", threadInfo.getLockName()));
			if (threadInfo.getLockOwnerName() != null)
				writer.print(String.format(" owned by \"%s\" Id=%s", threadInfo.getLockOwnerName(),
						threadInfo.getLockOwnerId()));
		}
		if (threadInfo.isInNative())
			writer.println(" (in native)");
		else
			writer.println();
		MonitorInfo[] lockedMonitors = threadInfo.getLockedMonitors();
		StackTraceElement stackTraceElements[] = threadInfo.getStackTrace();
		for (StackTraceElement stackTraceElement : stackTraceElements) {
			writer.println("    at " + stackTraceElement);
			MonitorInfo lockedMonitor = findLockedMonitor(stackTraceElement, lockedMonitors);
			if (lockedMonitor != null)
				writer.println(("    - locked " + lockedMonitor.getClassName() + "@" + lockedMonitor
						.getIdentityHashCode()));
		}

	}

	/**
	 * Find locked monitor.
	 *
	 * @param stackTraceElement the stack trace element
	 * @param lockedMonitors the locked monitors
	 * @return the monitor info
	 */
	private static MonitorInfo findLockedMonitor(StackTraceElement stackTraceElement, MonitorInfo lockedMonitors[]) {
		for (MonitorInfo monitorInfo : lockedMonitors) {
			if (stackTraceElement.equals(monitorInfo.getLockedStackFrame()))
				return monitorInfo;
		}

		return null;
	}
}
