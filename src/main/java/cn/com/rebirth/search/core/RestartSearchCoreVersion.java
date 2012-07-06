/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestartSearchCoreVersion.java 2012-7-6 14:29:09 l.xue.nong$$
 */

package cn.com.rebirth.search.core;

import cn.com.rebirth.commons.Version;

/**
 * The Class RestartSearchCoreVersion.
 *
 * @author l.xue.nong
 */
public class RestartSearchCoreVersion implements Version {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8043854155386241988L;

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.Version#getModuleVersion()
	 */
	@Override
	public String getModuleVersion() {
		return "0.0.1.RC1-SNAPSHOT";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.Version#getModuleName()
	 */
	@Override
	public String getModuleName() {
		return "restart-search-core";
	}

}
