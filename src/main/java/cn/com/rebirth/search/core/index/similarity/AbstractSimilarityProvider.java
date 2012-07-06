/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AbstractSimilarityProvider.java 2012-7-6 14:30:07 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.similarity;

import org.apache.lucene.search.Similarity;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class AbstractSimilarityProvider.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public abstract class AbstractSimilarityProvider<T extends Similarity> extends AbstractIndexComponent implements
		SimilarityProvider<T> {

	/** The name. */
	private final String name;

	/**
	 * Instantiates a new abstract similarity provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 */
	protected AbstractSimilarityProvider(Index index, @IndexSettings Settings indexSettings, String name) {
		super(index, indexSettings);
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.similarity.SimilarityProvider#name()
	 */
	@Override
	public String name() {
		return this.name;
	}
}
