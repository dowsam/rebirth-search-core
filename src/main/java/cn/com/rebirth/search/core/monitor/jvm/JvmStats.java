/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core JvmStats.java 2012-3-29 15:01:10 l.xue.nong$$
 */


package cn.com.rebirth.search.core.monitor.jvm;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.Booleans;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;

import com.google.common.collect.Iterators;


/**
 * The Class JvmStats.
 *
 * @author l.xue.nong
 */
public class JvmStats implements Streamable, Serializable, ToXContent {

    
	private static final long serialVersionUID = -2663076057656455225L;


	/** The enable last gc. */
    private static boolean enableLastGc;

    
    /**
     * Checks if is last gc enabled.
     *
     * @return true, if is last gc enabled
     */
    public static boolean isLastGcEnabled() {
        return enableLastGc;
    }

    
    /** The Constant runtimeMXBean. */
    private final static RuntimeMXBean runtimeMXBean;
    
    
    /** The Constant memoryMXBean. */
    private final static MemoryMXBean memoryMXBean;
    
    
    /** The Constant threadMXBean. */
    private final static ThreadMXBean threadMXBean;

    
    /** The get last gc info method. */
    private static Method getLastGcInfoMethod;
    
    
    /** The get memory usage before gc method. */
    private static Method getMemoryUsageBeforeGcMethod;
    
    
    /** The get memory usage after gc method. */
    private static Method getMemoryUsageAfterGcMethod;
    
    
    /** The get start time method. */
    private static Method getStartTimeMethod;
    
    
    /** The get end time method. */
    private static Method getEndTimeMethod;
    
    
    /** The get duration method. */
    private static Method getDurationMethod;

