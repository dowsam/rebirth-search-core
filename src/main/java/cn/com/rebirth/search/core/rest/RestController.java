/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestController.java 2012-3-29 15:01:44 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.exception.RestartIllegalStateException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.path.PathTrie;
import cn.com.rebirth.search.core.rest.support.RestUtils;


/**
 * The Class RestController.
 *
 * @author l.xue.nong
 */
public class RestController extends AbstractLifecycleComponent<RestController> {

	
	/** The get handlers. */
	private final PathTrie<RestHandler> getHandlers = new PathTrie<RestHandler>(RestUtils.REST_DECODER);

	
	/** The post handlers. */
	private final PathTrie<RestHandler> postHandlers = new PathTrie<RestHandler>(RestUtils.REST_DECODER);

	
	/** The put handlers. */
	private final PathTrie<RestHandler> putHandlers = new PathTrie<RestHandler>(RestUtils.REST_DECODER);

	
	/** The delete handlers. */
	private final PathTrie<RestHandler> deleteHandlers = new PathTrie<RestHandler>(RestUtils.REST_DECODER);

	
	/** The head handlers. */
	private final PathTrie<RestHandler> headHandlers = new PathTrie<RestHandler>(RestUtils.REST_DECODER);

	
	/** The options handlers. */
	private final PathTrie<RestHandler> optionsHandlers = new PathTrie<RestHandler>(RestUtils.REST_DECODER);

	
	/** The handler filter. */
	private final RestHandlerFilter handlerFilter = new RestHandlerFilter();

	
	
