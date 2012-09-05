/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PatternAnalyzerProvider.java 2012-7-6 14:30:34 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import java.util.Set;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PatternAnalyzer;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.regex.Regex;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class PatternAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class PatternAnalyzerProvider extends AbstractIndexAnalyzerProvider<PatternAnalyzer> {

	/** The analyzer. */
	private final PatternAnalyzer analyzer;

	/**
	 * Instantiates a new pattern analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public PatternAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);

		boolean lowercase = settings.getAsBoolean("lowercase", true);

		Set<?> stopWords = Analysis.parseStopWords(env, settings, StopAnalyzer.ENGLISH_STOP_WORDS_SET, version);

		String sPattern = settings.get("pattern", "\\W+");
		if (sPattern == null) {
			throw new RebirthIllegalArgumentException("Analyzer [" + name
					+ "] of type pattern must have a `pattern` set");
		}
		Pattern pattern = Regex.compile(sPattern, settings.get("flags"));

		analyzer = new PatternAnalyzer(version, pattern, lowercase, stopWords);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.AnalyzerProvider#get()
	 */
	@Override
	public PatternAnalyzer get() {
		return analyzer;
	}
}
