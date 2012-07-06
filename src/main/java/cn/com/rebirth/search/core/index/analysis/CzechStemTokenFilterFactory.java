/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CzechStemTokenFilterFactory.java 2012-3-29 15:02:20 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cz.CzechStemFilter;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * A factory for creating CzechStemTokenFilter objects.
 */
public class CzechStemTokenFilterFactory extends AbstractTokenFilterFactory {

    
    /**
     * Instantiates a new czech stem token filter factory.
     *
     * @param index the index
     * @param indexSettings the index settings
     * @param name the name
     * @param settings the settings
     */
    @Inject
    public CzechStemTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
     */
    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new CzechStemFilter(tokenStream);
    }
}
