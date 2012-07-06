/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AdapterActionFuture.java 2012-7-6 14:30:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.support;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cn.com.rebirth.commons.concurrent.BaseFuture;
import cn.com.rebirth.commons.concurrent.UncategorizedExecutionException;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.exception.RebirthInterruptedException;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.RestartTimeoutException;
import cn.com.rebirth.search.core.action.ActionFuture;
import cn.com.rebirth.search.core.action.ActionListener;

/**
 * The Class AdapterActionFuture.
 *
 * @param <T> the generic type
 * @param <L> the generic type
 * @author l.xue.nong
 */
public abstract class AdapterActionFuture<T, L> extends BaseFuture<T> implements ActionFuture<T>, ActionListener<L> {

	/** The root failure. */
	private Throwable rootFailure;

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionFuture#actionGet()
	 */
	@Override
	public T actionGet() throws RebirthException {
		try {
			return get();
		} catch (InterruptedException e) {
			throw new RebirthInterruptedException(e.getMessage());
		} catch (ExecutionException e) {
			throw rethrowExecutionException(e);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionFuture#actionGet(java.lang.String)
	 */
	@Override
	public T actionGet(String timeout) throws RebirthException {
		return actionGet(TimeValue.parseTimeValue(timeout, null));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionFuture#actionGet(long)
	 */
	@Override
	public T actionGet(long timeoutMillis) throws RebirthException {
		return actionGet(timeoutMillis, TimeUnit.MILLISECONDS);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionFuture#actionGet(cn.com.rebirth.commons.unit.TimeValue)
	 */
	@Override
	public T actionGet(TimeValue timeout) throws RebirthException {
		return actionGet(timeout.millis(), TimeUnit.MILLISECONDS);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionFuture#actionGet(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public T actionGet(long timeout, TimeUnit unit) throws RebirthException {
		try {
			return get(timeout, unit);
		} catch (TimeoutException e) {
			throw new RestartTimeoutException(e.getMessage());
		} catch (InterruptedException e) {
			throw new RebirthInterruptedException(e.getMessage());
		} catch (ExecutionException e) {
			throw rethrowExecutionException(e);
		}
	}

	/**
	 * Rethrow execution exception.
	 *
	 * @param e the e
	 * @return the rebirth exception
	 */
	static RebirthException rethrowExecutionException(ExecutionException e) {
		if (e.getCause() instanceof RebirthException) {
			RebirthException esEx = (RebirthException) e.getCause();
			Throwable root = esEx.unwrapCause();
			if (root instanceof RebirthException) {
				return (RebirthException) root;
			}
			return new UncategorizedExecutionException("Failed execution", root);
		} else {
			return new UncategorizedExecutionException("Failed execution", e);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionListener#onResponse(java.lang.Object)
	 */
	@Override
	public void onResponse(L result) {
		set(convert(result));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionListener#onFailure(java.lang.Throwable)
	 */
	@Override
	public void onFailure(Throwable e) {
		setException(e);
	}

	/**
	 * Convert.
	 *
	 * @param listenerResponse the listener response
	 * @return the t
	 */
	protected abstract T convert(L listenerResponse);

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionFuture#getRootFailure()
	 */
	@Override
	public Throwable getRootFailure() {
		return rootFailure;
	}
}
