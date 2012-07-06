/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TokenFilterFactoryFactory.java 2012-3-29 15:02:01 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import cn.com.rebirth.commons.settings.Settings;


/**
 * A factory for creating TokenFilterFactory objects.
 */
public interface TokenFilterFactoryFactory {

    
    /**
     * Creates the.
     *
     * @param name the name
     * @param settings the settings
     * @return the token filter factory
     */
    TokenFilterFactory create(String name, Settings settings);
}
