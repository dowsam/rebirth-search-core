/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RecoveryStatus.java 2012-3-29 15:01:30 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.gateway;

import java.util.concurrent.atomic.AtomicLong;


/**
 * The Class RecoveryStatus.
 *
 * @author l.xue.nong
 */
public class RecoveryStatus {

    
    /**
     * The Enum Stage.
     *
     * @author l.xue.nong
     */
    public static enum Stage {
        
        
        /** The INIT. */
        INIT,
        
        
        /** The INDEX. */
        INDEX,
        
        
        /** The START. */
        START,
        
        
        /** The TRANSLOG. */
        TRANSLOG,
        
        
        /** The DONE. */
        DONE
    }

    
    /** The stage. */
    private Stage stage = Stage.INIT;

    
    /** The start time. */
    private long startTime = System.currentTimeMillis();

    
    /** The time. */
    private long time;

    
    /** The index. */
    private Index index = new Index();

    
    /** The translog. */
    private Translog translog = new Translog();

    
    /** The start. */
    private Start start = new Start();

    
    /**
     * Stage.
     *
     * @return the stage
     */
    public Stage stage() {
        return this.stage;
    }

    
    /**
     * Update stage.
     *
     * @param stage the stage
     * @return the recovery status
     */
    public RecoveryStatus updateStage(Stage stage) {
        this.stage = stage;
        return this;
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
     * Start time.
     *
     * @param startTime the start time
     */
    public void startTime(long startTime) {
        this.startTime = startTime;
    }

    
    /**
     * Time.
     *
     * @return the long
     */
    public long time() {
        return this.time;
    }

    
    /**
     * Time.
     *
     * @param time the time
     */
    public void time(long time) {
        this.time = time;
    }

    
    /**
     * Index.
     *
     * @return the index
     */
    public Index index() {
        return index;
    }

    
    /**
     * Start.
     *
     * @return the start
     */
    public Start start() {
        return this.start;
    }

    
    /**
     * Translog.
     *
     * @return the translog
     */
    public Translog translog() {
        return translog;
    }

    
    /**
     * The Class Start.
     *
     * @author l.xue.nong
     */
    public static class Start {
        
        
        /** The start time. */
        private long startTime;
        
        
        /** The time. */
        private long time;
        
        
        /** The check index time. */
        private long checkIndexTime;

        
        /**
         * Start time.
         *
         * @return the long
         */
        public long startTime() {
            return this.startTime;
        }

        
        /**
         * Start time.
         *
         * @param startTime the start time
         */
        public void startTime(long startTime) {
            this.startTime = startTime;
        }

        
        /**
         * Time.
         *
         * @return the long
         */
        public long time() {
            return this.time;
        }

        
        /**
         * Time.
         *
         * @param time the time
         */
        public void time(long time) {
            this.time = time;
        }

        
        /**
         * Check index time.
         *
         * @return the long
         */
        public long checkIndexTime() {
            return checkIndexTime;
        }

        
        /**
         * Check index time.
         *
         * @param checkIndexTime the check index time
         */
        public void checkIndexTime(long checkIndexTime) {
            this.checkIndexTime = checkIndexTime;
        }
    }

    
    /**
     * The Class Translog.
     *
     * @author l.xue.nong
     */
    public static class Translog {
        
        
        /** The start time. */
        private long startTime = 0;
        
        
        /** The time. */
        private long time;
        
        
        /** The current translog operations. */
        private volatile int currentTranslogOperations = 0;

        
        /**
         * Start time.
         *
         * @return the long
         */
        public long startTime() {
            return this.startTime;
        }

        
        /**
         * Start time.
         *
         * @param startTime the start time
         */
        public void startTime(long startTime) {
            this.startTime = startTime;
        }

        
        /**
         * Time.
         *
         * @return the long
         */
        public long time() {
            return this.time;
        }

        
        /**
         * Time.
         *
         * @param time the time
         */
        public void time(long time) {
            this.time = time;
        }

        
        /**
         * Adds the translog operations.
         *
         * @param count the count
         */
        public void addTranslogOperations(int count) {
            this.currentTranslogOperations += count;
        }

        
        /**
         * Current translog operations.
         *
         * @return the int
         */
        public int currentTranslogOperations() {
            return this.currentTranslogOperations;
        }
    }

    
    /**
     * The Class Index.
     *
     * @author l.xue.nong
     */
    public static class Index {
        
        
        /** The start time. */
        private long startTime = 0;
        
        
        /** The time. */
        private long time = 0;

        
        /** The version. */
        private long version = -1;
        
        
        /** The number of files. */
        private int numberOfFiles = 0;
        
        
        /** The total size. */
        private long totalSize = 0;
        
        
        /** The number of reused files. */
        private int numberOfReusedFiles = 0;
        
        
        /** The reused total size. */
        private long reusedTotalSize = 0;
        
        
        /** The current files size. */
        private AtomicLong currentFilesSize = new AtomicLong();

        
        /**
         * Start time.
         *
         * @return the long
         */
        public long startTime() {
            return this.startTime;
        }

        
        /**
         * Start time.
         *
         * @param startTime the start time
         */
        public void startTime(long startTime) {
            this.startTime = startTime;
        }

        
        /**
         * Time.
         *
         * @return the long
         */
        public long time() {
            return this.time;
        }

        
        /**
         * Time.
         *
         * @param time the time
         */
        public void time(long time) {
            this.time = time;
        }

        
        /**
         * Version.
         *
         * @return the long
         */
        public long version() {
            return this.version;
        }

        
        /**
         * Files.
         *
         * @param numberOfFiles the number of files
         * @param totalSize the total size
         * @param numberOfReusedFiles the number of reused files
         * @param reusedTotalSize the reused total size
         */
        public void files(int numberOfFiles, long totalSize, int numberOfReusedFiles, long reusedTotalSize) {
            this.numberOfFiles = numberOfFiles;
            this.totalSize = totalSize;
            this.numberOfReusedFiles = numberOfReusedFiles;
            this.reusedTotalSize = reusedTotalSize;
        }

        
        /**
         * Number of files.
         *
         * @return the int
         */
        public int numberOfFiles() {
            return numberOfFiles;
        }

        
        /**
         * Number of recovered files.
         *
         * @return the int
         */
        public int numberOfRecoveredFiles() {
            return numberOfFiles - numberOfReusedFiles;
        }

        
        /**
         * Total size.
         *
         * @return the long
         */
        public long totalSize() {
            return this.totalSize;
        }

        
        /**
         * Number of reused files.
         *
         * @return the int
         */
        public int numberOfReusedFiles() {
            return numberOfReusedFiles;
        }

        
        /**
         * Reused total size.
         *
         * @return the long
         */
        public long reusedTotalSize() {
            return this.reusedTotalSize;
        }

        
        /**
         * Recovered total size.
         *
         * @return the long
         */
        public long recoveredTotalSize() {
            return totalSize - reusedTotalSize;
        }

        
        /**
         * Update version.
         *
         * @param version the version
         */
        public void updateVersion(long version) {
            this.version = version;
        }

        
        /**
         * Current files size.
         *
         * @return the long
         */
        public long currentFilesSize() {
            return this.currentFilesSize.get();
        }

        
        /**
         * Adds the current files size.
         *
         * @param updatedSize the updated size
         */
        public void addCurrentFilesSize(long updatedSize) {
            this.currentFilesSize.addAndGet(updatedSize);
        }
    }
}
