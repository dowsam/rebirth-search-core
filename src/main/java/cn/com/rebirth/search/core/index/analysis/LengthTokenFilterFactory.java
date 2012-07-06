/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core LengthTokenFilterFactory.java 2012-3-29 15:02:46 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.LengthFilter;
import org.apache.lucene.analysis.TokenStream;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * A factory for creating LengthTokenFilter objects.
 */
public class LengthTokenFilterFactory extends AbstractTokenFilterFactory {

	
	/** The min. */
	private final int min;

	
	/** The max. */
	private final int max;

	
	/** The enable position increments. */
	private final boolean enablePositionIncrements;

	
	/**
	 * Instantiates a new length token filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public LengthTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		min = settings.getAsInt("min", 0);
		max = settings.getAsInt("max", Integer.MAX_VALUE);
		enablePositionIncrements = settings.getAsBoolean("enabled_position_increments", true);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
	 */
	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new LengthFilter(enablePositionIncrements, tokenStream, min, max);
	}
}
