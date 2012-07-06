/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GatewaySnapshotAction.java 2012-7-6 14:30:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.gateway.snapshot;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;

/**
 * The Class GatewaySnapshotAction.
 *
 * @author l.xue.nong
 */
public class GatewaySnapshotAction extends
		IndicesAction<GatewaySnapshotRequest, GatewaySnapshotResponse, GatewaySnapshotRequestBuilder> {

	/** The Constant INSTANCE. */
	public static final GatewaySnapshotAction INSTANCE = new GatewaySnapshotAction();

	/** The Constant NAME. */
	public static final String NAME = "indices/gateway/snapshot";

	/**
	 * Instantiates a new gateway snapshot action.
	 */
	private GatewaySnapshotAction() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public GatewaySnapshotResponse newResponse() {
		return new GatewaySnapshotResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.rebirth.search.core.client.IndicesAdminClient)
	 */
	@Override
	public GatewaySnapshotRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new GatewaySnapshotRequestBuilder(client);
	}
}
