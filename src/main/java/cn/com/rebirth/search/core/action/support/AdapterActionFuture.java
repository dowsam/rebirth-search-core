/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AdapterActionFuture.java 2012-3-29 15:02:11 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.support;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cn.com.rebirth.commons.concurrent.BaseFuture;
import cn.com.rebirth.commons.concurrent.UncategorizedExecutionException;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.exception.RestartInterruptedException;
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
	 * @see cn.com.summall.search.core.action.ActionFuture#actionGet()
	 */
	@Override
	public T actionGet() throws RestartException {
		try {
			return get();
		} catch (InterruptedException e) {
			throw new RestartInterruptedException(e.getMessage());
		} catch (ExecutionException e) {
			throw rethrowExecutionException(e);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionFuture#actionGet(java.lang.String)
	 */
	@Override
	public T actionGet(String timeout) throws RestartException {
		return actionGet(TimeValue.parseTimeValue(timeout, null));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionFuture#actionGet(long)
	 */
	@Override
	public T actionGet(long timeoutMillis) throws RestartException {
		return actionGet(timeoutMillis, TimeUnit.MILLISECONDS);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionFuture#actionGet(cn.com.summall.search.commons.unit.TimeValue)
	 */
	@Override
	public T actionGet(TimeValue timeout) throws RestartException {
		return actionGet(timeout.millis(), TimeUnit.MILLISECONDS);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionFuture#actionGet(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public T actionGet(long timeout, TimeUnit unit) throws RestartException {
		try {
			return get(timeout, unit);
		} catch (TimeoutException e) {
			throw new RestartTimeoutException(e.getMessage());
		} catch (InterruptedException e) {
			throw new RestartInterruptedException(e.getMessage());
		} catch (ExecutionException e) {
			throw rethrowExecutionException(e);
		}
	}

	
	/**
	 * Rethrow execution exception.
	 *
	 * @param e the e
	 * @return the sum mall search exception
	 */
	static RestartException rethrowExecutionException(ExecutionException e) {
		if (e.getCause() instanceof RestartException) {
			RestartException esEx = (RestartException) e.getCause();
			Throwable root = esEx.unwrapCause();
			if (root instanceof RestartException) {
				return (RestartException) root;
			}
			return new UncategorizedExecutionException("Failed execution", root);
		} else {
			return new UncategorizedExecutionException("Failed execution", e);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionListener#onResponse(java.lang.Object)
	 */
	@Override
	public void onResponse(L result) {
		set(convert(result));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionListener#onFailure(java.lang.Throwable)
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
	 * @see cn.com.summall.search.core.action.ActionFuture#getRootFailure()
	 */
	@Override
	public Throwable getRootFailure() {
		return rootFailure;
	}
}
