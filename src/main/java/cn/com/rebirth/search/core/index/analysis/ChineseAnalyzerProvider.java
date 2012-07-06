/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ChineseAnalyzerProvider.java 2012-7-6 14:29:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.cn.ChineseAnalyzer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class ChineseAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class ChineseAnalyzerProvider extends AbstractIndexAnalyzerProvider<ChineseAnalyzer> {

	/** The analyzer. */
	private final ChineseAnalyzer analyzer;

	/**
	 * Instantiates a new chinese analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public ChineseAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		analyzer = new ChineseAnalyzer();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.AnalyzerProvider#get()
	 */
	@Override
	public ChineseAnalyzer get() {
		return this.analyzer;
	}
}