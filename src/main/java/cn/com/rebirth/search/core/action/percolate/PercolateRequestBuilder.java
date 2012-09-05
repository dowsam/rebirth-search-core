/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PercolateRequestBuilder.java 2012-7-6 14:29:19 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.percolate;

import java.util.Map;

import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.support.BaseRequestBuilder;
import cn.com.rebirth.search.core.client.Client;

/**
 * The Class PercolateRequestBuilder.
 *
 * @author l.xue.nong
 */
public class PercolateRequestBuilder extends BaseRequestBuilder<PercolateRequest, PercolateResponse> {

	/**
	 * Instantiates a new percolate request builder.
	 *
	 * @param client the client
	 */
	public PercolateRequestBuilder(Client client) {
		super(client, new PercolateRequest());
	}

	/**
	 * Instantiates a new percolate request builder.
	 *
	 * @param client the client
	 * @param index the index
	 * @param type the type
	 */
	public PercolateRequestBuilder(Client client, String index, String type) {
		super(client, new PercolateRequest(index, type));
	}

	/**
	 * Sets the index.
	 *
	 * @param index the index
	 * @return the percolate request builder
	 */
	public PercolateRequestBuilder setIndex(String index) {
		request.index(index);
		return this;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the type
	 * @return the percolate request builder
	 */
	public PercolateRequestBuilder setType(String type) {
		request.type(type);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the source
	 * @return the percolate request builder
	 */
	public PercolateRequestBuilder setSource(Map<String, Object> source) {
		request.source(source);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the source
	 * @param contentType the content type
	 * @return the percolate request builder
	 */
	public PercolateRequestBuilder setSource(Map<String, Object> source, XContentType contentType) {
		request.source(source, contentType);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the source
	 * @return the percolate request builder
	 */
	public PercolateRequestBuilder setSource(String source) {
		request.source(source);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param sourceBuilder the source builder
	 * @return the percolate request builder
	 */
	public PercolateRequestBuilder setSource(XContentBuilder sourceBuilder) {
		request.source(sourceBuilder);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the source
	 * @return the percolate request builder
	 */
	public PercolateRequestBuilder setSource(byte[] source) {
		request.source(source);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @return the percolate request builder
	 */
	public PercolateRequestBuilder setSource(byte[] source, int offset, int length) {
		request.source(source, offset, length);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @param unsafe the unsafe
	 * @return the percolate request builder
	 */
	public PercolateRequestBuilder setSource(byte[] source, int offset, int length, boolean unsafe) {
		request.source(source, offset, length, unsafe);
		return this;
	}

	/**
	 * Sets the listener threaded.
	 *
	 * @param listenerThreaded the listener threaded
	 * @return the percolate request builder
	 */
	public PercolateRequestBuilder setListenerThreaded(boolean listenerThreaded) {
		request.listenerThreaded(listenerThreaded);
		return this;
	}

	/**
	 * Sets the prefer local.
	 *
	 * @param preferLocal the prefer local
	 * @return the percolate request builder
	 */
	public PercolateRequestBuilder setPreferLocal(boolean preferLocal) {
		request.preferLocal(preferLocal);
		return this;
	}

	/**
	 * Sets the operation threaded.
	 *
	 * @param operationThreaded the operation threaded
	 * @return the percolate request builder
	 */
	public PercolateRequestBuilder setOperationThreaded(boolean operationThreaded) {
		request.operationThreaded(operationThreaded);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.BaseRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<PercolateResponse> listener) {
		client.percolate(request, listener);
	}

}
