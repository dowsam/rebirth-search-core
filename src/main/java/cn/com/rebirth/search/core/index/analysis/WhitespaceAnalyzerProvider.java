/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core WhitespaceAnalyzerProvider.java 2012-7-6 14:30:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.WhitespaceAnalyzer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class WhitespaceAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class WhitespaceAnalyzerProvider extends AbstractIndexAnalyzerProvider<WhitespaceAnalyzer> {

	/** The analyzer. */
	private final WhitespaceAnalyzer analyzer;

	/**
	 * Instantiates a new whitespace analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public WhitespaceAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		this.analyzer = new WhitespaceAnalyzer(version);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.AnalyzerProvider#get()
	 */
	@Override
	public WhitespaceAnalyzer get() {
		return this.analyzer;
	}
}
