/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RiverName.java 2012-7-6 14:29:31 l.xue.nong$$
 */

package cn.com.rebirth.search.core.river;

import java.io.Serializable;

/**
 * The Class RiverName.
 *
 * @author l.xue.nong
 */
public class RiverName implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4557172670023833854L;

	/** The type. */
	private final String type;

	/** The name. */
	private final String name;

	/**
	 * Instantiates a new river name.
	 *
	 * @param type the type
	 * @param name the name
	 */
	public RiverName(String type, String name) {
		this.type = type;
		this.name = name;
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
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type();
	}

	/**
	 * Name.
	 *
	 * @return the string
	 */
	public String name() {
		return this.name;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		RiverName that = (RiverName) o;

		if (name != null ? !name.equals(that.name) : that.name != null)
			return false;
		if (type != null ? !type.equals(that.type) : that.type != null)
			return false;

		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = type != null ? type.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}
}
