/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AnalysisModule.java 2012-7-6 14:29:37 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import java.util.LinkedList;
import java.util.Map;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.settings.NoClassSettingsException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Scopes;
import cn.com.rebirth.search.commons.inject.assistedinject.FactoryProvider;
import cn.com.rebirth.search.commons.inject.multibindings.MapBinder;
import cn.com.rebirth.search.core.index.analysis.compound.DictionaryCompoundWordTokenFilterFactory;
import cn.com.rebirth.search.core.index.analysis.compound.HyphenationCompoundWordTokenFilterFactory;
import cn.com.rebirth.search.core.indices.analysis.IndicesAnalysisService;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * The Class AnalysisModule.
 *
 * @author l.xue.nong
 */
public class AnalysisModule extends AbstractModule {

	/**
	 * The Class AnalysisBinderProcessor.
	 *
	 * @author l.xue.nong
	 */
	public static class AnalysisBinderProcessor {

		/**
		 * Process char filters.
		 *
		 * @param charFiltersBindings the char filters bindings
		 */
		public void processCharFilters(CharFiltersBindings charFiltersBindings) {

		}

		/**
		 * The Class CharFiltersBindings.
		 *
		 * @author l.xue.nong
		 */
		public static class CharFiltersBindings {

			/** The char filters. */
			private final Map<String, Class<? extends CharFilterFactory>> charFilters = Maps.newHashMap();

			/**
			 * Instantiates a new char filters bindings.
			 */
			public CharFiltersBindings() {
			}

			/**
			 * Process char filter.
			 *
			 * @param name the name
			 * @param charFilterFactory the char filter factory
			 */
			public void processCharFilter(String name, Class<? extends CharFilterFactory> charFilterFactory) {
				charFilters.put(name, charFilterFactory);
			}
		}

		/**
		 * Process token filters.
		 *
		 * @param tokenFiltersBindings the token filters bindings
		 */
		public void processTokenFilters(TokenFiltersBindings tokenFiltersBindings) {

		}

		/**
		 * The Class TokenFiltersBindings.
		 *
		 * @author l.xue.nong
		 */
		public static class TokenFiltersBindings {

			/** The token filters. */
			private final Map<String, Class<? extends TokenFilterFactory>> tokenFilters = Maps.newHashMap();

			/**
			 * Instantiates a new token filters bindings.
			 */
			public TokenFiltersBindings() {
			}

			/**
			 * Process token filter.
			 *
			 * @param name the name
			 * @param tokenFilterFactory the token filter factory
			 */
			public void processTokenFilter(String name, Class<? extends TokenFilterFactory> tokenFilterFactory) {
				tokenFilters.put(name, tokenFilterFactory);
			}
		}

		/**
		 * Process tokenizers.
		 *
		 * @param tokenizersBindings the tokenizers bindings
		 */
		public void processTokenizers(TokenizersBindings tokenizersBindings) {

		}

		/**
		 * The Class TokenizersBindings.
		 *
		 * @author l.xue.nong
		 */
		public static class TokenizersBindings {

			/** The tokenizers. */
			private final Map<String, Class<? extends TokenizerFactory>> tokenizers = Maps.newHashMap();

			/**
			 * Instantiates a new tokenizers bindings.
			 */
			public TokenizersBindings() {
			}

			/**
			 * Process tokenizer.
			 *
			 * @param name the name
			 * @param tokenizerFactory the tokenizer factory
			 */
			public void processTokenizer(String name, Class<? extends TokenizerFactory> tokenizerFactory) {
				tokenizers.put(name, tokenizerFactory);
			}
		}

		/**
		 * Process analyzers.
		 *
		 * @param analyzersBindings the analyzers bindings
		 */
		public void processAnalyzers(AnalyzersBindings analyzersBindings) {

		}

		/**
		 * The Class AnalyzersBindings.
		 *
		 * @author l.xue.nong
		 */
		public static class AnalyzersBindings {

