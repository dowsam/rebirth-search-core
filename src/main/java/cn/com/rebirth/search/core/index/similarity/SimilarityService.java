/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SimilarityService.java 2012-7-6 14:30:11 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.similarity;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import org.apache.lucene.search.Similarity;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

import com.google.common.collect.ImmutableMap;

/**
 * The Class SimilarityService.
 *
 * @author l.xue.nong
 */
public class SimilarityService extends AbstractIndexComponent {

	/** The similarity providers. */
	private final ImmutableMap<String, SimilarityProvider> similarityProviders;

	/** The similarities. */
	private final ImmutableMap<String, Similarity> similarities;

	/** The current search similar. */
	private Similarity currentSearchSimilar;

	/**
	 * Instantiates a new similarity service.
	 *
	 * @param index the index
	 */
	public SimilarityService(Index index) {
		this(index, ImmutableSettings.Builder.EMPTY_SETTINGS, null);
	}

	/**
	 * Instantiates a new similarity service.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param providerFactories the provider factories
	 */
	@Inject
	public SimilarityService(Index index, @IndexSettings Settings indexSettings,
			@Nullable Map<String, SimilarityProviderFactory> providerFactories) {
		super(index, indexSettings);

		Map<String, SimilarityProvider> similarityProviders = newHashMap();
		if (providerFactories != null) {
			Map<String, Settings> providersSettings = indexSettings.getGroups("index.similarity");
			for (Map.Entry<String, SimilarityProviderFactory> entry : providerFactories.entrySet()) {
				String similarityName = entry.getKey();
				SimilarityProviderFactory similarityProviderFactory = entry.getValue();

				Settings similaritySettings = providersSettings.get(similarityName);
				if (similaritySettings == null) {
					similaritySettings = ImmutableSettings.Builder.EMPTY_SETTINGS;
				}

				SimilarityProvider similarityProvider = similarityProviderFactory.create(similarityName,
						similaritySettings);
				similarityProviders.put(similarityName, similarityProvider);
			}
		}

		if (!similarityProviders.containsKey("index")) {
			similarityProviders.put("index", new DefaultSimilarityProvider(index, indexSettings, "index",
					ImmutableSettings.Builder.EMPTY_SETTINGS));
		}
		if (!similarityProviders.containsKey("search")) {
			similarityProviders.put("search", new DefaultSimilarityProvider(index, indexSettings, "search",
					ImmutableSettings.Builder.EMPTY_SETTINGS));
		}
		this.similarityProviders = ImmutableMap.copyOf(similarityProviders);

		Map<String, Similarity> similarities = newHashMap();
		for (SimilarityProvider provider : similarityProviders.values()) {
			similarities.put(provider.name(), provider.get());
		}
		this.similarities = ImmutableMap.copyOf(similarities);
	}

	/**
	 * Similarity.
	 *
	 * @param name the name
	 * @return the similarity
	 */
	public Similarity similarity(String name) {
		return similarities.get(name);
	}

	/**
	 * Default index similarity.
	 *
	 * @return the similarity
	 */
	public Similarity defaultIndexSimilarity() {
		return similarities.get("index");
	}

	/**
	 * Default search similarity.
	 *
	 * @return the similarity
	 */
	public Similarity defaultSearchSimilarity() {
		Similarity temp = currentSearchSimilar;
		if (temp != null) {
			currentSearchSimilar = null;
			return temp;
		}
		return similarities.get("search");
	}

	/**
	 * Put current search similarity.
	 *
	 * @param similarity the similarity
	 */
	public void putCurrentSearchSimilarity(Similarity similarity) {
		this.currentSearchSimilar = similarity;
	}
}
