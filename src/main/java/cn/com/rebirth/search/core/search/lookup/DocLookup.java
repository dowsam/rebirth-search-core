/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DocLookup.java 2012-7-6 14:29:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.lookup;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.DocFieldData;
import cn.com.rebirth.search.core.index.field.data.FieldData;
import cn.com.rebirth.search.core.index.field.data.NumericDocFieldData;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;

import com.google.common.collect.Maps;

/**
 * The Class DocLookup.
 *
 * @author l.xue.nong
 */
public class DocLookup implements Map {

	/** The local cache field data. */
	private final Map<String, FieldData> localCacheFieldData = Maps.newHashMapWithExpectedSize(4);

	/** The mapper service. */
	private final MapperService mapperService;

	/** The field data cache. */
	private final FieldDataCache fieldDataCache;

	/** The reader. */
	private IndexReader reader;

	/** The scorer. */
	private Scorer scorer;

	/** The doc id. */
	private int docId = -1;

	/**
	 * Instantiates a new doc lookup.
	 *
	 * @param mapperService the mapper service
	 * @param fieldDataCache the field data cache
	 */
	DocLookup(MapperService mapperService, FieldDataCache fieldDataCache) {
		this.mapperService = mapperService;
		this.fieldDataCache = fieldDataCache;
	}

	/**
	 * Mapper service.
	 *
	 * @return the mapper service
	 */
	public MapperService mapperService() {
		return this.mapperService;
	}

	/**
	 * Field data cache.
	 *
	 * @return the field data cache
	 */
	public FieldDataCache fieldDataCache() {
		return this.fieldDataCache;
	}

	/**
	 * Sets the next reader.
	 *
	 * @param reader the new next reader
	 */
	public void setNextReader(IndexReader reader) {
		if (this.reader == reader) {
			return;
		}
		this.reader = reader;
		this.docId = -1;
		localCacheFieldData.clear();
	}

	/**
	 * Sets the scorer.
	 *
	 * @param scorer the new scorer
	 */
	public void setScorer(Scorer scorer) {
		this.scorer = scorer;
	}

	/**
	 * Sets the next doc id.
	 *
	 * @param docId the new next doc id
	 */
	public void setNextDocId(int docId) {
		this.docId = docId;
	}

	/**
	 * Field.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the t
	 */
	public <T extends DocFieldData> T field(String key) {
		return (T) get(key);
	}

	/**
	 * Numeric.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the t
	 */
	public <T extends NumericDocFieldData> T numeric(String key) {
		return (T) get(key);
	}

	/**
	 * Score.
	 *
	 * @return the float
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public float score() throws IOException {
		return scorer.score();
	}

	/**
	 * Gets the score.
	 *
	 * @return the score
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public float getScore() throws IOException {
		return scorer.score();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	@Override
	public Object get(Object key) {

		String fieldName = key.toString();
		FieldData fieldData = localCacheFieldData.get(fieldName);
		if (fieldData == null) {
			FieldMapper mapper = mapperService.smartNameFieldMapper(fieldName);
			if (mapper == null) {
				throw new RebirthIllegalArgumentException("No field found for [" + fieldName + "]");
			}
			try {
				fieldData = fieldDataCache.cache(mapper.fieldDataType(), reader, mapper.names().indexName());
			} catch (IOException e) {
				throw new RebirthException("Failed to load field data for [" + fieldName + "]", e);
			}
			localCacheFieldData.put(fieldName, fieldData);
		}
		return fieldData.docFieldData(docId);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key) {

		String fieldName = key.toString();
		FieldData fieldData = localCacheFieldData.get(fieldName);
		if (fieldData == null) {
			FieldMapper mapper = mapperService.smartNameFieldMapper(fieldName);
			if (mapper == null) {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Map#size()
	 */
	public int size() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	public Object put(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public Object remove(Object key) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(Map m) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#keySet()
	 */
	public Set keySet() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#values()
	 */
	public Collection values() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#entrySet()
	 */
	public Set entrySet() {
		throw new UnsupportedOperationException();
	}
}
