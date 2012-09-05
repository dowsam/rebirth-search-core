/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DanishAnalyzerProvider.java 2012-7-6 14:30:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.da.DanishAnalyzer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class DanishAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class DanishAnalyzerProvider extends AbstractIndexAnalyzerProvider<DanishAnalyzer> {

	/** The analyzer. */
	private final DanishAnalyzer analyzer;

	/**
	 * Instantiates a new danish analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public DanishAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		analyzer = new DanishAnalyzer(version, Analysis.parseStopWords(env, settings,
				DanishAnalyzer.getDefaultStopSet(), version), Analysis.parseStemExclusion(settings,
				CharArraySet.EMPTY_SET));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.AnalyzerProvider#get()
	 */
	@Override
	public DanishAnalyzer get() {
		return this.analyzer;
	}
}