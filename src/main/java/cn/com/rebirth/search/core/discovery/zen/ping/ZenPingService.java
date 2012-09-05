/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ZenPingService.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.discovery.zen.ping;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import cn.com.rebirth.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.commons.network.NetworkService;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;
import cn.com.rebirth.search.core.discovery.zen.DiscoveryNodesProvider;
import cn.com.rebirth.search.core.discovery.zen.ping.multicast.MulticastZenPing;
import cn.com.rebirth.search.core.discovery.zen.ping.unicast.UnicastZenPing;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.ImmutableList;

/**
 * The Class ZenPingService.
 *
 * @author l.xue.nong
 */
public class ZenPingService extends AbstractLifecycleComponent<ZenPing> implements ZenPing {

	/** The zen pings. */
	private volatile ImmutableList<? extends ZenPing> zenPings = ImmutableList.of();

	/**
	 * Instantiates a new zen ping service.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param transportService the transport service
	 * @param clusterName the cluster name
	 * @param networkService the network service
	 */
	@Inject
	public ZenPingService(Settings settings, ThreadPool threadPool, TransportService transportService,
			ClusterName clusterName, NetworkService networkService) {
		super(settings);

		ImmutableList.Builder<ZenPing> zenPingsBuilder = ImmutableList.builder();
		if (componentSettings.getAsBoolean("multicast.enabled", true)) {
			zenPingsBuilder.add(new MulticastZenPing(settings, threadPool, transportService, clusterName,
					networkService));
		}

		zenPingsBuilder.add(new UnicastZenPing(settings, threadPool, transportService, clusterName));

		this.zenPings = zenPingsBuilder.build();
	}

	/**
	 * Zen pings.
	 *
	 * @return the immutable list<? extends zen ping>
	 */
	public ImmutableList<? extends ZenPing> zenPings() {
		return this.zenPings;
	}

	/**
	 * Zen pings.
	 *
	 * @param pings the pings
	 */
	public void zenPings(ImmutableList<? extends ZenPing> pings) {
		this.zenPings = pings;
		if (lifecycle.started()) {
			for (ZenPing zenPing : zenPings) {
				zenPing.start();
			}
		} else if (lifecycle.stopped()) {
			for (ZenPing zenPing : zenPings) {
				zenPing.stop();
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.discovery.zen.ping.ZenPing#setNodesProvider(cn.com.rebirth.search.core.discovery.zen.DiscoveryNodesProvider)
	 */
	@Override
	public void setNodesProvider(DiscoveryNodesProvider nodesProvider) {
		if (lifecycle.started()) {
			throw new RebirthIllegalStateException("Can't set nodes provider when started");
		}
		for (ZenPing zenPing : zenPings) {
			zenPing.setNodesProvider(nodesProvider);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RebirthException {
		for (ZenPing zenPing : zenPings) {
			zenPing.start();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RebirthException {
		for (ZenPing zenPing : zenPings) {
			zenPing.stop();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RebirthException {
		for (ZenPing zenPing : zenPings) {
			zenPing.close();
		}
	}

	/**
	 * Ping and wait.
	 *
	 * @param timeout the timeout
	 * @return the ping response[]
	 */
	public PingResponse[] pingAndWait(TimeValue timeout) {
		final AtomicReference<PingResponse[]> response = new AtomicReference<PingResponse[]>();
		final CountDownLatch latch = new CountDownLatch(1);
		ping(new PingListener() {
			@Override
			public void onPing(PingResponse[] pings) {
				response.set(pings);
				latch.countDown();
			}
		}, timeout);
		try {
			latch.await();
			return response.get();
		} catch (InterruptedException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.discovery.zen.ping.ZenPing#ping(cn.com.rebirth.search.core.discovery.zen.ping.ZenPing.PingListener, cn.com.rebirth.commons.unit.TimeValue)
	 */
	@Override
	public void ping(PingListener listener, TimeValue timeout) throws RebirthException {
		ImmutableList<? extends ZenPing> zenPings = this.zenPings;
		CompoundPingListener compoundPingListener = new CompoundPingListener(listener, zenPings);
		for (ZenPing zenPing : zenPings) {
			zenPing.ping(compoundPingListener, timeout);
		}
	}

	/**
	 * The listener interface for receiving compoundPing events.
	 * The class that is interested in processing a compoundPing
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addCompoundPingListener<code> method. When
	 * the compoundPing event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see CompoundPingEvent
	 */
	private static class CompoundPingListener implements PingListener {

		/** The listener. */
		private final PingListener listener;

		/** The zen pings. */
		private final ImmutableList<? extends ZenPing> zenPings;

		/** The counter. */
		private final AtomicInteger counter;

		/** The responses. */
		private ConcurrentMap<DiscoveryNode, PingResponse> responses = new ConcurrentHashMap<DiscoveryNode, PingResponse>();

		/**
		 * Instantiates a new compound ping listener.
		 *
		 * @param listener the listener
		 * @param zenPings the zen pings
		 */
		private CompoundPingListener(PingListener listener, ImmutableList<? extends ZenPing> zenPings) {
			this.listener = listener;
			this.zenPings = zenPings;
			this.counter = new AtomicInteger(zenPings.size());
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.discovery.zen.ping.ZenPing.PingListener#onPing(cn.com.rebirth.search.core.discovery.zen.ping.ZenPing.PingResponse[])
		 */
		@Override
		public void onPing(PingResponse[] pings) {
			if (pings != null) {
				for (PingResponse pingResponse : pings) {
					responses.put(pingResponse.target(), pingResponse);
				}
			}
			if (counter.decrementAndGet() == 0) {
				listener.onPing(responses.values().toArray(new PingResponse[responses.size()]));
			}
		}
	}
}
