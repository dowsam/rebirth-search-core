/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NGramTokenFilterFactory.java 2012-7-6 14:30:01 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * A factory for creating NGramTokenFilter objects.
 */
public class NGramTokenFilterFactory extends AbstractTokenFilterFactory {

	/** The min gram. */
	private final int minGram;

	/** The max gram. */
	private final int maxGram;

	/**
	 * Instantiates a new n gram token filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public NGramTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		this.minGram = settings.getAsInt("min_gram", NGramTokenFilter.DEFAULT_MIN_NGRAM_SIZE);
		this.maxGram = settings.getAsInt("max_gram", NGramTokenFilter.DEFAULT_MAX_NGRAM_SIZE);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
	 */
	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new NGramTokenFilter(tokenStream, minGram, maxGram);
	}
}