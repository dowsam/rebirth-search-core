/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StandardAnalyzerProvider.java 2012-7-6 14:29:49 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import java.util.Set;

import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class StandardAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class StandardAnalyzerProvider extends AbstractIndexAnalyzerProvider<StandardAnalyzer> {

	/** The standard analyzer. */
	private final StandardAnalyzer standardAnalyzer;

	/**
	 * Instantiates a new standard analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public StandardAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		Set<?> stopWords = Analysis.parseStopWords(env, settings, StopAnalyzer.ENGLISH_STOP_WORDS_SET, version);
		int maxTokenLength = settings.getAsInt("max_token_length", StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
		standardAnalyzer = new StandardAnalyzer(version, stopWords);
		standardAnalyzer.setMaxTokenLength(maxTokenLength);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.AnalyzerProvider#get()
	 */
	@Override
	public StandardAnalyzer get() {
		return this.standardAnalyzer;
	}
}
