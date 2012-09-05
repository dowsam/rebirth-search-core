/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SharedStorageGateway.java 2012-7-6 14:29:59 l.xue.nong$$
 */

package cn.com.rebirth.search.core.gateway.shared;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.StopWatch;
import cn.com.rebirth.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.commons.concurrent.EsExecutors;
import cn.com.rebirth.commons.exception.ExceptionsHelper;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.cluster.ClusterChangedEvent;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.ClusterStateListener;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.gateway.Gateway;
import cn.com.rebirth.search.core.gateway.GatewayException;

/**
 * The Class SharedStorageGateway.
 *
 * @author l.xue.nong
 */
public abstract class SharedStorageGateway extends AbstractLifecycleComponent<Gateway> implements Gateway,
		ClusterStateListener {

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The thread pool. */
	private final ThreadPool threadPool;

	/** The write state executor. */
	private ExecutorService writeStateExecutor;

	/**
	 * Instantiates a new shared storage gateway.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 */
	public SharedStorageGateway(Settings settings, ThreadPool threadPool, ClusterService clusterService) {
		super(settings);
		this.threadPool = threadPool;
		this.clusterService = clusterService;
		this.writeStateExecutor = newSingleThreadExecutor(EsExecutors.daemonThreadFactory(settings,
				"gateway#writeMetaData"));
		clusterService.add(this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RebirthException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RebirthException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RebirthException {
		clusterService.remove(this);
		writeStateExecutor.shutdown();
		try {
			writeStateExecutor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {

		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.gateway.Gateway#performStateRecovery(cn.com.rebirth.search.core.gateway.Gateway.GatewayStateRecoveredListener)
	 */
	@Override
	public void performStateRecovery(final GatewayStateRecoveredListener listener) throws GatewayException {
		threadPool.generic().execute(new Runnable() {
			@Override
			public void run() {
				logger.debug("reading state from gateway {} ...", this);
				StopWatch stopWatch = new StopWatch().start();
				MetaData metaData;
				try {
					metaData = read();
					logger.debug("read state from gateway {}, took {}", this, stopWatch.stop().totalTime());
					if (metaData == null) {
						logger.debug("no state read from gateway");
						listener.onSuccess(ClusterState.builder().build());
					} else {
						listener.onSuccess(ClusterState.builder().metaData(metaData).build());
					}
				} catch (Exception e) {
					logger.error("failed to read from gateway", e);
					listener.onFailure(ExceptionsHelper.detailedMessage(e));
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.cluster.ClusterStateListener#clusterChanged(cn.com.rebirth.search.core.cluster.ClusterChangedEvent)
	 */
	@Override
	public void clusterChanged(final ClusterChangedEvent event) {
		if (!lifecycle.started()) {
			return;
		}

		if (event.state().blocks().disableStatePersistence()) {
			return;
		}

		if (event.localNodeMaster()) {
			if (!event.metaDataChanged()) {
				return;
			}
			writeStateExecutor.execute(new Runnable() {
				@Override
				public void run() {
					logger.debug("writing to gateway {} ...", this);
					StopWatch stopWatch = new StopWatch().start();
					try {
						write(event.state().metaData());
						logger.debug("wrote to gateway {}, took {}", this, stopWatch.stop().totalTime());

					} catch (Exception e) {
						logger.error("failed to write to gateway", e);
					}
				}
			});
		}
	}

	/**
	 * Read.
	 *
	 * @return the meta data
	 * @throws RebirthException the rebirth exception
	 */
	protected abstract MetaData read() throws RebirthException;

	/**
	 * Write.
	 *
	 * @param metaData the meta data
	 * @throws RebirthException the rebirth exception
	 */
	protected abstract void write(MetaData metaData) throws RebirthException;
}
