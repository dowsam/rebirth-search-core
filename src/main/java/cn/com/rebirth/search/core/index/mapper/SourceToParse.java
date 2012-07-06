/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SourceToParse.java 2012-7-6 14:29:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper;

import cn.com.rebirth.search.commons.xcontent.XContentParser;

/**
 * The Class SourceToParse.
 *
 * @author l.xue.nong
 */
public class SourceToParse {

	/**
	 * Source.
	 *
	 * @param source the source
	 * @return the source to parse
	 */
	public static SourceToParse source(byte[] source) {
		return new SourceToParse(source);
	}

	/**
	 * Source.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @return the source to parse
	 */
	public static SourceToParse source(byte[] source, int offset, int length) {
		return new SourceToParse(source, offset, length);
	}

	/**
	 * Source.
	 *
	 * @param parser the parser
	 * @return the source to parse
	 */
	public static SourceToParse source(XContentParser parser) {
		return new SourceToParse(parser);
	}

	/** The source. */
	private final byte[] source;

	/** The source offset. */
	private final int sourceOffset;

	/** The source length. */
	private final int sourceLength;

	/** The parser. */
	private final XContentParser parser;

	/** The flyweight. */
	private boolean flyweight = false;

	/** The type. */
	private String type;

	/** The id. */
	private String id;

	/** The routing. */
	private String routing;

	/** The parent id. */
	private String parentId;

	/** The timestamp. */
	private long timestamp;

	/** The ttl. */
	private long ttl;

	/**
	 * Instantiates a new source to parse.
	 *
	 * @param parser the parser
	 */
	public SourceToParse(XContentParser parser) {
		this.parser = parser;
		this.source = null;
		this.sourceOffset = 0;
		this.sourceLength = 0;
	}

	/**
	 * Instantiates a new source to parse.
	 *
	 * @param source the source
	 */
	public SourceToParse(byte[] source) {
		this.source = source;
		this.sourceOffset = 0;
		this.sourceLength = source.length;
		this.parser = null;
	}

	/**
	 * Instantiates a new source to parse.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 */
	public SourceToParse(byte[] source, int offset, int length) {
		this.source = source;
		this.sourceOffset = offset;
		this.sourceLength = length;
		this.parser = null;
	}

	/**
	 * Parser.
	 *
	 * @return the x content parser
	 */
	public XContentParser parser() {
		return this.parser;
	}

	/**
	 * Source.
	 *
	 * @return the byte[]
	 */
	public byte[] source() {
		return this.source;
	}

	/**
	 * Source offset.
	 *
	 * @return the int
	 */
	public int sourceOffset() {
		return this.sourceOffset;
	}

	/**
	 * Source length.
	 *
	 * @return the int
	 */
	public int sourceLength() {
		return this.sourceLength;
	}

	/**
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		return this.type;
	}

	/**
	 * Type.
	 *
	 * @param type the type
	 * @return the source to parse
	 */
	public SourceToParse type(String type) {
		this.type = type;
		return this;
	}

	/**
	 * Flyweight.
	 *
	 * @param flyweight the flyweight
	 * @return the source to parse
	 */
	public SourceToParse flyweight(boolean flyweight) {
		this.flyweight = flyweight;
		return this;
	}

	/**
	 * Flyweight.
	 *
	 * @return true, if successful
	 */
	public boolean flyweight() {
		return this.flyweight;
	}

	/**
	 * Id.
	 *
	 * @return the string
	 */
	public String id() {
		return this.id;
	}

	/**
	 * Id.
	 *
	 * @param id the id
	 * @return the source to parse
	 */
	public SourceToParse id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Parent.
	 *
	 * @return the string
	 */
	public String parent() {
		return this.parentId;
	}

	/**
	 * Parent.
	 *
	 * @param parentId the parent id
	 * @return the source to parse
	 */
	public SourceToParse parent(String parentId) {
		this.parentId = parentId;
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
	 * @return the source to parse
	 */
	public SourceToParse routing(String routing) {
		this.routing = routing;
		return this;
	}

	/**
	 * Timestamp.
	 *
	 * @return the long
	 */
	public long timestamp() {
		return this.timestamp;
	}

	/**
	 * Timestamp.
	 *
	 * @param timestamp the timestamp
	 * @return the source to parse
	 */
	public SourceToParse timestamp(String timestamp) {
		this.timestamp = Long.parseLong(timestamp);
		return this;
	}

	/**
	 * Timestamp.
	 *
	 * @param timestamp the timestamp
	 * @return the source to parse
	 */
	public SourceToParse timestamp(long timestamp) {
		this.timestamp = timestamp;
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
	 * Ttl.
	 *
	 * @param ttl the ttl
	 * @return the source to parse
	 */
	public SourceToParse ttl(long ttl) {
		this.ttl = ttl;
		return this;
	}
}
