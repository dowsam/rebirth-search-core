/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestartSearchCoreVersion.java 2012-7-19 13:15:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core;

import cn.com.rebirth.commons.AbstractVersion;
import cn.com.rebirth.commons.Version;

/**
 * The Class RestartSearchCoreVersion.
 *
 * @author l.xue.nong
 */
public class RestartSearchCoreVersion extends AbstractVersion implements Version {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8043854155386241988L;

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.Version#getModuleName()
	 */
	@Override
	public String getModuleName() {
		return "restart-search-core";
	}

}
