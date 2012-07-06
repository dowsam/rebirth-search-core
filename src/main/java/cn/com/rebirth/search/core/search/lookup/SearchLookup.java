/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SearchLookup.java 2012-3-29 15:01:10 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.lookup;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.mapper.MapperService;

import com.google.common.collect.ImmutableMap;


/**
 * The Class SearchLookup.
 *
 * @author l.xue.nong
 */
public class SearchLookup {

	
	/** The doc map. */
	final DocLookup docMap;

	
	/** The source lookup. */
	final SourceLookup sourceLookup;

	
	/** The fields lookup. */
	final FieldsLookup fieldsLookup;

	
	/** The as map. */
	final ImmutableMap<String, Object> asMap;

	
	/**
	 * Instantiates a new search lookup.
	 *
	 * @param mapperService the mapper service
	 * @param fieldDataCache the field data cache
	 */
	public SearchLookup(MapperService mapperService, FieldDataCache fieldDataCache) {
		docMap = new DocLookup(mapperService, fieldDataCache);
		sourceLookup = new SourceLookup();
		fieldsLookup = new FieldsLookup(mapperService);
		asMap = ImmutableMap.<String, Object> of("doc", docMap, "_doc", docMap, "_source", sourceLookup, "_fields",
				fieldsLookup);
	}

	
	/**
	 * As map.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, Object> asMap() {
		return this.asMap;
	}

	
	/**
	 * Source.
	 *
	 * @return the source lookup
	 */
	public SourceLookup source() {
		return this.sourceLookup;
	}

	
	/**
	 * Fields.
	 *
	 * @return the fields lookup
	 */
	public FieldsLookup fields() {
		return this.fieldsLookup;
	}

	
	/**
	 * Doc.
	 *
	 * @return the doc lookup
	 */
	public DocLookup doc() {
		return this.docMap;
	}

	
	/**
	 * Sets the scorer.
	 *
	 * @param scorer the new scorer
	 */
	public void setScorer(Scorer scorer) {
		docMap.setScorer(scorer);
	}

	
	/**
	 * Sets the next reader.
	 *
	 * @param reader the new next reader
	 */
	public void setNextReader(IndexReader reader) {
		docMap.setNextReader(reader);
		sourceLookup.setNextReader(reader);
		fieldsLookup.setNextReader(reader);
	}

	
	/**
	 * Sets the next doc id.
	 *
	 * @param docId the new next doc id
	 */
	public void setNextDocId(int docId) {
		docMap.setNextDocId(docId);
		sourceLookup.setNextDocId(docId);
		fieldsLookup.setNextDocId(docId);
	}
}