			/** The analyzers. */
			private final Map<String, Class<? extends AnalyzerProvider>> analyzers = Maps.newHashMap();

			/**
			 * Instantiates a new analyzers bindings.
			 */
			public AnalyzersBindings() {
			}

			/**
			 * Process analyzer.
			 *
			 * @param name the name
			 * @param analyzerProvider the analyzer provider
			 */
			public void processAnalyzer(String name, Class<? extends AnalyzerProvider> analyzerProvider) {
				analyzers.put(name, analyzerProvider);
			}
		}
	}

	/** The settings. */
	private final Settings settings;

	/** The indices analysis service. */
	private final IndicesAnalysisService indicesAnalysisService;

	/** The processors. */
	private final LinkedList<AnalysisBinderProcessor> processors = Lists.newLinkedList();

	/**
	 * Instantiates a new analysis module.
	 *
	 * @param settings the settings
	 */
	public AnalysisModule(Settings settings) {
		this(settings, null);
	}

	/**
	 * Instantiates a new analysis module.
	 *
	 * @param settings the settings
	 * @param indicesAnalysisService the indices analysis service
	 */
	public AnalysisModule(Settings settings, IndicesAnalysisService indicesAnalysisService) {
		this.settings = settings;
		this.indicesAnalysisService = indicesAnalysisService;
		processors.add(new DefaultProcessor());
		try {
			processors.add(new ExtendedProcessor());
		} catch (Throwable t) {

		}
	}

