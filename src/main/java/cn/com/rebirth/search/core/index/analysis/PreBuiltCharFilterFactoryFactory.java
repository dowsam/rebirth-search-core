/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PreBuiltCharFilterFactoryFactory.java 2012-3-29 15:02:32 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import cn.com.rebirth.commons.settings.Settings;


/**
 * A factory for creating PreBuiltCharFilterFactory objects.
 */
public class PreBuiltCharFilterFactoryFactory implements CharFilterFactoryFactory {

    
    /** The char filter factory. */
    private final CharFilterFactory charFilterFactory;

    
    /**
     * Instantiates a new pre built char filter factory factory.
     *
     * @param charFilterFactory the char filter factory
     */
    public PreBuiltCharFilterFactoryFactory(CharFilterFactory charFilterFactory) {
        this.charFilterFactory = charFilterFactory;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.analysis.CharFilterFactoryFactory#create(java.lang.String, cn.com.summall.search.commons.settings.Settings)
     */
    @Override
    public CharFilterFactory create(String name, Settings settings) {
        return charFilterFactory;
    }
}