/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core KeywordTokenizerFactory.java 2012-7-6 14:30:03 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.Tokenizer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * A factory for creating KeywordTokenizer objects.
 */
public class KeywordTokenizerFactory extends AbstractTokenizerFactory {

	/** The buffer size. */
	private final int bufferSize;

	/**
	 * Instantiates a new keyword tokenizer factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public KeywordTokenizerFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		bufferSize = settings.getAsInt("buffer_size", 256);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.TokenizerFactory#create(java.io.Reader)
	 */
	@Override
	public Tokenizer create(Reader reader) {
		return new KeywordTokenizer(reader, bufferSize);
	}
}
