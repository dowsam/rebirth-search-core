/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core HeapDumpContributor.java 2012-3-29 15:02:15 l.xue.nong$$
 */


package cn.com.rebirth.search.core.monitor.dump.heap;

import java.lang.reflect.Method;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.monitor.dump.Dump;
import cn.com.rebirth.search.core.monitor.dump.DumpContributionFailedException;
import cn.com.rebirth.search.core.monitor.dump.DumpContributor;


/**
 * The Class HeapDumpContributor.
 *
 * @author l.xue.nong
 */
public class HeapDumpContributor implements DumpContributor {

	
	/** The Constant HEAP_DUMP. */
	public static final String HEAP_DUMP = "heap";

	
	/** The heap dump method. */
	private final Method heapDumpMethod;

	
	/** The diagnostic m bean. */
	private final Object diagnosticMBean;

	
	/** The name. */
	private final String name;

	
	/**
	 * Instantiates a new heap dump contributor.
	 *
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public HeapDumpContributor(@Assisted String name, @Assisted Settings settings) {
		this.name = name;
		Method heapDumpMethod;
		Object diagnosticMBean;
		try {
			Class managementFactoryClass = Class.forName("sun.management.ManagementFactory", true,
					HeapDumpContributor.class.getClassLoader());
			Method method = managementFactoryClass.getMethod("getDiagnosticMXBean");
			diagnosticMBean = method.invoke(null);
			heapDumpMethod = diagnosticMBean.getClass().getMethod("dumpHeap", String.class, boolean.class);
		} catch (Exception _ex) {
			heapDumpMethod = null;
			diagnosticMBean = null;
		}
		this.heapDumpMethod = heapDumpMethod;
		this.diagnosticMBean = diagnosticMBean;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.monitor.dump.DumpContributor#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.monitor.dump.DumpContributor#contribute(cn.com.summall.search.core.monitor.dump.Dump)
	 */
	@Override
	public void contribute(Dump dump) throws DumpContributionFailedException {
		if (heapDumpMethod == null) {
			throw new DumpContributionFailedException(getName(), "Heap dump not enalbed on this JVM");
		}
		try {
			heapDumpMethod.invoke(diagnosticMBean, dump.createFile("heap.hprof").getAbsolutePath(), true);
		} catch (Exception e) {
			throw new DumpContributionFailedException(getName(), "Failed to generate heap dump", e);
		}
	}
}
