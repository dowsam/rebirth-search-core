/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DocsStatus.java 2012-7-6 14:30:22 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.status;

/**
 * The Class DocsStatus.
 *
 * @author l.xue.nong
 */
public class DocsStatus {

	/** The num docs. */
	long numDocs = 0;

	/** The max doc. */
	long maxDoc = 0;

	/** The deleted docs. */
	long deletedDocs = 0;

	/**
	 * Num docs.
	 *
	 * @return the long
	 */
	public long numDocs() {
		return numDocs;
	}

	/**
	 * Gets the num docs.
	 *
	 * @return the num docs
	 */
	public long getNumDocs() {
		return numDocs();
	}

	/**
	 * Max doc.
	 *
	 * @return the long
	 */
	public long maxDoc() {
		return maxDoc;
	}

	/**
	 * Gets the max doc.
	 *
	 * @return the max doc
	 */
	public long getMaxDoc() {
		return maxDoc();
	}

	/**
	 * Deleted docs.
	 *
	 * @return the long
	 */
	public long deletedDocs() {
		return deletedDocs;
	}

	/**
	 * Gets the deleted docs.
	 *
	 * @return the deleted docs
	 */
	public long getDeletedDocs() {
		return deletedDocs();
	}
}
