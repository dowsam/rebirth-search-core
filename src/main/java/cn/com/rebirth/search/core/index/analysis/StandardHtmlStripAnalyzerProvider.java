/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StandardHtmlStripAnalyzerProvider.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class StandardHtmlStripAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class StandardHtmlStripAnalyzerProvider extends AbstractIndexAnalyzerProvider<StandardHtmlStripAnalyzer> {

	/** The analyzer. */
	private final StandardHtmlStripAnalyzer analyzer;

	/**
	 * Instantiates a new standard html strip analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public StandardHtmlStripAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		analyzer = new StandardHtmlStripAnalyzer(version);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.AnalyzerProvider#get()
	 */
	@Override
	public StandardHtmlStripAnalyzer get() {
		return this.analyzer;
	}
}
