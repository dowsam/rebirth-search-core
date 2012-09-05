/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DiscoveryService.java 2012-7-6 14:29:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.discovery;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNode;

/**
 * The Class DiscoveryService.
 *
 * @author l.xue.nong
 */
public class DiscoveryService extends AbstractLifecycleComponent<DiscoveryService> {

	/** The initial state timeout. */
	private final TimeValue initialStateTimeout;

	/** The discovery. */
	private final Discovery discovery;

	/** The initial state received. */
	private volatile boolean initialStateReceived;

	/**
	 * Instantiates a new discovery service.
	 *
	 * @param settings the settings
	 * @param discovery the discovery
	 */
	@Inject
	public DiscoveryService(Settings settings, Discovery discovery) {
		super(settings);
		this.discovery = discovery;
		this.initialStateTimeout = componentSettings.getAsTime("initial_state_timeout", TimeValue.timeValueSeconds(30));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RebirthException {
		final CountDownLatch latch = new CountDownLatch(1);
		InitialStateDiscoveryListener listener = new InitialStateDiscoveryListener() {
			@Override
			public void initialStateProcessed() {
				latch.countDown();
			}
		};
		discovery.addListener(listener);
		try {
			discovery.start();
			try {
				logger.trace("waiting for {} for the initial state to be set by the discovery", initialStateTimeout);
				if (latch.await(initialStateTimeout.millis(), TimeUnit.MILLISECONDS)) {
					logger.trace("initial state set from discovery");
					initialStateReceived = true;
				} else {
					initialStateReceived = false;
					logger.warn("waited for {} and no initial state was set by the discovery", initialStateTimeout);
				}
			} catch (InterruptedException e) {

			}
		} finally {
			discovery.removeListener(listener);
		}
		logger.info(discovery.nodeDescription());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RebirthException {
		discovery.stop();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RebirthException {
		discovery.close();
	}

	/**
	 * Local node.
	 *
	 * @return the discovery node
	 */
	public DiscoveryNode localNode() {
		return discovery.localNode();
	}

	/**
	 * Initial state received.
	 *
	 * @return true, if successful
	 */
	public boolean initialStateReceived() {
		return initialStateReceived;
	}

	/**
	 * Node description.
	 *
	 * @return the string
	 */
	public String nodeDescription() {
		return discovery.nodeDescription();
	}

	/**
	 * Publish.
	 *
	 * @param clusterState the cluster state
	 */
	public void publish(ClusterState clusterState) {
		if (!lifecycle.started()) {
			return;
		}
		discovery.publish(clusterState);
	}
}
