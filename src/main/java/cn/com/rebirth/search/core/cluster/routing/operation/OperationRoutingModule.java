/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core OperationRoutingModule.java 2012-3-29 15:01:52 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing.operation;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.inject.Modules;
import cn.com.rebirth.search.commons.inject.SpawnModules;
import cn.com.rebirth.search.core.cluster.routing.operation.hash.HashFunction;
import cn.com.rebirth.search.core.cluster.routing.operation.hash.djb.DjbHashFunction;
import cn.com.rebirth.search.core.cluster.routing.operation.plain.PlainOperationRoutingModule;

import com.google.common.collect.ImmutableList;


/**
 * The Class OperationRoutingModule.
 *
 * @author l.xue.nong
 */
public class OperationRoutingModule extends AbstractModule implements SpawnModules {

    
    /** The settings. */
    private final Settings settings;

    
    /**
     * Instantiates a new operation routing module.
     *
     * @param settings the settings
     */
    public OperationRoutingModule(Settings settings) {
        this.settings = settings;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.inject.SpawnModules#spawnModules()
     */
    @Override
    public Iterable<? extends Module> spawnModules() {
        return ImmutableList.of(Modules.createModule(settings.getAsClass("cluster.routing.operation.type", PlainOperationRoutingModule.class, "cn.com.summall.search.core.cluster.routing.operation.", "OperationRoutingModule"), settings));
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        bind(HashFunction.class).to(settings.getAsClass("cluster.routing.operation.hash.type", DjbHashFunction.class, "cn.com.summall.search.core.cluster.routing.operation.hash.", "HashFunction")).asEagerSingleton();
    }
}