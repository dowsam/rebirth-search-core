/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BlobStoreIndexGateway.java 2012-3-29 15:00:48 l.xue.nong$$
 */
package cn.com.rebirth.search.core.index.gateway.blobstore;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.commons.blobstore.BlobPath;
import cn.com.rebirth.search.commons.blobstore.BlobStore;
import cn.com.rebirth.search.core.gateway.Gateway;
import cn.com.rebirth.search.core.gateway.blobstore.BlobStoreGateway;
import cn.com.rebirth.search.core.gateway.none.NoneGateway;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.gateway.IndexGateway;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class BlobStoreIndexGateway.
 *
 * @author l.xue.nong
 */
public abstract class BlobStoreIndexGateway extends AbstractIndexComponent implements IndexGateway {

	/** The gateway. */
	private final BlobStoreGateway gateway;

	/** The blob store. */
	private final BlobStore blobStore;

	/** The index path. */
	private final BlobPath indexPath;

	/** The chunk size. */
	protected ByteSizeValue chunkSize;

	/**
	 * Instantiates a new blob store index gateway.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param gateway the gateway
	 */
	protected BlobStoreIndexGateway(Index index, @IndexSettings Settings indexSettings, Gateway gateway) {
		super(index, indexSettings);

		if (gateway.type().equals(NoneGateway.TYPE)) {
			logger.warn("index gateway is configured, but no cluster level gateway configured, cluster level metadata will be lost on full shutdown");
		}

		this.gateway = (BlobStoreGateway) gateway;
		this.blobStore = this.gateway.blobStore();

		this.chunkSize = componentSettings.getAsBytesSize("chunk_size", this.gateway.chunkSize());

		this.indexPath = this.gateway.basePath().add("indices").add(index.name());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return type() + "://" + blobStore + "/" + indexPath;
	}

	/**
	 * Blob store.
	 *
	 * @return the blob store
	 */
	public BlobStore blobStore() {
		return blobStore;
	}

	/**
	 * Chunk size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue chunkSize() {
		return this.chunkSize;
	}

	/**
	 * Shard path.
	 *
	 * @param shardId the shard id
	 * @return the blob path
	 */
	public BlobPath shardPath(int shardId) {
		return indexPath.add(Integer.toString(shardId));
	}

	/**
	 * Shard path.
	 *
	 * @param basePath the base path
	 * @param index the index
	 * @param shardId the shard id
	 * @return the blob path
	 */
	public static BlobPath shardPath(BlobPath basePath, String index, int shardId) {
		return basePath.add("indices").add(index).add(Integer.toString(shardId));
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.CloseableIndexComponent#close(boolean)
	 */
	@Override
	public void close(boolean delete) throws RestartException {
		if (delete) {
			blobStore.delete(indexPath);
		}
	}
}
