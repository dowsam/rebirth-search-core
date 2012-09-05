/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GreekAnalyzerProvider.java 2012-7-6 14:30:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.el.GreekAnalyzer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class GreekAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class GreekAnalyzerProvider extends AbstractIndexAnalyzerProvider<GreekAnalyzer> {

	/** The analyzer. */
	private final GreekAnalyzer analyzer;

	/**
	 * Instantiates a new greek analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public GreekAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		analyzer = new GreekAnalyzer(version, Analysis.parseStopWords(env, settings, GreekAnalyzer.getDefaultStopSet(),
				version));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.AnalyzerProvider#get()
	 */
	@Override
	public GreekAnalyzer get() {
		return this.analyzer;
	}
}