/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchRequest.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.lucene.util.UnicodeUtil;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.Unicode;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.io.BytesStream;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentFactory;
import cn.com.rebirth.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.RestartGenerationException;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.search.Scroll;
import cn.com.rebirth.search.core.search.builder.SearchSourceBuilder;

/**
 * The Class SearchRequest.
 *
 * @author l.xue.nong
 */
public class SearchRequest implements ActionRequest {

	/** The Constant contentType. */
	private static final XContentType contentType = Requests.CONTENT_TYPE;

	/** The search type. */
	private SearchType searchType = SearchType.DEFAULT;

	/** The indices. */
	private String[] indices;

	/** The query hint. */
	@Nullable
	private String queryHint;

	/** The routing. */
	@Nullable
	private String routing;

	/** The preference. */
	@Nullable
	private String preference;

	/** The source. */
	private byte[] source;

	/** The source offset. */
	private int sourceOffset;

	/** The source length. */
	private int sourceLength;

	/** The source unsafe. */
	private boolean sourceUnsafe;

	/** The extra source. */
	private byte[] extraSource;

	/** The extra source offset. */
	private int extraSourceOffset;

	/** The extra source length. */
	private int extraSourceLength;

	/** The extra source unsafe. */
	private boolean extraSourceUnsafe;

	/** The scroll. */
	private Scroll scroll;

	/** The types. */
	private String[] types = Strings.EMPTY_ARRAY;

	/** The listener threaded. */
	private boolean listenerThreaded = false;

	/** The operation threading. */
	private SearchOperationThreading operationThreading = SearchOperationThreading.THREAD_PER_SHARD;

	/**
	 * Instantiates a new search request.
	 */
	public SearchRequest() {
	}

	/**
	 * Instantiates a new search request.
	 *
	 * @param indices the indices
	 */
	public SearchRequest(String... indices) {
		this.indices = indices;
	}

	/**
	 * Instantiates a new search request.
	 *
	 * @param indices the indices
	 * @param source the source
	 */
	public SearchRequest(String[] indices, byte[] source) {
		this.indices = indices;
		this.source = source;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;

		return validationException;
	}

	/**
	 * Before start.
	 */
	public void beforeStart() {

		if (source != null && sourceUnsafe) {
			source = Arrays.copyOfRange(source, sourceOffset, sourceOffset + sourceLength);
			sourceOffset = 0;
			sourceUnsafe = false;
		}
		if (extraSource != null && extraSourceUnsafe) {
			extraSource = Arrays.copyOfRange(extraSource, extraSourceOffset, extraSourceOffset + extraSourceLength);
			extraSourceOffset = 0;
			extraSourceUnsafe = false;
		}
	}

	/**
	 * Before local fork.
	 */
	public void beforeLocalFork() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#listenerThreaded()
	 */
	@Override
	public boolean listenerThreaded() {
		return listenerThreaded;
	}

