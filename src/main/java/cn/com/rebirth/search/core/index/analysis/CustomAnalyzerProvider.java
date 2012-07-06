/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CustomAnalyzerProvider.java 2012-3-29 15:01:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * The Class CustomAnalyzerProvider.
 *
 * @author l.xue.nong
 */
public class CustomAnalyzerProvider extends AbstractIndexAnalyzerProvider<CustomAnalyzer> {

    
    /** The analyzer settings. */
    private final Settings analyzerSettings;

    
    /** The custom analyzer. */
    private CustomAnalyzer customAnalyzer;

    
    /**
     * Instantiates a new custom analyzer provider.
     *
     * @param index the index
     * @param indexSettings the index settings
     * @param name the name
     * @param settings the settings
     */
    @Inject
    public CustomAnalyzerProvider(Index index, @IndexSettings Settings indexSettings,
                                  @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        this.analyzerSettings = settings;
    }

    
    /**
     * Builds the.
     *
     * @param analysisService the analysis service
     */
    public void build(AnalysisService analysisService) {
        String tokenizerName = analyzerSettings.get("tokenizer");
        if (tokenizerName == null) {
            throw new IllegalArgumentException("Custom Analyzer [" + name() + "] must be configured with a tokenizer");
        }

        TokenizerFactory tokenizer = analysisService.tokenizer(tokenizerName);
        if (tokenizer == null) {
            throw new IllegalArgumentException("Custom Analyzer [" + name() + "] failed to find tokenizer under name [" + tokenizerName + "]");
        }

        List<CharFilterFactory> charFilters = newArrayList();
        String[] charFilterNames = analyzerSettings.getAsArray("char_filter");
        for (String charFilterName : charFilterNames) {
            CharFilterFactory charFilter = analysisService.charFilter(charFilterName);
            if (charFilter == null) {
                throw new IllegalArgumentException("Custom Analyzer [" + name() + "] failed to find char_filter under name [" + charFilterName + "]");
            }
            charFilters.add(charFilter);
        }

        List<TokenFilterFactory> tokenFilters = newArrayList();
        String[] tokenFilterNames = analyzerSettings.getAsArray("filter");
        for (String tokenFilterName : tokenFilterNames) {
            TokenFilterFactory tokenFilter = analysisService.tokenFilter(tokenFilterName);
            if (tokenFilter == null) {
                throw new IllegalArgumentException("Custom Analyzer [" + name() + "] failed to find filter under name [" + tokenFilterName + "]");
            }
            tokenFilters.add(tokenFilter);
        }

        this.customAnalyzer = new CustomAnalyzer(tokenizer,
                charFilters.toArray(new CharFilterFactory[charFilters.size()]),
                tokenFilters.toArray(new TokenFilterFactory[tokenFilters.size()]));
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.analysis.AnalyzerProvider#get()
     */
    @Override
    public CustomAnalyzer get() {
        return this.customAnalyzer;
    }
}
