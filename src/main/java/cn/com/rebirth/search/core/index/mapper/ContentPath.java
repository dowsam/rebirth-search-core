/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ContentPath.java 2012-7-6 14:29:12 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper;

/**
 * The Class ContentPath.
 *
 * @author l.xue.nong
 */
public class ContentPath {

	/**
	 * The Enum Type.
	 *
	 * @author l.xue.nong
	 */
	public static enum Type {

		/** The just name. */
		JUST_NAME,

		/** The full. */
		FULL,
	}

	/** The path type. */
	private Type pathType;

	/** The delimiter. */
	private final char delimiter;

	/** The sb. */
	private final StringBuilder sb;

	/** The offset. */
	private final int offset;

	/** The index. */
	private int index = 0;

	/** The path. */
	private String[] path = new String[10];

	/** The source path. */
	private String sourcePath;

	/**
	 * Instantiates a new content path.
	 */
	public ContentPath() {
		this(0);
	}

	/**
	 * Instantiates a new content path.
	 *
	 * @param offset the offset
	 */
	public ContentPath(int offset) {
		this.delimiter = '.';
		this.sb = new StringBuilder();
		this.offset = offset;
		reset();
	}

	/**
	 * Reset.
	 */
	public void reset() {
		this.index = 0;
		this.sourcePath = null;
	}

	/**
	 * Adds the.
	 *
	 * @param name the name
	 */
	public void add(String name) {
		path[index++] = name;
		if (index == path.length) {
			String[] newPath = new String[path.length + 10];
			System.arraycopy(path, 0, newPath, 0, path.length);
			path = newPath;
		}
	}

	/**
	 * Removes the.
	 */
	public void remove() {
		path[index--] = null;
	}

	/**
	 * Path as text.
	 *
	 * @param name the name
	 * @return the string
	 */
	public String pathAsText(String name) {
		if (pathType == Type.JUST_NAME) {
			return name;
		}
		return fullPathAsText(name);
	}

	/**
	 * Full path as text.
	 *
	 * @param name the name
	 * @return the string
	 */
	public String fullPathAsText(String name) {
		sb.setLength(0);
		for (int i = offset; i < index; i++) {
			sb.append(path[i]).append(delimiter);
		}
		sb.append(name);
		return sb.toString();
	}

	/**
	 * Path type.
	 *
	 * @return the type
	 */
	public Type pathType() {
		return pathType;
	}

	/**
	 * Path type.
	 *
	 * @param type the type
	 */
	public void pathType(Type type) {
		this.pathType = type;
	}

	/**
	 * Source path.
	 *
	 * @param sourcePath the source path
	 * @return the string
	 */
	public String sourcePath(String sourcePath) {
		String orig = this.sourcePath;
		this.sourcePath = sourcePath;
		return orig;
	}

	/**
	 * Source path.
	 *
	 * @return the string
	 */
	public String sourcePath() {
		return this.sourcePath;
	}
}
