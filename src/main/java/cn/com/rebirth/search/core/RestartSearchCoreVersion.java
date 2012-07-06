/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core VersionImpl.java 2012-3-29 15:00:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core;

import cn.com.rebirth.commons.Version;

/**
 * The Class VersionImpl.
 *
 * @author l.xue.nong
 */
public class RestartSearchCoreVersion implements Version {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8043854155386241988L;

	/* (non-Javadoc)
	 * @see cn.com.summall.commons.Version#getModuleVersion()
	 */
	@Override
	public String getModuleVersion() {
		return "0.0.1.RC1-SNAPSHOT";
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.commons.Version#getModuleName()
	 */
	@Override
	public String getModuleName() {
		return "restart-search-core";
	}

}
