/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SourceLookup.java 2012-7-6 14:30:17 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.lookup;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.exception.RebirthParseException;
import cn.com.rebirth.search.commons.xcontent.XContentHelper;
import cn.com.rebirth.search.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.core.index.mapper.internal.SourceFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.SourceFieldSelector;

import com.google.common.collect.ImmutableMap;

/**
 * The Class SourceLookup.
 *
 * @author l.xue.nong
 */
public class SourceLookup implements Map {

	/** The reader. */
	private IndexReader reader;

	/** The doc id. */
	private int docId = -1;

	/** The source as bytes. */
	private byte[] sourceAsBytes;

	/** The source as bytes offset. */
	private int sourceAsBytesOffset;

	/** The source as bytes length. */
	private int sourceAsBytesLength;

	/** The source. */
	private Map<String, Object> source;

	/**
	 * Source.
	 *
	 * @return the map
	 */
	public Map<String, Object> source() {
		return source;
	}

	/**
	 * Load source if needed.
	 *
	 * @return the map
	 */
	private Map<String, Object> loadSourceIfNeeded() {
		if (source != null) {
			return source;
		}
		if (sourceAsBytes != null) {
			source = sourceAsMap(sourceAsBytes, sourceAsBytesOffset, sourceAsBytesLength);
			return source;
		}
		try {
			Document doc = reader.document(docId, SourceFieldSelector.INSTANCE);
			Fieldable sourceField = doc.getFieldable(SourceFieldMapper.NAME);
			if (sourceField == null) {
				source = ImmutableMap.of();
			} else {
				this.source = sourceAsMap(sourceField.getBinaryValue(), sourceField.getBinaryOffset(),
						sourceField.getBinaryLength());
			}
		} catch (Exception e) {
			throw new RebirthParseException("failed to parse / load source", e);
		}
		return this.source;
	}

	/**
	 * Source as map.
	 *
	 * @param bytes the bytes
	 * @param offset the offset
	 * @param length the length
	 * @return the map
	 * @throws RebirthParseException the rebirth parse exception
	 */
	public static Map<String, Object> sourceAsMap(byte[] bytes, int offset, int length) throws RebirthParseException {
		return XContentHelper.convertToMap(bytes, offset, length, false).v2();
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
		this.source = null;
		this.sourceAsBytes = null;
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
		this.sourceAsBytes = null;
		this.source = null;
	}

	/**
	 * Sets the next source.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 */
	public void setNextSource(byte[] source, int offset, int length) {
		this.sourceAsBytes = source;
		this.sourceAsBytesOffset = offset;
		this.sourceAsBytesLength = length;
	}

	/**
	 * Sets the next source.
	 *
	 * @param source the source
	 */
	public void setNextSource(Map<String, Object> source) {
		this.source = source;
	}

	/**
	 * Extract raw values.
	 *
	 * @param path the path
	 * @return the list
	 */
	public List<Object> extractRawValues(String path) {
		return XContentMapValues.extractRawValues(path, loadSourceIfNeeded());
	}

	/**
	 * Filter.
	 *
	 * @param includes the includes
	 * @param excludes the excludes
	 * @return the object
	 */
	public Object filter(String[] includes, String[] excludes) {
		return XContentMapValues.filter(loadSourceIfNeeded(), includes, excludes);
	}

	/**
	 * Extract value.
	 *
	 * @param path the path
	 * @return the object
	 */
	public Object extractValue(String path) {
		return XContentMapValues.extractValue(path, loadSourceIfNeeded());
	}

	/* (non-Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	@Override
	public Object get(Object key) {
		return loadSourceIfNeeded().get(key);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#size()
	 */
	@Override
	public int size() {
		return loadSourceIfNeeded().size();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return loadSourceIfNeeded().isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(Object key) {
		return loadSourceIfNeeded().containsKey(key);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(Object value) {
		return loadSourceIfNeeded().containsValue(value);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#keySet()
	 */
	@Override
	public Set keySet() {
		return loadSourceIfNeeded().keySet();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#values()
	 */
	@Override
	public Collection values() {
		return loadSourceIfNeeded().values();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#entrySet()
	 */
	@Override
	public Set entrySet() {
		return loadSourceIfNeeded().entrySet();
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
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	@Override
	public void putAll(Map m) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#clear()
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}
}