	/**
	 * Indices.
	 *
	 * @param indices the indices
	 * @return the search request
	 */
	public SearchRequest indices(String... indices) {
		this.indices = indices;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#listenerThreaded(boolean)
	 */
	@Override
	public SearchRequest listenerThreaded(boolean listenerThreaded) {
		this.listenerThreaded = listenerThreaded;
		return this;
	}

	/**
	 * Operation threading.
	 *
	 * @return the search operation threading
	 */
	public SearchOperationThreading operationThreading() {
		return this.operationThreading;
	}

	/**
	 * Operation threading.
	 *
	 * @param operationThreading the operation threading
	 * @return the search request
	 */
	public SearchRequest operationThreading(SearchOperationThreading operationThreading) {
		this.operationThreading = operationThreading;
		return this;
	}

	/**
	 * Operation threading.
	 *
	 * @param operationThreading the operation threading
	 * @return the search request
	 */
	public SearchRequest operationThreading(String operationThreading) {
		return operationThreading(SearchOperationThreading.fromString(operationThreading, this.operationThreading));
	}

	/**
	 * Types.
	 *
	 * @return the string[]
	 */
	public String[] types() {
		return types;
	}

	/**
	 * Types.
	 *
	 * @param types the types
	 * @return the search request
	 */
	public SearchRequest types(String... types) {
		this.types = types;
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
	 * Routing.
	 *
	 * @param routing the routing
	 * @return the search request
	 */
	public SearchRequest routing(String routing) {
		this.routing = routing;
		return this;
	}

	/**
	 * Routing.
	 *
	 * @param routings the routings
	 * @return the search request
	 */
	public SearchRequest routing(String... routings) {
		this.routing = Strings.arrayToCommaDelimitedString(routings);
		return this;
	}

	/**
	 * Preference.
	 *
	 * @param preference the preference
	 * @return the search request
	 */
	public SearchRequest preference(String preference) {
		this.preference = preference;
		return this;
	}

	/**
	 * Preference.
	 *
	 * @return the string
	 */
	public String preference() {
		return this.preference;
	}

	/**
	 * Search type.
	 *
	 * @param searchType the search type
	 * @return the search request
	 */
	public SearchRequest searchType(SearchType searchType) {
		this.searchType = searchType;
		return this;
	}

	/**
	 * Search type.
	 *
	 * @param searchType the search type
	 * @return the search request
	 * @throws RebirthIllegalArgumentException the rebirth illegal argument exception
	 */
	public SearchRequest searchType(String searchType) throws RebirthIllegalArgumentException {
		return searchType(SearchType.fromString(searchType));
	}

	/**
	 * Source.
	 *
	 * @param sourceBuilder the source builder
	 * @return the search request
	 */
	public SearchRequest source(SearchSourceBuilder sourceBuilder) {
		BytesStream bos = sourceBuilder.buildAsBytesStream(Requests.CONTENT_TYPE);
		this.source = bos.underlyingBytes();
		this.sourceOffset = 0;
		this.sourceLength = bos.size();
		this.sourceUnsafe = false;
		return this;
	}

	/**
	 * Source.
	 *
	 * @param source the source
	 * @return the search request
	 */
	public SearchRequest source(String source) {
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
	 * @param source the source
	 * @return the search request
	 */
	public SearchRequest source(Map source) {
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
	 * @param builder the builder
	 * @return the search request
	 */
	public SearchRequest source(XContentBuilder builder) {
		try {
			this.source = builder.underlyingBytes();
			this.sourceOffset = 0;
			this.sourceLength = builder.underlyingBytesLength();
			this.sourceUnsafe = false;
			return this;
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + builder + "]", e);
		}
	}

	/**
	 * Source.
	 *
	 * @param source the source
	 * @return the search request
	 */
	public SearchRequest source(byte[] source) {
		return source(source, 0, source.length, false);
	}

	/**
	 * Source.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @return the search request
	 */
	public SearchRequest source(byte[] source, int offset, int length) {
		return source(source, offset, length, false);
	}

	/**
	 * Source.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @param unsafe the unsafe
	 * @return the search request
	 */
	public SearchRequest source(byte[] source, int offset, int length, boolean unsafe) {
		this.source = source;
		this.sourceOffset = offset;
		this.sourceLength = length;
		this.sourceUnsafe = unsafe;
		return this;
	}

	/**
	 * Source.
	 *
	 * @return the byte[]
	 */
	public byte[] source() {
		return source;
	}

	/**
	 * Source offset.
	 *
	 * @return the int
	 */
	public int sourceOffset() {
		return sourceOffset;
	}

	/**
	 * Source length.
	 *
	 * @return the int
	 */
	public int sourceLength() {
		return sourceLength;
	}

	/**
	 * Extra source.
	 *
	 * @param sourceBuilder the source builder
	 * @return the search request
	 */
	public SearchRequest extraSource(SearchSourceBuilder sourceBuilder) {
		if (sourceBuilder == null) {
			extraSource = null;
			return this;
		}
		BytesStream bos = sourceBuilder.buildAsBytesStream(Requests.CONTENT_TYPE);
		this.extraSource = bos.underlyingBytes();
		this.extraSourceOffset = 0;
		this.extraSourceLength = bos.size();
		this.extraSourceUnsafe = false;
		return this;
	}

	/**
	 * Extra source.
	 *
	 * @param extraSource the extra source
	 * @return the search request
	 */
	public SearchRequest extraSource(Map extraSource) {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(contentType);
			builder.map(extraSource);
			return extraSource(builder);
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + source + "]", e);
		}
	}

	/**
	 * Extra source.
	 *
	 * @param builder the builder
	 * @return the search request
	 */
	public SearchRequest extraSource(XContentBuilder builder) {
		try {
			this.extraSource = builder.underlyingBytes();
			this.extraSourceOffset = 0;
			this.extraSourceLength = builder.underlyingBytesLength();
			this.extraSourceUnsafe = false;
			return this;
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + builder + "]", e);
		}
	}

