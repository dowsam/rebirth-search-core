/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core Gateway.java 2012-3-29 15:00:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.gateway;

import cn.com.rebirth.search.commons.component.LifecycleComponent;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.core.cluster.ClusterState;

/**
 * The Interface Gateway.
 *
 * @author l.xue.nong
 */
public interface Gateway extends LifecycleComponent<Gateway> {

	/**
	 * Type.
	 *
	 * @return the string
	 */
	String type();

	/**
	 * Perform state recovery.
	 *
	 * @param listener the listener
	 * @throws GatewayException the gateway exception
	 */
	void performStateRecovery(GatewayStateRecoveredListener listener) throws GatewayException;

	/**
	 * Suggest index gateway.
	 *
	 * @return the class<? extends module>
	 */
	Class<? extends Module> suggestIndexGateway();

	/**
	 * Reset.
	 *
	 * @throws Exception the exception
	 */
	void reset() throws Exception;

	/**
	 * The listener interface for receiving gatewayStateRecovered events.
	 * The class that is interested in processing a gatewayStateRecovered
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addGatewayStateRecoveredListener<code> method. When
	 * the gatewayStateRecovered event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see GatewayStateRecoveredEvent
	 */
	interface GatewayStateRecoveredListener {

		/**
		 * On success.
		 *
		 * @param recoveredState the recovered state
		 */
		void onSuccess(ClusterState recoveredState);

		/**
		 * On failure.
		 *
		 * @param message the message
		 */
		void onFailure(String message);
	}
}
