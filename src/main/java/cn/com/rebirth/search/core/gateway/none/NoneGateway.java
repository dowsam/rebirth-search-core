/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NoneGateway.java 2012-7-6 14:30:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.gateway.none;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.gateway.Gateway;
import cn.com.rebirth.search.core.gateway.GatewayException;
import cn.com.rebirth.search.core.index.gateway.none.NoneIndexGatewayModule;

/**
 * The Class NoneGateway.
 *
 * @author l.xue.nong
 */
public class NoneGateway extends AbstractLifecycleComponent<Gateway> implements Gateway {

	/** The Constant TYPE. */
	public static final String TYPE = "none";

	/**
	 * Instantiates a new none gateway.
	 *
	 * @param settings the settings
	 */
	@Inject
	public NoneGateway(Settings settings) {
		super(settings);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.gateway.Gateway#type()
	 */
	@Override
	public String type() {
		return TYPE;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "_none_";
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
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.gateway.Gateway#performStateRecovery(cn.com.rebirth.search.core.gateway.Gateway.GatewayStateRecoveredListener)
	 */
	@Override
	public void performStateRecovery(GatewayStateRecoveredListener listener) throws GatewayException {
		logger.debug("performing state recovery");
		listener.onSuccess(ClusterState.builder().build());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.gateway.Gateway#suggestIndexGateway()
	 */
	@Override
	public Class<? extends Module> suggestIndexGateway() {
		return NoneIndexGatewayModule.class;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.gateway.Gateway#reset()
	 */
	@Override
	public void reset() {
	}
}
