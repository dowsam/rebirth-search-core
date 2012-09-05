/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportAction.java 2012-7-6 14:28:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support;

import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.action.ActionFuture;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ActionResponse;

/**
 * The Class TransportAction.
 *
 * @param <Request> the generic type
 * @param <Response> the generic type
 * @author l.xue.nong
 */
public abstract class TransportAction<Request extends ActionRequest, Response extends ActionResponse> extends
		AbstractComponent {

	/** The thread pool. */
	protected final ThreadPool threadPool;

	/**
	 * Instantiates a new transport action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 */
	protected TransportAction(Settings settings, ThreadPool threadPool) {
		super(settings);
		this.threadPool = threadPool;
	}

	/**
	 * Execute.
	 *
	 * @param request the request
	 * @return the action future
	 * @throws RebirthException the rebirth exception
	 */
	public ActionFuture<Response> execute(Request request) throws RebirthException {
		PlainActionFuture<Response> future = PlainActionFuture.newFuture();

		request.listenerThreaded(false);
		execute(request, future);
		return future;
	}

	/**
	 * Execute.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	public void execute(Request request, ActionListener<Response> listener) {
		if (request.listenerThreaded()) {
			listener = new ThreadedActionListener<Response>(threadPool, listener);
		}
		ActionRequestValidationException validationException = request.validate();
		if (validationException != null) {
			listener.onFailure(validationException);
			return;
		}
		try {
			doExecute(request, listener);
		} catch (Exception e) {
			listener.onFailure(e);
		}
	}

	/**
	 * Do execute.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	protected abstract void doExecute(Request request, ActionListener<Response> listener);

	/**
	 * The listener interface for receiving threadedAction events.
	 * The class that is interested in processing a threadedAction
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addThreadedActionListener<code> method. When
	 * the threadedAction event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @param <Response> the generic type
	 * @see ThreadedActionEvent
	 */
	static class ThreadedActionListener<Response> implements ActionListener<Response> {

		/** The thread pool. */
		private final ThreadPool threadPool;

		/** The listener. */
		private final ActionListener<Response> listener;

		/**
		 * Instantiates a new threaded action listener.
		 *
		 * @param threadPool the thread pool
		 * @param listener the listener
		 */
		ThreadedActionListener(ThreadPool threadPool, ActionListener<Response> listener) {
			this.threadPool = threadPool;
			this.listener = listener;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.ActionListener#onResponse(java.lang.Object)
		 */
		@Override
		public void onResponse(final Response response) {
			threadPool.generic().execute(new Runnable() {
				@Override
				public void run() {
					try {
						listener.onResponse(response);
					} catch (Exception e) {
						listener.onFailure(e);
					}
				}
			});
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.action.ActionListener#onFailure(java.lang.Throwable)
		 */
		@Override
		public void onFailure(final Throwable e) {
			threadPool.generic().execute(new Runnable() {
				@Override
				public void run() {
					listener.onFailure(e);
				}
			});
		}
	}
}
