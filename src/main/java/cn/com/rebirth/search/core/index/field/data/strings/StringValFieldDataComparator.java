/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core StringValFieldDataComparator.java 2012-3-29 15:02:05 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.strings;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldData;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;


/**
 * The Class StringValFieldDataComparator.
 *
 * @author l.xue.nong
 */
public class StringValFieldDataComparator extends FieldComparator {

	
	/** The field name. */
	private final String fieldName;

	
	/** The field data cache. */
	protected final FieldDataCache fieldDataCache;

	
	/** The current field data. */
	protected FieldData currentFieldData;

	
	/** The values. */
	private String[] values;

	
	/** The bottom. */
	private String bottom;

	
	/**
	 * Instantiates a new string val field data comparator.
	 *
	 * @param numHits the num hits
	 * @param fieldName the field name
	 * @param fieldDataCache the field data cache
	 */
	public StringValFieldDataComparator(int numHits, String fieldName, FieldDataCache fieldDataCache) {
		this.fieldName = fieldName;
		this.fieldDataCache = fieldDataCache;
		values = new String[numHits];
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#compare(int, int)
	 */
	@Override
	public int compare(int slot1, int slot2) {
		final String val1 = values[slot1];
		final String val2 = values[slot2];
		if (val1 == null) {
			if (val2 == null) {
				return 0;
			}
			return -1;
		} else if (val2 == null) {
			return 1;
		}

		return val1.compareTo(val2);
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#compareBottom(int)
	 */
	@Override
	public int compareBottom(int doc) {
		final String val2 = currentFieldData.stringValue(doc);
		if (bottom == null) {
			if (val2 == null) {
				return 0;
			}
			return -1;
		} else if (val2 == null) {
			return 1;
		}
		return bottom.compareTo(val2);
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#copy(int, int)
	 */
	@Override
	public void copy(int slot, int doc) {
		values[slot] = currentFieldData.stringValue(doc);
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#setNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		currentFieldData = fieldDataCache.cache(FieldDataType.DefaultTypes.STRING, reader, fieldName);
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#setBottom(int)
	 */
	@Override
	public void setBottom(final int bottom) {
		this.bottom = values[bottom];
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#value(int)
	 */
	@Override
	public Comparable value(int slot) {
		return values[slot];
	}
}
