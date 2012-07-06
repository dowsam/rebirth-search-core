/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NestedChildrenCollector.java 2012-3-29 15:02:28 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.search.nested;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.util.FixedBitSet;

import cn.com.rebirth.search.commons.lucene.docset.DocSet;
import cn.com.rebirth.search.commons.lucene.docset.DocSets;
import cn.com.rebirth.search.commons.lucene.docset.FixedBitDocSet;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetCollector;


/**
 * The Class NestedChildrenCollector.
 *
 * @author l.xue.nong
 */
public class NestedChildrenCollector extends FacetCollector {

	
	/** The collector. */
	private final FacetCollector collector;

	
	/** The parent filter. */
	private final Filter parentFilter;

	
	/** The child filter. */
	private final Filter childFilter;

	
	/** The child docs. */
	private DocSet childDocs;

	
	/** The parent docs. */
	private FixedBitSet parentDocs;

	
	/** The current reader. */
	private IndexReader currentReader;

	
	/**
	 * Instantiates a new nested children collector.
	 *
	 * @param collector the collector
	 * @param parentFilter the parent filter
	 * @param childFilter the child filter
	 */
	public NestedChildrenCollector(FacetCollector collector, Filter parentFilter, Filter childFilter) {
		this.collector = collector;
		this.parentFilter = parentFilter;
		this.childFilter = childFilter;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		return collector.facet();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetCollector#setFilter(org.apache.lucene.search.Filter)
	 */
	@Override
	public void setFilter(Filter filter) {
		
		collector.setFilter(filter);
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Collector#setScorer(org.apache.lucene.search.Scorer)
	 */
	@Override
	public void setScorer(Scorer scorer) throws IOException {
		collector.setScorer(scorer);
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Collector#setNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		collector.setNextReader(reader, docBase);
		currentReader = reader;
		childDocs = DocSets.convert(reader, childFilter.getDocIdSet(reader));
		parentDocs = ((FixedBitDocSet) parentFilter.getDocIdSet(reader)).set();
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Collector#acceptsDocsOutOfOrder()
	 */
	@Override
	public boolean acceptsDocsOutOfOrder() {
		return collector.acceptsDocsOutOfOrder();
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Collector#collect(int)
	 */
	@Override
	public void collect(int parentDoc) throws IOException {
		if (parentDoc == 0) {
			return;
		}
		int prevParentDoc = parentDocs.prevSetBit(parentDoc - 1);
		for (int i = (parentDoc - 1); i > prevParentDoc; i--) {
			if (!currentReader.isDeleted(i) && childDocs.get(i)) {
				collector.collect(i);
			}
		}
	}
}