/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RiversTypesRegistry.java 2012-7-6 14:29:20 l.xue.nong$$
 */

package cn.com.rebirth.search.core.river;

import cn.com.rebirth.search.commons.inject.Module;

import com.google.common.collect.ImmutableMap;

/**
 * The Class RiversTypesRegistry.
 *
 * @author l.xue.nong
 */
public class RiversTypesRegistry {

	/** The river types. */
	private final ImmutableMap<String, Class<? extends Module>> riverTypes;

	/**
	 * Instantiates a new rivers types registry.
	 *
	 * @param riverTypes the river types
	 */
	public RiversTypesRegistry(ImmutableMap<String, Class<? extends Module>> riverTypes) {
		this.riverTypes = riverTypes;
	}

	/**
	 * Type.
	 *
	 * @param type the type
	 * @return the class<? extends module>
	 */
	public Class<? extends Module> type(String type) {
		return riverTypes.get(type);
	}
}