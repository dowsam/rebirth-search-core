/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PartialFieldsContext.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.fetch.partial;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * The Class PartialFieldsContext.
 *
 * @author l.xue.nong
 */
public class PartialFieldsContext {

	/**
	 * The Class PartialField.
	 *
	 * @author l.xue.nong
	 */
	public static class PartialField {

		/** The name. */
		private final String name;

		/** The includes. */
		private final String[] includes;

		/** The excludes. */
		private final String[] excludes;

		/**
		 * Instantiates a new partial field.
		 *
		 * @param name the name
		 * @param includes the includes
		 * @param excludes the excludes
		 */
		public PartialField(String name, String[] includes, String[] excludes) {
			this.name = name;
			this.includes = includes;
			this.excludes = excludes;
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
		 * Includes.
		 *
		 * @return the string[]
		 */
		public String[] includes() {
			return this.includes;
		}

		/**
		 * Excludes.
		 *
		 * @return the string[]
		 */
		public String[] excludes() {
			return this.excludes;
		}
	}

	/** The fields. */
	private final List<PartialField> fields = Lists.newArrayList();

	/**
	 * Instantiates a new partial fields context.
	 */
	public PartialFieldsContext() {

	}

	/**
	 * Adds the.
	 *
	 * @param field the field
	 */
	public void add(PartialField field) {
		fields.add(field);
	}

	/**
	 * Fields.
	 *
	 * @return the list
	 */
	public List<PartialField> fields() {
		return this.fields;
	}
}
