/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ScriptEngineService.java 2012-3-29 15:02:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.script;

import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.core.search.lookup.SearchLookup;


/**
 * The Interface ScriptEngineService.
 *
 * @author l.xue.nong
 */
public interface ScriptEngineService {

	
	/**
	 * Types.
	 *
	 * @return the string[]
	 */
	String[] types();

	
	/**
	 * Extensions.
	 *
	 * @return the string[]
	 */
	String[] extensions();

	
	/**
	 * Compile.
	 *
	 * @param script the script
	 * @return the object
	 */
	Object compile(String script);

	
	/**
	 * Executable.
	 *
	 * @param compiledScript the compiled script
	 * @param vars the vars
	 * @return the executable script
	 */
	ExecutableScript executable(Object compiledScript, @Nullable Map<String, Object> vars);

	
	/**
	 * Search.
	 *
	 * @param compiledScript the compiled script
	 * @param lookup the lookup
	 * @param vars the vars
	 * @return the search script
	 */
	SearchScript search(Object compiledScript, SearchLookup lookup, @Nullable Map<String, Object> vars);

	
	/**
	 * Execute.
	 *
	 * @param compiledScript the compiled script
	 * @param vars the vars
	 * @return the object
	 */
	Object execute(Object compiledScript, Map<String, Object> vars);

	
	/**
	 * Unwrap.
	 *
	 * @param value the value
	 * @return the object
	 */
	Object unwrap(Object value);

	
	/**
	 * Close.
	 */
	void close();
}
