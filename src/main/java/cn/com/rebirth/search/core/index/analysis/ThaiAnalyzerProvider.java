/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ThaiAnalyzerProvider.java 2012-7-6 14:30:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.th.ThaiAnalyzer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class ThaiAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class ThaiAnalyzerProvider extends AbstractIndexAnalyzerProvider<ThaiAnalyzer> {

	/** The analyzer. */
	private final ThaiAnalyzer analyzer;

	/**
	 * Instantiates a new thai analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public ThaiAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		analyzer = new ThaiAnalyzer(version);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.AnalyzerProvider#get()
	 */
	@Override
	public ThaiAnalyzer get() {
		return this.analyzer;
	}
}