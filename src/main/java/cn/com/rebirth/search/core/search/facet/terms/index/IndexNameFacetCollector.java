/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexNameFacetCollector.java 2012-3-29 15:01:40 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.terms.index;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.terms.TermsFacet;
import cn.com.rebirth.search.core.search.facet.terms.strings.InternalStringTermsFacet;

import com.google.common.collect.Sets;


/**
 * The Class IndexNameFacetCollector.
 *
 * @author l.xue.nong
 */
public class IndexNameFacetCollector extends AbstractFacetCollector {

	
	/** The index name. */
	private final String indexName;

	
	/** The comparator type. */
	private final InternalStringTermsFacet.ComparatorType comparatorType;

	
	/** The size. */
	private final int size;

	
	/** The count. */
	private int count = 0;

	
	/**
	 * Instantiates a new index name facet collector.
	 *
	 * @param facetName the facet name
	 * @param indexName the index name
	 * @param comparatorType the comparator type
	 * @param size the size
	 */
	public IndexNameFacetCollector(String facetName, String indexName, TermsFacet.ComparatorType comparatorType,
			int size) {
		super(facetName);
		this.indexName = indexName;
		this.comparatorType = comparatorType;
		this.size = size;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		count++;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		return new InternalStringTermsFacet(facetName, comparatorType, size,
				Sets.newHashSet(new InternalStringTermsFacet.StringEntry(indexName, count)), 0, count);
	}
}
