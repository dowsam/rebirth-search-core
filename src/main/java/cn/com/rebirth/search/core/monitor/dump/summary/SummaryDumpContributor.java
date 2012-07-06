/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SummaryDumpContributor.java 2012-3-29 15:02:33 l.xue.nong$$
 */


package cn.com.rebirth.search.core.monitor.dump.summary;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.monitor.dump.Dump;
import cn.com.rebirth.search.core.monitor.dump.DumpContributionFailedException;
import cn.com.rebirth.search.core.monitor.dump.DumpContributor;


/**
 * The Class SummaryDumpContributor.
 *
 * @author l.xue.nong
 */
public class SummaryDumpContributor implements DumpContributor {

    
    /** The date format. */
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    
    
    /** The formatter lock. */
    private final Object formatterLock = new Object();

    
    /** The Constant SUMMARY. */
    public static final String SUMMARY = "summary";

    
    /** The name. */
    private final String name;

    
    /**
     * Instantiates a new summary dump contributor.
     *
     * @param name the name
     * @param settings the settings
     */
    @Inject
    public SummaryDumpContributor(@Assisted String name, @Assisted Settings settings) {
        this.name = name;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.monitor.dump.DumpContributor#getName()
     */
    public String getName() {
        return name;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.monitor.dump.DumpContributor#contribute(cn.com.summall.search.core.monitor.dump.Dump)
     */
    public void contribute(Dump dump) throws DumpContributionFailedException {
        PrintWriter writer = new PrintWriter(dump.createFileWriter("summary.txt"));
        try {
            processHeader(writer, dump.timestamp());
            processCause(writer, dump.cause());
            processThrowables(writer, dump);
        } catch (Exception e) {
            throw new DumpContributionFailedException(getName(), "Failed to generate", e);
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                
            }
        }
    }

    
    /**
     * Process header.
     *
     * @param writer the writer
     * @param timestamp the timestamp
     */
    private void processHeader(PrintWriter writer, long timestamp) {
        synchronized (formatterLock) {
            writer.println("===== TIME =====");
            writer.println(dateFormat.format(new Date(timestamp)));
            writer.println();
        }
    }

    
    /**
     * Process cause.
     *
     * @param writer the writer
     * @param cause the cause
     */
    private void processCause(PrintWriter writer, String cause) {
        writer.println("===== CAUSE =====");
        writer.println(cause);
        writer.println();
    }

    
    /**
     * Process throwables.
     *
     * @param writer the writer
     * @param dump the dump
     */
    private void processThrowables(PrintWriter writer, Dump dump) {
        writer.println("===== EXCEPTIONS =====");
        Object throwables = dump.context().get("throwables");
        if (throwables == null) {
            return;
        }
        if (throwables instanceof Throwable[]) {
            Throwable[] array = (Throwable[]) throwables;
            for (Throwable t : array) {
                writer.println();
                writer.println("---- Exception ----");
                t.printStackTrace(writer);
            }
        } else if (throwables instanceof Collection) {
            Collection collection = (Collection) throwables;
            for (Object o : collection) {
                Throwable t = (Throwable) o;
                writer.println();
                writer.println("---- Exception ----");
                t.printStackTrace(writer);
            }
        } else {
            throw new DumpContributionFailedException(getName(), "Can't handle throwables type [" + throwables.getClass() + "]");
        }
        writer.println();
    }
}
