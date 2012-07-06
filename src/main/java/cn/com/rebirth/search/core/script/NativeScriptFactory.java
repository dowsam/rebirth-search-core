/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NativeScriptFactory.java 2012-7-6 14:29:35 l.xue.nong$$
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