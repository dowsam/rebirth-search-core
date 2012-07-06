/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CharFilterFactoryFactory.java 2012-3-29 15:02:22 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import cn.com.rebirth.commons.settings.Settings;


/**
 * A factory for creating CharFilterFactory objects.
 */
public interface CharFilterFactoryFactory {

    
    /**
     * Creates the.
     *
     * @param name the name
     * @param settings the settings
     * @return the char filter factory
     */
    CharFilterFactory create(String name, Settings settings);
}
