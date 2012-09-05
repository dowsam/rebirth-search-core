/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CjkAnalyzerProvider.java 2012-7-6 14:29:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import java.util.Set;

import org.apache.lucene.analysis.cjk.CJKAnalyzer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class CjkAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class CjkAnalyzerProvider extends AbstractIndexAnalyzerProvider<CJKAnalyzer> {

	/** The analyzer. */
	private final CJKAnalyzer analyzer;

	/**
	 * Instantiates a new cjk analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public CjkAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		Set<?> stopWords = Analysis.parseStopWords(env, settings, CJKAnalyzer.getDefaultStopSet(), version);

		analyzer = new CJKAnalyzer(version, stopWords);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.AnalyzerProvider#get()
	 */
	@Override
	public CJKAnalyzer get() {
		return this.analyzer;
	}
}