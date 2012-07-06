/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexServiceManagement.java 2012-7-6 14:30:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.CloseableComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.jmx.JmxService;
import cn.com.rebirth.search.core.jmx.MBean;
import cn.com.rebirth.search.core.jmx.ManagedAttribute;

/**
 * The Class IndexServiceManagement.
 *
 * @author l.xue.nong
 */
@MBean(objectName = "", description = "")
public class IndexServiceManagement extends AbstractIndexComponent implements CloseableComponent {

	/**
	 * Builds the index group name.
	 *
	 * @param index the index
	 * @return the string
	 */
	public static String buildIndexGroupName(Index index) {
		return "service=indices,index=" + index.name();
	}

	/** The jmx service. */
	private final JmxService jmxService;

	/** The index service. */
	private final IndexService indexService;

	/**
	 * Instantiates a new index service management.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param jmxService the jmx service
	 * @param indexService the index service
	 */
	@Inject
	public IndexServiceManagement(Index index, @IndexSettings Settings indexSettings, JmxService jmxService,
			IndexService indexService) {
		super(index, indexSettings);
		this.jmxService = jmxService;
		this.indexService = indexService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.CloseableComponent#close()
	 */
	public void close() {
		jmxService.unregisterGroup(buildIndexGroupName(indexService.index()));
	}

	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	@ManagedAttribute(description = "Index Name")
	public String getIndex() {
		return indexService.index().name();
	}
}
