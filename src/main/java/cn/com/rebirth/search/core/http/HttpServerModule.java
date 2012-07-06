/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core HttpServerModule.java 2012-4-25 10:02:45 l.xue.nong$$
 */


package cn.com.rebirth.search.core.http;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.inject.Modules;
import cn.com.rebirth.search.commons.inject.SpawnModules;
import cn.com.rebirth.search.core.http.netty.NettyHttpServerTransportModule;

import com.google.common.collect.ImmutableList;


/**
 * The Class HttpServerModule.
 *
 * @author l.xue.nong
 */
public class HttpServerModule extends AbstractModule implements SpawnModules {

	
	/** The settings. */
	private final Settings settings;

	
	/**
	 * Instantiates a new http server module.
	 *
	 * @param settings the settings
	 */
	public HttpServerModule(Settings settings) {
		this.settings = settings;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.SpawnModules#spawnModules()
	 */
	@Override
	public Iterable<? extends Module> spawnModules() {
		return ImmutableList
				.of(Modules.createModule(settings.getAsClass("http.type", NettyHttpServerTransportModule.class,
						"cn.com.summall.search.core.http.", "HttpServerTransportModule"), settings));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	protected void configure() {
		bind(HttpServer.class).asEagerSingleton();
	}
}