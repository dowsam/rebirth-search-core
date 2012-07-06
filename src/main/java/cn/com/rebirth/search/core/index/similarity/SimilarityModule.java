/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SimilarityModule.java 2012-3-29 15:02:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.similarity;

import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Scopes;
import cn.com.rebirth.search.commons.inject.assistedinject.FactoryProvider;
import cn.com.rebirth.search.commons.inject.multibindings.MapBinder;


/**
 * The Class SimilarityModule.
 *
 * @author l.xue.nong
 */
public class SimilarityModule extends AbstractModule {

	
	/** The settings. */
	private final Settings settings;

	
	/**
	 * Instantiates a new similarity module.
	 *
	 * @param settings the settings
	 */
	public SimilarityModule(Settings settings) {
		this.settings = settings;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		MapBinder<String, SimilarityProviderFactory> similarityBinder = MapBinder.newMapBinder(binder(), String.class,
				SimilarityProviderFactory.class);

		Map<String, Settings> similarityProvidersSettings = settings.getGroups("index.similarity");
		for (Map.Entry<String, Settings> entry : similarityProvidersSettings.entrySet()) {
			String name = entry.getKey();
			Settings settings = entry.getValue();

			Class<? extends SimilarityProvider> type = settings.getAsClass("type", null,
					"cn.com.summall.search.core.index.similarity.", "SimilarityProvider");
			if (type == null) {
				throw new IllegalArgumentException("Similarity [" + name + "] must have a type associated with it");
			}
			similarityBinder.addBinding(name)
					.toProvider(FactoryProvider.newFactory(SimilarityProviderFactory.class, type)).in(Scopes.SINGLETON);
		}

		bind(SimilarityService.class).in(Scopes.SINGLETON);
	}
}
