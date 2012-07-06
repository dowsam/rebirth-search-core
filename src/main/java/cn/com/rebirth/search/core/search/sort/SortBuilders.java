/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SortBuilders.java 2012-7-6 14:30:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.sort;

/**
 * The Class SortBuilders.
 *
 * @author l.xue.nong
 */
public class SortBuilders {

	/**
	 * Score sort.
	 *
	 * @return the score sort builder
	 */
	public static ScoreSortBuilder scoreSort() {
		return new ScoreSortBuilder();
	}

	/**
	 * Field sort.
	 *
	 * @param field the field
	 * @return the field sort builder
	 */
	public static FieldSortBuilder fieldSort(String field) {
		return new FieldSortBuilder(field);
	}

	/**
	 * Script sort.
	 *
	 * @param script the script
	 * @param type the type
	 * @return the script sort builder
	 */
	public static ScriptSortBuilder scriptSort(String script, String type) {
		return new ScriptSortBuilder(script, type);
	}

	/**
	 * Geo distance sort.
	 *
	 * @param fieldName the field name
	 * @return the geo distance sort builder
	 */
	public static GeoDistanceSortBuilder geoDistanceSort(String fieldName) {
		return new GeoDistanceSortBuilder(fieldName);
	}
}
