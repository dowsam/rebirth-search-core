/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core Facets.java 2012-7-6 14:29:20 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet;

import java.util.List;
import java.util.Map;

/**
 * The Interface Facets.
 *
 * @author l.xue.nong
 */
public interface Facets extends Iterable<Facet> {

	/**
	 * Facets.
	 *
	 * @return the list
	 */
	List<Facet> facets();

	/**
	 * Gets the facets.
	 *
	 * @return the facets
	 */
	Map<String, Facet> getFacets();

	/**
	 * Facets as map.
	 *
	 * @return the map
	 */
	Map<String, Facet> facetsAsMap();

	/**
	 * Facet.
	 *
	 * @param <T> the generic type
	 * @param facetType the facet type
	 * @param name the name
	 * @return the t
	 */
	<T extends Facet> T facet(Class<T> facetType, String name);

	/**
	 * Facet.
	 *
	 * @param <T> the generic type
	 * @param name the name
	 * @return the t
	 */
	<T extends Facet> T facet(String name);
}
