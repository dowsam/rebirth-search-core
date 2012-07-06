/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MetaDataService.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.metadata;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.cluster.routing.operation.hash.djb.DjbHashFunction;

/**
 * The Class MetaDataService.
 *
 * @author l.xue.nong
 */
public class MetaDataService extends AbstractComponent {

	/** The index md locks. */
	private final MdLock[] indexMdLocks;

	/**
	 * Instantiates a new meta data service.
	 *
	 * @param settings the settings
	 */
	@Inject
	public MetaDataService(Settings settings) {
		super(settings);
		indexMdLocks = new MdLock[500];
		for (int i = 0; i < indexMdLocks.length; i++) {
			indexMdLocks[i] = new MdLock();
		}
	}

	/**
	 * Index meta data lock.
	 *
	 * @param index the index
	 * @return the md lock
	 */
	public MdLock indexMetaDataLock(String index) {
		return indexMdLocks[Math.abs(DjbHashFunction.DJB_HASH(index) % indexMdLocks.length)];
	}

	/**
	 * The Class MdLock.
	 *
	 * @author l.xue.nong
	 */
	public class MdLock {

		/** The is locked. */
		private boolean isLocked = false;

		/**
		 * Lock.
		 *
		 * @throws InterruptedException the interrupted exception
		 */
		public synchronized void lock() throws InterruptedException {
			while (isLocked) {
				wait();
			}
			isLocked = true;
		}

		/**
		 * Unlock.
		 */
		public synchronized void unlock() {
			isLocked = false;
			notifyAll();
		}
	}
}
