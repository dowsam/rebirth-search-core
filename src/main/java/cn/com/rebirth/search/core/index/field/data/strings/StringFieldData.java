/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StringFieldData.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.strings;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.search.commons.RamUsage;
import cn.com.rebirth.search.core.index.field.data.FieldData;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.FieldData.StringValueProc;
import cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader;

/**
 * The Class StringFieldData.
 *
 * @author l.xue.nong
 */
public abstract class StringFieldData extends FieldData<StringDocFieldData> {

	/** The values. */
	protected final String[] values;

	/**
	 * Instantiates a new string field data.
	 *
	 * @param fieldName the field name
	 * @param values the values
	 */
	protected StringFieldData(String fieldName, String[] values) {
		super(fieldName);
		this.values = values;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#computeSizeInBytes()
	 */
	@Override
	protected long computeSizeInBytes() {
		long size = RamUsage.NUM_BYTES_ARRAY_HEADER;
		for (String value : values) {
			if (value != null) {
				size += RamUsage.NUM_BYTES_OBJECT_HEADER
						+ ((value.length() * RamUsage.NUM_BYTES_CHAR) + (3 * RamUsage.NUM_BYTES_INT));
			}
		}
		return size;
	}

	/**
	 * Values.
	 *
	 * @return the string[]
	 */
	public String[] values() {
		return this.values;
	}

	/**
	 * Value.
	 *
	 * @param docId the doc id
	 * @return the string
	 */
	abstract public String value(int docId);

	/**
	 * Values.
	 *
	 * @param docId the doc id
	 * @return the string[]
	 */
	abstract public String[] values(int docId);

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#docFieldData(int)
	 */
	@Override
	public StringDocFieldData docFieldData(int docId) {
		return super.docFieldData(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#stringValue(int)
	 */
	@Override
	public String stringValue(int docId) {
		return value(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#createFieldData()
	 */
	@Override
	protected StringDocFieldData createFieldData() {
		return new StringDocFieldData(this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#type()
	 */
	@Override
	public FieldDataType type() {
		return FieldDataType.DefaultTypes.STRING;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#forEachValue(cn.com.rebirth.search.core.index.field.data.FieldData.StringValueProc)
	 */
	@Override
	public void forEachValue(StringValueProc proc) {
		for (int i = 1; i < values.length; i++) {
			proc.onValue(values[i]);
		}
	}

	/**
	 * Load.
	 *
	 * @param reader the reader
	 * @param field the field
	 * @return the string field data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static StringFieldData load(IndexReader reader, String field) throws IOException {
		return FieldDataLoader.load(reader, field, new StringTypeLoader());
	}

	/**
	 * The Class StringTypeLoader.
	 *
	 * @author l.xue.nong
	 */
	static class StringTypeLoader extends FieldDataLoader.FreqsTypeLoader<StringFieldData> {

		/** The terms. */
		private final ArrayList<String> terms = new ArrayList<String>();

		/**
		 * Instantiates a new string type loader.
		 */
		StringTypeLoader() {
			super();

			terms.add(null);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#collectTerm(java.lang.String)
		 */
		@Override
		public void collectTerm(String term) {
			terms.add(term);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#buildSingleValue(java.lang.String, int[])
		 */
		@Override
		public StringFieldData buildSingleValue(String field, int[] ordinals) {
			return new SingleValueStringFieldData(field, ordinals, terms.toArray(new String[terms.size()]));
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#buildMultiValue(java.lang.String, int[][])
		 */
		@Override
		public StringFieldData buildMultiValue(String field, int[][] ordinals) {
			return new MultiValueStringFieldData(field, ordinals, terms.toArray(new String[terms.size()]));
		}
	}
}
