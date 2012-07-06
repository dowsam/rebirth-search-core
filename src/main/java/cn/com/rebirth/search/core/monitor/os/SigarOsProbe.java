/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SigarOsProbe.java 2012-3-29 15:02:32 l.xue.nong$$
 */


package cn.com.rebirth.search.core.monitor.os;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.monitor.sigar.SigarService;


/**
 * The Class SigarOsProbe.
 *
 * @author l.xue.nong
 */
public class SigarOsProbe extends AbstractComponent implements OsProbe {

	
	/** The sigar service. */
	private final SigarService sigarService;

	
	/**
	 * Instantiates a new sigar os probe.
	 *
	 * @param settings the settings
	 * @param sigarService the sigar service
	 */
	@Inject
	public SigarOsProbe(Settings settings, SigarService sigarService) {
		super(settings);
		this.sigarService = sigarService;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.monitor.os.OsProbe#osInfo()
	 */
	@Override
	public OsInfo osInfo() {
		Sigar sigar = sigarService.sigar();
		OsInfo info = new OsInfo();
		try {
			CpuInfo[] infos = sigar.getCpuInfoList();
			info.cpu = new OsInfo.Cpu();
			info.cpu.vendor = infos[0].getVendor();
			info.cpu.model = infos[0].getModel();
			info.cpu.mhz = infos[0].getMhz();
			info.cpu.totalCores = infos[0].getTotalCores();
			info.cpu.totalSockets = infos[0].getTotalSockets();
			info.cpu.coresPerSocket = infos[0].getCoresPerSocket();
			if (infos[0].getCacheSize() != Sigar.FIELD_NOTIMPL) {
				info.cpu.cacheSize = infos[0].getCacheSize();
			}
		} catch (SigarException e) {
			
		}

		try {
			Mem mem = sigar.getMem();
			info.mem = new OsInfo.Mem();
			info.mem.total = mem.getTotal();
		} catch (SigarException e) {
			
		}

		try {
			Swap swap = sigar.getSwap();
			info.swap = new OsInfo.Swap();
			info.swap.total = swap.getTotal();
		} catch (SigarException e) {
			
		}

		return info;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.monitor.os.OsProbe#osStats()
	 */
	@Override
	public OsStats osStats() {
		Sigar sigar = sigarService.sigar();
		OsStats stats = new OsStats();
		stats.timestamp = System.currentTimeMillis();
		try {
			stats.loadAverage = sigar.getLoadAverage();
		} catch (SigarException e) {
			
		}

		try {
			stats.uptime = (long) sigar.getUptime().getUptime();
		} catch (SigarException e) {
			
		}

		try {
			CpuPerc cpuPerc = sigar.getCpuPerc();
			stats.cpu = new OsStats.Cpu();
			stats.cpu.sys = (short) (cpuPerc.getSys() * 100);
			stats.cpu.user = (short) (cpuPerc.getUser() * 100);
			stats.cpu.idle = (short) (cpuPerc.getIdle() * 100);
		} catch (SigarException e) {
			
		}

		try {
			Mem mem = sigar.getMem();
			stats.mem = new OsStats.Mem();
			stats.mem.free = mem.getFree();
			stats.mem.freePercent = (short) mem.getFreePercent();
			stats.mem.used = mem.getUsed();
			stats.mem.usedPercent = (short) mem.getUsedPercent();
			stats.mem.actualFree = mem.getActualFree();
			stats.mem.actualUsed = mem.getActualUsed();
		} catch (SigarException e) {
			
		}

		try {
			Swap swap = sigar.getSwap();
			stats.swap = new OsStats.Swap();
			stats.swap.free = swap.getFree();
			stats.swap.used = swap.getUsed();
		} catch (SigarException e) {
			
		}

		return stats;
	}
}
