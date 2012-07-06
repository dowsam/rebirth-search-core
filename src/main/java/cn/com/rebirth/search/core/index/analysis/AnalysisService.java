/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AnalysisService.java 2012-3-29 15:02:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.CloseableComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.indices.analysis.IndicesAnalysisService;
import cn.com.rebirth.search.index.analysis.AnalyzerScope;
import cn.com.rebirth.search.index.analysis.NamedAnalyzer;

import com.google.common.collect.ImmutableMap;

/**
 * The Class AnalysisService.
 *
 * @author l.xue.nong
 */
public class AnalysisService extends AbstractIndexComponent implements CloseableComponent {

	/** The analyzers. */
	private final ImmutableMap<String, NamedAnalyzer> analyzers;

	/** The tokenizers. */
	private final ImmutableMap<String, TokenizerFactory> tokenizers;

	/** The char filters. */
	private final ImmutableMap<String, CharFilterFactory> charFilters;

	/** The token filters. */
	private final ImmutableMap<String, TokenFilterFactory> tokenFilters;

	/** The default analyzer. */
	private final NamedAnalyzer defaultAnalyzer;

	/** The default index analyzer. */
	private final NamedAnalyzer defaultIndexAnalyzer;

	/** The default search analyzer. */
	private final NamedAnalyzer defaultSearchAnalyzer;

	/**
	 * Instantiates a new analysis service.
	 *
	 * @param index the index
	 */
	public AnalysisService(Index index) {
		this(index, ImmutableSettings.Builder.EMPTY_SETTINGS, null, null, null, null, null);
	}

