/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LocalIndexGateway.java 2012-7-6 14:30:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.gateway.local;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.gateway.IndexGateway;
import cn.com.rebirth.search.core.index.gateway.IndexShardGateway;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class LocalIndexGateway.
 *
 * @author l.xue.nong
 */
public class LocalIndexGateway extends AbstractIndexComponent implements IndexGateway {

	/**
	 * Instantiates a new local index gateway.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 */
	@Inject
	public LocalIndexGateway(Index index, @IndexSettings Settings indexSettings) {
		super(index, indexSettings);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexGateway#type()
	 */
	@Override
	public String type() {
		return "local";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.gateway.IndexGateway#shardGatewayClass()
	 */
	@Override
	public Class<? extends IndexShardGateway> shardGatewayClass() {
		return LocalIndexShardGateway.class;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "local";
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.CloseableIndexComponent#close(boolean)
	 */
	@Override
	public void close(boolean delete) {
	}
}
