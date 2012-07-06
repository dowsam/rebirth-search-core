/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ChildCollector.java 2012-3-29 15:01:29 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.search.child;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.util.FixedBitSet;

import cn.com.rebirth.commons.collect.Tuple;
import cn.com.rebirth.search.commons.BytesWrap;
import cn.com.rebirth.search.core.index.cache.id.IdReaderTypeCache;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class ChildCollector.
 *
 * @author l.xue.nong
 */
public class ChildCollector extends Collector {

	
	/** The parent type. */
	private final String parentType;

	
	/** The context. */
	private final SearchContext context;

	
	/** The readers. */
	private final Tuple<IndexReader, IdReaderTypeCache>[] readers;

	
	/** The parent docs. */
	private final Map<Object, FixedBitSet> parentDocs;

	
	/** The type cache. */
	private IdReaderTypeCache typeCache;

	
	/**
	 * Instantiates a new child collector.
	 *
	 * @param parentType the parent type
	 * @param context the context
	 */
	public ChildCollector(String parentType, SearchContext context) {
		this.parentType = parentType;
		this.context = context;
		this.parentDocs = new HashMap<Object, FixedBitSet>();

		
		this.readers = new Tuple[context.searcher().subReaders().length];
		for (int i = 0; i < readers.length; i++) {
			IndexReader reader = context.searcher().subReaders()[i];
			readers[i] = new Tuple<IndexReader, IdReaderTypeCache>(reader, context.idCache().reader(reader)
					.type(parentType));
		}
	}

	
	/**
	 * Parent docs.
	 *
	 * @return the map
	 */
	public Map<Object, FixedBitSet> parentDocs() {
		return this.parentDocs;
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Collector#setScorer(org.apache.lucene.search.Scorer)
	 */
	@Override
	public void setScorer(Scorer scorer) throws IOException {

	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Collector#collect(int)
	 */
	@Override
	public void collect(int doc) throws IOException {
		BytesWrap parentId = typeCache.parentIdByDoc(doc);
		if (parentId == null) {
			return;
		}
		for (Tuple<IndexReader, IdReaderTypeCache> tuple : readers) {
			IndexReader indexReader = tuple.v1();
			IdReaderTypeCache idReaderTypeCache = tuple.v2();
			if (idReaderTypeCache == null) { 
				continue;
			}
			int parentDocId = idReaderTypeCache.docById(parentId);
			if (parentDocId != -1 && !indexReader.isDeleted(parentDocId)) {
				FixedBitSet docIdSet = parentDocs().get(indexReader.getCoreCacheKey());
				if (docIdSet == null) {
					docIdSet = new FixedBitSet(indexReader.maxDoc());
					parentDocs.put(indexReader.getCoreCacheKey(), docIdSet);
				}
				docIdSet.set(parentDocId);
				return;
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Collector#setNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		typeCache = context.idCache().reader(reader).type(parentType);
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Collector#acceptsDocsOutOfOrder()
	 */
	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}
}