	/**
	 * Adds the processor.
	 *
	 * @param processor the processor
	 * @return the analysis module
	 */
	public AnalysisModule addProcessor(AnalysisBinderProcessor processor) {
		processors.addFirst(processor);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		MapBinder<String, CharFilterFactoryFactory> charFilterBinder = MapBinder.newMapBinder(binder(), String.class,
				CharFilterFactoryFactory.class);

		AnalysisBinderProcessor.CharFiltersBindings charFiltersBindings = new AnalysisBinderProcessor.CharFiltersBindings();
		for (AnalysisBinderProcessor processor : processors) {
			processor.processCharFilters(charFiltersBindings);
		}

		Map<String, Settings> charFiltersSettings = settings.getGroups("index.analysis.char_filter");
		for (Map.Entry<String, Settings> entry : charFiltersSettings.entrySet()) {
			String charFilterName = entry.getKey();
			Settings charFilterSettings = entry.getValue();

			Class<? extends CharFilterFactory> type = null;
			try {
				type = charFilterSettings.getAsClass("type", null, "cn.com.rebirth.search.core.index.analysis.",
						"CharFilterFactory");
			} catch (NoClassSettingsException e) {

				if (charFilterSettings.get("type") != null) {
					type = charFiltersBindings.charFilters
							.get(Strings.toUnderscoreCase(charFilterSettings.get("type")));
					if (type == null) {
						type = charFiltersBindings.charFilters.get(Strings.toCamelCase(charFilterSettings.get("type")));
					}
				}
				if (type == null) {
					throw new RebirthIllegalArgumentException("failed to find char filter type ["
							+ charFilterSettings.get("type") + "] for [" + charFilterName + "]", e);
				}
			}
			if (type == null) {
				throw new RebirthIllegalArgumentException("Char Filter [" + charFilterName
						+ "] must have a type associated with it");
			}
			charFilterBinder.addBinding(charFilterName)
					.toProvider(FactoryProvider.newFactory(CharFilterFactoryFactory.class, type)).in(Scopes.SINGLETON);
		}

		for (Map.Entry<String, Class<? extends CharFilterFactory>> entry : charFiltersBindings.charFilters.entrySet()) {
			String charFilterName = entry.getKey();
			Class<? extends CharFilterFactory> clazz = entry.getValue();

			if (charFiltersSettings.containsKey(charFilterName)) {
				continue;
			}

			if (clazz.getAnnotation(AnalysisSettingsRequired.class) != null) {
				continue;
			}

			if (indicesAnalysisService != null && indicesAnalysisService.hasCharFilter(charFilterName)) {

			} else {
				charFilterBinder.addBinding(charFilterName)
						.toProvider(FactoryProvider.newFactory(CharFilterFactoryFactory.class, clazz))
						.in(Scopes.SINGLETON);
			}
		}

		MapBinder<String, TokenFilterFactoryFactory> tokenFilterBinder = MapBinder.newMapBinder(binder(), String.class,
				TokenFilterFactoryFactory.class);

		AnalysisBinderProcessor.TokenFiltersBindings tokenFiltersBindings = new AnalysisBinderProcessor.TokenFiltersBindings();
		for (AnalysisBinderProcessor processor : processors) {
			processor.processTokenFilters(tokenFiltersBindings);
		}

		Map<String, Settings> tokenFiltersSettings = settings.getGroups("index.analysis.filter");
		for (Map.Entry<String, Settings> entry : tokenFiltersSettings.entrySet()) {
			String tokenFilterName = entry.getKey();
			Settings tokenFilterSettings = entry.getValue();

			Class<? extends TokenFilterFactory> type = null;
			try {
				type = tokenFilterSettings.getAsClass("type", null, "cn.com.rebirth.search.core.index.analysis.",
						"TokenFilterFactory");
			} catch (NoClassSettingsException e) {

				if (tokenFilterSettings.get("type") != null) {
					type = tokenFiltersBindings.tokenFilters.get(Strings.toUnderscoreCase(tokenFilterSettings
							.get("type")));
					if (type == null) {
						type = tokenFiltersBindings.tokenFilters.get(Strings.toCamelCase(tokenFilterSettings
								.get("type")));
					}
				}
				if (type == null) {
					throw new RebirthIllegalArgumentException("failed to find token filter type ["
							+ tokenFilterSettings.get("type") + "] for [" + tokenFilterName + "]", e);
				}
			}
			if (type == null) {
				throw new RebirthIllegalArgumentException("token filter [" + tokenFilterName
						+ "] must have a type associated with it");
			}
			tokenFilterBinder.addBinding(tokenFilterName)
					.toProvider(FactoryProvider.newFactory(TokenFilterFactoryFactory.class, type)).in(Scopes.SINGLETON);
		}

		for (Map.Entry<String, Class<? extends TokenFilterFactory>> entry : tokenFiltersBindings.tokenFilters
				.entrySet()) {
			String tokenFilterName = entry.getKey();
			Class<? extends TokenFilterFactory> clazz = entry.getValue();

			if (tokenFiltersSettings.containsKey(tokenFilterName)) {
				continue;
			}

			if (clazz.getAnnotation(AnalysisSettingsRequired.class) != null) {
				continue;
			}

			if (indicesAnalysisService != null && indicesAnalysisService.hasTokenFilter(tokenFilterName)) {

			} else {
				tokenFilterBinder.addBinding(tokenFilterName)
						.toProvider(FactoryProvider.newFactory(TokenFilterFactoryFactory.class, clazz))
						.in(Scopes.SINGLETON);
			}
		}

		MapBinder<String, TokenizerFactoryFactory> tokenizerBinder = MapBinder.newMapBinder(binder(), String.class,
				TokenizerFactoryFactory.class);

		AnalysisBinderProcessor.TokenizersBindings tokenizersBindings = new AnalysisBinderProcessor.TokenizersBindings();
		for (AnalysisBinderProcessor processor : processors) {
			processor.processTokenizers(tokenizersBindings);
		}

		Map<String, Settings> tokenizersSettings = settings.getGroups("index.analysis.tokenizer");
		for (Map.Entry<String, Settings> entry : tokenizersSettings.entrySet()) {
			String tokenizerName = entry.getKey();
			Settings tokenizerSettings = entry.getValue();

			Class<? extends TokenizerFactory> type = null;
			try {
				type = tokenizerSettings.getAsClass("type", null, "cn.com.rebirth.search.core.index.analysis.",
						"TokenizerFactory");
			} catch (NoClassSettingsException e) {

				if (tokenizerSettings.get("type") != null) {
					type = tokenizersBindings.tokenizers.get(Strings.toUnderscoreCase(tokenizerSettings.get("type")));
					if (type == null) {
						type = tokenizersBindings.tokenizers.get(Strings.toCamelCase(tokenizerSettings.get("type")));
					}
				}
				if (type == null) {
					throw new RebirthIllegalArgumentException("failed to find tokenizer type ["
							+ tokenizerSettings.get("type") + "] for [" + tokenizerName + "]", e);
				}
			}
			if (type == null) {
				throw new RebirthIllegalArgumentException("token filter [" + tokenizerName
						+ "] must have a type associated with it");
			}
			tokenizerBinder.addBinding(tokenizerName)
					.toProvider(FactoryProvider.newFactory(TokenizerFactoryFactory.class, type)).in(Scopes.SINGLETON);
		}

		for (Map.Entry<String, Class<? extends TokenizerFactory>> entry : tokenizersBindings.tokenizers.entrySet()) {
			String tokenizerName = entry.getKey();
			Class<? extends TokenizerFactory> clazz = entry.getValue();
			if (tokenizersSettings.containsKey(tokenizerName)) {
				continue;
			}
			if (clazz.getAnnotation(AnalysisSettingsRequired.class) != null) {
				continue;
			}

			if (indicesAnalysisService != null && indicesAnalysisService.hasTokenizer(tokenizerName)) {

			} else {
				tokenizerBinder.addBinding(tokenizerName)
						.toProvider(FactoryProvider.newFactory(TokenizerFactoryFactory.class, clazz))
						.in(Scopes.SINGLETON);
			}
		}

		MapBinder<String, AnalyzerProviderFactory> analyzerBinder = MapBinder.newMapBinder(binder(), String.class,
				AnalyzerProviderFactory.class);

		AnalysisBinderProcessor.AnalyzersBindings analyzersBindings = new AnalysisBinderProcessor.AnalyzersBindings();
		for (AnalysisBinderProcessor processor : processors) {
			processor.processAnalyzers(analyzersBindings);
		}

		Map<String, Settings> analyzersSettings = settings.getGroups("index.analysis.analyzer");
		for (Map.Entry<String, Settings> entry : analyzersSettings.entrySet()) {
			String analyzerName = entry.getKey();
			Settings analyzerSettings = entry.getValue();
			Class<? extends AnalyzerProvider> type = null;
			try {
				type = analyzerSettings.getAsClass("type", null, "cn.com.rebirth.search.core.index.analysis.",
						"AnalyzerProvider");
			} catch (NoClassSettingsException e) {

				if (analyzerSettings.get("type") != null) {
					type = analyzersBindings.analyzers.get(Strings.toUnderscoreCase(analyzerSettings.get("type")));
					if (type == null) {
						type = analyzersBindings.analyzers.get(Strings.toCamelCase(analyzerSettings.get("type")));
					}
				}
				if (type == null) {

					String tokenizerName = analyzerSettings.get("tokenizer");
					if (tokenizerName != null) {

						type = CustomAnalyzerProvider.class;
					} else {
						throw new RebirthIllegalArgumentException("failed to find analyzer type ["
								+ analyzerSettings.get("type") + "] or tokenizer for [" + analyzerName + "]", e);
					}
				}
			}
			if (type == null) {

				String tokenizerName = analyzerSettings.get("tokenizer");
				if (tokenizerName != null) {

					type = CustomAnalyzerProvider.class;
				} else {
					throw new RebirthIllegalArgumentException("failed to find analyzer type ["
							+ analyzerSettings.get("type") + "] or tokenizer for [" + analyzerName + "]");
				}
			}
			analyzerBinder.addBinding(analyzerName)
					.toProvider(FactoryProvider.newFactory(AnalyzerProviderFactory.class, type)).in(Scopes.SINGLETON);
		}

		for (Map.Entry<String, Class<? extends AnalyzerProvider>> entry : analyzersBindings.analyzers.entrySet()) {
			String analyzerName = entry.getKey();
			Class<? extends AnalyzerProvider> clazz = entry.getValue();

			if (analyzersSettings.containsKey(analyzerName)) {
				continue;
			}

			if (clazz.getAnnotation(AnalysisSettingsRequired.class) != null) {
				continue;
			}

			if (indicesAnalysisService != null && indicesAnalysisService.hasAnalyzer(analyzerName)) {

			} else {
				analyzerBinder.addBinding(analyzerName)
						.toProvider(FactoryProvider.newFactory(AnalyzerProviderFactory.class, clazz))
						.in(Scopes.SINGLETON);
			}
		}

		bind(AnalysisService.class).in(Scopes.SINGLETON);
	}

