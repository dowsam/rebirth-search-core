/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StopTokenFilterFactory.java 2012-7-6 14:30:12 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import java.util.Set;

import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * A factory for creating StopTokenFilter objects.
 */
public class StopTokenFilterFactory extends AbstractTokenFilterFactory {

	/** The stop words. */
	private final Set<?> stopWords;

	/** The ignore case. */
	private final boolean ignoreCase;

	/** The enable position increments. */
	private final boolean enablePositionIncrements;

	/**
	 * Instantiates a new stop token filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public StopTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		this.stopWords = Analysis.parseStopWords(env, settings, StopAnalyzer.ENGLISH_STOP_WORDS_SET, version);
		this.ignoreCase = settings.getAsBoolean("ignore_case", false);
		this.enablePositionIncrements = settings.getAsBoolean("enable_position_increments",
				version.onOrAfter(Version.LUCENE_29));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
	 */
	@Override
	public TokenStream create(TokenStream tokenStream) {
		StopFilter filter = new StopFilter(version, tokenStream, stopWords, ignoreCase);
		filter.setEnablePositionIncrements(enablePositionIncrements);
		return filter;
	}

	/**
	 * Stop words.
	 *
	 * @return the sets the
	 */
	public Set<?> stopWords() {
		return stopWords;
	}

	/**
	 * Ignore case.
	 *
	 * @return true, if successful
	 */
	public boolean ignoreCase() {
		return ignoreCase;
	}

	/**
	 * Enable position increments.
	 *
	 * @return true, if successful
	 */
	public boolean enablePositionIncrements() {
		return this.enablePositionIncrements;
	}
}
