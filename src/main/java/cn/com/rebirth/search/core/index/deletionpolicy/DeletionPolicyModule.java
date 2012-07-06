/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DeletionPolicyModule.java 2012-3-29 15:01:30 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.deletionpolicy;

import org.apache.lucene.index.IndexDeletionPolicy;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.name.Names;


/**
 * The Class DeletionPolicyModule.
 *
 * @author l.xue.nong
 */
public class DeletionPolicyModule extends AbstractModule {

	
	/**
	 * The Class DeletionPolicySettings.
	 *
	 * @author l.xue.nong
	 */
	public static class DeletionPolicySettings {

		
		/** The Constant TYPE. */
		public static final String TYPE = "index.deletionpolicy.type";
	}

	
	/** The settings. */
	private final Settings settings;

	
	/**
	 * Instantiates a new deletion policy module.
	 *
	 * @param settings the settings
	 */
	public DeletionPolicyModule(Settings settings) {
		this.settings = settings;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(IndexDeletionPolicy.class).annotatedWith(Names.named("actual"))
				.to(settings.getAsClass(DeletionPolicySettings.TYPE, KeepOnlyLastDeletionPolicy.class))
				.asEagerSingleton();

		bind(SnapshotDeletionPolicy.class).asEagerSingleton();
	}
}
