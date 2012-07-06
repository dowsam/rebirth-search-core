/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SnapshotDeletionPolicy.java 2012-3-29 15:01:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.deletionpolicy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexDeletionPolicy;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.name.Named;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.IndexShardComponent;


/**
 * The Class SnapshotDeletionPolicy.
 *
 * @author l.xue.nong
 */
public class SnapshotDeletionPolicy extends AbstractIndexShardComponent implements IndexDeletionPolicy {

    
    /** The primary. */
    private final IndexDeletionPolicy primary;

    
    /** The snapshots. */
    private ConcurrentMap<Long, SnapshotHolder> snapshots = new ConcurrentHashMap<Long, SnapshotHolder>();

    
    /** The commits. */
    private volatile List<SnapshotIndexCommit> commits;

    
    /** The mutex. */
    private final Object mutex = new Object();

    
    /** The last commit. */
    private SnapshotIndexCommit lastCommit;

    
    /**
     * Instantiates a new snapshot deletion policy.
     *
     * @param primary the primary
     */
    @Inject
    public SnapshotDeletionPolicy(@Named("actual") IndexDeletionPolicy primary) {
        super(((IndexShardComponent) primary).shardId(), ((IndexShardComponent) primary).indexSettings());
        this.primary = primary;
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.index.IndexDeletionPolicy#onInit(java.util.List)
     */
    public void onInit(List<? extends IndexCommit> commits) throws IOException {
        onCommit(commits);
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.index.IndexDeletionPolicy#onCommit(java.util.List)
     */
    public void onCommit(List<? extends IndexCommit> commits) throws IOException {
        synchronized (mutex) {
            List<SnapshotIndexCommit> snapshotCommits = wrapCommits(commits);
            primary.onCommit(snapshotCommits);

            
            for (Iterator<SnapshotHolder> it = snapshots.values().iterator(); it.hasNext(); ) {
                SnapshotHolder holder = it.next();
                if (holder.counter <= 0) {
                    it.remove();
                }
            }
            
            List<SnapshotIndexCommit> newCommits = new ArrayList<SnapshotIndexCommit>();
            for (SnapshotIndexCommit commit : snapshotCommits) {
                if (!commit.isDeleted()) {
                    newCommits.add(commit);
                }
            }
            this.commits = newCommits;
            
            this.lastCommit = newCommits.get(newCommits.size() - 1);
        }
    }

    
    /**
     * Snapshots.
     *
     * @return the snapshot index commits
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public SnapshotIndexCommits snapshots() throws IOException {
        synchronized (mutex) {
            if (snapshots == null) {
                throw new IllegalStateException("Snapshot deletion policy has not been init yet...");
            }
            List<SnapshotIndexCommit> result = new ArrayList<SnapshotIndexCommit>(commits.size());
            for (SnapshotIndexCommit commit : commits) {
                result.add(snapshot(commit));
            }
            return new SnapshotIndexCommits(result);
        }
    }

    
    /**
     * Snapshot.
     *
     * @return the snapshot index commit
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public SnapshotIndexCommit snapshot() throws IOException {
        synchronized (mutex) {
            if (lastCommit == null) {
                throw new IllegalStateException("Snapshot deletion policy has not been init yet...");
            }
            return snapshot(lastCommit);
        }
    }

    
    /**
     * Snapshot.
     *
     * @param commit the commit
     * @return the snapshot index commit
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private SnapshotIndexCommit snapshot(SnapshotIndexCommit commit) throws IOException {
        SnapshotHolder snapshotHolder = snapshots.get(commit.getVersion());
        if (snapshotHolder == null) {
            snapshotHolder = new SnapshotHolder(0);
            snapshots.put(commit.getVersion(), snapshotHolder);
        }
        snapshotHolder.counter++;
        return new OneTimeReleaseSnapshotIndexCommit(this, commit);
    }

    
    /**
     * Checks if is held.
     *
     * @param version the version
     * @return true, if is held
     */
    boolean isHeld(long version) {
        SnapshotDeletionPolicy.SnapshotHolder holder = snapshots.get(version);
        return holder != null && holder.counter > 0;
    }

    
    /**
     * Release.
     *
     * @param version the version
     * @return true, if successful
     */
    boolean release(long version) {
        synchronized (mutex) {
            SnapshotDeletionPolicy.SnapshotHolder holder = snapshots.get(version);
            if (holder == null) {
                return false;
            }
            if (holder.counter <= 0) {
                snapshots.remove(version);
                return false;
            }
            if (--holder.counter == 0) {
                snapshots.remove(version);
            }
            return true;
        }
    }

    
    /**
     * The Class OneTimeReleaseSnapshotIndexCommit.
     *
     * @author l.xue.nong
     */
    private static class OneTimeReleaseSnapshotIndexCommit extends SnapshotIndexCommit {
        
        
        /** The released. */
        private volatile boolean released = false;

        
        /**
         * Instantiates a new one time release snapshot index commit.
         *
         * @param deletionPolicy the deletion policy
         * @param cp the cp
         * @throws IOException Signals that an I/O exception has occurred.
         */
        OneTimeReleaseSnapshotIndexCommit(SnapshotDeletionPolicy deletionPolicy, IndexCommit cp) throws IOException {
            super(deletionPolicy, cp);
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.core.index.deletionpolicy.SnapshotIndexCommit#release()
         */
        @Override
        public boolean release() {
            if (released) {
                return false;
            }
            released = true;
            return ((SnapshotIndexCommit) delegate).release();
        }
    }

    
    /**
     * The Class SnapshotHolder.
     *
     * @author l.xue.nong
     */
    private static class SnapshotHolder {
        
        
        /** The counter. */
        int counter;

        
        /**
         * Instantiates a new snapshot holder.
         *
         * @param counter the counter
         */
        private SnapshotHolder(int counter) {
            this.counter = counter;
        }
    }

    
    /**
     * Wrap commits.
     *
     * @param commits the commits
     * @return the list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private List<SnapshotIndexCommit> wrapCommits(List<? extends IndexCommit> commits) throws IOException {
        final int count = commits.size();
        List<SnapshotIndexCommit> snapshotCommits = new ArrayList<SnapshotIndexCommit>(count);
        for (int i = 0; i < count; i++)
            snapshotCommits.add(new SnapshotIndexCommit(this, commits.get(i)));
        return snapshotCommits;
    }
}
