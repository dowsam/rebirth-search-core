/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexStoreModule.java 2012-3-29 15:01:10 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.store;

import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Constants;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.inject.Modules;
import cn.com.rebirth.search.commons.inject.SpawnModules;
import cn.com.rebirth.search.core.index.store.fs.MmapFsIndexStoreModule;
import cn.com.rebirth.search.core.index.store.fs.NioFsIndexStoreModule;
import cn.com.rebirth.search.core.index.store.fs.SimpleFsIndexStoreModule;
import cn.com.rebirth.search.core.index.store.memory.MemoryIndexStoreModule;
import cn.com.rebirth.search.core.index.store.ram.RamIndexStoreModule;

import com.google.common.collect.ImmutableList;

/**
 * The Class IndexStoreModule.
 *
 * @author l.xue.nong
 */
public class IndexStoreModule extends AbstractModule implements SpawnModules {

	/** The settings. */
	private final Settings settings;

	/**
	 * Instantiates a new index store module.
	 *
	 * @param settings the settings
	 */
	public IndexStoreModule(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.SpawnModules#spawnModules()
	 */
	@Override
	public Iterable<? extends Module> spawnModules() {
		Class<? extends Module> indexStoreModule = NioFsIndexStoreModule.class;
		if ((Constants.WINDOWS || Constants.SUN_OS) && Constants.JRE_IS_64BIT && MMapDirectory.UNMAP_SUPPORTED) {
			indexStoreModule = MmapFsIndexStoreModule.class;
		} else if (Constants.WINDOWS) {
			indexStoreModule = SimpleFsIndexStoreModule.class;
		}
		String storeType = settings.get("index.store.type");
		if ("ram".equalsIgnoreCase(storeType)) {
			indexStoreModule = RamIndexStoreModule.class;
		} else if ("memory".equalsIgnoreCase(storeType)) {
			indexStoreModule = MemoryIndexStoreModule.class;
		} else if ("fs".equalsIgnoreCase(storeType)) {

		} else if ("simplefs".equalsIgnoreCase(storeType) || "simple_fs".equals(storeType)) {
			indexStoreModule = SimpleFsIndexStoreModule.class;
		} else if ("niofs".equalsIgnoreCase(storeType) || "nio_fs".equalsIgnoreCase(storeType)) {
			indexStoreModule = NioFsIndexStoreModule.class;
		} else if ("mmapfs".equalsIgnoreCase(storeType) || "mmap_fs".equalsIgnoreCase(storeType)) {
			indexStoreModule = MmapFsIndexStoreModule.class;
		} else if (storeType != null) {
			indexStoreModule = settings.getAsClass("index.store.type", indexStoreModule,
					"cn.com.summall.search.core.index.store.", "IndexStoreModule");
		}
		return ImmutableList.of(Modules.createModule(indexStoreModule, settings));
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
	}
}