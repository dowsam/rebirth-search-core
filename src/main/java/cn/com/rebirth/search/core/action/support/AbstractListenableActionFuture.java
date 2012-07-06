/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AbstractListenableActionFuture.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support;

import java.util.List;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.ListenableActionFuture;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

import com.google.common.collect.Lists;

/**
 * The Class AbstractListenableActionFuture.
 *
 * @param <T> the generic type
 * @param <L> the generic type
 * @author l.xue.nong
 */
public abstract class AbstractListenableActionFuture<T, L> extends AdapterActionFuture<T, L> implements
		ListenableActionFuture<T> {

	/** The listener threaded. */
	final boolean listenerThreaded;

	/** The thread pool. */
	final ThreadPool threadPool;

	/** The listeners. */
	volatile Object listeners;

	/** The executed listeners. */
	boolean executedListeners = false;

	/**
	 * Instantiates a new abstract listenable action future.
	 *
	 * @param listenerThreaded the listener threaded
	 * @param threadPool the thread pool
	 */
	protected AbstractListenableActionFuture(boolean listenerThreaded, ThreadPool threadPool) {
		this.listenerThreaded = listenerThreaded;
		this.threadPool = threadPool;
	}

	/**
	 * Listener threaded.
	 *
	 * @return true, if successful
	 */
	public boolean listenerThreaded() {
		return false;
	}

	/**
	 * Thread pool.
	 *
	 * @return the thread pool
	 */
	public ThreadPool threadPool() {
		return threadPool;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ListenableActionFuture#addListener(cn.com.rebirth.search.core.action.ActionListener)
	 */
	public void addListener(final ActionListener<T> listener) {
		internalAddListener(listener);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ListenableActionFuture#addListener(java.lang.Runnable)
	 */
	public void addListener(final Runnable listener) {
		internalAddListener(listener);
	}

	/**
	 * Internal add listener.
	 *
	 * @param listener the listener
	 */
	public void internalAddListener(Object listener) {
		boolean executeImmediate = false;
		synchronized (this) {
			if (executedListeners) {
				executeImmediate = true;
			} else {
				Object listeners = this.listeners;
				if (listeners == null) {
					listeners = listener;
				} else if (listeners instanceof List) {
					((List) this.listeners).add(listener);
				} else {
					Object orig = listeners;
					listeners = Lists.newArrayListWithCapacity(2);
					((List) listeners).add(orig);
					((List) listeners).add(listener);
				}
				this.listeners = listeners;
			}
		}
		if (executeImmediate) {
			executeListener(listener);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.concurrent.BaseFuture#done()
	 */
	@Override
	protected void done() {
		super.done();
		synchronized (this) {
			executedListeners = true;
		}
		Object listeners = this.listeners;
		if (listeners != null) {
			if (listeners instanceof List) {
				List list = (List) listeners;
				for (Object listener : list) {
					executeListener(listener);
				}
			} else {
				executeListener(listeners);
			}
		}
	}

	/**
	 * Execute listener.
	 *
	 * @param listener the listener
	 */
	private void executeListener(final Object listener) {
		if (listenerThreaded) {
			if (listener instanceof Runnable) {
				threadPool.generic().execute((Runnable) listener);
			} else {
				threadPool.generic().execute(new Runnable() {
					@Override
					public void run() {
						ActionListener<T> lst = (ActionListener<T>) listener;
						try {
							lst.onResponse(actionGet());
						} catch (RebirthException e) {
							lst.onFailure(e);
						}
					}
				});
			}
		} else {
			if (listener instanceof Runnable) {
				((Runnable) listener).run();
			} else {
				ActionListener<T> lst = (ActionListener<T>) listener;
				try {
					lst.onResponse(actionGet());
				} catch (RebirthException e) {
					lst.onFailure(e);
				}
			}
		}
	}
}
