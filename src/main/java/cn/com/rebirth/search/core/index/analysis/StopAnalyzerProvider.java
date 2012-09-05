/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StopAnalyzerProvider.java 2012-7-6 14:29:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import java.util.Set;

import org.apache.lucene.analysis.StopAnalyzer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class StopAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class StopAnalyzerProvider extends AbstractIndexAnalyzerProvider<StopAnalyzer> {

	/** The stop analyzer. */
	private final StopAnalyzer stopAnalyzer;

	/**
	 * Instantiates a new stop analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public StopAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		Set<?> stopWords = Analysis.parseStopWords(env, settings, StopAnalyzer.ENGLISH_STOP_WORDS_SET, version);
		this.stopAnalyzer = new StopAnalyzer(version, stopWords);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.AnalyzerProvider#get()
	 */
	@Override
	public StopAnalyzer get() {
		return this.stopAnalyzer;
	}
}