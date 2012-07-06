/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CzechAnalyzerProvider.java 2012-7-6 14:29:27 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.cz.CzechAnalyzer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class CzechAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class CzechAnalyzerProvider extends AbstractIndexAnalyzerProvider<CzechAnalyzer> {

	/** The analyzer. */
	private final CzechAnalyzer analyzer;

	/**
	 * Instantiates a new czech analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public CzechAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		analyzer = new CzechAnalyzer(version, Analysis.parseStopWords(env, settings, CzechAnalyzer.getDefaultStopSet(),
				version), Analysis.parseStemExclusion(settings, CharArraySet.EMPTY_SET));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.AnalyzerProvider#get()
	 */
	@Override
	public CzechAnalyzer get() {
		return this.analyzer;
	}
}