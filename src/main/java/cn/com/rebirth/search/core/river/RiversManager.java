/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RiversManager.java 2012-3-29 15:02:09 l.xue.nong$$
 */


package cn.com.rebirth.search.core.river;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.river.cluster.RiverClusterService;
import cn.com.rebirth.search.core.river.routing.RiversRouter;


/**
 * The Class RiversManager.
 *
 * @author l.xue.nong
 */
public class RiversManager extends AbstractLifecycleComponent<RiversManager> {

	
	/** The rivers service. */
	private final RiversService riversService;

	
	/** The cluster service. */
	private final RiverClusterService clusterService;

	
	/** The rivers router. */
	private final RiversRouter riversRouter;

	
	/**
	 * Instantiates a new rivers manager.
	 *
	 * @param settings the settings
	 * @param riversService the rivers service
	 * @param clusterService the cluster service
	 * @param riversRouter the rivers router
	 */
	@Inject
	public RiversManager(Settings settings, RiversService riversService, RiverClusterService clusterService,
			RiversRouter riversRouter) {
		super(settings);
		this.riversService = riversService;
		this.clusterService = clusterService;
		this.riversRouter = riversRouter;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RestartException {
		riversRouter.start();
		riversService.start();
		clusterService.start();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RestartException {
		riversRouter.stop();
		clusterService.stop();
		riversService.stop();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RestartException {
		riversRouter.close();
		clusterService.close();
		riversService.close();
	}
}
