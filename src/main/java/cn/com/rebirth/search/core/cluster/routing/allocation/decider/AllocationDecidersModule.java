/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AllocationDecidersModule.java 2012-3-29 15:02:12 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.routing.allocation.decider;

import java.util.List;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.multibindings.Multibinder;

import com.google.common.collect.Lists;


/**
 * The Class AllocationDecidersModule.
 *
 * @author l.xue.nong
 */
public class AllocationDecidersModule extends AbstractModule {

    
    /** The settings. */
    private final Settings settings;

    
    /** The allocations. */
    private List<Class<? extends AllocationDecider>> allocations = Lists.newArrayList();

    
    /**
     * Instantiates a new allocation deciders module.
     *
     * @param settings the settings
     */
    public AllocationDecidersModule(Settings settings) {
        this.settings = settings;
    }

    
    /**
     * Adds the.
     *
     * @param allocationDecider the allocation decider
     * @return the allocation deciders module
     */
    public AllocationDecidersModule add(Class<? extends AllocationDecider> allocationDecider) {
        this.allocations.add(allocationDecider);
        return this;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        Multibinder<AllocationDecider> allocationMultibinder = Multibinder.newSetBinder(binder(), AllocationDecider.class);
        allocationMultibinder.addBinding().to(SameShardAllocationDecider.class);
        allocationMultibinder.addBinding().to(FilterAllocationDecider.class);
        allocationMultibinder.addBinding().to(ReplicaAfterPrimaryActiveAllocationDecider.class);
        allocationMultibinder.addBinding().to(ThrottlingAllocationDecider.class);
        allocationMultibinder.addBinding().to(RebalanceOnlyWhenActiveAllocationDecider.class);
        allocationMultibinder.addBinding().to(ClusterRebalanceAllocationDecider.class);
        allocationMultibinder.addBinding().to(ConcurrentRebalanceAllocationDecider.class);
        allocationMultibinder.addBinding().to(DisableAllocationDecider.class);
        allocationMultibinder.addBinding().to(AwarenessAllocationDecider.class);
        allocationMultibinder.addBinding().to(ShardsLimitAllocationDecider.class);
        for (Class<? extends AllocationDecider> allocation : allocations) {
            allocationMultibinder.addBinding().to(allocation);
        }

        bind(AllocationDeciders.class).asEagerSingleton();
    }
}