	/**
	 * The Class DefaultProcessor.
	 *
	 * @author l.xue.nong
	 */
	private static class DefaultProcessor extends AnalysisBinderProcessor {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.analysis.AnalysisModule.AnalysisBinderProcessor#processCharFilters(cn.com.rebirth.search.core.index.analysis.AnalysisModule.AnalysisBinderProcessor.CharFiltersBindings)
		 */
		@Override
		public void processCharFilters(CharFiltersBindings charFiltersBindings) {
			charFiltersBindings.processCharFilter("html_strip", HtmlStripCharFilterFactory.class);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.analysis.AnalysisModule.AnalysisBinderProcessor#processTokenFilters(cn.com.rebirth.search.core.index.analysis.AnalysisModule.AnalysisBinderProcessor.TokenFiltersBindings)
		 */
		@Override
		public void processTokenFilters(TokenFiltersBindings tokenFiltersBindings) {
			tokenFiltersBindings.processTokenFilter("stop", StopTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("reverse", ReverseTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("asciifolding", ASCIIFoldingTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("length", LengthTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("lowercase", LowerCaseTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("porter_stem", PorterStemTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("kstem", KStemTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("standard", StandardTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("nGram", NGramTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("ngram", NGramTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("edgeNGram", EdgeNGramTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("edge_ngram", EdgeNGramTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("shingle", ShingleTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("unique", UniqueTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("truncate", TruncateTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("trim", TrimTokenFilterFactory.class);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.analysis.AnalysisModule.AnalysisBinderProcessor#processTokenizers(cn.com.rebirth.search.core.index.analysis.AnalysisModule.AnalysisBinderProcessor.TokenizersBindings)
		 */
		@Override
		public void processTokenizers(TokenizersBindings tokenizersBindings) {
			tokenizersBindings.processTokenizer("standard", StandardTokenizerFactory.class);
			tokenizersBindings.processTokenizer("uax_url_email", UAX29URLEmailTokenizerFactory.class);
			tokenizersBindings.processTokenizer("path_hierarchy", PathHierarchyTokenizerFactory.class);
			tokenizersBindings.processTokenizer("keyword", KeywordTokenizerFactory.class);
			tokenizersBindings.processTokenizer("letter", LetterTokenizerFactory.class);
			tokenizersBindings.processTokenizer("lowercase", LowerCaseTokenizerFactory.class);
			tokenizersBindings.processTokenizer("whitespace", WhitespaceTokenizerFactory.class);

			tokenizersBindings.processTokenizer("nGram", NGramTokenizerFactory.class);
			tokenizersBindings.processTokenizer("ngram", NGramTokenizerFactory.class);
			tokenizersBindings.processTokenizer("edgeNGram", EdgeNGramTokenizerFactory.class);
			tokenizersBindings.processTokenizer("edge_ngram", EdgeNGramTokenizerFactory.class);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.analysis.AnalysisModule.AnalysisBinderProcessor#processAnalyzers(cn.com.rebirth.search.core.index.analysis.AnalysisModule.AnalysisBinderProcessor.AnalyzersBindings)
		 */
		@Override
		public void processAnalyzers(AnalyzersBindings analyzersBindings) {
			analyzersBindings.processAnalyzer("default", StandardAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("standard", StandardAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("standard_html_strip", StandardHtmlStripAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("simple", SimpleAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("stop", StopAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("whitespace", WhitespaceAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("keyword", KeywordAnalyzerProvider.class);
		}
	}

	/**
	 * The Class ExtendedProcessor.
	 *
	 * @author l.xue.nong
	 */
	private static class ExtendedProcessor extends AnalysisBinderProcessor {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.analysis.AnalysisModule.AnalysisBinderProcessor#processCharFilters(cn.com.rebirth.search.core.index.analysis.AnalysisModule.AnalysisBinderProcessor.CharFiltersBindings)
		 */
		@Override
		public void processCharFilters(CharFiltersBindings charFiltersBindings) {
			charFiltersBindings.processCharFilter("mapping", MappingCharFilterFactory.class);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.analysis.AnalysisModule.AnalysisBinderProcessor#processTokenFilters(cn.com.rebirth.search.core.index.analysis.AnalysisModule.AnalysisBinderProcessor.TokenFiltersBindings)
		 */
		@Override
		public void processTokenFilters(TokenFiltersBindings tokenFiltersBindings) {
			tokenFiltersBindings.processTokenFilter("snowball", SnowballTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("stemmer", StemmerTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("word_delimiter", WordDelimiterTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("synonym", SynonymTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("elision", ElisionTokenFilterFactory.class);

			tokenFiltersBindings.processTokenFilter("pattern_replace", PatternReplaceTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("dictionary_decompounder",
					DictionaryCompoundWordTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("hyphenation_decompounder",
					HyphenationCompoundWordTokenFilterFactory.class);

			tokenFiltersBindings.processTokenFilter("arabic_stem", ArabicStemTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("brazilian_stem", BrazilianStemTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("czech_stem", CzechStemTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("dutch_stem", DutchStemTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("french_stem", FrenchStemTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("german_stem", GermanStemTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("russian_stem", RussianStemTokenFilterFactory.class);

			tokenFiltersBindings.processTokenFilter("keyword_marker", KeywordMarkerTokenFilterFactory.class);
			tokenFiltersBindings.processTokenFilter("stemmer_override", StemmerOverrideTokenFilterFactory.class);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.analysis.AnalysisModule.AnalysisBinderProcessor#processTokenizers(cn.com.rebirth.search.core.index.analysis.AnalysisModule.AnalysisBinderProcessor.TokenizersBindings)
		 */
		@Override
		public void processTokenizers(TokenizersBindings tokenizersBindings) {
			tokenizersBindings.processTokenizer("pattern", PatternTokenizerFactory.class);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.analysis.AnalysisModule.AnalysisBinderProcessor#processAnalyzers(cn.com.rebirth.search.core.index.analysis.AnalysisModule.AnalysisBinderProcessor.AnalyzersBindings)
		 */
		@Override
		public void processAnalyzers(AnalyzersBindings analyzersBindings) {
			analyzersBindings.processAnalyzer("pattern", PatternAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("snowball", SnowballAnalyzerProvider.class);

			analyzersBindings.processAnalyzer("arabic", ArabicAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("armenian", ArmenianAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("basque", BasqueAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("brazilian", BrazilianAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("bulgarian", BulgarianAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("catalan", CatalanAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("chinese", ChineseAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("cjk", CjkAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("czech", CzechAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("danish", DanishAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("dutch", DutchAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("english", EnglishAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("finnish", FinnishAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("french", FrenchAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("galician", GalicianAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("german", GermanAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("greek", GreekAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("hindi", HindiAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("hungarian", HungarianAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("indonesian", IndonesianAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("italian", ItalianAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("norwegian", NorwegianAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("persian", PersianAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("portuguese", PortugueseAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("romanian", RomanianAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("russian", RussianAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("spanish", SpanishAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("swedish", SwedishAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("turkish", TurkishAnalyzerProvider.class);
			analyzersBindings.processAnalyzer("thai", ThaiAnalyzerProvider.class);
		}
	}
}