	/**
	 * Instantiates a new analysis service.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param indicesAnalysisService the indices analysis service
	 * @param analyzerFactoryFactories the analyzer factory factories
	 * @param tokenizerFactoryFactories the tokenizer factory factories
	 * @param charFilterFactoryFactories the char filter factory factories
	 * @param tokenFilterFactoryFactories the token filter factory factories
	 */
	@Inject
	public AnalysisService(Index index, @IndexSettings Settings indexSettings,
			@Nullable IndicesAnalysisService indicesAnalysisService,
			@Nullable Map<String, AnalyzerProviderFactory> analyzerFactoryFactories,
			@Nullable Map<String, TokenizerFactoryFactory> tokenizerFactoryFactories,
			@Nullable Map<String, CharFilterFactoryFactory> charFilterFactoryFactories,
			@Nullable Map<String, TokenFilterFactoryFactory> tokenFilterFactoryFactories) {
		super(index, indexSettings);

		Map<String, TokenizerFactory> tokenizers = newHashMap();
		if (tokenizerFactoryFactories != null) {
			Map<String, Settings> tokenizersSettings = indexSettings.getGroups("index.analysis.tokenizer");
			for (Map.Entry<String, TokenizerFactoryFactory> entry : tokenizerFactoryFactories.entrySet()) {
				String tokenizerName = entry.getKey();
				TokenizerFactoryFactory tokenizerFactoryFactory = entry.getValue();

				Settings tokenizerSettings = tokenizersSettings.get(tokenizerName);
				if (tokenizerSettings == null) {
					tokenizerSettings = ImmutableSettings.Builder.EMPTY_SETTINGS;
				}

				TokenizerFactory tokenizerFactory = tokenizerFactoryFactory.create(tokenizerName, tokenizerSettings);
				tokenizers.put(tokenizerName, tokenizerFactory);
				tokenizers.put(Strings.toCamelCase(tokenizerName), tokenizerFactory);
			}
		}

		if (indicesAnalysisService != null) {
			for (Map.Entry<String, PreBuiltTokenizerFactoryFactory> entry : indicesAnalysisService.tokenizerFactories()
					.entrySet()) {
				String name = entry.getKey();
				if (!tokenizers.containsKey(name)) {
					tokenizers.put(name, entry.getValue().create(name, ImmutableSettings.Builder.EMPTY_SETTINGS));
				}
				name = Strings.toCamelCase(entry.getKey());
				if (!name.equals(entry.getKey())) {
					if (!tokenizers.containsKey(name)) {
						tokenizers.put(name, entry.getValue().create(name, ImmutableSettings.Builder.EMPTY_SETTINGS));
					}
				}
			}
		}

		this.tokenizers = ImmutableMap.copyOf(tokenizers);

		Map<String, CharFilterFactory> charFilters = newHashMap();
		if (charFilterFactoryFactories != null) {
			Map<String, Settings> charFiltersSettings = indexSettings.getGroups("index.analysis.char_filter");
			for (Map.Entry<String, CharFilterFactoryFactory> entry : charFilterFactoryFactories.entrySet()) {
				String charFilterName = entry.getKey();
				CharFilterFactoryFactory charFilterFactoryFactory = entry.getValue();

				Settings charFilterSettings = charFiltersSettings.get(charFilterName);
				if (charFilterSettings == null) {
					charFilterSettings = ImmutableSettings.Builder.EMPTY_SETTINGS;
				}

				CharFilterFactory tokenFilterFactory = charFilterFactoryFactory.create(charFilterName,
						charFilterSettings);
				charFilters.put(charFilterName, tokenFilterFactory);
				charFilters.put(Strings.toCamelCase(charFilterName), tokenFilterFactory);
			}
		}

		if (indicesAnalysisService != null) {
			for (Map.Entry<String, PreBuiltCharFilterFactoryFactory> entry : indicesAnalysisService
					.charFilterFactories().entrySet()) {
				String name = entry.getKey();
				if (!charFilters.containsKey(name)) {
					charFilters.put(name, entry.getValue().create(name, ImmutableSettings.Builder.EMPTY_SETTINGS));
				}
				name = Strings.toCamelCase(entry.getKey());
				if (!name.equals(entry.getKey())) {
					if (!charFilters.containsKey(name)) {
						charFilters.put(name, entry.getValue().create(name, ImmutableSettings.Builder.EMPTY_SETTINGS));
					}
				}
			}
		}

		this.charFilters = ImmutableMap.copyOf(charFilters);

		Map<String, TokenFilterFactory> tokenFilters = newHashMap();
		if (tokenFilterFactoryFactories != null) {
			Map<String, Settings> tokenFiltersSettings = indexSettings.getGroups("index.analysis.filter");
			for (Map.Entry<String, TokenFilterFactoryFactory> entry : tokenFilterFactoryFactories.entrySet()) {
				String tokenFilterName = entry.getKey();
				TokenFilterFactoryFactory tokenFilterFactoryFactory = entry.getValue();

				Settings tokenFilterSettings = tokenFiltersSettings.get(tokenFilterName);
				if (tokenFilterSettings == null) {
					tokenFilterSettings = ImmutableSettings.Builder.EMPTY_SETTINGS;
				}

				TokenFilterFactory tokenFilterFactory = tokenFilterFactoryFactory.create(tokenFilterName,
						tokenFilterSettings);
				tokenFilters.put(tokenFilterName, tokenFilterFactory);
				tokenFilters.put(Strings.toCamelCase(tokenFilterName), tokenFilterFactory);
			}
		}

		if (indicesAnalysisService != null) {
			for (Map.Entry<String, PreBuiltTokenFilterFactoryFactory> entry : indicesAnalysisService
					.tokenFilterFactories().entrySet()) {
				String name = entry.getKey();
				if (!tokenFilters.containsKey(name)) {
					tokenFilters.put(name, entry.getValue().create(name, ImmutableSettings.Builder.EMPTY_SETTINGS));
				}
				name = Strings.toCamelCase(entry.getKey());
				if (!name.equals(entry.getKey())) {
					if (!tokenFilters.containsKey(name)) {
						tokenFilters.put(name, entry.getValue().create(name, ImmutableSettings.Builder.EMPTY_SETTINGS));
					}
				}
			}
		}
		this.tokenFilters = ImmutableMap.copyOf(tokenFilters);

		Map<String, AnalyzerProvider> analyzerProviders = newHashMap();
		if (analyzerFactoryFactories != null) {
			Map<String, Settings> analyzersSettings = indexSettings.getGroups("index.analysis.analyzer");
			for (Map.Entry<String, AnalyzerProviderFactory> entry : analyzerFactoryFactories.entrySet()) {
				String analyzerName = entry.getKey();
				AnalyzerProviderFactory analyzerFactoryFactory = entry.getValue();

				Settings analyzerSettings = analyzersSettings.get(analyzerName);
				if (analyzerSettings == null) {
					analyzerSettings = ImmutableSettings.Builder.EMPTY_SETTINGS;
				}

				AnalyzerProvider analyzerFactory = analyzerFactoryFactory.create(analyzerName, analyzerSettings);
				analyzerProviders.put(analyzerName, analyzerFactory);
			}
		}
		if (indicesAnalysisService != null) {
			for (Map.Entry<String, PreBuiltAnalyzerProviderFactory> entry : indicesAnalysisService
					.analyzerProviderFactories().entrySet()) {
				String name = entry.getKey();
				if (!analyzerProviders.containsKey(name)) {
					analyzerProviders
							.put(name, entry.getValue().create(name, ImmutableSettings.Builder.EMPTY_SETTINGS));
				}
				name = Strings.toCamelCase(entry.getKey());
				if (!name.equals(entry.getKey())) {
					if (!analyzerProviders.containsKey(name)) {
						analyzerProviders.put(name,
								entry.getValue().create(name, ImmutableSettings.Builder.EMPTY_SETTINGS));
					}
				}
			}
		}

		if (!analyzerProviders.containsKey("default")) {
			analyzerProviders.put("default", new StandardAnalyzerProvider(index, indexSettings, null, "default",
					ImmutableSettings.Builder.EMPTY_SETTINGS));
		}
		if (!analyzerProviders.containsKey("default_index")) {
			analyzerProviders.put("default_index", analyzerProviders.get("default"));
		}
		if (!analyzerProviders.containsKey("default_search")) {
			analyzerProviders.put("default_search", analyzerProviders.get("default"));
		}

		Map<String, NamedAnalyzer> analyzers = newHashMap();
		for (AnalyzerProvider analyzerFactory : analyzerProviders.values()) {
			if (analyzerFactory instanceof CustomAnalyzerProvider) {
				((CustomAnalyzerProvider) analyzerFactory).build(this);
			}
			NamedAnalyzer analyzer = new NamedAnalyzer(analyzerFactory.name(), analyzerFactory.scope(),
					analyzerFactory.get());
			analyzers.put(analyzerFactory.name(), analyzer);
			analyzers.put(Strings.toCamelCase(analyzerFactory.name()), analyzer);
			String strAliases = indexSettings.get("index.analysis.analyzer." + analyzerFactory.name() + ".alias");
			if (strAliases != null) {
				for (String alias : Strings.commaDelimitedListToStringArray(strAliases)) {
					analyzers.put(alias, analyzer);
				}
			}
			String[] aliases = indexSettings.getAsArray("index.analysis.analyzer." + analyzerFactory.name() + ".alias");
			for (String alias : aliases) {
				analyzers.put(alias, analyzer);
			}
		}

		defaultAnalyzer = analyzers.get("default");
		defaultIndexAnalyzer = analyzers.containsKey("default_index") ? analyzers.get("default_index") : analyzers
				.get("default");
		defaultSearchAnalyzer = analyzers.containsKey("default_search") ? analyzers.get("default_search") : analyzers
				.get("default");

		this.analyzers = ImmutableMap.copyOf(analyzers);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.component.CloseableComponent#close()
	 */
	public void close() {
		for (NamedAnalyzer analyzer : analyzers.values()) {
			if (analyzer.scope() == AnalyzerScope.INDEX) {
				try {
					analyzer.close();
				} catch (NullPointerException e) {

				} catch (Exception e) {
					logger.debug("failed to close analyzer " + analyzer);
				}
			}
		}
	}

	/**
	 * Analyzer.
	 *
	 * @param name the name
	 * @return the named analyzer
	 */
	public NamedAnalyzer analyzer(String name) {
		return analyzers.get(name);
	}

	/**
	 * Default analyzer.
	 *
	 * @return the named analyzer
	 */
	public NamedAnalyzer defaultAnalyzer() {
		return defaultAnalyzer;
	}

	/**
	 * Default index analyzer.
	 *
	 * @return the named analyzer
	 */
	public NamedAnalyzer defaultIndexAnalyzer() {
		return defaultIndexAnalyzer;
	}

	/**
	 * Default search analyzer.
	 *
	 * @return the named analyzer
	 */
	public NamedAnalyzer defaultSearchAnalyzer() {
		return defaultSearchAnalyzer;
	}

	/**
	 * Tokenizer.
	 *
	 * @param name the name
	 * @return the tokenizer factory
	 */
	public TokenizerFactory tokenizer(String name) {
		return tokenizers.get(name);
	}

	/**
	 * Char filter.
	 *
	 * @param name the name
	 * @return the char filter factory
	 */
	public CharFilterFactory charFilter(String name) {
		return charFilters.get(name);
	}

	/**
	 * Token filter.
	 *
	 * @param name the name
	 * @return the token filter factory
	 */
	public TokenFilterFactory tokenFilter(String name) {
		return tokenFilters.get(name);
	}
}
