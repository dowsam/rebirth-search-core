/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core KeywordAnalyzerProvider.java 2012-3-29 15:02:38 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.KeywordAnalyzer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * The Class KeywordAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class KeywordAnalyzerProvider extends AbstractIndexAnalyzerProvider<KeywordAnalyzer> {

	
	/** The keyword analyzer. */
	private final KeywordAnalyzer keywordAnalyzer;

	
	/**
	 * Instantiates a new keyword analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public KeywordAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		this.keywordAnalyzer = new KeywordAnalyzer();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.AnalyzerProvider#get()
	 */
	@Override
	public KeywordAnalyzer get() {
		return this.keywordAnalyzer;
	}
}