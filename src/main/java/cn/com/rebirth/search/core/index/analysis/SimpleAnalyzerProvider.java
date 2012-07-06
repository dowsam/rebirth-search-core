/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SimpleAnalyzerProvider.java 2012-3-29 15:01:56 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.SimpleAnalyzer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * The Class SimpleAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class SimpleAnalyzerProvider extends AbstractIndexAnalyzerProvider<SimpleAnalyzer> {

	
	/** The simple analyzer. */
	private final SimpleAnalyzer simpleAnalyzer;

	
	/**
	 * Instantiates a new simple analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public SimpleAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		this.simpleAnalyzer = new SimpleAnalyzer(version);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.AnalyzerProvider#get()
	 */
	@Override
	public SimpleAnalyzer get() {
		return this.simpleAnalyzer;
	}
}