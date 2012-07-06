/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RiverIndexName.java 2012-7-6 14:30:29 l.xue.nong$$
 */

package cn.com.rebirth.search.core.river;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.BindingAnnotation;

/**
 * The Interface RiverIndexName.
 *
 * @author l.xue.nong
 */
@BindingAnnotation
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
@Documented
public @interface RiverIndexName {

	/**
	 * The Class Conf.
	 *
	 * @author l.xue.nong
	 */
	static class Conf {

		/** The Constant DEFAULT_INDEX_NAME. */
		public static final String DEFAULT_INDEX_NAME = "_river";

		/**
		 * Index name.
		 *
		 * @param settings the settings
		 * @return the string
		 */
		public static String indexName(Settings settings) {
			return settings.get("river.index_name", DEFAULT_INDEX_NAME);
		}
	}
}
