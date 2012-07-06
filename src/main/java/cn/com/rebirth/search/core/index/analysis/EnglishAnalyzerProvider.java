/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core EnglishAnalyzerProvider.java 2012-3-29 15:01:59 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * The Class EnglishAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class EnglishAnalyzerProvider extends AbstractIndexAnalyzerProvider<EnglishAnalyzer> {

    
    /** The analyzer. */
    private final EnglishAnalyzer analyzer;

    
    /**
     * Instantiates a new english analyzer provider.
     *
     * @param index the index
     * @param indexSettings the index settings
     * @param env the env
     * @param name the name
     * @param settings the settings
     */
    @Inject
    public EnglishAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        analyzer = new EnglishAnalyzer(version,
                Analysis.parseStopWords(env, settings, EnglishAnalyzer.getDefaultStopSet(), version),
                Analysis.parseStemExclusion(settings, CharArraySet.EMPTY_SET));
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.analysis.AnalyzerProvider#get()
     */
    @Override
    public EnglishAnalyzer get() {
        return this.analyzer;
    }
}