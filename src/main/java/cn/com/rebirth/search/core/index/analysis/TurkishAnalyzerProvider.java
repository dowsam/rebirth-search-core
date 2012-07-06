/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TurkishAnalyzerProvider.java 2012-3-29 15:02:21 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * The Class TurkishAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class TurkishAnalyzerProvider extends AbstractIndexAnalyzerProvider<TurkishAnalyzer> {

	
	/** The analyzer. */
	private final TurkishAnalyzer analyzer;

	
	/**
	 * Instantiates a new turkish analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public TurkishAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		analyzer = new TurkishAnalyzer(version, Analysis.parseStopWords(env, settings,
				TurkishAnalyzer.getDefaultStopSet(), version), Analysis.parseStemExclusion(settings,
				CharArraySet.EMPTY_SET));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.AnalyzerProvider#get()
	 */
	@Override
	public TurkishAnalyzer get() {
		return this.analyzer;
	}
}