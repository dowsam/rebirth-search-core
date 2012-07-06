/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BlobStoreGateway.java 2012-7-6 14:29:27 l.xue.nong$$
 */

package cn.com.rebirth.search.core.gateway.blobstore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.compress.lzf.LZF;
import cn.com.rebirth.commons.io.stream.CachedStreamInput;
import cn.com.rebirth.commons.io.stream.LZFStreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.commons.blobstore.BlobContainer;
import cn.com.rebirth.search.commons.blobstore.BlobMetaData;
import cn.com.rebirth.search.commons.blobstore.BlobPath;
import cn.com.rebirth.search.commons.blobstore.BlobStore;
import cn.com.rebirth.search.commons.blobstore.ImmutableBlobContainer;
import cn.com.rebirth.search.commons.io.stream.BytesStreamInput;
import cn.com.rebirth.search.commons.io.stream.CachedStreamOutput;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.gateway.GatewayException;
import cn.com.rebirth.search.core.gateway.shared.SharedStorageGateway;
import cn.com.rebirth.search.core.index.gateway.CommitPoint;
import cn.com.rebirth.search.core.index.gateway.CommitPoints;
import cn.com.rebirth.search.core.index.gateway.blobstore.BlobStoreIndexGateway;
import cn.com.rebirth.search.core.threadpool.ThreadPool;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * The Class BlobStoreGateway.
 *
 * @author l.xue.nong
 */
public abstract class BlobStoreGateway extends SharedStorageGateway {

	/** The blob store. */
	private BlobStore blobStore;

	/** The chunk size. */
	private ByteSizeValue chunkSize;

	/** The base path. */
	private BlobPath basePath;

	/** The meta data blob container. */
	private ImmutableBlobContainer metaDataBlobContainer;

	/** The compress. */
	private boolean compress;

	/** The current index. */
	private volatile int currentIndex;

	/**
	 * Instantiates a new blob store gateway.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 */
	protected BlobStoreGateway(Settings settings, ThreadPool threadPool, ClusterService clusterService) {
		super(settings, threadPool, clusterService);
	}

	/**
	 * Initialize.
	 *
	 * @param blobStore the blob store
	 * @param clusterName the cluster name
	 * @param defaultChunkSize the default chunk size
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void initialize(BlobStore blobStore, ClusterName clusterName, @Nullable ByteSizeValue defaultChunkSize)
			throws IOException {
		this.blobStore = blobStore;
		this.chunkSize = componentSettings.getAsBytesSize("chunk_size", defaultChunkSize);
		this.basePath = BlobPath.cleanPath().add(clusterName.value());
		this.metaDataBlobContainer = blobStore.immutableBlobContainer(basePath.add("metadata"));
		this.currentIndex = findLatestIndex();
		this.compress = componentSettings.getAsBoolean("compress", true);
		logger.debug("Latest metadata found at index [" + currentIndex + "]");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return type() + "://" + blobStore + "/" + basePath;
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
	 * Base path.
	 *
	 * @return the blob path
	 */
	public BlobPath basePath() {
		return basePath;
	}

