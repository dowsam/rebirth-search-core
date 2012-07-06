/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexAlias.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.aliases;

import org.apache.lucene.search.Filter;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.compress.CompressedString;

/**
 * The Class IndexAlias.
 *
 * @author l.xue.nong
 */
public class IndexAlias {

	/** The alias. */
	private String alias;

	/** The filter. */
	private CompressedString filter;

	/** The parsed filter. */
	private Filter parsedFilter;

	/**
	 * Instantiates a new index alias.
	 *
	 * @param alias the alias
	 * @param filter the filter
	 * @param parsedFilter the parsed filter
	 */
	public IndexAlias(String alias, @Nullable CompressedString filter, @Nullable Filter parsedFilter) {
		this.alias = alias;
		this.filter = filter;
		this.parsedFilter = parsedFilter;
	}

	/**
	 * Alias.
	 *
	 * @return the string
	 */
	public String alias() {
		return alias;
	}

	/**
	 * Filter.
	 *
	 * @return the compressed string
	 */
	@Nullable
	public CompressedString filter() {
		return filter;
	}

	/**
	 * Parsed filter.
	 *
	 * @return the filter
	 */
	@Nullable
	public Filter parsedFilter() {
		return parsedFilter;
	}

}
