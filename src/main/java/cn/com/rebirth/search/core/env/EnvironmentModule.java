/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core EnvironmentModule.java 2012-3-29 15:01:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.env;

import cn.com.rebirth.search.commons.inject.AbstractModule;


/**
 * The Class EnvironmentModule.
 *
 * @author l.xue.nong
 */
public class EnvironmentModule extends AbstractModule {

    
    /** The environment. */
    private final Environment environment;

    
    /**
     * Instantiates a new environment module.
     *
     * @param environment the environment
     */
    public EnvironmentModule(Environment environment) {
        this.environment = environment;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(Environment.class).toInstance(environment);
    }
}
