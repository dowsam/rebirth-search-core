/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SnowballTokenFilterFactory.java 2012-3-29 15:01:02 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * A factory for creating SnowballTokenFilter objects.
 */
public class SnowballTokenFilterFactory extends AbstractTokenFilterFactory {

	
	/** The language. */
	private String language;

	
	/**
	 * Instantiates a new snowball token filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public SnowballTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		this.language = Strings.capitalize(settings.get("language", settings.get("name", "English")));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
	 */
	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new SnowballFilter(tokenStream, language);
	}

}
