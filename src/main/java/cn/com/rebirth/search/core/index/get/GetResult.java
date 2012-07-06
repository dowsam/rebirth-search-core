/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GetResult.java 2012-3-29 15:02:49 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.get;

import static com.google.common.collect.Iterators.emptyIterator;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.Unicode;
import cn.com.rebirth.commons.compress.lzf.LZF;
import cn.com.rebirth.commons.compress.lzf.LZFDecoder;
import cn.com.rebirth.commons.exception.RestartParseException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;
import cn.com.rebirth.search.core.search.lookup.SourceLookup;

import com.google.common.collect.ImmutableMap;


/**
 * The Class GetResult.
 *
 * @author l.xue.nong
 */
public class GetResult implements Streamable, Iterable<GetField>, ToXContent {

	
	/** The index. */
	private String index;

	
	/** The type. */
	private String type;

	
	/** The id. */
	private String id;

	
	/** The version. */
	private long version;

	
	/** The exists. */
	private boolean exists;

	
	/** The fields. */
	private Map<String, GetField> fields;

	
	/** The source as map. */
	private Map<String, Object> sourceAsMap;

	
	/** The source. */
	private BytesHolder source;

	
	/** The source as bytes. */
	private byte[] sourceAsBytes;

	
	/**
	 * Instantiates a new gets the result.
	 */
	GetResult() {
	}

	
	/**
	 * Instantiates a new gets the result.
	 *
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 * @param version the version
	 * @param exists the exists
	 * @param source the source
	 * @param fields the fields
	 */
	GetResult(String index, String type, String id, long version, boolean exists, BytesHolder source,
			Map<String, GetField> fields) {
		this.index = index;
		this.type = type;
		this.id = id;
		this.version = version;
		this.exists = exists;
		this.source = source;
		this.fields = fields;
		if (this.fields == null) {
			this.fields = ImmutableMap.of();
		}
	}

	
	/**
	 * Exists.
	 *
	 * @return true, if successful
	 */
	public boolean exists() {
		return exists;
	}

	
	/**
	 * Checks if is exists.
	 *
	 * @return true, if is exists
	 */
	public boolean isExists() {
		return exists;
	}

	
	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		return this.index;
	}

	
	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public String getIndex() {
		return index;
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
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
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
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
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
	 * Gets the version.
	 *
	 * @return the version
	 */
	public long getVersion() {
		return this.version;
	}

	
	/**
	 * Source.
	 *
	 * @return the byte[]
	 */
	public byte[] source() {
		if (source == null) {
			return null;
		}
		if (sourceAsBytes != null) {
			return sourceAsBytes;
		}
		this.sourceAsBytes = sourceRef().copyBytes();
		return this.sourceAsBytes;
	}

	
	/**
	 * Source ref.
	 *
	 * @return the bytes holder
	 */
	public BytesHolder sourceRef() {
		if (LZF.isCompressed(source.bytes(), source.offset(), source.length())) {
			try {
				this.source = new BytesHolder(LZFDecoder.decode(source.bytes(), source.offset(), source.length()));
			} catch (IOException e) {
				throw new RestartParseException("failed to decompress source", e);
			}
		}
		return this.source;
	}

	
	/**
	 * Internal source ref.
	 *
	 * @return the bytes holder
	 */
	public BytesHolder internalSourceRef() {
		return source;
	}

	
	/**
	 * Checks if is source empty.
	 *
	 * @return true, if is source empty
	 */
	public boolean isSourceEmpty() {
		return source == null;
	}

	
	/**
	 * Source as string.
	 *
	 * @return the string
	 */
	public String sourceAsString() {
		if (source == null) {
			return null;
		}
		BytesHolder source = sourceRef();
		return Unicode.fromBytes(source.bytes(), source.offset(), source.length());
	}

	
	/**
	 * Source as map.
	 *
	 * @return the map
	 * @throws SumMallSearchParseException the sum mall search parse exception
	 */
	public Map<String, Object> sourceAsMap() throws RestartParseException {
		if (source == null) {
			return null;
		}
		if (sourceAsMap != null) {
			return sourceAsMap;
		}

		sourceAsMap = SourceLookup.sourceAsMap(source.bytes(), source.offset(), source.length());
		return sourceAsMap;
	}

	
	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	public Map<String, Object> getSource() {
		return sourceAsMap();
	}

	
	/**
	 * Fields.
	 *
	 * @return the map
	 */
	public Map<String, GetField> fields() {
		return this.fields;
	}

	
	/**
	 * Gets the fields.
	 *
	 * @return the fields
	 */
	public Map<String, GetField> getFields() {
		return fields;
	}

	
	/**
	 * Field.
	 *
	 * @param name the name
	 * @return the gets the field
	 */
	public GetField field(String name) {
		return fields.get(name);
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<GetField> iterator() {
		if (fields == null) {
			return emptyIterator();
		}
		return fields.values().iterator();
	}

	
	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		
		/** The Constant _INDEX. */
		static final XContentBuilderString _INDEX = new XContentBuilderString("_index");

		
		/** The Constant _TYPE. */
		static final XContentBuilderString _TYPE = new XContentBuilderString("_type");

		
		/** The Constant _ID. */
		static final XContentBuilderString _ID = new XContentBuilderString("_id");

		
		/** The Constant _VERSION. */
		static final XContentBuilderString _VERSION = new XContentBuilderString("_version");

		
		/** The Constant EXISTS. */
		static final XContentBuilderString EXISTS = new XContentBuilderString("exists");

		
		/** The Constant FIELDS. */
		static final XContentBuilderString FIELDS = new XContentBuilderString("fields");
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		if (!exists()) {
			builder.startObject();
			builder.field(Fields._INDEX, index);
			builder.field(Fields._TYPE, type);
			builder.field(Fields._ID, id);
			builder.field(Fields.EXISTS, false);
			builder.endObject();
		} else {
			builder.startObject();
			builder.field(Fields._INDEX, index);
			builder.field(Fields._TYPE, type);
			builder.field(Fields._ID, id);
			if (version != -1) {
				builder.field(Fields._VERSION, version);
			}
			builder.field(Fields.EXISTS, true);
			if (source != null) {
				RestXContentBuilder.restDocumentSource(source.bytes(), source.offset(), source.length(), builder,
						params);
			}

			if (fields != null && !fields.isEmpty()) {
				builder.startObject(Fields.FIELDS);
				for (GetField field : fields.values()) {
					if (field.values().isEmpty()) {
						continue;
					}
					if (field.values().size() == 1) {
						builder.field(field.name(), field.values().get(0));
					} else {
						builder.field(field.name());
						builder.startArray();
						for (Object value : field.values()) {
							builder.value(value);
						}
						builder.endArray();
					}
				}
				builder.endObject();
			}

			builder.endObject();
		}
		return builder;
	}

	
	/**
	 * Read get result.
	 *
	 * @param in the in
	 * @return the gets the result
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static GetResult readGetResult(StreamInput in) throws IOException {
		GetResult result = new GetResult();
		result.readFrom(in);
		return result;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		index = in.readUTF();
		type = in.readOptionalUTF();
		id = in.readUTF();
		version = in.readLong();
		exists = in.readBoolean();
		if (exists) {
			source = in.readBytesReference();
			if (source.length() == 0) {
				source = null;
			}
			int size = in.readVInt();
			if (size == 0) {
				fields = ImmutableMap.of();
			} else {
				fields = newHashMapWithExpectedSize(size);
				for (int i = 0; i < size; i++) {
					GetField field = GetField.readGetField(in);
					fields.put(field.name(), field);
				}
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(index);
		out.writeOptionalUTF(type);
		out.writeUTF(id);
		out.writeLong(version);
		out.writeBoolean(exists);
		if (exists) {
			out.writeBytesHolder(source);
			if (fields == null) {
				out.writeVInt(0);
			} else {
				out.writeVInt(fields.size());
				for (GetField field : fields.values()) {
					field.writeTo(out);
				}
			}
		}
	}
}
