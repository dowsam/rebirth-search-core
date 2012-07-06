/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core JmxOsProbe.java 2012-3-29 15:01:24 l.xue.nong$$
 */


package cn.com.rebirth.search.core.monitor.os;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;


/**
 * The Class JmxOsProbe.
 *
 * @author l.xue.nong
 */
public class JmxOsProbe extends AbstractComponent implements OsProbe {

    
    /**
     * Instantiates a new jmx os probe.
     *
     * @param settings the settings
     */
    @Inject
    public JmxOsProbe(Settings settings) {
        super(settings);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.monitor.os.OsProbe#osInfo()
     */
    @Override
    public OsInfo osInfo() {
        return new OsInfo();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.monitor.os.OsProbe#osStats()
     */
    @Override
    public OsStats osStats() {
        OsStats stats = new OsStats();
        stats.timestamp = System.currentTimeMillis();
        return stats;
    }
}
