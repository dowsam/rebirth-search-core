/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core UAX29URLEmailTokenizerFactory.java 2012-7-6 14:29:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.UAX29URLEmailTokenizer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * A factory for creating UAX29URLEmailTokenizer objects.
 */
public class UAX29URLEmailTokenizerFactory extends AbstractTokenizerFactory {

	/** The max token length. */
	private final int maxTokenLength;

	/**
	 * Instantiates a new uA x29 url email tokenizer factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public UAX29URLEmailTokenizerFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		maxTokenLength = settings.getAsInt("max_token_length", StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.TokenizerFactory#create(java.io.Reader)
	 */
	@Override
	public Tokenizer create(Reader reader) {
		UAX29URLEmailTokenizer tokenizer = new UAX29URLEmailTokenizer(reader);
		tokenizer.setMaxTokenLength(maxTokenLength);
		return tokenizer;
	}
}