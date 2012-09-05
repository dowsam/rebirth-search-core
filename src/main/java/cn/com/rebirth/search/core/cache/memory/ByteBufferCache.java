/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ByteBufferCache.java 2012-7-6 14:30:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cache.memory;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.lucene.store.bytebuffer.ByteBufferAllocator;
import org.apache.lucene.store.bytebuffer.CachingByteBufferAllocator;
import org.apache.lucene.store.bytebuffer.PlainByteBufferAllocator;

import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.settings.ImmutableSettings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.ByteSizeUnit;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.core.inject.Inject;

/**
 * The Class ByteBufferCache.
 *
 * @author l.xue.nong
 */
public class ByteBufferCache extends AbstractComponent implements ByteBufferAllocator {

	/** The direct. */
	private final boolean direct;

	/** The small buffer size. */
	private final ByteSizeValue smallBufferSize;

	/** The large buffer size. */
	private final ByteSizeValue largeBufferSize;

	/** The small cache size. */
	private final ByteSizeValue smallCacheSize;

	/** The large cache size. */
	private final ByteSizeValue largeCacheSize;

	/** The allocator. */
	private final ByteBufferAllocator allocator;

	/**
	 * Instantiates a new byte buffer cache.
	 */
	public ByteBufferCache() {
		this(ImmutableSettings.Builder.EMPTY_SETTINGS);
	}

	/**
	 * Instantiates a new byte buffer cache.
	 *
	 * @param bufferSizeInBytes the buffer size in bytes
	 * @param cacheSizeInBytes the cache size in bytes
	 * @param direct the direct
	 */
	public ByteBufferCache(int bufferSizeInBytes, int cacheSizeInBytes, boolean direct) {
		this(ImmutableSettings.settingsBuilder().put("cache.memory.small_buffer_size", bufferSizeInBytes)
				.put("cache.memory.small_cache_size", cacheSizeInBytes)
				.put("cache.memory.large_buffer_size", bufferSizeInBytes)
				.put("cache.memory.large_cache_size", cacheSizeInBytes).put("cache.memory.direct", direct).build());
	}

	/**
	 * Instantiates a new byte buffer cache.
	 *
	 * @param settings the settings
	 */
	@Inject
	public ByteBufferCache(Settings settings) {
		super(settings);

		this.direct = componentSettings.getAsBoolean("direct", true);
		this.smallBufferSize = componentSettings.getAsBytesSize("small_buffer_size", new ByteSizeValue(1,
				ByteSizeUnit.KB));
		this.largeBufferSize = componentSettings.getAsBytesSize("large_buffer_size", new ByteSizeValue(1,
				ByteSizeUnit.MB));
		this.smallCacheSize = componentSettings.getAsBytesSize("small_cache_size", new ByteSizeValue(10,
				ByteSizeUnit.MB));
		this.largeCacheSize = componentSettings.getAsBytesSize("large_cache_size", new ByteSizeValue(500,
				ByteSizeUnit.MB));

		if (smallCacheSize.bytes() == 0 || largeCacheSize.bytes() == 0) {
			this.allocator = new PlainByteBufferAllocator(direct, (int) smallBufferSize.bytes(),
					(int) largeBufferSize.bytes());
		} else {
			this.allocator = new CachingByteBufferAllocator(direct, (int) smallBufferSize.bytes(),
					(int) largeBufferSize.bytes(), (int) smallCacheSize.bytes(), (int) largeCacheSize.bytes());
		}

		if (logger.isDebugEnabled()) {
			logger.debug("using bytebuffer cache with small_buffer_size [" + smallBufferSize + "], large_buffer_size ["
					+ largeBufferSize + "], small_cache_size [" + smallCacheSize
					+ "], large_cache_size [{}], direct [{}]", largeCacheSize, direct);
		}
	}

	/**
	 * Direct.
	 *
	 * @return true, if successful
	 */
	public boolean direct() {
		return this.direct;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.bytebuffer.ByteBufferAllocator#close()
	 */
	public void close() {
		allocator.close();
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.bytebuffer.ByteBufferAllocator#sizeInBytes(org.apache.lucene.store.bytebuffer.ByteBufferAllocator.Type)
	 */
	@Override
	public int sizeInBytes(Type type) {
		return allocator.sizeInBytes(type);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.bytebuffer.ByteBufferAllocator#allocate(org.apache.lucene.store.bytebuffer.ByteBufferAllocator.Type)
	 */
	@Override
	public ByteBuffer allocate(Type type) throws IOException {
		return allocator.allocate(type);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.store.bytebuffer.ByteBufferAllocator#release(java.nio.ByteBuffer)
	 */
	@Override
	public void release(ByteBuffer buffer) {
		allocator.release(buffer);
	}
}
