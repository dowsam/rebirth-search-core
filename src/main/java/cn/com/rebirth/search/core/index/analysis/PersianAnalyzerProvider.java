/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PersianAnalyzerProvider.java 2012-7-6 14:30:01 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.fa.PersianAnalyzer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class PersianAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class PersianAnalyzerProvider extends AbstractIndexAnalyzerProvider<PersianAnalyzer> {

	/** The analyzer. */
	private final PersianAnalyzer analyzer;

	/**
	 * Instantiates a new persian analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public PersianAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		analyzer = new PersianAnalyzer(version, Analysis.parseStopWords(env, settings,
				PersianAnalyzer.getDefaultStopSet(), version));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.AnalyzerProvider#get()
	 */
	@Override
	public PersianAnalyzer get() {
		return this.analyzer;
	}
}