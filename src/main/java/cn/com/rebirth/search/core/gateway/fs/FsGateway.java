/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FsGateway.java 2012-7-6 14:28:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.gateway.fs;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.concurrent.EsExecutors;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.blobstore.fs.FsBlobStore;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.gateway.blobstore.BlobStoreGateway;
import cn.com.rebirth.search.core.index.gateway.fs.FsIndexGatewayModule;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

/**
 * The Class FsGateway.
 *
 * @author l.xue.nong
 */
public class FsGateway extends BlobStoreGateway {

	/** The concurrent stream pool. */
	private final ExecutorService concurrentStreamPool;

	/**
	 * Instantiates a new fs gateway.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param environment the environment
	 * @param clusterName the cluster name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Inject
	public FsGateway(Settings settings, ThreadPool threadPool, ClusterService clusterService, Environment environment,
			ClusterName clusterName) throws IOException {
		super(settings, threadPool, clusterService);

		File gatewayFile;
		String location = componentSettings.get("location");
		if (location == null) {
			logger.warn("using local fs location for gateway, should be changed to be a shared location across nodes");
			gatewayFile = new File(environment.dataFiles()[0], "gateway");
		} else {
			gatewayFile = new File(location);
		}

		int concurrentStreams = componentSettings.getAsInt("concurrent_streams", 5);
		this.concurrentStreamPool = EsExecutors.newScalingExecutorService(1, concurrentStreams, 60, TimeUnit.SECONDS,
				EsExecutors.daemonThreadFactory(settings, "[fs_stream]"));

		initialize(new FsBlobStore(componentSettings, concurrentStreamPool, gatewayFile), clusterName, null);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.gateway.Gateway#type()
	 */
	@Override
	public String type() {
		return "fs";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.gateway.Gateway#suggestIndexGateway()
	 */
	@Override
	public Class<? extends Module> suggestIndexGateway() {
		return FsIndexGatewayModule.class;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.gateway.shared.SharedStorageGateway#doClose()
	 */
	@Override
	protected void doClose() throws RebirthException {
		super.doClose();
		concurrentStreamPool.shutdown();
	}
}
