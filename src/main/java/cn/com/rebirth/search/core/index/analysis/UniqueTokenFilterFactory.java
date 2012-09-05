/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core UniqueTokenFilterFactory.java 2012-7-6 14:30:20 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.UniqueTokenFilter;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * A factory for creating UniqueTokenFilter objects.
 */
public class UniqueTokenFilterFactory extends AbstractTokenFilterFactory {

	/** The only on same position. */
	private final boolean onlyOnSamePosition;

	/**
	 * Instantiates a new unique token filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public UniqueTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		this.onlyOnSamePosition = settings.getAsBoolean("only_on_same_position", false);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
	 */
	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new UniqueTokenFilter(tokenStream, onlyOnSamePosition);
	}
}
