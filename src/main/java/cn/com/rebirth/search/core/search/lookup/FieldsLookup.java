/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FieldsLookup.java 2012-3-29 15:01:19 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.lookup;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.exception.RestartParseException;
import cn.com.rebirth.search.commons.lucene.document.SingleFieldSelector;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;

import com.google.common.collect.Maps;


/**
 * The Class FieldsLookup.
 *
 * @author l.xue.nong
 */
public class FieldsLookup implements Map {

	
	/** The mapper service. */
	private final MapperService mapperService;

	
	/** The reader. */
	private IndexReader reader;

	
	/** The doc id. */
	private int docId = -1;

	
	/** The cached field data. */
	private final Map<String, FieldLookup> cachedFieldData = Maps.newHashMap();

	
	/** The field selector. */
	private final SingleFieldSelector fieldSelector = new SingleFieldSelector();

	
	/**
	 * Instantiates a new fields lookup.
	 *
	 * @param mapperService the mapper service
	 */
	FieldsLookup(MapperService mapperService) {
		this.mapperService = mapperService;
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
		clearCache();
		this.docId = -1;
	}

	
	/**
	 * Sets the next doc id.
	 *
	 * @param docId the new next doc id
	 */
	public void setNextDocId(int docId) {
		if (this.docId == docId) { 
			return;
		}
		this.docId = docId;
		clearCache();
	}

	
	/* (non-Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	@Override
	public Object get(Object key) {
		return loadFieldData(key.toString());
	}

	
	/* (non-Javadoc)
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(Object key) {
		try {
			loadFieldData(key.toString());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	
	/* (non-Javadoc)
	 * @see java.util.Map#size()
	 */
	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	
	/* (non-Javadoc)
	 * @see java.util.Map#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	
	/* (non-Javadoc)
	 * @see java.util.Map#keySet()
	 */
	@Override
	public Set keySet() {
		throw new UnsupportedOperationException();
	}

	
	/* (non-Javadoc)
	 * @see java.util.Map#values()
	 */
	@Override
	public Collection values() {
		throw new UnsupportedOperationException();
	}

	
	/* (non-Javadoc)
	 * @see java.util.Map#entrySet()
	 */
	@Override
	public Set entrySet() {
		throw new UnsupportedOperationException();
	}

	
	/* (non-Javadoc)
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Object put(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	
	/* (non-Javadoc)
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	@Override
	public Object remove(Object key) {
		throw new UnsupportedOperationException();
	}

	
	/* (non-Javadoc)
	 * @see java.util.Map#clear()
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	
	/* (non-Javadoc)
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	@Override
	public void putAll(Map m) {
		throw new UnsupportedOperationException();
	}

	
	/* (non-Javadoc)
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	
	/**
	 * Load field data.
	 *
	 * @param name the name
	 * @return the field lookup
	 */
	private FieldLookup loadFieldData(String name) {
		FieldLookup data = cachedFieldData.get(name);
		if (data == null) {
			FieldMapper mapper = mapperService.smartNameFieldMapper(name);
			if (mapper == null) {
				throw new RestartIllegalArgumentException("No field found for [" + name + "]");
			}
			data = new FieldLookup(mapper);
			cachedFieldData.put(name, data);
		}
		if (data.doc() == null) {
			fieldSelector.name(data.mapper().names().indexName());
			try {
				data.doc(reader.document(docId, fieldSelector));
			} catch (IOException e) {
				throw new RestartParseException("failed to load field [" + name + "]", e);
			}
		}
		return data;
	}

	
	/**
	 * Clear cache.
	 */
	private void clearCache() {
		for (Entry<String, FieldLookup> entry : cachedFieldData.entrySet()) {
			entry.getValue().clear();
		}
	}

}
