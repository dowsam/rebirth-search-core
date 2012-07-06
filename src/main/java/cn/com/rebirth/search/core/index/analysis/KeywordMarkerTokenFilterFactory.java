/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core KeywordMarkerTokenFilterFactory.java 2012-3-29 15:02:49 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import java.util.Set;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.KeywordMarkerFilter;
import org.apache.lucene.analysis.TokenStream;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * A factory for creating KeywordMarkerTokenFilter objects.
 */
@AnalysisSettingsRequired
public class KeywordMarkerTokenFilterFactory extends AbstractTokenFilterFactory {

	
	/** The keyword lookup. */
	private final CharArraySet keywordLookup;

	
	/**
	 * Instantiates a new keyword marker token filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public KeywordMarkerTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);

		boolean ignoreCase = settings.getAsBoolean("ignore_case", false);
		Set<?> rules = Analysis.getWordSet(env, settings, "keywords", version);
		if (rules == null) {
			throw new RestartIllegalArgumentException(
					"keyword filter requires either `keywords` or `keywords_path` to be configured");
		}
		keywordLookup = new CharArraySet(version, rules, ignoreCase);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
	 */
	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new KeywordMarkerFilter(tokenStream, keywordLookup);
	}
}
