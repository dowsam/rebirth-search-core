/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LowerCaseTokenFilterFactory.java 2012-7-6 14:28:44 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.el.GreekLowerCaseFilter;
import org.apache.lucene.analysis.tr.TurkishLowerCaseFilter;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * A factory for creating LowerCaseTokenFilter objects.
 */
public class LowerCaseTokenFilterFactory extends AbstractTokenFilterFactory {

	/** The lang. */
	private final String lang;

	/**
	 * Instantiates a new lower case token filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public LowerCaseTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		this.lang = settings.get("language", null);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
	 */
	@Override
	public TokenStream create(TokenStream tokenStream) {
		if (lang == null) {
			return new LowerCaseFilter(version, tokenStream);
		} else if (lang.equalsIgnoreCase("greek")) {
			return new GreekLowerCaseFilter(version, tokenStream);
		} else if (lang.equalsIgnoreCase("turkish")) {
			return new TurkishLowerCaseFilter(tokenStream);
		} else {
			throw new RebirthIllegalArgumentException("language [" + lang + "] not support for lower case");
		}
	}
}
