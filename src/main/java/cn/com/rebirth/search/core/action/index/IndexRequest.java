/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexRequest.java 2012-3-29 15:02:03 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.index;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.lucene.util.UnicodeUtil;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Unicode;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.exception.RestartParseException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.Required;
import cn.com.rebirth.search.commons.UUID;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.RestartGenerationException;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.RoutingMissingException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;
import cn.com.rebirth.search.core.action.support.replication.ReplicationType;
import cn.com.rebirth.search.core.action.support.replication.ShardReplicationOperationRequest;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.cluster.metadata.MappingMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.index.VersionType;
import cn.com.rebirth.search.core.index.mapper.internal.TimestampFieldMapper;


/**
 * The Class IndexRequest.
 *
 * @author l.xue.nong
 */
public class IndexRequest extends ShardReplicationOperationRequest {

	
	/**
	 * The Enum OpType.
	 *
	 * @author l.xue.nong
	 */
	public static enum OpType {

		
		/** The INDEX. */
		INDEX((byte) 0),

		
		/** The CREATE. */
		CREATE((byte) 1);

		
		/** The id. */
		private byte id;

		
		/**
		 * Instantiates a new op type.
		 *
		 * @param id the id
		 */
		OpType(byte id) {
			this.id = id;
		}

		
		/**
		 * Id.
		 *
		 * @return the byte
		 */
		public byte id() {
			return id;
		}

		
		/**
		 * From id.
		 *
		 * @param id the id
		 * @return the op type
		 */
		public static OpType fromId(byte id) {
			if (id == 0) {
				return INDEX;
			} else if (id == 1) {
				return CREATE;
			} else {
				throw new RestartIllegalArgumentException("No type match for [" + id + "]");
			}
		}
	}

	
	/** The type. */
	private String type;

	
	/** The id. */
	private String id;

	
	/** The routing. */
	@Nullable
	private String routing;

	
	/** The parent. */
	@Nullable
	private String parent;

	
	/** The timestamp. */
	@Nullable
	private String timestamp;

	
	/** The ttl. */
	private long ttl = -1;

	
	/** The source. */
	private byte[] source;

	
	/** The source offset. */
	private int sourceOffset;

	
	/** The source length. */
	private int sourceLength;

	
	/** The source unsafe. */
	private boolean sourceUnsafe;

	
	/** The op type. */
	private OpType opType = OpType.INDEX;

	
	/** The refresh. */
	private boolean refresh = false;

	
	/** The version. */
	private long version = 0;

	
	/** The version type. */
	private VersionType versionType = VersionType.INTERNAL;

	
	/** The percolate. */
	private String percolate;

	
	/** The content type. */
	private XContentType contentType = Requests.INDEX_CONTENT_TYPE;

	
	/**
	 * Instantiates a new index request.
	 */
	public IndexRequest() {
	}

	
	/**
	 * Instantiates a new index request.
	 *
	 * @param index the index
	 */
	public IndexRequest(String index) {
		this.index = index;
	}

	
	/**
	 * Instantiates a new index request.
	 *
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 */
	public IndexRequest(String index, String type, String id) {
		this.index = index;
		this.type = type;
		this.id = id;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.ShardReplicationOperationRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = super.validate();
		if (type == null) {
			validationException = ValidateActions.addValidationError("type is missing", validationException);
		}
		if (source == null) {
			validationException = ValidateActions.addValidationError("source is missing", validationException);
		}
		return validationException;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.ShardReplicationOperationRequest#beforeLocalFork()
	 */
	@Override
	public void beforeLocalFork() {
		
		if (sourceUnsafe) {
			source();
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.ShardReplicationOperationRequest#index(java.lang.String)
	 */
	@Override
	public IndexRequest index(String index) {
		super.index(index);
		return this;
	}

	
	/**
	 * Content type.
	 *
	 * @param contentType the content type
	 * @return the index request
	 */
	public IndexRequest contentType(XContentType contentType) {
		this.contentType = contentType;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.ShardReplicationOperationRequest#listenerThreaded(boolean)
	 */
	@Override
	public IndexRequest listenerThreaded(boolean threadedListener) {
		super.listenerThreaded(threadedListener);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.ShardReplicationOperationRequest#operationThreaded(boolean)
	 */
	@Override
	public IndexRequest operationThreaded(boolean threadedOperation) {
		super.operationThreaded(threadedOperation);
		return this;
	}

	
	/**
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		return type;
	}

	
	/**
	 * Type.
	 *
	 * @param type the type
	 * @return the index request
	 */
	@Required
	public IndexRequest type(String type) {
		this.type = type;
		return this;
	}

	
	/**
	 * Id.
	 *
	 * @return the string
	 */
	public String id() {
		return id;
	}

	
	/**
	 * Id.
	 *
	 * @param id the id
	 * @return the index request
	 */
	public IndexRequest id(String id) {
		this.id = id;
		return this;
	}

	
	/**
	 * Routing.
	 *
	 * @param routing the routing
	 * @return the index request
	 */
	public IndexRequest routing(String routing) {
		if (routing != null && routing.length() == 0) {
			this.routing = null;
		} else {
			this.routing = routing;
		}
		return this;
	}

	
	/**
	 * Routing.
	 *
	 * @return the string
	 */
	public String routing() {
		return this.routing;
	}

	
	/**
	 * Parent.
	 *
	 * @param parent the parent
	 * @return the index request
	 */
	public IndexRequest parent(String parent) {
		this.parent = parent;
		if (routing == null) {
			routing = parent;
		}
		return this;
	}

	
	/**
	 * Parent.
	 *
	 * @return the string
	 */
	public String parent() {
		return this.parent;
	}

	
	/**
	 * Timestamp.
	 *
	 * @param timestamp the timestamp
	 * @return the index request
	 */
	public IndexRequest timestamp(String timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	
	/**
	 * Timestamp.
	 *
	 * @return the string
	 */
	public String timestamp() {
		return this.timestamp;
	}

	
	/**
	 * Ttl.
	 *
	 * @param ttl the ttl
	 * @return the index request
	 * @throws RestartGenerationException the sum mall search generation exception
	 */
	public IndexRequest ttl(Long ttl) throws RestartGenerationException {
		if (ttl == null) {
			this.ttl = -1;
			return this;
		}
		if (ttl <= 0) {
			throw new RestartIllegalArgumentException("TTL value must be > 0. Illegal value provided [" + ttl
					+ "]");
		}
		this.ttl = ttl;
		return this;
	}

	
	/**
	 * Ttl.
	 *
	 * @return the long
	 */
	public long ttl() {
		return this.ttl;
	}

	
	/**
	 * Source.
	 *
	 * @return the byte[]
	 */
	public byte[] source() {
		if (sourceUnsafe || sourceOffset > 0 || source.length != sourceLength) {
			source = Arrays.copyOfRange(source, sourceOffset, sourceOffset + sourceLength);
			sourceOffset = 0;
			sourceUnsafe = false;
		}
		return source;
	}

	
	/**
	 * Underlying source.
	 *
	 * @return the byte[]
	 */
	public byte[] underlyingSource() {
		if (sourceUnsafe) {
			source();
		}
		return this.source;
	}

	
	/**
	 * Underlying source offset.
	 *
	 * @return the int
	 */
	public int underlyingSourceOffset() {
		if (sourceUnsafe) {
			source();
		}
		return this.sourceOffset;
	}

	
	/**
	 * Underlying source length.
	 *
	 * @return the int
	 */
	public int underlyingSourceLength() {
		if (sourceUnsafe) {
			source();
		}
		return this.sourceLength;
	}

	
	/**
	 * Source.
	 *
	 * @param source the source
	 * @return the index request
	 * @throws RestartGenerationException the sum mall search generation exception
	 */
	@Required
	public IndexRequest source(Map source) throws RestartGenerationException {
		return source(source, contentType);
	}

	
	/**
	 * Source.
	 *
	 * @param source the source
	 * @param contentType the content type
	 * @return the index request
	 * @throws RestartGenerationException the sum mall search generation exception
	 */
	@Required
	public IndexRequest source(Map source, XContentType contentType) throws RestartGenerationException {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(contentType);
			builder.map(source);
			return source(builder);
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + source + "]", e);
		}
	}

	
	/**
	 * Source.
	 *
	 * @param source the source
	 * @return the index request
	 */
	@Required
	public IndexRequest source(String source) {
		UnicodeUtil.UTF8Result result = Unicode.fromStringAsUtf8(source);
		this.source = result.result;
		this.sourceOffset = 0;
		this.sourceLength = result.length;
		this.sourceUnsafe = true;
		return this;
	}

	
	/**
	 * Source.
	 *
	 * @param sourceBuilder the source builder
	 * @return the index request
	 */
	@Required
	public IndexRequest source(XContentBuilder sourceBuilder) {
		try {
			source = sourceBuilder.underlyingBytes();
			sourceOffset = 0;
			sourceLength = sourceBuilder.underlyingBytesLength();
			sourceUnsafe = false;
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + sourceBuilder + "]", e);
		}
		return this;
	}

	
	/**
	 * Source.
	 *
	 * @param field1 the field1
	 * @param value1 the value1
	 * @return the index request
	 */
	@Required
	public IndexRequest source(String field1, Object value1) {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(contentType);
			builder.startObject().field(field1, value1).endObject();
			return source(builder);
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate", e);
		}
	}

	
	/**
	 * Source.
	 *
	 * @param field1 the field1
	 * @param value1 the value1
	 * @param field2 the field2
	 * @param value2 the value2
	 * @return the index request
	 */
	@Required
	public IndexRequest source(String field1, Object value1, String field2, Object value2) {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(contentType);
			builder.startObject().field(field1, value1).field(field2, value2).endObject();
			return source(builder);
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate", e);
		}
	}

	
	/**
	 * Source.
	 *
	 * @param field1 the field1
	 * @param value1 the value1
	 * @param field2 the field2
	 * @param value2 the value2
	 * @param field3 the field3
	 * @param value3 the value3
	 * @return the index request
	 */
	@Required
	public IndexRequest source(String field1, Object value1, String field2, Object value2, String field3, Object value3) {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(contentType);
			builder.startObject().field(field1, value1).field(field2, value2).field(field3, value3).endObject();
			return source(builder);
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate", e);
		}
	}

	
	/**
	 * Source.
	 *
	 * @param field1 the field1
	 * @param value1 the value1
	 * @param field2 the field2
	 * @param value2 the value2
	 * @param field3 the field3
	 * @param value3 the value3
	 * @param field4 the field4
	 * @param value4 the value4
	 * @return the index request
	 */
	@Required
	public IndexRequest source(String field1, Object value1, String field2, Object value2, String field3,
			Object value3, String field4, Object value4) {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(contentType);
			builder.startObject().field(field1, value1).field(field2, value2).field(field3, value3)
					.field(field4, value4).endObject();
			return source(builder);
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate", e);
		}
	}

	
	/**
	 * Source.
	 *
	 * @param source the source
	 * @return the index request
	 */
	public IndexRequest source(byte[] source) {
		return source(source, 0, source.length);
	}

	
	/**
	 * Source.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @return the index request
	 */
	@Required
	public IndexRequest source(byte[] source, int offset, int length) {
		return source(source, offset, length, false);
	}

	
	/**
	 * Source.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @param unsafe the unsafe
	 * @return the index request
	 */
	@Required
	public IndexRequest source(byte[] source, int offset, int length, boolean unsafe) {
		this.source = source;
		this.sourceOffset = offset;
		this.sourceLength = length;
		this.sourceUnsafe = unsafe;
		return this;
	}

	
	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the index request
	 */
	public IndexRequest timeout(TimeValue timeout) {
		this.timeout = timeout;
		return this;
	}

	
	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the index request
	 */
	public IndexRequest timeout(String timeout) {
		return timeout(TimeValue.parseTimeValue(timeout, null));
	}

	
	/**
	 * Op type.
	 *
	 * @param opType the op type
	 * @return the index request
	 */
	public IndexRequest opType(OpType opType) {
		this.opType = opType;
		return this;
	}

	
	/**
	 * Op type.
	 *
	 * @param opType the op type
	 * @return the index request
	 * @throws SumMallSearchIllegalArgumentException the sum mall search illegal argument exception
	 */
	public IndexRequest opType(String opType) throws RestartIllegalArgumentException {
		if ("create".equals(opType)) {
			return opType(OpType.CREATE);
		} else if ("index".equals(opType)) {
			return opType(OpType.INDEX);
		} else {
			throw new RestartIllegalArgumentException("No index opType matching [" + opType + "]");
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.ShardReplicationOperationRequest#replicationType(cn.com.summall.search.core.action.support.replication.ReplicationType)
	 */
	@Override
	public IndexRequest replicationType(ReplicationType replicationType) {
		super.replicationType(replicationType);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.ShardReplicationOperationRequest#consistencyLevel(cn.com.summall.search.core.action.WriteConsistencyLevel)
	 */
	@Override
	public IndexRequest consistencyLevel(WriteConsistencyLevel consistencyLevel) {
		super.consistencyLevel(consistencyLevel);
		return this;
	}

	
	/**
	 * Replication type.
	 *
	 * @param replicationType the replication type
	 * @return the index request
	 */
	public IndexRequest replicationType(String replicationType) {
		super.replicationType(ReplicationType.fromString(replicationType));
		return this;
	}

	
	/**
	 * Creates the.
	 *
	 * @param create the create
	 * @return the index request
	 */
	public IndexRequest create(boolean create) {
		if (create) {
			return opType(OpType.CREATE);
		} else {
			return opType(OpType.INDEX);
		}
	}

	
	/**
	 * Op type.
	 *
	 * @return the op type
	 */
	public OpType opType() {
		return this.opType;
	}

	
	/**
	 * Refresh.
	 *
	 * @param refresh the refresh
	 * @return the index request
	 */
	public IndexRequest refresh(boolean refresh) {
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
	 * Version.
	 *
	 * @param version the version
	 * @return the index request
	 */
	public IndexRequest version(long version) {
		this.version = version;
		return this;
	}

	
	/**
	 * Version.
	 *
	 * @return the long
	 */
	public long version() {
		return this.version;
	}

	
	/**
	 * Version type.
	 *
	 * @param versionType the version type
	 * @return the index request
	 */
	public IndexRequest versionType(VersionType versionType) {
		this.versionType = versionType;
		return this;
	}

	
	/**
	 * Version type.
	 *
	 * @return the version type
	 */
	public VersionType versionType() {
		return this.versionType;
	}

	
	/**
	 * Percolate.
	 *
	 * @param percolate the percolate
	 * @return the index request
	 */
	public IndexRequest percolate(String percolate) {
		this.percolate = percolate;
		return this;
	}

	
	/**
	 * Percolate.
	 *
	 * @return the string
	 */
	public String percolate() {
		return this.percolate;
	}

	
	/**
	 * Process.
	 *
	 * @param metaData the meta data
	 * @param aliasOrIndex the alias or index
	 * @param mappingMd the mapping md
	 * @param allowIdGeneration the allow id generation
	 * @throws SumMallSearchException the sum mall search exception
	 */
	public void process(MetaData metaData, String aliasOrIndex, @Nullable MappingMetaData mappingMd,
			boolean allowIdGeneration) throws RestartException {
		
		routing(metaData.resolveIndexRouting(routing, aliasOrIndex));
		
		if (timestamp != null) {
			timestamp = MappingMetaData.Timestamp.parseStringTimestamp(timestamp, mappingMd != null ? mappingMd
					.timestamp().dateTimeFormatter() : TimestampFieldMapper.Defaults.DATE_TIME_FORMATTER);
		}
		
		if (mappingMd != null) {
			MappingMetaData.ParseContext parseContext = mappingMd.createParseContext(id, routing, timestamp);

			if (parseContext.shouldParse()) {
				XContentParser parser = null;
				try {
					parser = XContentFactory.xContent(source, sourceOffset, sourceLength).createParser(source,
							sourceOffset, sourceLength);
					mappingMd.parse(parser, parseContext);
					if (parseContext.shouldParseId()) {
						id = parseContext.id();
					}
					if (parseContext.shouldParseRouting()) {
						routing = parseContext.routing();
					}
					if (parseContext.shouldParseTimestamp()) {
						timestamp = parseContext.timestamp();
						timestamp = MappingMetaData.Timestamp.parseStringTimestamp(timestamp, mappingMd.timestamp()
								.dateTimeFormatter());
					}
				} catch (Exception e) {
					throw new RestartParseException("failed to parse doc to extract routing/timestamp", e);
				} finally {
					if (parser != null) {
						parser.close();
					}
				}
			}

			
			if (mappingMd.routing().required() && routing == null) {
				throw new RoutingMissingException(index, type, id);
			}
		}

		
		if (allowIdGeneration) {
			if (id == null) {
				id(UUID.randomBase64UUID());
				
				opType(IndexRequest.OpType.CREATE);
			}
		}

		
		if (timestamp == null) {
			timestamp = String.valueOf(System.currentTimeMillis());
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.ShardReplicationOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		type = in.readUTF();
		if (in.readBoolean()) {
			id = in.readUTF();
		}
		if (in.readBoolean()) {
			routing = in.readUTF();
		}
		if (in.readBoolean()) {
			parent = in.readUTF();
		}
		if (in.readBoolean()) {
			timestamp = in.readUTF();
		}
		ttl = in.readLong();
		BytesHolder bytes = in.readBytesReference();
		sourceUnsafe = false;
		source = bytes.bytes();
		sourceOffset = bytes.offset();
		sourceLength = bytes.length();

		opType = OpType.fromId(in.readByte());
		refresh = in.readBoolean();
		version = in.readLong();
		if (in.readBoolean()) {
			percolate = in.readUTF();
		}
		versionType = VersionType.fromValue(in.readByte());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.replication.ShardReplicationOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeUTF(type);
		if (id == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(id);
		}
		if (routing == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(routing);
		}
		if (parent == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(parent);
		}
		if (timestamp == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(timestamp);
		}
		out.writeLong(ttl);
		out.writeBytesHolder(source, sourceOffset, sourceLength);
		out.writeByte(opType.id());
		out.writeBoolean(refresh);
		out.writeLong(version);
		if (percolate == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(percolate);
		}
		out.writeByte(versionType.getValue());
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String sSource = "_na_";
		try {
			sSource = Unicode.fromBytes(source, sourceOffset, sourceLength);
		} catch (Exception e) {
			
		}
		return "index {[" + index + "][" + type + "][" + id + "], source[" + sSource + "]}";
	}
}