    static {
        runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        threadMXBean = ManagementFactory.getThreadMXBean();

        JvmInfo info = JvmInfo.jvmInfo();
        boolean defaultEnableLastGc = false;
        if (info.versionAsInteger() == 170) {
            defaultEnableLastGc = info.versionUpdatePack() >= 4;
        } else if (info.versionAsInteger() > 170) {
            defaultEnableLastGc = true;
        }

        boolean enableLastGc = Booleans.parseBoolean(System.getProperty("monitor.jvm.enable_last_gc"), defaultEnableLastGc);
        if (enableLastGc) {
            try {
                Class<?> sunGcClass = Class.forName("com.sun.management.GarbageCollectorMXBean");
                Class<?> gcInfoClass = Class.forName("com.sun.management.GcInfo");

                getLastGcInfoMethod = sunGcClass.getDeclaredMethod("getLastGcInfo");
                getLastGcInfoMethod.setAccessible(true);

                getMemoryUsageBeforeGcMethod = gcInfoClass.getDeclaredMethod("getMemoryUsageBeforeGc");
                getMemoryUsageBeforeGcMethod.setAccessible(true);
                getMemoryUsageAfterGcMethod = gcInfoClass.getDeclaredMethod("getMemoryUsageAfterGc");
                getMemoryUsageAfterGcMethod.setAccessible(true);
                getStartTimeMethod = gcInfoClass.getDeclaredMethod("getStartTime");
                getStartTimeMethod.setAccessible(true);
                getEndTimeMethod = gcInfoClass.getDeclaredMethod("getEndTime");
                getEndTimeMethod.setAccessible(true);
                getDurationMethod = gcInfoClass.getDeclaredMethod("getDuration");
                getDurationMethod.setAccessible(true);

            } catch (Throwable ex) {
                enableLastGc = false;
            }
        }

        JvmStats.enableLastGc = enableLastGc;
    }

    
    /**
     * Jvm stats.
     *
     * @return the jvm stats
     */
    @SuppressWarnings("unchecked")
	public static JvmStats jvmStats() {
        JvmStats stats = new JvmStats(System.currentTimeMillis(), runtimeMXBean.getUptime());
        stats.mem = new Mem();
        MemoryUsage memUsage = memoryMXBean.getHeapMemoryUsage();
        stats.mem.heapUsed = memUsage.getUsed() < 0 ? 0 : memUsage.getUsed();
        stats.mem.heapCommitted = memUsage.getCommitted() < 0 ? 0 : memUsage.getCommitted();
        memUsage = memoryMXBean.getNonHeapMemoryUsage();
        stats.mem.nonHeapUsed = memUsage.getUsed() < 0 ? 0 : memUsage.getUsed();
        stats.mem.nonHeapCommitted = memUsage.getCommitted() < 0 ? 0 : memUsage.getCommitted();

        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        stats.mem.pools = new MemoryPool[memoryPoolMXBeans.size()];
        for (int i = 0; i < memoryPoolMXBeans.size(); i++) {
            MemoryPoolMXBean memoryPoolMXBean = memoryPoolMXBeans.get(i);
            MemoryUsage usage = memoryPoolMXBean.getUsage();
            MemoryUsage peakUsage = memoryPoolMXBean.getPeakUsage();
            stats.mem.pools[i] = new MemoryPool(memoryPoolMXBean.getName(),
                    usage.getUsed() < 0 ? 0 : usage.getUsed(),
                    usage.getMax() < 0 ? 0 : usage.getMax(),
                    peakUsage.getUsed() < 0 ? 0 : peakUsage.getUsed(),
                    peakUsage.getMax() < 0 ? 0 : peakUsage.getMax()
            );
        }

        stats.threads = new Threads();
        stats.threads.count = threadMXBean.getThreadCount();
        stats.threads.peakCount = threadMXBean.getPeakThreadCount();

        List<GarbageCollectorMXBean> gcMxBeans = ManagementFactory.getGarbageCollectorMXBeans();
        stats.gc = new GarbageCollectors();
        stats.gc.collectors = new GarbageCollector[gcMxBeans.size()];
        for (int i = 0; i < stats.gc.collectors.length; i++) {
            GarbageCollectorMXBean gcMxBean = gcMxBeans.get(i);
            stats.gc.collectors[i] = new GarbageCollector();
            stats.gc.collectors[i].name = gcMxBean.getName();
            stats.gc.collectors[i].collectionCount = gcMxBean.getCollectionCount();
            stats.gc.collectors[i].collectionTime = gcMxBean.getCollectionTime();
            if (enableLastGc) {
                try {
                    Object lastGcInfo = getLastGcInfoMethod.invoke(gcMxBean);
                    if (lastGcInfo != null) {
                        Map<String, MemoryUsage> usageBeforeGc = (Map<String, MemoryUsage>) getMemoryUsageBeforeGcMethod.invoke(lastGcInfo);
                        Map<String, MemoryUsage> usageAfterGc = (Map<String, MemoryUsage>) getMemoryUsageAfterGcMethod.invoke(lastGcInfo);
                        long startTime = (Long) getStartTimeMethod.invoke(lastGcInfo);
                        long endTime = (Long) getEndTimeMethod.invoke(lastGcInfo);
                        long duration = (Long) getDurationMethod.invoke(lastGcInfo);

                        long previousMemoryUsed = 0;
                        long memoryUsed = 0;
                        long memoryMax = 0;
                        for (Map.Entry<String, MemoryUsage> entry : usageBeforeGc.entrySet()) {
                            previousMemoryUsed += entry.getValue().getUsed();
                        }
                        for (Map.Entry<String, MemoryUsage> entry : usageAfterGc.entrySet()) {
                            MemoryUsage mu = entry.getValue();
                            memoryUsed += mu.getUsed();
                            memoryMax += mu.getMax();
                        }

                        stats.gc.collectors[i].lastGc = new GarbageCollector.LastGc(startTime, endTime, memoryMax, previousMemoryUsed, memoryUsed, duration);
                    }
                } catch (Exception e) {

                }
            }
        }

        return stats;
    }

    
    /** The timestamp. */
    long timestamp = -1;

    
    /** The uptime. */
    long uptime;

    
    /** The mem. */
    Mem mem;

    
    /** The threads. */
    Threads threads;

    
    /** The gc. */
    GarbageCollectors gc;

    
    /**
     * Instantiates a new jvm stats.
     */
    private JvmStats() {
    }

    
    /**
     * Instantiates a new jvm stats.
     *
     * @param timestamp the timestamp
     * @param uptime the uptime
     */
    public JvmStats(long timestamp, long uptime) {
        this.timestamp = timestamp;
        this.uptime = uptime;
    }

    
    /**
     * Timestamp.
     *
     * @return the long
     */
    public long timestamp() {
        return timestamp;
    }

    
    /**
     * Gets the timestamp.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    
    /**
     * Uptime.
     *
     * @return the time value
     */
    public TimeValue uptime() {
        return new TimeValue(uptime);
    }

    
    /**
     * Gets the uptime.
     *
     * @return the uptime
     */
    public TimeValue getUptime() {
        return uptime();
    }

    
    /**
     * Mem.
     *
     * @return the mem
     */
    public Mem mem() {
        return this.mem;
    }

    
    /**
     * Gets the mem.
     *
     * @return the mem
     */
    public Mem getMem() {
        return mem();
    }

    
    /**
     * Threads.
     *
     * @return the threads
     */
    public Threads threads() {
        return threads;
    }

    
    /**
     * Gets the threads.
     *
     * @return the threads
     */
    public Threads getThreads() {
        return threads();
    }

    
    /**
     * Gc.
     *
     * @return the garbage collectors
     */
    public GarbageCollectors gc() {
        return gc;
    }

    
    /**
     * Gets the gc.
     *
     * @return the gc
     */
    public GarbageCollectors getGc() {
        return gc();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
     */
    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(Fields.JVM);
        builder.field(Fields.TIMESTAMP, timestamp);
        builder.field(Fields.UPTIME, uptime().format());
        builder.field(Fields.UPTIME_IN_MILLIS, uptime().millis());
        if (mem != null) {
            builder.startObject(Fields.MEM);
            builder.field(Fields.HEAP_USED, mem.heapUsed().toString());
            builder.field(Fields.HEAP_USED_IN_BYTES, mem.heapUsed().bytes());
            builder.field(Fields.HEAP_COMMITTED, mem.heapCommitted().toString());
            builder.field(Fields.HEAP_COMMITTED_IN_BYTES, mem.heapCommitted().bytes());

            builder.field(Fields.NON_HEAP_USED, mem.nonHeapUsed().toString());
            builder.field(Fields.NON_HEAP_USED_IN_BYTES, mem.nonHeapUsed);
            builder.field(Fields.NON_HEAP_COMMITTED, mem.nonHeapCommitted().toString());
            builder.field(Fields.NON_HEAP_COMMITTED_IN_BYTES, mem.nonHeapCommitted);

            builder.startObject(Fields.POOLS);
            for (MemoryPool pool : mem) {
                builder.startObject(pool.name());
                builder.field(Fields.USED, pool.used().toString());
                builder.field(Fields.USED_IN_BYTES, pool.used);
                builder.field(Fields.MAX, pool.max().toString());
                builder.field(Fields.MAX_IN_BYTES, pool.max);

                builder.field(Fields.PEAK_USED, pool.peakUsed().toString());
                builder.field(Fields.PEAK_USED_IN_BYTES, pool.peakUsed);
                builder.field(Fields.PEAK_MAX, pool.peakMax().toString());
                builder.field(Fields.PEAK_MAX_IN_BYTES, pool.peakMax);

                builder.endObject();
            }
            builder.endObject();

            builder.endObject();
        }
        if (threads != null) {
            builder.startObject(Fields.THREADS);
            builder.field(Fields.COUNT, threads.count());
            builder.field(Fields.PEAK_COUNT, threads.peakCount());
            builder.endObject();
        }
        if (gc != null) {
            builder.startObject(Fields.GC);
            builder.field(Fields.COLLECTION_COUNT, gc.collectionCount());
            builder.field(Fields.COLLECTION_TIME, gc.collectionTime().format());
            builder.field(Fields.COLLECTION_TIME_IN_MILLIS, gc.collectionTime().millis());

            builder.startObject(Fields.COLLECTORS);
            for (GarbageCollector collector : gc) {
                builder.startObject(collector.name());
                builder.field(Fields.COLLECTION_COUNT, collector.collectionCount());
                builder.field(Fields.COLLECTION_TIME, collector.collectionTime().format());
                builder.field(Fields.COLLECTION_TIME_IN_MILLIS, collector.collectionTime().millis());
                builder.endObject();
            }
            builder.endObject();

            builder.endObject();
        }
        builder.endObject();
        return builder;
    }

    
    /**
     * The Class Fields.
     *
     * @author l.xue.nong
     */
    static final class Fields {
        
        
        /** The Constant JVM. */
        static final XContentBuilderString JVM = new XContentBuilderString("jvm");
        
        
        /** The Constant TIMESTAMP. */
        static final XContentBuilderString TIMESTAMP = new XContentBuilderString("timestamp");
        
        
        /** The Constant UPTIME. */
        static final XContentBuilderString UPTIME = new XContentBuilderString("uptime");
        
        
        /** The Constant UPTIME_IN_MILLIS. */
        static final XContentBuilderString UPTIME_IN_MILLIS = new XContentBuilderString("uptime_in_millis");

        
        /** The Constant MEM. */
        static final XContentBuilderString MEM = new XContentBuilderString("mem");
        
        
        /** The Constant HEAP_USED. */
        static final XContentBuilderString HEAP_USED = new XContentBuilderString("heap_used");
        
        
        /** The Constant HEAP_USED_IN_BYTES. */
        static final XContentBuilderString HEAP_USED_IN_BYTES = new XContentBuilderString("heap_used_in_bytes");
        
        
        /** The Constant HEAP_COMMITTED. */
        static final XContentBuilderString HEAP_COMMITTED = new XContentBuilderString("heap_committed");
        
        
        /** The Constant HEAP_COMMITTED_IN_BYTES. */
        static final XContentBuilderString HEAP_COMMITTED_IN_BYTES = new XContentBuilderString("heap_committed_in_bytes");

        
        /** The Constant NON_HEAP_USED. */
        static final XContentBuilderString NON_HEAP_USED = new XContentBuilderString("non_heap_used");
        
        
        /** The Constant NON_HEAP_USED_IN_BYTES. */
        static final XContentBuilderString NON_HEAP_USED_IN_BYTES = new XContentBuilderString("non_heap_used_in_bytes");
        
        
        /** The Constant NON_HEAP_COMMITTED. */
        static final XContentBuilderString NON_HEAP_COMMITTED = new XContentBuilderString("non_heap_committed");
        
        
        /** The Constant NON_HEAP_COMMITTED_IN_BYTES. */
        static final XContentBuilderString NON_HEAP_COMMITTED_IN_BYTES = new XContentBuilderString("non_heap_committed_in_bytes");

        
        /** The Constant POOLS. */
        static final XContentBuilderString POOLS = new XContentBuilderString("pools");
        
        
        /** The Constant USED. */
        static final XContentBuilderString USED = new XContentBuilderString("used");
        
        
        /** The Constant USED_IN_BYTES. */
        static final XContentBuilderString USED_IN_BYTES = new XContentBuilderString("used_in_bytes");
        
        
        /** The Constant MAX. */
        static final XContentBuilderString MAX = new XContentBuilderString("max");
        
        
        /** The Constant MAX_IN_BYTES. */
        static final XContentBuilderString MAX_IN_BYTES = new XContentBuilderString("max_in_bytes");
        
        
        /** The Constant PEAK_USED. */
        static final XContentBuilderString PEAK_USED = new XContentBuilderString("peak_used");
        
        
        /** The Constant PEAK_USED_IN_BYTES. */
        static final XContentBuilderString PEAK_USED_IN_BYTES = new XContentBuilderString("peak_used_in_bytes");
        
        
        /** The Constant PEAK_MAX. */
        static final XContentBuilderString PEAK_MAX = new XContentBuilderString("peak_max");
        
        
        /** The Constant PEAK_MAX_IN_BYTES. */
        static final XContentBuilderString PEAK_MAX_IN_BYTES = new XContentBuilderString("peak_max_in_bytes");

        
        /** The Constant THREADS. */
        static final XContentBuilderString THREADS = new XContentBuilderString("threads");
        
        
        /** The Constant COUNT. */
        static final XContentBuilderString COUNT = new XContentBuilderString("count");
        
        
        /** The Constant PEAK_COUNT. */
        static final XContentBuilderString PEAK_COUNT = new XContentBuilderString("peak_count");

        
        /** The Constant GC. */
        static final XContentBuilderString GC = new XContentBuilderString("gc");
        
        
        /** The Constant COLLECTORS. */
        static final XContentBuilderString COLLECTORS = new XContentBuilderString("collectors");
        
        
        /** The Constant COLLECTION_COUNT. */
        static final XContentBuilderString COLLECTION_COUNT = new XContentBuilderString("collection_count");
        
        
        /** The Constant COLLECTION_TIME. */
        static final XContentBuilderString COLLECTION_TIME = new XContentBuilderString("collection_time");
        
        
        /** The Constant COLLECTION_TIME_IN_MILLIS. */
        static final XContentBuilderString COLLECTION_TIME_IN_MILLIS = new XContentBuilderString("collection_time_in_millis");
    }


    
    /**
     * Read jvm stats.
     *
     * @param in the in
     * @return the jvm stats
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static JvmStats readJvmStats(StreamInput in) throws IOException {
        JvmStats jvmStats = new JvmStats();
        jvmStats.readFrom(in);
        return jvmStats;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        timestamp = in.readVLong();
        uptime = in.readVLong();

        mem = Mem.readMem(in);
        threads = Threads.readThreads(in);
        gc = GarbageCollectors.readGarbageCollectors(in);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeVLong(timestamp);
        out.writeVLong(uptime);

        mem.writeTo(out);
        threads.writeTo(out);
        gc.writeTo(out);
    }

    
    /**
     * The Class GarbageCollectors.
     *
     * @author l.xue.nong
     */
    public static class GarbageCollectors implements Streamable, Serializable, Iterable<GarbageCollector> {

        
        /** The collectors. */
        GarbageCollector[] collectors;

        
        /**
         * Instantiates a new garbage collectors.
         */
        GarbageCollectors() {
        }

        
        /**
         * Read garbage collectors.
         *
         * @param in the in
         * @return the garbage collectors
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static GarbageCollectors readGarbageCollectors(StreamInput in) throws IOException {
            GarbageCollectors collectors = new GarbageCollectors();
            collectors.readFrom(in);
            return collectors;
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
         */
        @Override
        public void readFrom(StreamInput in) throws IOException {
            collectors = new GarbageCollector[in.readVInt()];
            for (int i = 0; i < collectors.length; i++) {
                collectors[i] = GarbageCollector.readGarbageCollector(in);
            }
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
         */
        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVInt(collectors.length);
            for (GarbageCollector gc : collectors) {
                gc.writeTo(out);
            }
        }

        
        /**
         * Collectors.
         *
         * @return the garbage collector[]
         */
        public GarbageCollector[] collectors() {
            return this.collectors;
        }

        
        /* (non-Javadoc)
         * @see java.lang.Iterable#iterator()
         */
        @Override
        public Iterator<GarbageCollector> iterator() {
            return Iterators.forArray(collectors);
        }

        
        /**
         * Collection count.
         *
         * @return the long
         */
        public long collectionCount() {
            long collectionCount = 0;
            for (GarbageCollector gc : collectors) {
                collectionCount += gc.collectionCount();
            }
            return collectionCount;
        }

        
        /**
         * Collection time.
         *
         * @return the time value
         */
        public TimeValue collectionTime() {
            long collectionTime = 0;
            for (GarbageCollector gc : collectors) {
                collectionTime += gc.collectionTime;
            }
            return new TimeValue(collectionTime, TimeUnit.MILLISECONDS);
        }
    }

    
    /**
     * The Class GarbageCollector.
     *
     * @author l.xue.nong
     */
    public static class GarbageCollector implements Streamable, Serializable {

        
        /**
         * The Class LastGc.
         *
         * @author l.xue.nong
         */
        public static class LastGc implements Streamable {

            
            /** The start time. */
            long startTime;
            
            
            /** The end time. */
            long endTime;
            
            
            /** The max. */
            long max;
            
            
            /** The before used. */
            long beforeUsed;
            
            
            /** The after used. */
            long afterUsed;
            
            
            /** The duration. */
            long duration;

            
            /**
             * Instantiates a new last gc.
             */
            LastGc() {
            }

            
            /**
             * Instantiates a new last gc.
             *
             * @param startTime the start time
             * @param endTime the end time
             * @param max the max
             * @param beforeUsed the before used
             * @param afterUsed the after used
             * @param duration the duration
             */
            public LastGc(long startTime, long endTime, long max, long beforeUsed, long afterUsed, long duration) {
                this.startTime = startTime;
                this.endTime = endTime;
                this.max = max;
                this.beforeUsed = beforeUsed;
                this.afterUsed = afterUsed;
                this.duration = duration;
            }

            
            /**
             * Start time.
             *
             * @return the long
             */
            public long startTime() {
                return this.startTime;
            }

            
            /**
             * Gets the start time.
             *
             * @return the start time
             */
            public long getStartTime() {
                return startTime();
            }

            
            /**
             * End time.
             *
             * @return the long
             */
            public long endTime() {
                return this.endTime;
            }

            
            /**
             * Gets the end time.
             *
             * @return the end time
             */
            public long getEndTime() {
                return endTime();
            }

            
            /**
             * Max.
             *
             * @return the byte size value
             */
            public ByteSizeValue max() {
                return new ByteSizeValue(max);
            }

            
            /**
             * Gets the max.
             *
             * @return the max
             */
            public ByteSizeValue getMax() {
                return max();
            }

            
            /**
             * After used.
             *
             * @return the byte size value
             */
            public ByteSizeValue afterUsed() {
                return new ByteSizeValue(afterUsed);
            }

            
            /**
             * Gets the after used.
             *
             * @return the after used
             */
            public ByteSizeValue getAfterUsed() {
                return afterUsed();
            }

            
            /**
             * Before used.
             *
             * @return the byte size value
             */
            public ByteSizeValue beforeUsed() {
                return new ByteSizeValue(beforeUsed);
            }

            
            /**
             * Gets the before used.
             *
             * @return the before used
             */
            public ByteSizeValue getBeforeUsed() {
                return beforeUsed();
            }

            
            /**
             * Reclaimed.
             *
             * @return the byte size value
             */
            public ByteSizeValue reclaimed() {
                return new ByteSizeValue(beforeUsed - afterUsed);
            }

            
            /**
             * Gets the reclaimed.
             *
             * @return the reclaimed
             */
            public ByteSizeValue getReclaimed() {
                return reclaimed();
            }

            
            /**
             * Duration.
             *
             * @return the time value
             */
            public TimeValue duration() {
                return new TimeValue(this.duration);
            }

            
            /**
             * Gets the duration.
             *
             * @return the duration
             */
            public TimeValue getDuration() {
                return duration();
            }

            
            /**
             * Read last gc.
             *
             * @param in the in
             * @return the last gc
             * @throws IOException Signals that an I/O exception has occurred.
             */
            public static LastGc readLastGc(StreamInput in) throws IOException {
                LastGc lastGc = new LastGc();
                lastGc.readFrom(in);
                return lastGc;
            }

            
            /* (non-Javadoc)
             * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
             */
            @Override
            public void readFrom(StreamInput in) throws IOException {
                startTime = in.readVLong();
                endTime = in.readVLong();
                max = in.readVLong();
                beforeUsed = in.readVLong();
                afterUsed = in.readVLong();
                duration = in.readVLong();
            }

            
            /* (non-Javadoc)
             * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
             */
            @Override
            public void writeTo(StreamOutput out) throws IOException {
                out.writeVLong(startTime);
                out.writeVLong(endTime);
                out.writeVLong(max);
                out.writeVLong(beforeUsed);
                out.writeVLong(afterUsed);
                out.writeVLong(duration);
            }
        }

        
        /** The name. */
        String name;
        
        
        /** The collection count. */
        long collectionCount;
        
        
        /** The collection time. */
        long collectionTime;
        
        
        /** The last gc. */
        LastGc lastGc;

        
        /**
         * Instantiates a new garbage collector.
         */
        GarbageCollector() {
        }

        
        /**
         * Read garbage collector.
         *
         * @param in the in
         * @return the garbage collector
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static GarbageCollector readGarbageCollector(StreamInput in) throws IOException {
            GarbageCollector gc = new GarbageCollector();
            gc.readFrom(in);
            return gc;
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
         */
        @Override
        public void readFrom(StreamInput in) throws IOException {
            name = in.readUTF();
            collectionCount = in.readVLong();
            collectionTime = in.readVLong();
            if (in.readBoolean()) {
                lastGc = LastGc.readLastGc(in);
            }
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
         */
        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeUTF(name);
            out.writeVLong(collectionCount);
            out.writeVLong(collectionTime);
            if (lastGc == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                lastGc.writeTo(out);
            }
        }

        
        /**
         * Name.
         *
         * @return the string
         */
        public String name() {
            return name;
        }

        
        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return name();
        }

        
        /**
         * Collection count.
         *
         * @return the long
         */
        public long collectionCount() {
            return collectionCount;
        }

        
        /**
         * Gets the collection count.
         *
         * @return the collection count
         */
        public long getCollectionCount() {
            return collectionCount();
        }

        
        /**
         * Collection time.
         *
         * @return the time value
         */
        public TimeValue collectionTime() {
            return new TimeValue(collectionTime, TimeUnit.MILLISECONDS);
        }

        
        /**
         * Gets the collection time.
         *
         * @return the collection time
         */
        public TimeValue getCollectionTime() {
            return collectionTime();
        }

        
        /**
         * Last gc.
         *
         * @return the last gc
         */
        public LastGc lastGc() {
            return this.lastGc;
        }

        
        /**
         * Gets the last gc.
         *
         * @return the last gc
         */
        public LastGc getLastGc() {
            return lastGc();
        }
    }

    
    /**
     * The Class Threads.
     *
     * @author l.xue.nong
     */
    public static class Threads implements Streamable, Serializable {

        
        /** The count. */
        int count;
        
        
        /** The peak count. */
        int peakCount;

        
        /**
         * Instantiates a new threads.
         */
        Threads() {
        }

        
        /**
         * Count.
         *
         * @return the int
         */
        public int count() {
            return count;
        }

        
        /**
         * Gets the count.
         *
         * @return the count
         */
        public int getCount() {
            return count();
        }

        
        /**
         * Peak count.
         *
         * @return the int
         */
        public int peakCount() {
            return peakCount;
        }

        
        /**
         * Gets the peak count.
         *
         * @return the peak count
         */
        public int getPeakCount() {
            return peakCount();
        }

        
        /**
         * Read threads.
         *
         * @param in the in
         * @return the threads
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Threads readThreads(StreamInput in) throws IOException {
            Threads threads = new Threads();
            threads.readFrom(in);
            return threads;
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
         */
        @Override
        public void readFrom(StreamInput in) throws IOException {
            count = in.readVInt();
            peakCount = in.readVInt();
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
         */
        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVInt(count);
            out.writeVInt(peakCount);
        }
    }

    
    /**
     * The Class MemoryPool.
     *
     * @author l.xue.nong
     */
    public static class MemoryPool implements Streamable, Serializable {

        
        /** The name. */
        String name;
        
        
        /** The used. */
        long used;
        
        
        /** The max. */
        long max;

        
        /** The peak used. */
        long peakUsed;
        
        
        /** The peak max. */
        long peakMax;

        
        /**
         * Instantiates a new memory pool.
         */
        MemoryPool() {

        }

        
        /**
         * Instantiates a new memory pool.
         *
         * @param name the name
         * @param used the used
         * @param max the max
         * @param peakUsed the peak used
         * @param peakMax the peak max
         */
        public MemoryPool(String name, long used, long max, long peakUsed, long peakMax) {
            this.name = name;
            this.used = used;
            this.max = max;
            this.peakUsed = peakUsed;
            this.peakMax = peakMax;
        }

        
        /**
         * Read memory pool.
         *
         * @param in the in
         * @return the memory pool
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static MemoryPool readMemoryPool(StreamInput in) throws IOException {
            MemoryPool pool = new MemoryPool();
            pool.readFrom(in);
            return pool;
        }

        
        /**
         * Name.
         *
         * @return the string
         */
        public String name() {
            return this.name;
        }

        
        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return this.name;
        }

        
        /**
         * Used.
         *
         * @return the byte size value
         */
        public ByteSizeValue used() {
            return new ByteSizeValue(used);
        }

        
        /**
         * Gets the used.
         *
         * @return the used
         */
        public ByteSizeValue getUsed() {
            return used();
        }

        
        /**
         * Max.
         *
         * @return the byte size value
         */
        public ByteSizeValue max() {
            return new ByteSizeValue(max);
        }

        
        /**
         * Gets the max.
         *
         * @return the max
         */
        public ByteSizeValue getMax() {
            return max();
        }

        
        /**
         * Peak used.
         *
         * @return the byte size value
         */
        public ByteSizeValue peakUsed() {
            return new ByteSizeValue(peakUsed);
        }

        
        /**
         * Gets the peak used.
         *
         * @return the peak used
         */
        public ByteSizeValue getPeakUsed() {
            return peakUsed();
        }

        
        /**
         * Peak max.
         *
         * @return the byte size value
         */
        public ByteSizeValue peakMax() {
            return new ByteSizeValue(peakMax);
        }

        
        /**
         * Gets the peak max.
         *
         * @return the peak max
         */
        public ByteSizeValue getPeakMax() {
            return peakMax();
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
         */
        @Override
        public void readFrom(StreamInput in) throws IOException {
            name = in.readUTF();
            used = in.readVLong();
            max = in.readVLong();
            peakUsed = in.readVLong();
            peakMax = in.readVLong();
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
         */
        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeUTF(name);
            out.writeVLong(used);
            out.writeVLong(max);
            out.writeVLong(peakUsed);
            out.writeVLong(peakMax);
        }
    }

    
    /**
     * The Class Mem.
     *
     * @author l.xue.nong
     */
    public static class Mem implements Streamable, Serializable, Iterable<MemoryPool> {

        
        /** The heap committed. */
        long heapCommitted;
        
        
        /** The heap used. */
        long heapUsed;
        
        
        /** The non heap committed. */
        long nonHeapCommitted;
        
        
        /** The non heap used. */
        long nonHeapUsed;

        
        /** The pools. */
        MemoryPool[] pools = new MemoryPool[0];

        
        /**
         * Instantiates a new mem.
         */
        Mem() {
        }

        
        /**
         * Read mem.
         *
         * @param in the in
         * @return the mem
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public static Mem readMem(StreamInput in) throws IOException {
            Mem mem = new Mem();
            mem.readFrom(in);
            return mem;
        }

        
        /* (non-Javadoc)
         * @see java.lang.Iterable#iterator()
         */
        @Override
        public Iterator<MemoryPool> iterator() {
            return Iterators.forArray(pools);
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
         */
        @Override
        public void readFrom(StreamInput in) throws IOException {
            heapCommitted = in.readVLong();
            heapUsed = in.readVLong();
            nonHeapCommitted = in.readVLong();
            nonHeapUsed = in.readVLong();

            pools = new MemoryPool[in.readVInt()];
            for (int i = 0; i < pools.length; i++) {
                pools[i] = MemoryPool.readMemoryPool(in);
            }
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
         */
        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeVLong(heapCommitted);
            out.writeVLong(heapUsed);
            out.writeVLong(nonHeapCommitted);
            out.writeVLong(nonHeapUsed);

            out.writeVInt(pools.length);
            for (MemoryPool pool : pools) {
                pool.writeTo(out);
            }
        }

        
        /**
         * Heap committed.
         *
         * @return the byte size value
         */
        public ByteSizeValue heapCommitted() {
            return new ByteSizeValue(heapCommitted);
        }

        
        /**
         * Gets the heap committed.
         *
         * @return the heap committed
         */
        public ByteSizeValue getHeapCommitted() {
            return heapCommitted();
        }

        
        /**
         * Heap used.
         *
         * @return the byte size value
         */
        public ByteSizeValue heapUsed() {
            return new ByteSizeValue(heapUsed);
        }

        
        /**
         * Gets the heap used.
         *
         * @return the heap used
         */
        public ByteSizeValue getHeapUsed() {
            return heapUsed();
        }

        
        /**
         * Non heap committed.
         *
         * @return the byte size value
         */
        public ByteSizeValue nonHeapCommitted() {
            return new ByteSizeValue(nonHeapCommitted);
        }

        
        /**
         * Gets the non heap committed.
         *
         * @return the non heap committed
         */
        public ByteSizeValue getNonHeapCommitted() {
            return nonHeapCommitted();
        }

        
        /**
         * Non heap used.
         *
         * @return the byte size value
         */
        public ByteSizeValue nonHeapUsed() {
            return new ByteSizeValue(nonHeapUsed);
        }

        
        /**
         * Gets the non heap used.
         *
         * @return the non heap used
         */
        public ByteSizeValue getNonHeapUsed() {
            return nonHeapUsed();
        }
    }
}
