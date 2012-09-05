/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StandardTokenFilterFactory.java 2012-7-6 14:30:36 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * A factory for creating StandardTokenFilter objects.
 */
public class StandardTokenFilterFactory extends AbstractTokenFilterFactory {

	/**
	 * Instantiates a new standard token filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public StandardTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
	 */
	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new StandardFilter(version, tokenStream);
	}
}