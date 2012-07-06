/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NetworkService.java 2012-7-6 14:29:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;

/**
 * The Class NetworkService.
 *
 * @author l.xue.nong
 */
public class NetworkService extends AbstractComponent {

	/** The probe. */
	private final NetworkProbe probe;

	/** The info. */
	private final NetworkInfo info;

	/** The refresh interval. */
	private final TimeValue refreshInterval;

	/** The cached stats. */
	private NetworkStats cachedStats;

	/**
	 * Instantiates a new network service.
	 *
	 * @param settings the settings
	 * @param probe the probe
	 */
	@Inject
	public NetworkService(Settings settings, NetworkProbe probe) {
		super(settings);
		this.probe = probe;

		this.refreshInterval = componentSettings.getAsTime("refresh_interval", TimeValue.timeValueSeconds(5));

		logger.debug("Using probe [{}] with refresh_interval [{}]", probe, refreshInterval);

		this.info = probe.networkInfo();
		this.info.refreshInterval = refreshInterval.millis();
		this.cachedStats = probe.networkStats();

		if (logger.isDebugEnabled()) {
			StringBuilder netDebug = new StringBuilder("net_info");
			try {
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				String hostName = InetAddress.getLocalHost().getHostName();
				netDebug.append("\nhost [").append(hostName).append("]\n");
				while (interfaces.hasMoreElements()) {
					NetworkInterface net = interfaces.nextElement();

					netDebug.append(net.getName()).append('\t').append("display_name [").append(net.getDisplayName())
							.append("]\n");
					Enumeration<InetAddress> addresses = net.getInetAddresses();
					netDebug.append("\t\taddress ");
					while (addresses.hasMoreElements()) {
						netDebug.append("[").append(addresses.nextElement()).append("] ");
					}
					netDebug.append('\n');
					netDebug.append("\t\tmtu [").append(net.getMTU()).append("] multicast [")
							.append(net.supportsMulticast()).append("] ptp [").append(net.isPointToPoint())
							.append("] loopback [").append(net.isLoopback()).append("] up [").append(net.isUp())
							.append("] virtual [").append(net.isVirtual()).append("]").append('\n');

					Enumeration<NetworkInterface> subInterfaces = net.getSubInterfaces();
					if (subInterfaces != null && subInterfaces.hasMoreElements()) {
						netDebug.append("\t\t\tsub interfaces:\n");

						while (subInterfaces.hasMoreElements()) {

							net = subInterfaces.nextElement();

							netDebug.append("\t\t\t").append(net.getName()).append("\t").append("display_name [")
									.append(net.getDisplayName()).append("]\n");
							addresses = net.getInetAddresses();
							netDebug.append("\t\t\t\t\taddress ");
							while (addresses.hasMoreElements()) {
								netDebug.append("[").append(addresses.nextElement()).append("] ");
							}
							netDebug.append('\n');
							netDebug.append("\t\t\t\t\tmtu [").append(net.getMTU()).append("] multicast [")
									.append(net.supportsMulticast()).append("] ptp [").append(net.isPointToPoint())
									.append("] loopback [").append(net.isLoopback()).append("] up [")
									.append(net.isUp()).append("] virtual [").append(net.isVirtual()).append("]")
									.append('\n');
						}
					}
				}
			} catch (Exception ex) {
				netDebug.append("failed to get Network Interface Info [" + ex.getMessage() + "]");
			}
			logger.debug(netDebug.toString());
		}

		if (logger.isTraceEnabled()) {
			logger.trace("ifconfig\n\n" + ifconfig());
		}
	}

	/**
	 * Info.
	 *
	 * @return the network info
	 */
	public NetworkInfo info() {
		return this.info;
	}

	/**
	 * Stats.
	 *
	 * @return the network stats
	 */
	public synchronized NetworkStats stats() {
		if ((System.currentTimeMillis() - cachedStats.timestamp()) > refreshInterval.millis()) {
			cachedStats = probe.networkStats();
		}
		return cachedStats;
	}

	/**
	 * Ifconfig.
	 *
	 * @return the string
	 */
	public String ifconfig() {
		return probe.ifconfig();
	}
}
