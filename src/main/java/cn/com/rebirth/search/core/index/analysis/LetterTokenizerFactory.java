/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core LetterTokenizerFactory.java 2012-3-29 15:01:45 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.LetterTokenizer;
import org.apache.lucene.analysis.Tokenizer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * A factory for creating LetterTokenizer objects.
 */
public class LetterTokenizerFactory extends AbstractTokenizerFactory {

	
	/**
	 * Instantiates a new letter tokenizer factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public LetterTokenizerFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.TokenizerFactory#create(java.io.Reader)
	 */
	@Override
	public Tokenizer create(Reader reader) {
		return new LetterTokenizer(version, reader);
	}
}
