/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core UpdateSettingsAction.java 2012-3-29 15:00:49 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.settings;

import cn.com.rebirth.search.core.action.admin.indices.IndicesAction;
import cn.com.rebirth.search.core.client.IndicesAdminClient;


/**
 * The Class UpdateSettingsAction.
 *
 * @author l.xue.nong
 */
public class UpdateSettingsAction extends
		IndicesAction<UpdateSettingsRequest, UpdateSettingsResponse, UpdateSettingsRequestBuilder> {

	
	/** The Constant INSTANCE. */
	public static final UpdateSettingsAction INSTANCE = new UpdateSettingsAction();

	
	/** The Constant NAME. */
	public static final String NAME = "indices/settings/update";

	
	/**
	 * Instantiates a new update settings action.
	 */
	private UpdateSettingsAction() {
		super(NAME);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.GenericAction#newResponse()
	 */
	@Override
	public UpdateSettingsResponse newResponse() {
		return new UpdateSettingsResponse();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.admin.indices.IndicesAction#newRequestBuilder(cn.com.summall.search.core.client.IndicesAdminClient)
	 */
	@Override
	public UpdateSettingsRequestBuilder newRequestBuilder(IndicesAdminClient client) {
		return new UpdateSettingsRequestBuilder(client);
	}
}
