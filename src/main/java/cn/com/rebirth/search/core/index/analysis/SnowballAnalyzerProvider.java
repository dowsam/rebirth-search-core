/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SnowballAnalyzerProvider.java 2012-3-29 15:02:36 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import java.util.Set;

import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;


/**
 * The Class SnowballAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class SnowballAnalyzerProvider extends AbstractIndexAnalyzerProvider<SnowballAnalyzer> {

	
	/** The Constant defaultLanguageStopwords. */
	private static final ImmutableMap<String, Set<?>> defaultLanguageStopwords = MapBuilder
			.<String, Set<?>> newMapBuilder().put("English", StopAnalyzer.ENGLISH_STOP_WORDS_SET)
			.put("Dutch", DutchAnalyzer.getDefaultStopSet()).put("German", GermanAnalyzer.getDefaultStopSet())
			.put("German2", GermanAnalyzer.getDefaultStopSet()).put("French", FrenchAnalyzer.getDefaultStopSet())
			.immutableMap();

	
	/** The analyzer. */
	private final SnowballAnalyzer analyzer;

	
	/**
	 * Instantiates a new snowball analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public SnowballAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);

		String language = settings.get("language", settings.get("name", "English"));
		Set<?> defaultStopwords = defaultLanguageStopwords.containsKey(language) ? defaultLanguageStopwords
				.get(language) : ImmutableSet.<Set<?>> of();
		Set<?> stopWords = Analysis.parseStopWords(env, settings, defaultStopwords, version);

		analyzer = new SnowballAnalyzer(version, language, stopWords);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.AnalyzerProvider#get()
	 */
	@Override
	public SnowballAnalyzer get() {
		return this.analyzer;
	}
}