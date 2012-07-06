/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core HyphenationCompoundWordTokenFilterFactory.java 2012-3-29 15:00:49 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis.compound;

import java.net.URL;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.compound.HyphenationCompoundWordTokenFilter;
import org.apache.lucene.analysis.compound.hyphenation.HyphenationTree;
import org.xml.sax.InputSource;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.analysis.AnalysisSettingsRequired;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * A factory for creating HyphenationCompoundWordTokenFilter objects.
 */
@AnalysisSettingsRequired
public class HyphenationCompoundWordTokenFilterFactory extends AbstractCompoundWordTokenFilterFactory {

    
    /** The hyphenation tree. */
    private final HyphenationTree hyphenationTree;

    
    /**
     * Instantiates a new hyphenation compound word token filter factory.
     *
     * @param index the index
     * @param indexSettings the index settings
     * @param env the env
     * @param name the name
     * @param settings the settings
     */
    @Inject
    public HyphenationCompoundWordTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, Environment env, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, env, name, settings);

        String hyphenationPatternsPath = settings.get("hyphenation_patterns_path", null);
        if (hyphenationPatternsPath == null) {
            throw new RestartIllegalArgumentException("hyphenation_patterns_path is a required setting.");
        }

        URL hyphenationPatternsFile = env.resolveConfig(hyphenationPatternsPath);

        try {
            hyphenationTree = HyphenationCompoundWordTokenFilter.getHyphenationTree(new InputSource(hyphenationPatternsFile.toExternalForm()));
        } catch (Exception e) {
            throw new RestartIllegalArgumentException("Exception while reading hyphenation_patterns_path: " + e.getMessage());
        }
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
     */
    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new HyphenationCompoundWordTokenFilter(version, tokenStream,
                hyphenationTree, wordList,
                minWordSize, minSubwordSize, maxSubwordSize, onlyLongestMatch);
    }
}