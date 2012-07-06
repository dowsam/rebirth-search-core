/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PreBuiltTokenizerFactoryFactory.java 2012-7-6 14:29:35 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import cn.com.rebirth.commons.settings.Settings;

/**
 * A factory for creating PreBuiltTokenizerFactory objects.
 */
public class PreBuiltTokenizerFactoryFactory implements TokenizerFactoryFactory {

	/** The tokenizer factory. */
	private final TokenizerFactory tokenizerFactory;

	/**
	 * Instantiates a new pre built tokenizer factory factory.
	 *
	 * @param tokenizerFactory the tokenizer factory
	 */
	public PreBuiltTokenizerFactoryFactory(TokenizerFactory tokenizerFactory) {
		this.tokenizerFactory = tokenizerFactory;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.TokenizerFactoryFactory#create(java.lang.String, cn.com.rebirth.commons.settings.Settings)
	 */
	@Override
	public TokenizerFactory create(String name, Settings settings) {
		return tokenizerFactory;
	}
}