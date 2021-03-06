/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CompiledScript.java 2012-7-6 14:29:19 l.xue.nong$$
 */

package cn.com.rebirth.search.core.script;

/**
 * The Class CompiledScript.
 *
 * @author l.xue.nong
 */
public class CompiledScript {

	/** The type. */
	private final String type;

	/** The compiled. */
	private final Object compiled;

	/**
	 * Instantiates a new compiled script.
	 *
	 * @param type the type
	 * @param compiled the compiled
	 */
	public CompiledScript(String type, Object compiled) {
		this.type = type;
		this.compiled = compiled;
	}

	/**
	 * Lang.
	 *
	 * @return the string
	 */
	public String lang() {
		return type;
	}

	/**
	 * Compiled.
	 *
	 * @return the object
	 */
	public Object compiled() {
		return compiled;
	}
}