	/**
	 * Chunk size.
	 *
	 * @return the byte size value
	 */
	public ByteSizeValue chunkSize() {
		return this.chunkSize;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.gateway.Gateway#reset()
	 */
	@Override
	public void reset() throws Exception {
		blobStore.delete(BlobPath.cleanPath());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.gateway.shared.SharedStorageGateway#read()
	 */
	@Override
	public MetaData read() throws GatewayException {
		try {
			this.currentIndex = findLatestIndex();
		} catch (IOException e) {
			throw new GatewayException("Failed to find latest metadata to read from", e);
		}
		if (currentIndex == -1)
			return null;
		String metaData = "metadata-" + currentIndex;

		try {
			return readMetaData(metaDataBlobContainer.readBlobFully(metaData));
		} catch (GatewayException e) {
			throw e;
		} catch (Exception e) {
			throw new GatewayException("Failed to read metadata [" + metaData + "] from gateway", e);
		}
	}

	/**
	 * Find commit point.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 * @return the commit point
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public CommitPoint findCommitPoint(String index, int shardId) throws IOException {
		BlobPath path = BlobStoreIndexGateway.shardPath(basePath, index, shardId);
		ImmutableBlobContainer container = blobStore.immutableBlobContainer(path);
		ImmutableMap<String, BlobMetaData> blobs = container.listBlobs();
		List<CommitPoint> commitPointsList = Lists.newArrayList();
		for (BlobMetaData md : blobs.values()) {
			if (md.length() == 0) {
				continue;
			}
			if (md.name().startsWith("commit-")) {
				try {
					commitPointsList.add(CommitPoints.fromXContent(container.readBlobFully(md.name())));
				} catch (Exception e) {
					logger.warn("failed to read commit point at path " + path + " with name [" + md.name() + "]", e);
				}
			}
		}
		CommitPoints commitPoints = new CommitPoints(commitPointsList);
		if (commitPoints.commits().isEmpty()) {
			return null;
		}
		return commitPoints.commits().get(0);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.gateway.shared.SharedStorageGateway#write(cn.com.rebirth.search.core.cluster.metadata.MetaData)
	 */
	@Override
	public void write(MetaData metaData) throws GatewayException {
		final String newMetaData = "metadata-" + (currentIndex + 1);
		CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
		try {
			StreamOutput streamOutput;
			if (compress) {
				streamOutput = cachedEntry.cachedLZFBytes();
			} else {
				streamOutput = cachedEntry.cachedBytes();
			}
			XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON, streamOutput);
			builder.startObject();
			MetaData.Builder.toXContent(metaData, builder, ToXContent.EMPTY_PARAMS);
			builder.endObject();
			builder.close();
			metaDataBlobContainer.writeBlob(newMetaData, new ByteArrayInputStream(
					cachedEntry.bytes().underlyingBytes(), 0, cachedEntry.bytes().size()), cachedEntry.bytes().size());
		} catch (IOException e) {
			throw new GatewayException("Failed to write metadata [" + newMetaData + "]", e);
		} finally {
			CachedStreamOutput.pushEntry(cachedEntry);
		}

		currentIndex++;

		try {
			metaDataBlobContainer.deleteBlobsByFilter(new BlobContainer.BlobNameFilter() {
				@Override
				public boolean accept(String blobName) {
					return blobName.startsWith("metadata-") && !newMetaData.equals(blobName);
				}
			});
		} catch (IOException e) {
			logger.debug("Failed to delete old metadata, will do it next time", e);
		}
	}

	/**
	 * Find latest index.
	 *
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private int findLatestIndex() throws IOException {
		ImmutableMap<String, BlobMetaData> blobs = metaDataBlobContainer.listBlobsByPrefix("metadata-");

		int index = -1;
		for (BlobMetaData md : blobs.values()) {
			if (logger.isTraceEnabled()) {
				logger.trace("[findLatestMetadata]: Processing [" + md.name() + "]");
			}
			String name = md.name();
			int fileIndex = Integer.parseInt(name.substring(name.indexOf('-') + 1));
			if (fileIndex >= index) {

				byte[] data = null;
				try {
					data = metaDataBlobContainer.readBlobFully(name);
					readMetaData(data);
					index = fileIndex;
				} catch (IOException e) {
					logger.warn("[findLatestMetadata]: failed to read metadata from [" + name + "], data_length ["
							+ (data == null ? "na" : data.length) + "] ignoring...", e);
				}
			}
		}

		return index;
	}

	/**
	 * Read meta data.
	 *
	 * @param data the data
	 * @return the meta data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private MetaData readMetaData(byte[] data) throws IOException {
		XContentParser parser = null;
		try {
			if (LZF.isCompressed(data)) {
				BytesStreamInput siBytes = new BytesStreamInput(data, false);
				LZFStreamInput siLzf = CachedStreamInput.cachedLzf(siBytes);
				parser = XContentFactory.xContent(XContentType.JSON).createParser(siLzf);
			} else {
				parser = XContentFactory.xContent(XContentType.JSON).createParser(data);
			}
			return MetaData.Builder.fromXContent(parser);
		} finally {
			if (parser != null) {
				parser.close();
			}
		}
	}
}
