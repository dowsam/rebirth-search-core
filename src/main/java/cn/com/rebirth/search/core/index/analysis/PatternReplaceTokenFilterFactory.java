/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PatternReplaceTokenFilterFactory.java 2012-3-29 15:02:20 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.regex.Regex;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * A factory for creating PatternReplaceTokenFilter objects.
 */
@AnalysisSettingsRequired
public class PatternReplaceTokenFilterFactory extends AbstractTokenFilterFactory {

	
	/** The pattern. */
	private final Pattern pattern;

	
	/** The replacement. */
	private final String replacement;

	
	/** The all. */
	private final boolean all;

	
	/**
	 * Instantiates a new pattern replace token filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public PatternReplaceTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);

		String sPattern = settings.get("pattern", null);
		if (sPattern == null) {
			throw new RestartIllegalArgumentException("pattern is missing for [" + name
					+ "] token filter of type 'pattern_replace'");
		}

		this.pattern = Regex.compile(sPattern, settings.get("flags"));

		String sReplacement = settings.get("replacement", null);
		if (sReplacement == null) {
			throw new RestartIllegalArgumentException("replacement is missing for [" + name
					+ "] token filter of type 'pattern_replace'");
		}

		this.replacement = sReplacement;

		this.all = settings.getAsBoolean("all", true);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
	 */
	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new PatternReplaceFilter(tokenStream, pattern, replacement, all);
	}
}