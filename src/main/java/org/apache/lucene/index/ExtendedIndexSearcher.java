/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ExtendedIndexSearcher.java 2012-7-6 14:29:50 l.xue.nong$$
 */

package org.apache.lucene.index;

import org.apache.lucene.search.IndexSearcher;

/**
 * The Class ExtendedIndexSearcher.
 *
 * @author l.xue.nong
 */
public class ExtendedIndexSearcher extends IndexSearcher {

	/**
	 * Instantiates a new extended index searcher.
	 *
	 * @param searcher the searcher
	 */
	public ExtendedIndexSearcher(ExtendedIndexSearcher searcher) {
		super(searcher.getIndexReader(), searcher.subReaders(), searcher.docStarts());
		setSimilarity(searcher.getSimilarity());
	}

	/**
	 * Instantiates a new extended index searcher.
	 *
	 * @param r the r
	 */
	public ExtendedIndexSearcher(IndexReader r) {
		super(r);
	}

	/**
	 * Sub readers.
	 *
	 * @return the index reader[]
	 */
	public IndexReader[] subReaders() {
		return this.subReaders;
	}

	/**
	 * Doc starts.
	 *
	 * @return the int[]
	 */
	public int[] docStarts() {
		return this.docStarts;
	}

	/**
	 * Reader index.
	 *
	 * @param doc the doc
	 * @return the int
	 */
	public int readerIndex(int doc) {
		return DirectoryReader.readerIndex(doc, docStarts, subReaders.length);
	}
}
