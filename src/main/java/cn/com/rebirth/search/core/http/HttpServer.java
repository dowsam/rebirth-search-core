/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core HttpServer.java 2012-4-25 10:02:29 l.xue.nong$$
 */


package cn.com.rebirth.search.core.http;

import static cn.com.rebirth.search.core.rest.RestStatus.FORBIDDEN;
import static cn.com.rebirth.search.core.rest.RestStatus.INTERNAL_SERVER_ERROR;
import static cn.com.rebirth.search.core.rest.RestStatus.NOT_FOUND;
import static cn.com.rebirth.search.core.rest.RestStatus.OK;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.Streams;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.node.service.NodeService;
import cn.com.rebirth.search.core.rest.BytesRestResponse;
import cn.com.rebirth.search.core.rest.RestChannel;
import cn.com.rebirth.search.core.rest.RestController;
import cn.com.rebirth.search.core.rest.RestFilter;
import cn.com.rebirth.search.core.rest.RestFilterChain;
import cn.com.rebirth.search.core.rest.RestRequest;
import cn.com.rebirth.search.core.rest.StringRestResponse;

import com.google.common.collect.ImmutableMap;


/**
 * The Class HttpServer.
 *
 * @author l.xue.nong
 */
public class HttpServer extends AbstractLifecycleComponent<HttpServer> {

	
	/** The environment. */
	private final Environment environment;

	
	/** The transport. */
	private final HttpServerTransport transport;

	
	/** The rest controller. */
	private final RestController restController;

	
	/** The node service. */
	private final NodeService nodeService;

	
	/** The disable sites. */
	private final boolean disableSites;

	
	/** The plugin site filter. */
	private final PluginSiteFilter pluginSiteFilter = new PluginSiteFilter();

	
	/**
	 * Instantiates a new http server.
	 *
	 * @param settings the settings
	 * @param environment the environment
	 * @param transport the transport
	 * @param restController the rest controller
	 * @param nodeService the node service
	 */
	@Inject
	public HttpServer(Settings settings, Environment environment, HttpServerTransport transport,
			RestController restController, NodeService nodeService) {
		super(settings);
		this.environment = environment;
		this.transport = transport;
		this.restController = restController;
		this.nodeService = nodeService;
		nodeService.setHttpServer(this);

		this.disableSites = componentSettings.getAsBoolean("disable_sites", false);

		transport.httpServerAdapter(new Dispatcher(this));
	}

	
	/**
	 * The Class Dispatcher.
	 *
	 * @author l.xue.nong
	 */
	static class Dispatcher implements HttpServerAdapter {

		
		/** The server. */
		private final HttpServer server;

		
		/**
		 * Instantiates a new dispatcher.
		 *
		 * @param server the server
		 */
		Dispatcher(HttpServer server) {
			this.server = server;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.http.HttpServerAdapter#dispatchRequest(cn.com.summall.search.core.http.HttpRequest, cn.com.summall.search.core.http.HttpChannel)
		 */
		@Override
		public void dispatchRequest(HttpRequest request, HttpChannel channel) {
			server.internalDispatchRequest(request, channel);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RestartException {
		transport.start();
		if (logger.isInfoEnabled()) {
			logger.info("{}", transport.boundAddress());
		}
		nodeService.putAttribute("http_address", transport.boundAddress().publishAddress().toString());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RestartException {
		nodeService.removeAttribute("http_address");
		transport.stop();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RestartException {
		transport.close();
	}

	
	/**
	 * Info.
	 *
	 * @return the http info
	 */
	public HttpInfo info() {
		return new HttpInfo(transport.boundAddress());
	}

	
	/**
	 * Stats.
	 *
	 * @return the http stats
	 */
	public HttpStats stats() {
		return transport.stats();
	}

	
	/**
	 * Internal dispatch request.
	 *
	 * @param request the request
	 * @param channel the channel
	 */
	public void internalDispatchRequest(final HttpRequest request, final HttpChannel channel) {
		if (request.rawPath().startsWith("/_plugin/")) {
			RestFilterChain filterChain = restController.filterChain(pluginSiteFilter);
			filterChain.continueProcessing(request, channel);
			return;
		}
		restController.dispatchRequest(request, channel);
	}

	
	/**
	 * The Class PluginSiteFilter.
	 *
	 * @author l.xue.nong
	 */
	class PluginSiteFilter extends RestFilter {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.rest.RestFilter#process(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel, cn.com.summall.search.core.rest.RestFilterChain)
		 */
		@Override
		public void process(RestRequest request, RestChannel channel, RestFilterChain filterChain) {
			handlePluginSite((HttpRequest) request, (HttpChannel) channel);
		}
	}

	
	/**
	 * Handle plugin site.
	 *
	 * @param request the request
	 * @param channel the channel
	 */
	void handlePluginSite(HttpRequest request, HttpChannel channel) {
		if (disableSites) {
			channel.sendResponse(new StringRestResponse(FORBIDDEN));
			return;
		}
		if (request.method() == RestRequest.Method.OPTIONS) {
			
			StringRestResponse response = new StringRestResponse(OK);
			channel.sendResponse(response);
			return;
		}
		if (request.method() != RestRequest.Method.GET) {
			channel.sendResponse(new StringRestResponse(FORBIDDEN));
			return;
		}
		

		String path = request.rawPath().substring("/_plugin/".length());
		int i1 = path.indexOf('/');
		String pluginName;
		String sitePath;
		if (i1 == -1) {
			pluginName = path;
			sitePath = null;
			
			
			channel.sendResponse(new StringRestResponse(NOT_FOUND));
			return;
		} else {
			pluginName = path.substring(0, i1);
			sitePath = path.substring(i1 + 1);
		}

		if (sitePath.length() == 0) {
			sitePath = "/index.html";
		}

		
		sitePath = sitePath.replace('/', File.separatorChar);

		
		File siteFile = new File(new File(environment.pluginsFile(), pluginName), "_site");
		File file = new File(siteFile, sitePath);
		if (!file.exists() || file.isHidden()) {
			channel.sendResponse(new StringRestResponse(NOT_FOUND));
			return;
		}
		if (!file.isFile()) {
			channel.sendResponse(new StringRestResponse(FORBIDDEN));
			return;
		}
		if (!file.getAbsolutePath().startsWith(siteFile.getAbsolutePath())) {
			channel.sendResponse(new StringRestResponse(FORBIDDEN));
			return;
		}
		try {
			byte[] data = Streams.copyToByteArray(file);
			channel.sendResponse(new BytesRestResponse(data, guessMimeType(sitePath)));
		} catch (IOException e) {
			channel.sendResponse(new StringRestResponse(INTERNAL_SERVER_ERROR));
		}
	}

	
	
	/**
	 * Guess mime type.
	 *
	 * @param path the path
	 * @return the string
	 */
	private String guessMimeType(String path) {
		int lastDot = path.lastIndexOf('.');
		if (lastDot == -1) {
			return "";
		}
		String extension = path.substring(lastDot + 1).toLowerCase();
		String mimeType = DEFAULT_MIME_TYPES.get(extension);
		if (mimeType == null) {
			return "";
		}
		return mimeType;
	}

	static {
		
		Map<String, String> mimeTypes = new HashMap<String, String>();
		mimeTypes.put("txt", "text/plain");
		mimeTypes.put("css", "text/css");
		mimeTypes.put("csv", "text/csv");
		mimeTypes.put("htm", "text/html");
		mimeTypes.put("html", "text/html");
		mimeTypes.put("xml", "text/xml");
		mimeTypes.put("js", "text/javascript"); 
		mimeTypes.put("xhtml", "application/xhtml+xml");
		mimeTypes.put("json", "application/json");
		mimeTypes.put("pdf", "application/pdf");
		mimeTypes.put("zip", "application/zip");
		mimeTypes.put("tar", "application/x-tar");
		mimeTypes.put("gif", "image/gif");
		mimeTypes.put("jpeg", "image/jpeg");
		mimeTypes.put("jpg", "image/jpeg");
		mimeTypes.put("tiff", "image/tiff");
		mimeTypes.put("tif", "image/tiff");
		mimeTypes.put("png", "image/png");
		mimeTypes.put("svg", "image/svg+xml");
		mimeTypes.put("ico", "image/vnd.microsoft.icon");
		DEFAULT_MIME_TYPES = ImmutableMap.copyOf(mimeTypes);
	}

	
	/** The Constant DEFAULT_MIME_TYPES. */
	public static final Map<String, String> DEFAULT_MIME_TYPES;
}
