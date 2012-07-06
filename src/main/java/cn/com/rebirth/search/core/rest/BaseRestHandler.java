/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BaseRestHandler.java 2012-3-29 15:01:24 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.core.client.Client;


/**
 * The Class BaseRestHandler.
 *
 * @author l.xue.nong
 */
public abstract class BaseRestHandler extends AbstractComponent implements RestHandler {

	
	/** The client. */
	protected final Client client;

	
	/**
	 * Instantiates a new base rest handler.
	 *
	 * @param settings the settings
	 * @param client the client
	 */
	protected BaseRestHandler(Settings settings, Client client) {
		super(settings);
		this.client = client;
	}
}