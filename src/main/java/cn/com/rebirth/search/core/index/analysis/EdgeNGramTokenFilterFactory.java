/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core EdgeNGramTokenFilterFactory.java 2012-3-29 15:00:49 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenizer;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;



/**
 * A factory for creating EdgeNGramTokenFilter objects.
 */
public class EdgeNGramTokenFilterFactory extends AbstractTokenFilterFactory {

    
    /** The min gram. */
    private final int minGram;

    
    /** The max gram. */
    private final int maxGram;

    
    /** The side. */
    private final EdgeNGramTokenFilter.Side side;

    
    /**
     * Instantiates a new edge n gram token filter factory.
     *
     * @param index the index
     * @param indexSettings the index settings
     * @param name the name
     * @param settings the settings
     */
    @Inject
    public EdgeNGramTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        this.minGram = settings.getAsInt("min_gram", NGramTokenFilter.DEFAULT_MIN_NGRAM_SIZE);
        this.maxGram = settings.getAsInt("max_gram", NGramTokenFilter.DEFAULT_MAX_NGRAM_SIZE);
        this.side = EdgeNGramTokenFilter.Side.getSide(settings.get("side", EdgeNGramTokenizer.DEFAULT_SIDE.getLabel()));
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
     */
    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new EdgeNGramTokenFilter(tokenStream, side, minGram, maxGram);
    }
}