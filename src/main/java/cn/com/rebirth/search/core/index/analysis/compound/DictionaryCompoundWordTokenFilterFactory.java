/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DictionaryCompoundWordTokenFilterFactory.java 2012-3-29 15:02:38 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis.compound;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.compound.DictionaryCompoundWordTokenFilter;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.analysis.AnalysisSettingsRequired;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * A factory for creating DictionaryCompoundWordTokenFilter objects.
 */
@AnalysisSettingsRequired
public class DictionaryCompoundWordTokenFilterFactory extends AbstractCompoundWordTokenFilterFactory {

	
	/**
	 * Instantiates a new dictionary compound word token filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public DictionaryCompoundWordTokenFilterFactory(Index index, @IndexSettings Settings indexSettings,
			Environment env, @Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, env, name, settings);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
	 */
	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new DictionaryCompoundWordTokenFilter(version, tokenStream, wordList, minWordSize, minSubwordSize,
				maxSubwordSize, onlyLongestMatch);
	}
}