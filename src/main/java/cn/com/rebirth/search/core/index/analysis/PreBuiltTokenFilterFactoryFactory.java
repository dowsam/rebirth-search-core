/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PreBuiltTokenFilterFactoryFactory.java 2012-3-29 15:02:41 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import cn.com.rebirth.commons.settings.Settings;


/**
 * A factory for creating PreBuiltTokenFilterFactory objects.
 */
public class PreBuiltTokenFilterFactoryFactory implements TokenFilterFactoryFactory {

	
	/** The token filter factory. */
	private final TokenFilterFactory tokenFilterFactory;

	
	/**
	 * Instantiates a new pre built token filter factory factory.
	 *
	 * @param tokenFilterFactory the token filter factory
	 */
	public PreBuiltTokenFilterFactoryFactory(TokenFilterFactory tokenFilterFactory) {
		this.tokenFilterFactory = tokenFilterFactory;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.TokenFilterFactoryFactory#create(java.lang.String, cn.com.summall.search.commons.settings.Settings)
	 */
	@Override
	public TokenFilterFactory create(String name, Settings settings) {
		return tokenFilterFactory;
	}
}