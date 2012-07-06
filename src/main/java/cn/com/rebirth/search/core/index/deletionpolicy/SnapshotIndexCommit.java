/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SnapshotIndexCommit.java 2012-3-29 15:01:02 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.deletionpolicy;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.index.IndexCommit;

import cn.com.rebirth.search.commons.lease.Releasable;
import cn.com.rebirth.search.commons.lucene.IndexCommitDelegate;


/**
 * The Class SnapshotIndexCommit.
 *
 * @author l.xue.nong
 */
public class SnapshotIndexCommit extends IndexCommitDelegate implements Releasable {

	
	/** The deletion policy. */
	private final SnapshotDeletionPolicy deletionPolicy;

	
	/** The files. */
	private final String[] files;

	
	/**
	 * Instantiates a new snapshot index commit.
	 *
	 * @param deletionPolicy the deletion policy
	 * @param cp the cp
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	SnapshotIndexCommit(SnapshotDeletionPolicy deletionPolicy, IndexCommit cp) throws IOException {
		super(cp);
		this.deletionPolicy = deletionPolicy;
		ArrayList<String> tmpFiles = new ArrayList<String>();
		for (String o : cp.getFileNames()) {
			tmpFiles.add(o);
		}
		files = tmpFiles.toArray(new String[tmpFiles.size()]);
	}

	
	/**
	 * Gets the files.
	 *
	 * @return the files
	 */
	public String[] getFiles() {
		return files;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.lease.Releasable#release()
	 */
	public boolean release() {
		return deletionPolicy.release(getVersion());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.lucene.IndexCommitDelegate#delete()
	 */
	@Override
	public void delete() {
		if (!deletionPolicy.isHeld(getVersion())) {
			delegate.delete();
		}
	}
}
