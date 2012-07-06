/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DefaultSimilarityProvider.java 2012-7-6 14:29:07 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.similarity;

import org.apache.lucene.search.DefaultSimilarity;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class DefaultSimilarityProvider.
 *
 * @author l.xue.nong
 */
public class DefaultSimilarityProvider extends AbstractSimilarityProvider<DefaultSimilarity> {

	/** The similarity. */
	private DefaultSimilarity similarity;

	/**
	 * Instantiates a new default similarity provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public DefaultSimilarityProvider(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name);
		this.similarity = new DefaultSimilarity();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.similarity.SimilarityProvider#get()
	 */
	@Override
	public DefaultSimilarity get() {
		return similarity;
	}
}
