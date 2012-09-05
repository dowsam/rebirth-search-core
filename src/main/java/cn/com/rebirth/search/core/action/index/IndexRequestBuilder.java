/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexRequestBuilder.java 2012-7-6 14:29:49 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.index;

import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;
import cn.com.rebirth.search.core.action.support.BaseRequestBuilder;
import cn.com.rebirth.search.core.action.support.replication.ReplicationType;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.index.VersionType;

/**
 * The Class IndexRequestBuilder.
 *
 * @author l.xue.nong
 */
public class IndexRequestBuilder extends BaseRequestBuilder<IndexRequest, IndexResponse> {

	/**
	 * Instantiates a new index request builder.
	 *
	 * @param client the client
	 */
	public IndexRequestBuilder(Client client) {
		super(client, new IndexRequest());
	}

	/**
	 * Instantiates a new index request builder.
	 *
	 * @param client the client
	 * @param index the index
	 */
	public IndexRequestBuilder(Client client, @Nullable String index) {
		super(client, new IndexRequest(index));
	}

	/**
	 * Sets the index.
	 *
	 * @param index the index
	 * @return the index request builder
	 */
	public IndexRequestBuilder setIndex(String index) {
		request.index(index);
		return this;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the type
	 * @return the index request builder
	 */
	public IndexRequestBuilder setType(String type) {
		request.type(type);
		return this;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the id
	 * @return the index request builder
	 */
	public IndexRequestBuilder setId(String id) {
		request.id(id);
		return this;
	}

	/**
	 * Sets the routing.
	 *
	 * @param routing the routing
	 * @return the index request builder
	 */
	public IndexRequestBuilder setRouting(String routing) {
		request.routing(routing);
		return this;
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent the parent
	 * @return the index request builder
	 */
	public IndexRequestBuilder setParent(String parent) {
		request.parent(parent);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the source
	 * @return the index request builder
	 */
	public IndexRequestBuilder setSource(Map<String, Object> source) {
		request.source(source);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the source
	 * @param contentType the content type
	 * @return the index request builder
	 */
	public IndexRequestBuilder setSource(Map<String, Object> source, XContentType contentType) {
		request.source(source, contentType);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the source
	 * @return the index request builder
	 */
	public IndexRequestBuilder setSource(String source) {
		request.source(source);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param sourceBuilder the source builder
	 * @return the index request builder
	 */
	public IndexRequestBuilder setSource(XContentBuilder sourceBuilder) {
		request.source(sourceBuilder);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the source
	 * @return the index request builder
	 */
	public IndexRequestBuilder setSource(byte[] source) {
		request.source(source);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @return the index request builder
	 */
	public IndexRequestBuilder setSource(byte[] source, int offset, int length) {
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
	 * @return the index request builder
	 */
	public IndexRequestBuilder setSource(byte[] source, int offset, int length, boolean unsafe) {
		request.source(source, offset, length, unsafe);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param field1 the field1
	 * @param value1 the value1
	 * @return the index request builder
	 */
	public IndexRequestBuilder setSource(String field1, Object value1) {
		request.source(field1, value1);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param field1 the field1
	 * @param value1 the value1
	 * @param field2 the field2
	 * @param value2 the value2
	 * @return the index request builder
	 */
	public IndexRequestBuilder setSource(String field1, Object value1, String field2, Object value2) {
		request.source(field1, value1, field2, value2);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param field1 the field1
	 * @param value1 the value1
	 * @param field2 the field2
	 * @param value2 the value2
	 * @param field3 the field3
	 * @param value3 the value3
	 * @return the index request builder
	 */
	public IndexRequestBuilder setSource(String field1, Object value1, String field2, Object value2, String field3,
			Object value3) {
		request.source(field1, value1, field2, value2, field3, value3);
		return this;
	}

	/**
	 * Sets the source.
	 *
	 * @param field1 the field1
	 * @param value1 the value1
	 * @param field2 the field2
	 * @param value2 the value2
	 * @param field3 the field3
	 * @param value3 the value3
	 * @param field4 the field4
	 * @param value4 the value4
	 * @return the index request builder
	 */
	public IndexRequestBuilder setSource(String field1, Object value1, String field2, Object value2, String field3,
			Object value3, String field4, Object value4) {
		request.source(field1, value1, field2, value2, field3, value3, field4, value4);
		return this;
	}

	/**
	 * Sets the content type.
	 *
	 * @param contentType the content type
	 * @return the index request builder
	 */
	public IndexRequestBuilder setContentType(XContentType contentType) {
		request.contentType(contentType);
		return this;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the index request builder
	 */
	public IndexRequestBuilder setTimeout(TimeValue timeout) {
		request.timeout(timeout);
		return this;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the timeout
	 * @return the index request builder
	 */
	public IndexRequestBuilder setTimeout(String timeout) {
		request.timeout(timeout);
		return this;
	}

	/**
	 * Sets the op type.
	 *
	 * @param opType the op type
	 * @return the index request builder
	 */
	public IndexRequestBuilder setOpType(IndexRequest.OpType opType) {
		request.opType(opType);
		return this;
	}

	/**
	 * Sets the op type.
	 *
	 * @param opType the op type
	 * @return the index request builder
	 */
	public IndexRequestBuilder setOpType(String opType) {
		request.opType(opType);
		return this;
	}

	/**
	 * Sets the create.
	 *
	 * @param create the create
	 * @return the index request builder
	 */
	public IndexRequestBuilder setCreate(boolean create) {
		request.create(create);
		return this;
	}

	/**
	 * Sets the refresh.
	 *
	 * @param refresh the refresh
	 * @return the index request builder
	 */
	public IndexRequestBuilder setRefresh(boolean refresh) {
		request.refresh(refresh);
		return this;
	}

	/**
	 * Sets the replication type.
	 *
	 * @param replicationType the replication type
	 * @return the index request builder
	 */
	public IndexRequestBuilder setReplicationType(ReplicationType replicationType) {
		request.replicationType(replicationType);
		return this;
	}

	/**
	 * Sets the consistency level.
	 *
	 * @param consistencyLevel the consistency level
	 * @return the index request builder
	 */
	public IndexRequestBuilder setConsistencyLevel(WriteConsistencyLevel consistencyLevel) {
		request.consistencyLevel(consistencyLevel);
		return this;
	}

	/**
	 * Sets the replication type.
	 *
	 * @param replicationType the replication type
	 * @return the index request builder
	 */
	public IndexRequestBuilder setReplicationType(String replicationType) {
		request.replicationType(replicationType);
		return this;
	}

	/**
	 * Sets the version.
	 *
	 * @param version the version
	 * @return the index request builder
	 */
	public IndexRequestBuilder setVersion(long version) {
		request.version(version);
		return this;
	}

	/**
	 * Sets the version type.
	 *
	 * @param versionType the version type
	 * @return the index request builder
	 */
	public IndexRequestBuilder setVersionType(VersionType versionType) {
		request.versionType(versionType);
		return this;
	}

	/**
	 * Sets the percolate.
	 *
	 * @param percolate the percolate
	 * @return the index request builder
	 */
	public IndexRequestBuilder setPercolate(String percolate) {
		request.percolate(percolate);
		return this;
	}

	/**
	 * Sets the timestamp.
	 *
	 * @param timestamp the timestamp
	 * @return the index request builder
	 */
	public IndexRequestBuilder setTimestamp(String timestamp) {
		request.timestamp(timestamp);
		return this;
	}

	/**
	 * Sets the ttl.
	 *
	 * @param ttl the ttl
	 * @return the index request builder
	 */
	public IndexRequestBuilder setTTL(long ttl) {
		request.ttl(ttl);
		return this;
	}

	/**
	 * Sets the listener threaded.
	 *
	 * @param listenerThreaded the listener threaded
	 * @return the index request builder
	 */
	public IndexRequestBuilder setListenerThreaded(boolean listenerThreaded) {
		request.listenerThreaded(listenerThreaded);
		return this;
	}

	/**
	 * Sets the operation threaded.
	 *
	 * @param operationThreaded the operation threaded
	 * @return the index request builder
	 */
	public IndexRequestBuilder setOperationThreaded(boolean operationThreaded) {
		request.operationThreaded(operationThreaded);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.BaseRequestBuilder#doExecute(cn.com.rebirth.search.core.action.ActionListener)
	 */
	@Override
	protected void doExecute(ActionListener<IndexResponse> listener) {
		client.index(request, listener);
	}
}
