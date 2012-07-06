/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BulkRequest.java 2012-3-29 15:01:24 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.bulk;

import java.io.IOException;
import java.util.List;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.xcontent.XContent;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;
import cn.com.rebirth.search.core.action.delete.DeleteRequest;
import cn.com.rebirth.search.core.action.index.IndexRequest;
import cn.com.rebirth.search.core.action.support.replication.ReplicationType;
import cn.com.rebirth.search.core.index.VersionType;

import com.google.common.collect.Lists;


/**
 * The Class BulkRequest.
 *
 * @author l.xue.nong
 */
public class BulkRequest implements ActionRequest {

	
	/** The requests. */
	final List<ActionRequest> requests = Lists.newArrayList();

	
	/** The listener threaded. */
	private boolean listenerThreaded = false;

	
	/** The replication type. */
	private ReplicationType replicationType = ReplicationType.DEFAULT;

	
	/** The consistency level. */
	private WriteConsistencyLevel consistencyLevel = WriteConsistencyLevel.DEFAULT;

	
	/** The refresh. */
	private boolean refresh = false;

	
	/**
	 * Adds the.
	 *
	 * @param request the request
	 * @return the bulk request
	 */
	public BulkRequest add(IndexRequest request) {
		request.beforeLocalFork();
		return internalAdd(request);
	}

	
	/**
	 * Internal add.
	 *
	 * @param request the request
	 * @return the bulk request
	 */
	private BulkRequest internalAdd(IndexRequest request) {
		requests.add(request);
		return this;
	}

	
	/**
	 * Adds the.
	 *
	 * @param request the request
	 * @return the bulk request
	 */
	public BulkRequest add(DeleteRequest request) {
		requests.add(request);
		return this;
	}

	
	/**
	 * Adds the.
	 *
	 * @param data the data
	 * @param from the from
	 * @param length the length
	 * @param contentUnsafe the content unsafe
	 * @return the bulk request
	 * @throws Exception the exception
	 */
	public BulkRequest add(byte[] data, int from, int length, boolean contentUnsafe) throws Exception {
		return add(data, from, length, contentUnsafe, null, null);
	}

	
	/**
	 * Adds the.
	 *
	 * @param data the data
	 * @param from the from
	 * @param length the length
	 * @param contentUnsafe the content unsafe
	 * @param defaultIndex the default index
	 * @param defaultType the default type
	 * @return the bulk request
	 * @throws Exception the exception
	 */
	public BulkRequest add(byte[] data, int from, int length, boolean contentUnsafe, @Nullable String defaultIndex,
			@Nullable String defaultType) throws Exception {
		XContent xContent = XContentFactory.xContent(data, from, length);
		byte marker = xContent.streamSeparator();
		while (true) {
			int nextMarker = findNextMarker(marker, from, data, length);
			if (nextMarker == -1) {
				break;
			}
			
			XContentParser parser = xContent.createParser(data, from, nextMarker - from);

			try {
				
				from = nextMarker + 1;

				
				XContentParser.Token token = parser.nextToken();
				if (token == null) {
					continue;
				}
				assert token == XContentParser.Token.START_OBJECT;
				
				token = parser.nextToken();
				assert token == XContentParser.Token.FIELD_NAME;
				String action = parser.currentName();

				String index = defaultIndex;
				String type = defaultType;
				String id = null;
				String routing = null;
				String parent = null;
				String timestamp = null;
				Long ttl = null;
				String opType = null;
				long version = 0;
				VersionType versionType = VersionType.INTERNAL;
				String percolate = null;

				
				

				String currentFieldName = null;
				while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
					if (token == XContentParser.Token.FIELD_NAME) {
						currentFieldName = parser.currentName();
					} else if (token.isValue()) {
						if ("_index".equals(currentFieldName)) {
							index = parser.text();
						} else if ("_type".equals(currentFieldName)) {
							type = parser.text();
						} else if ("_id".equals(currentFieldName)) {
							id = parser.text();
						} else if ("_routing".equals(currentFieldName) || "routing".equals(currentFieldName)) {
							routing = parser.text();
						} else if ("_parent".equals(currentFieldName) || "parent".equals(currentFieldName)) {
							parent = parser.text();
						} else if ("_timestamp".equals(currentFieldName) || "timestamp".equals(currentFieldName)) {
							timestamp = parser.text();
						} else if ("_ttl".equals(currentFieldName) || "ttl".equals(currentFieldName)) {
							if (parser.currentToken() == XContentParser.Token.VALUE_STRING) {
								ttl = TimeValue.parseTimeValue(parser.text(), null).millis();
							} else {
								ttl = parser.longValue();
							}
						} else if ("op_type".equals(currentFieldName) || "opType".equals(currentFieldName)) {
							opType = parser.text();
						} else if ("_version".equals(currentFieldName) || "version".equals(currentFieldName)) {
							version = parser.longValue();
						} else if ("_version_type".equals(currentFieldName) || "_versionType".equals(currentFieldName)
								|| "version_type".equals(currentFieldName) || "versionType".equals(currentFieldName)) {
							versionType = VersionType.fromString(parser.text());
						} else if ("percolate".equals(currentFieldName) || "_percolate".equals(currentFieldName)) {
							percolate = parser.textOrNull();
						}
					}
				}

				if ("delete".equals(action)) {
					add(new DeleteRequest(index, type, id).parent(parent).version(version).versionType(versionType)
							.routing(routing));
				} else {
					nextMarker = findNextMarker(marker, from, data, length);
					if (nextMarker == -1) {
						break;
					}
					
					
					
					if ("index".equals(action)) {
						if (opType == null) {
							internalAdd(new IndexRequest(index, type, id).routing(routing).parent(parent)
									.timestamp(timestamp).ttl(ttl).version(version).versionType(versionType)
									.source(data, from, nextMarker - from, contentUnsafe).percolate(percolate));
						} else {
							internalAdd(new IndexRequest(index, type, id).routing(routing).parent(parent)
									.timestamp(timestamp).ttl(ttl).version(version).versionType(versionType)
									.create("create".equals(opType))
									.source(data, from, nextMarker - from, contentUnsafe).percolate(percolate));
						}
					} else if ("create".equals(action)) {
						internalAdd(new IndexRequest(index, type, id).routing(routing).parent(parent)
								.timestamp(timestamp).ttl(ttl).version(version).versionType(versionType).create(true)
								.source(data, from, nextMarker - from, contentUnsafe).percolate(percolate));
					}
					
					from = nextMarker + 1;
				}
			} finally {
				parser.close();
			}
		}
		return this;
	}

	
	/**
	 * Consistency level.
	 *
	 * @param consistencyLevel the consistency level
	 * @return the bulk request
	 */
	public BulkRequest consistencyLevel(WriteConsistencyLevel consistencyLevel) {
		this.consistencyLevel = consistencyLevel;
		return this;
	}

	
	/**
	 * Consistency level.
	 *
	 * @return the write consistency level
	 */
	public WriteConsistencyLevel consistencyLevel() {
		return this.consistencyLevel;
	}

	
	/**
	 * Refresh.
	 *
	 * @param refresh the refresh
	 * @return the bulk request
	 */
	public BulkRequest refresh(boolean refresh) {
		this.refresh = refresh;
		return this;
	}

	
	/**
	 * Refresh.
	 *
	 * @return true, if successful
	 */
	public boolean refresh() {
		return this.refresh;
	}

	
	/**
	 * Replication type.
	 *
	 * @param replicationType the replication type
	 * @return the bulk request
	 */
	public BulkRequest replicationType(ReplicationType replicationType) {
		this.replicationType = replicationType;
		return this;
	}

	
	/**
	 * Replication type.
	 *
	 * @return the replication type
	 */
	public ReplicationType replicationType() {
		return this.replicationType;
	}

	
	/**
	 * Find next marker.
	 *
	 * @param marker the marker
	 * @param from the from
	 * @param data the data
	 * @param length the length
	 * @return the int
	 */
	private int findNextMarker(byte marker, int from, byte[] data, int length) {
		for (int i = from; i < length; i++) {
			if (data[i] == marker) {
				return i;
			}
		}
		return -1;
	}

	
	/**
	 * Number of actions.
	 *
	 * @return the int
	 */
	public int numberOfActions() {
		return requests.size();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;
		if (requests.isEmpty()) {
			validationException = ValidateActions.addValidationError("no requests added", validationException);
		}
		for (int i = 0; i < requests.size(); i++) {
			ActionRequestValidationException ex = requests.get(i).validate();
			if (ex != null) {
				if (validationException == null) {
					validationException = new ActionRequestValidationException();
				}
				validationException.addValidationErrors(ex.validationErrors());
			}
		}

		return validationException;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#listenerThreaded()
	 */
	@Override
	public boolean listenerThreaded() {
		return listenerThreaded;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#listenerThreaded(boolean)
	 */
	@Override
	public BulkRequest listenerThreaded(boolean listenerThreaded) {
		this.listenerThreaded = listenerThreaded;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		replicationType = ReplicationType.fromId(in.readByte());
		consistencyLevel = WriteConsistencyLevel.fromId(in.readByte());
		int size = in.readVInt();
		for (int i = 0; i < size; i++) {
			byte type = in.readByte();
			if (type == 0) {
				IndexRequest request = new IndexRequest();
				request.readFrom(in);
				requests.add(request);
			} else if (type == 1) {
				DeleteRequest request = new DeleteRequest();
				request.readFrom(in);
				requests.add(request);
			}
		}
		refresh = in.readBoolean();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeByte(replicationType.id());
		out.writeByte(consistencyLevel.id());
		out.writeVInt(requests.size());
		for (ActionRequest request : requests) {
			if (request instanceof IndexRequest) {
				out.writeByte((byte) 0);
			} else if (request instanceof DeleteRequest) {
				out.writeByte((byte) 1);
			}
			request.writeTo(out);
		}
		out.writeBoolean(refresh);
	}
}