	/**
	 * Extra source.
	 *
	 * @param source the source
	 * @return the search request
	 */
	public SearchRequest extraSource(String source) {
		UnicodeUtil.UTF8Result result = Unicode.fromStringAsUtf8(source);
		this.extraSource = result.result;
		this.extraSourceOffset = 0;
		this.extraSourceLength = result.length;
		this.extraSourceUnsafe = true;
		return this;
	}

	/**
	 * Extra source.
	 *
	 * @param source the source
	 * @return the search request
	 */
	public SearchRequest extraSource(byte[] source) {
		return extraSource(source, 0, source.length, false);
	}

	/**
	 * Extra source.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @return the search request
	 */
	public SearchRequest extraSource(byte[] source, int offset, int length) {
		return extraSource(source, offset, length, false);
	}

	/**
	 * Extra source.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @param unsafe the unsafe
	 * @return the search request
	 */
	public SearchRequest extraSource(byte[] source, int offset, int length, boolean unsafe) {
		this.extraSource = source;
		this.extraSourceOffset = offset;
		this.extraSourceLength = length;
		this.extraSourceUnsafe = unsafe;
		return this;
	}

	/**
	 * Extra source.
	 *
	 * @return the byte[]
	 */
	public byte[] extraSource() {
		return this.extraSource;
	}

	/**
	 * Extra source offset.
	 *
	 * @return the int
	 */
	public int extraSourceOffset() {
		return extraSourceOffset;
	}

	/**
	 * Extra source length.
	 *
	 * @return the int
	 */
	public int extraSourceLength() {
		return extraSourceLength;
	}

	/**
	 * Search type.
	 *
	 * @return the search type
	 */
	public SearchType searchType() {
		return searchType;
	}

	/**
	 * Indices.
	 *
	 * @return the string[]
	 */
	public String[] indices() {
		return indices;
	}

	/**
	 * Query hint.
	 *
	 * @param queryHint the query hint
	 * @return the search request
	 */
	public SearchRequest queryHint(String queryHint) {
		this.queryHint = queryHint;
		return this;
	}

	/**
	 * Query hint.
	 *
	 * @return the string
	 */
	public String queryHint() {
		return queryHint;
	}

	/**
	 * Scroll.
	 *
	 * @return the scroll
	 */
	public Scroll scroll() {
		return scroll;
	}

	/**
	 * Scroll.
	 *
	 * @param scroll the scroll
	 * @return the search request
	 */
	public SearchRequest scroll(Scroll scroll) {
		this.scroll = scroll;
		return this;
	}

	/**
	 * Scroll.
	 *
	 * @param keepAlive the keep alive
	 * @return the search request
	 */
	public SearchRequest scroll(TimeValue keepAlive) {
		return scroll(new Scroll(keepAlive));
	}

	/**
	 * Scroll.
	 *
	 * @param keepAlive the keep alive
	 * @return the search request
	 */
	public SearchRequest scroll(String keepAlive) {
		return scroll(new Scroll(TimeValue.parseTimeValue(keepAlive, null)));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		operationThreading = SearchOperationThreading.fromId(in.readByte());
		searchType = SearchType.fromId(in.readByte());

		indices = new String[in.readVInt()];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = in.readUTF();
		}

		if (in.readBoolean()) {
			queryHint = in.readUTF();
		}
		if (in.readBoolean()) {
			routing = in.readUTF();
		}
		if (in.readBoolean()) {
			preference = in.readUTF();
		}

		if (in.readBoolean()) {
			scroll = Scroll.readScroll(in);
		}

		BytesHolder bytes = in.readBytesReference();
		sourceUnsafe = false;
		source = bytes.bytes();
		sourceOffset = bytes.offset();
		sourceLength = bytes.length();

		bytes = in.readBytesReference();
		extraSourceUnsafe = false;
		extraSource = bytes.bytes();
		extraSourceOffset = bytes.offset();
		extraSourceLength = bytes.length();

		int typesSize = in.readVInt();
		if (typesSize > 0) {
			types = new String[typesSize];
			for (int i = 0; i < typesSize; i++) {
				types[i] = in.readUTF();
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeByte(operationThreading.id());
		out.writeByte(searchType.id());

		out.writeVInt(indices.length);
		for (String index : indices) {
			out.writeUTF(index);
		}

		if (queryHint == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(queryHint);
		}
		if (routing == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(routing);
		}
		if (preference == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(preference);
		}

		if (scroll == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			scroll.writeTo(out);
		}
		out.writeBytesHolder(source, sourceOffset, sourceLength);
		out.writeBytesHolder(extraSource, extraSourceOffset, extraSourceLength);
		out.writeVInt(types.length);
		for (String type : types) {
			out.writeUTF(type);
		}
	}
}
