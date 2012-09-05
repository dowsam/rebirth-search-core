/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NGramTokenizerFactory.java 2012-7-6 14:30:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * A factory for creating NGramTokenizer objects.
 */
public class NGramTokenizerFactory extends AbstractTokenizerFactory {

	/** The min gram. */
	private final int minGram;

	/** The max gram. */
	private final int maxGram;

	/**
	 * Instantiates a new n gram tokenizer factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public NGramTokenizerFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		this.minGram = settings.getAsInt("min_gram", NGramTokenizer.DEFAULT_MIN_NGRAM_SIZE);
		this.maxGram = settings.getAsInt("max_gram", NGramTokenizer.DEFAULT_MAX_NGRAM_SIZE);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.TokenizerFactory#create(java.io.Reader)
	 */
	@Override
	public Tokenizer create(Reader reader) {
		return new NGramTokenizer(reader, minGram, maxGram);
	}
}