/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NativeScriptFactory.java 2012-3-29 15:02:42 l.xue.nong$$
 */


package cn.com.rebirth.search.core.script;

import java.util.Map;

import cn.com.rebirth.commons.Nullable;


/**
 * A factory for creating NativeScript objects.
 */
public interface NativeScriptFactory {

	
	/**
	 * New script.
	 *
	 * @param params the params
	 * @return the executable script
	 */
	ExecutableScript newScript(@Nullable Map<String, Object> params);
}