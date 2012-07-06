/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchHitField.java 2012-7-6 14:28:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search;

import java.util.List;

import cn.com.rebirth.commons.io.stream.Streamable;

/**
 * The Interface SearchHitField.
 *
 * @author l.xue.nong
 */
public interface SearchHitField extends Streamable, Iterable<Object> {

	/**
	 * Name.
	 *
	 * @return the string
	 */
	String name();

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	String getName();

	/**
	 * Value.
	 *
	 * @param <V> the value type
	 * @return the v
	 */
	<V> V value();

	/**
	 * Gets the value.
	 *
	 * @param <V> the value type
	 * @return the value
	 */
	<V> V getValue();

	/**
	 * Values.
	 *
	 * @return the list
	 */
	List<Object> values();

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	List<Object> getValues();
}
