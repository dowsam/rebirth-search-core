/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SimilarityProvider.java 2012-3-29 15:02:02 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.similarity;

import org.apache.lucene.search.Similarity;

import cn.com.rebirth.search.commons.inject.Provider;
import cn.com.rebirth.search.core.index.IndexComponent;

/**
 * The Interface SimilarityProvider.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public interface SimilarityProvider<T extends Similarity> extends IndexComponent, Provider<T> {

	/**
	 * Name.
	 *
	 * @return the string
	 */
	String name();

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.Provider#get()
	 */
	T get();
}
