/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SigarProcessProbe.java 2012-7-6 14:29:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.process;

import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.monitor.sigar.SigarService;

/**
 * The Class SigarProcessProbe.
 *
 * @author l.xue.nong
 */
public class SigarProcessProbe extends AbstractComponent implements ProcessProbe {

	/** The sigar service. */
	private final SigarService sigarService;

	/**
	 * Instantiates a new sigar process probe.
	 *
	 * @param settings the settings
	 * @param sigarService the sigar service
	 */
	@Inject
	public SigarProcessProbe(Settings settings, SigarService sigarService) {
		super(settings);
		this.sigarService = sigarService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.monitor.process.ProcessProbe#processInfo()
	 */
	@Override
	public synchronized ProcessInfo processInfo() {
		return new ProcessInfo(sigarService.sigar().getPid(), JmxProcessProbe.getMaxFileDescriptorCount());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.monitor.process.ProcessProbe#processStats()
	 */
	@Override
	public synchronized ProcessStats processStats() {
		Sigar sigar = sigarService.sigar();
		ProcessStats stats = new ProcessStats();
		stats.timestamp = System.currentTimeMillis();
		stats.openFileDescriptors = JmxProcessProbe.getOpenFileDescriptorCount();

		try {
			ProcCpu cpu = sigar.getProcCpu(sigar.getPid());
			stats.cpu = new ProcessStats.Cpu();
			stats.cpu.percent = (short) (cpu.getPercent() * 100);
			stats.cpu.sys = cpu.getSys();
			stats.cpu.user = cpu.getUser();
			stats.cpu.total = cpu.getTotal();
		} catch (SigarException e) {

		}

		try {
			ProcMem mem = sigar.getProcMem(sigar.getPid());
			stats.mem = new ProcessStats.Mem();
			stats.mem.totalVirtual = mem.getSize();
			stats.mem.resident = mem.getResident();
			stats.mem.share = mem.getShare();
		} catch (SigarException e) {

		}

		return stats;
	}
}
