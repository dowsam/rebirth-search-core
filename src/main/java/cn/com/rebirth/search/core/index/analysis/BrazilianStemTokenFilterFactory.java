/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BrazilianStemTokenFilterFactory.java 2012-3-29 15:02:18 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.br.BrazilianStemFilter;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;


/**
 * A factory for creating BrazilianStemTokenFilter objects.
 */
public class BrazilianStemTokenFilterFactory extends AbstractTokenFilterFactory {

    
    /** The exclusions. */
    private final Set<?> exclusions;

    
    /**
     * Instantiates a new brazilian stem token filter factory.
     *
     * @param index the index
     * @param indexSettings the index settings
     * @param name the name
     * @param settings the settings
     */
    @Inject
    public BrazilianStemTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        String[] stemExclusion = settings.getAsArray("stem_exclusion");
        if (stemExclusion.length > 0) {
            this.exclusions = ImmutableSet.copyOf(Iterators.forArray(stemExclusion));
        } else {
            this.exclusions = ImmutableSet.of();
        }
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
     */
    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new BrazilianStemFilter(tokenStream, exclusions);
    }
}