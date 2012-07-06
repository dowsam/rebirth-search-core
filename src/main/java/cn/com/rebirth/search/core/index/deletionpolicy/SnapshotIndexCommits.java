/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SnapshotIndexCommits.java 2012-7-6 14:29:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.deletionpolicy;

import java.util.Iterator;
import java.util.List;

import cn.com.rebirth.search.commons.lease.Releasable;

/**
 * The Class SnapshotIndexCommits.
 *
 * @author l.xue.nong
 */
public class SnapshotIndexCommits implements Iterable<SnapshotIndexCommit>, Releasable {

	/** The commits. */
	private final List<SnapshotIndexCommit> commits;

	/**
	 * Instantiates a new snapshot index commits.
	 *
	 * @param commits the commits
	 */
	public SnapshotIndexCommits(List<SnapshotIndexCommit> commits) {
		this.commits = commits;
	}

	/**
	 * Size.
	 *
	 * @return the int
	 */
	public int size() {
		return commits.size();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<SnapshotIndexCommit> iterator() {
		return commits.iterator();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.lease.Releasable#release()
	 */
	public boolean release() {
		boolean result = false;
		for (SnapshotIndexCommit snapshot : commits) {
			result |= snapshot.release();
		}
		return result;
	}
}
