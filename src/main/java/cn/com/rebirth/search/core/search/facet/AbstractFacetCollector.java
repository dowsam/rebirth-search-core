/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AbstractFacetCollector.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.search.commons.lucene.docset.DocSet;
import cn.com.rebirth.search.commons.lucene.docset.DocSets;
import cn.com.rebirth.search.commons.lucene.search.AndFilter;

import com.google.common.collect.ImmutableList;

/**
 * The Class AbstractFacetCollector.
 *
 * @author l.xue.nong
 */
public abstract class AbstractFacetCollector extends FacetCollector {

	/** The facet name. */
	protected final String facetName;

	/** The filter. */
	protected Filter filter;

	/** The doc set. */
	private DocSet docSet = null;

	/**
	 * Instantiates a new abstract facet collector.
	 *
	 * @param facetName the facet name
	 */
	public AbstractFacetCollector(String facetName) {
		this.facetName = facetName;
	}

	/**
	 * Gets the filter.
	 *
	 * @return the filter
	 */
	public Filter getFilter() {
		return this.filter;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetCollector#setFilter(org.apache.lucene.search.Filter)
	 */
	@Override
	public void setFilter(Filter filter) {
		if (this.filter == null) {
			this.filter = filter;
		} else {
			this.filter = new AndFilter(ImmutableList.of(filter, this.filter));
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Collector#setScorer(org.apache.lucene.search.Scorer)
	 */
	@Override
	public void setScorer(Scorer scorer) throws IOException {

	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Collector#acceptsDocsOutOfOrder()
	 */
	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Collector#setNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		if (filter != null) {
			docSet = DocSets.convert(reader, filter.getDocIdSet(reader));
		}
		doSetNextReader(reader, docBase);
	}

	/**
	 * Do set next reader.
	 *
	 * @param reader the reader
	 * @param docBase the doc base
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected abstract void doSetNextReader(IndexReader reader, int docBase) throws IOException;

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Collector#collect(int)
	 */
	@Override
	public void collect(int doc) throws IOException {
		if (docSet == null) {
			doCollect(doc);
		} else if (docSet.get(doc)) {
			doCollect(doc);
		}
	}

	/**
	 * Do collect.
	 *
	 * @param doc the doc
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected abstract void doCollect(int doc) throws IOException;
}
