/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GetResponse.java 2012-7-6 14:29:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.get;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.exception.RebirthParseException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.xcontent.ToXContent;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.index.get.GetField;
import cn.com.rebirth.search.core.index.get.GetResult;

/**
 * The Class GetResponse.
 *
 * @author l.xue.nong
 */
public class GetResponse implements ActionResponse, Streamable, Iterable<GetField>, ToXContent {

	/** The get result. */
	private GetResult getResult;

	/**
	 * Instantiates a new gets the response.
	 */
	GetResponse() {
	}

	/**
	 * Instantiates a new gets the response.
	 *
	 * @param getResult the get result
	 */
	GetResponse(GetResult getResult) {
		this.getResult = getResult;
	}

	/**
	 * Exists.
	 *
	 * @return true, if successful
	 */
	public boolean exists() {
		return getResult.exists();
	}

	/**
	 * Checks if is exists.
	 *
	 * @return true, if is exists
	 */
	public boolean isExists() {
		return exists();
	}

	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		return getResult.index();
	}

	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public String getIndex() {
		return index();
	}

	/**
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		return getResult.type();
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type();
	}

	/**
	 * Id.
	 *
	 * @return the string
	 */
	public String id() {
		return getResult.id();
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id();
	}

	/**
	 * Version.
	 *
	 * @return the long
	 */
	public long version() {
		return getResult.version();
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public long getVersion() {
		return version();
	}

	/**
	 * Source.
	 *
	 * @return the byte[]
	 */
	public byte[] source() {
		return getResult.source();
	}

	/**
	 * Source ref.
	 *
	 * @return the bytes holder
	 */
	public BytesHolder sourceRef() {
		return getResult.sourceRef();
	}

	/**
	 * Checks if is source empty.
	 *
	 * @return true, if is source empty
	 */
	public boolean isSourceEmpty() {
		return getResult.isSourceEmpty();
	}

	/**
	 * Source as string.
	 *
	 * @return the string
	 */
	public String sourceAsString() {
		return getResult.sourceAsString();
	}

	/**
	 * Source as map.
	 *
	 * @return the map
	 * @throws RebirthParseException the rebirth parse exception
	 */
	public Map<String, Object> sourceAsMap() throws RebirthParseException {
		return getResult.sourceAsMap();
	}

	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	public Map<String, Object> getSource() {
		return getResult.getSource();
	}

	/**
	 * Fields.
	 *
	 * @return the map
	 */
	public Map<String, GetField> fields() {
		return getResult.fields();
	}

	/**
	 * Gets the fields.
	 *
	 * @return the fields
	 */
	public Map<String, GetField> getFields() {
		return fields();
	}

	/**
	 * Field.
	 *
	 * @param name the name
	 * @return the gets the field
	 */
	public GetField field(String name) {
		return getResult.field(name);
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<GetField> iterator() {
		return getResult.iterator();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		return getResult.toXContent(builder, params);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		getResult = GetResult.readGetResult(in);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		getResult.writeTo(out);
	}
}