	/** The filters. */
	private RestFilter[] filters = new RestFilter[0];

	
	/**
	 * Instantiates a new rest controller.
	 *
	 * @param settings the settings
	 */
	@Inject
	public RestController(Settings settings) {
		super(settings);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RestartException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RestartException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RestartException {
		for (RestFilter filter : filters) {
			filter.close();
		}
	}

	
	/**
	 * Register filter.
	 *
	 * @param preProcessor the pre processor
	 */
	public synchronized void registerFilter(RestFilter preProcessor) {
		RestFilter[] copy = new RestFilter[filters.length + 1];
		System.arraycopy(filters, 0, copy, 0, filters.length);
		copy[filters.length] = preProcessor;
		Arrays.sort(copy, new Comparator<RestFilter>() {
			@Override
			public int compare(RestFilter o1, RestFilter o2) {
				return o2.order() - o1.order();
			}
		});
		filters = copy;
	}

	
	/**
	 * Register handler.
	 *
	 * @param method the method
	 * @param path the path
	 * @param handler the handler
	 */
	public void registerHandler(RestRequest.Method method, String path, RestHandler handler) {
		switch (method) {
		case GET:
			getHandlers.insert(path, handler);
			break;
		case DELETE:
			deleteHandlers.insert(path, handler);
			break;
		case POST:
			postHandlers.insert(path, handler);
			break;
		case PUT:
			putHandlers.insert(path, handler);
			break;
		case OPTIONS:
			optionsHandlers.insert(path, handler);
			break;
		case HEAD:
			headHandlers.insert(path, handler);
			break;
		default:
			throw new RestartIllegalArgumentException("Can't handle [" + method + "] for path [" + path + "]");
		}
	}

	
	/**
	 * Filter chain or null.
	 *
	 * @param executionFilter the execution filter
	 * @return the rest filter chain
	 */
	@Nullable
	public RestFilterChain filterChainOrNull(RestFilter executionFilter) {
		if (filters.length == 0) {
			return null;
		}
		return new ControllerFilterChain(executionFilter);
	}

	
	/**
	 * Filter chain.
	 *
	 * @param executionFilter the execution filter
	 * @return the rest filter chain
	 */
	public RestFilterChain filterChain(RestFilter executionFilter) {
		return new ControllerFilterChain(executionFilter);
	}

	
	/**
	 * Dispatch request.
	 *
	 * @param request the request
	 * @param channel the channel
	 */
	public void dispatchRequest(final RestRequest request, final RestChannel channel) {
		if (filters.length == 0) {
			try {
				executeHandler(request, channel);
			} catch (Exception e) {
				try {
					channel.sendResponse(new XContentThrowableRestResponse(request, e));
				} catch (IOException e1) {
					logger.error("Failed to send failure response for uri [" + request.uri() + "]", e1);
				}
			}
		} else {
			ControllerFilterChain filterChain = new ControllerFilterChain(handlerFilter);
			filterChain.continueProcessing(request, channel);
		}
	}

	
	/**
	 * Execute handler.
	 *
	 * @param request the request
	 * @param channel the channel
	 */
	void executeHandler(RestRequest request, RestChannel channel) {
		final RestHandler handler = getHandler(request);
		if (handler != null) {
			handler.handleRequest(request, channel);
		} else {
			if (request.method() == RestRequest.Method.OPTIONS) {
				
				StringRestResponse response = new StringRestResponse(RestStatus.OK);
				channel.sendResponse(response);
			} else {
				channel.sendResponse(new StringRestResponse(RestStatus.BAD_REQUEST, "No handler found for uri ["
						+ request.uri() + "] and method [" + request.method() + "]"));
			}
		}
	}

	
	/**
	 * Gets the handler.
	 *
	 * @param request the request
	 * @return the handler
	 */
	private RestHandler getHandler(RestRequest request) {
		String path = getPath(request);
		RestRequest.Method method = request.method();
		if (method == RestRequest.Method.GET) {
			return getHandlers.retrieve(path, request.params());
		} else if (method == RestRequest.Method.POST) {
			return postHandlers.retrieve(path, request.params());
		} else if (method == RestRequest.Method.PUT) {
			return putHandlers.retrieve(path, request.params());
		} else if (method == RestRequest.Method.DELETE) {
			return deleteHandlers.retrieve(path, request.params());
		} else if (method == RestRequest.Method.HEAD) {
			return headHandlers.retrieve(path, request.params());
		} else if (method == RestRequest.Method.OPTIONS) {
			return optionsHandlers.retrieve(path, request.params());
		} else {
			return null;
		}
	}

	
	/**
	 * Gets the path.
	 *
	 * @param request the request
	 * @return the path
	 */
	private String getPath(RestRequest request) {
		
		
		
		return request.rawPath();
	}

	
	/**
	 * The Class ControllerFilterChain.
	 *
	 * @author l.xue.nong
	 */
	class ControllerFilterChain implements RestFilterChain {

		
		/** The execution filter. */
		private final RestFilter executionFilter;

		
		/** The index. */
		private volatile int index;

		
		/**
		 * Instantiates a new controller filter chain.
		 *
		 * @param executionFilter the execution filter
		 */
		ControllerFilterChain(RestFilter executionFilter) {
			this.executionFilter = executionFilter;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.rest.RestFilterChain#continueProcessing(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel)
		 */
		@Override
		public void continueProcessing(RestRequest request, RestChannel channel) {
			try {
				int loc = index;
				if (loc > filters.length) {
					throw new RestartIllegalStateException(
							"filter continueProcessing was called more than expected");
				} else if (loc == filters.length) {
					executionFilter.process(request, channel, this);
				} else {
					RestFilter preProcessor = filters[loc];
					preProcessor.process(request, channel, this);
				}
				index++;
			} catch (Exception e) {
				try {
					channel.sendResponse(new XContentThrowableRestResponse(request, e));
				} catch (IOException e1) {
					logger.error("Failed to send failure response for uri [" + request.uri() + "]", e1);
				}
			}
		}
	}

	
	/**
	 * The Class RestHandlerFilter.
	 *
	 * @author l.xue.nong
	 */
	class RestHandlerFilter extends RestFilter {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.rest.RestFilter#process(cn.com.summall.search.core.rest.RestRequest, cn.com.summall.search.core.rest.RestChannel, cn.com.summall.search.core.rest.RestFilterChain)
		 */
		@Override
		public void process(RestRequest request, RestChannel channel, RestFilterChain filterChain) {
			executeHandler(request, channel);
		}
	}
}
