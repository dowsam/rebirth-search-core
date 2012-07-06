/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AnalyzerProviderFactory.java 2012-3-29 15:02:24 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import cn.com.rebirth.commons.settings.Settings;


/**
 * A factory for creating AnalyzerProvider objects.
 */
public interface AnalyzerProviderFactory {

    
    /**
     * Creates the.
     *
     * @param name the name
     * @param settings the settings
     * @return the analyzer provider
     */
    AnalyzerProvider create(String name, Settings settings);
}
