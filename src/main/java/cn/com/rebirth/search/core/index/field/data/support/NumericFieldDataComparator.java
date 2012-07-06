/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NumericFieldDataComparator.java 2012-3-29 15:02:42 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.support;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.NumericFieldData;


/**
 * The Class NumericFieldDataComparator.
 *
 * @author l.xue.nong
 */
public abstract class NumericFieldDataComparator extends FieldComparator {

	
	/** The field name. */
	private final String fieldName;

	
	/** The field data cache. */
	protected final FieldDataCache fieldDataCache;

	
	/** The current field data. */
	protected NumericFieldData currentFieldData;

	
	/**
	 * Instantiates a new numeric field data comparator.
	 *
	 * @param fieldName the field name
	 * @param fieldDataCache the field data cache
	 */
	public NumericFieldDataComparator(String fieldName, FieldDataCache fieldDataCache) {
		this.fieldName = fieldName;
		this.fieldDataCache = fieldDataCache;
	}

	
	/**
	 * Field data type.
	 *
	 * @return the field data type
	 */
	public abstract FieldDataType fieldDataType();

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#setNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		currentFieldData = (NumericFieldData) fieldDataCache.cache(fieldDataType(), reader, fieldName);
	}
}