/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchPhase.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search;

import java.util.Map;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Interface SearchPhase.
 *
 * @author l.xue.nong
 */
public interface SearchPhase {

	/**
	 * Parses the elements.
	 *
	 * @return the map< string,? extends search parse element>
	 */
	Map<String, ? extends SearchParseElement> parseElements();

	/**
	 * Pre process.
	 *
	 * @param context the context
	 */
	void preProcess(SearchContext context);

	/**
	 * Execute.
	 *
	 * @param context the context
	 * @throws RebirthException the rebirth exception
	 */
	void execute(SearchContext context) throws RebirthException;
}
