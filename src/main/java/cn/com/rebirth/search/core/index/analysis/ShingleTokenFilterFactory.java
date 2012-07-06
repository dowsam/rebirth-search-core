/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShingleTokenFilterFactory.java 2012-3-29 15:02:32 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleFilter;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * A factory for creating ShingleTokenFilter objects.
 */
public class ShingleTokenFilterFactory extends AbstractTokenFilterFactory {

	
	/** The max shingle size. */
	private final int maxShingleSize;

	
	/** The output unigrams. */
	private final boolean outputUnigrams;

	
	/**
	 * Instantiates a new shingle token filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public ShingleTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		maxShingleSize = settings.getAsInt("max_shingle_size", ShingleFilter.DEFAULT_MAX_SHINGLE_SIZE);
		outputUnigrams = settings.getAsBoolean("output_unigrams", true);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
	 */
	@Override
	public TokenStream create(TokenStream tokenStream) {
		ShingleFilter filter = new ShingleFilter(tokenStream, maxShingleSize);
		filter.setOutputUnigrams(outputUnigrams);
		return filter;
	}
}