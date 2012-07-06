/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ScriptFieldsContext.java 2012-7-6 14:29:12 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.fetch.script;

import java.util.List;

import cn.com.rebirth.search.core.script.SearchScript;

import com.google.common.collect.Lists;

/**
 * The Class ScriptFieldsContext.
 *
 * @author l.xue.nong
 */
public class ScriptFieldsContext {

	/**
	 * The Class ScriptField.
	 *
	 * @author l.xue.nong
	 */
	public static class ScriptField {

		/** The name. */
		private final String name;

		/** The script. */
		private final SearchScript script;

		/** The ignore exception. */
		private final boolean ignoreException;

		/**
		 * Instantiates a new script field.
		 *
		 * @param name the name
		 * @param script the script
		 * @param ignoreException the ignore exception
		 */
		public ScriptField(String name, SearchScript script, boolean ignoreException) {
			this.name = name;
			this.script = script;
			this.ignoreException = ignoreException;
		}

		/**
		 * Name.
		 *
		 * @return the string
		 */
		public String name() {
			return name;
		}

		/**
		 * Script.
		 *
		 * @return the search script
		 */
		public SearchScript script() {
			return this.script;
		}

		/**
		 * Ignore exception.
		 *
		 * @return true, if successful
		 */
		public boolean ignoreException() {
			return ignoreException;
		}
	}

	/** The fields. */
	private List<ScriptField> fields = Lists.newArrayList();

	/**
	 * Instantiates a new script fields context.
	 */
	public ScriptFieldsContext() {
	}

	/**
	 * Adds the.
	 *
	 * @param field the field
	 */
	public void add(ScriptField field) {
		this.fields.add(field);
	}

	/**
	 * Fields.
	 *
	 * @return the list
	 */
	public List<ScriptField> fields() {
		return this.fields;
	}
}
