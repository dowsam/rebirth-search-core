/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IntFieldData.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.ints;

import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;

import cn.com.rebirth.search.commons.RamUsage;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.NumericFieldData;
import cn.com.rebirth.search.core.index.field.data.FieldData.StringValueProc;
import cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader;

/**
 * The Class IntFieldData.
 *
 * @author l.xue.nong
 */
public abstract class IntFieldData extends NumericFieldData<IntDocFieldData> {

	/** The Constant EMPTY_INT_ARRAY. */
	static final int[] EMPTY_INT_ARRAY = new int[0];

	/** The values. */
	protected final int[] values;

	/**
	 * Instantiates a new int field data.
	 *
	 * @param fieldName the field name
	 * @param values the values
	 */
	protected IntFieldData(String fieldName, int[] values) {
		super(fieldName);
		this.values = values;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#computeSizeInBytes()
	 */
	@Override
	protected long computeSizeInBytes() {
		return RamUsage.NUM_BYTES_INT * values.length + RamUsage.NUM_BYTES_ARRAY_HEADER;
	}

	/**
	 * Values.
	 *
	 * @return the int[]
	 */
	public final int[] values() {
		return this.values;
	}

	/**
	 * Value.
	 *
	 * @param docId the doc id
	 * @return the int
	 */
	abstract public int value(int docId);

	/**
	 * Values.
	 *
	 * @param docId the doc id
	 * @return the int[]
	 */
	abstract public int[] values(int docId);

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#docFieldData(int)
	 */
	@Override
	public IntDocFieldData docFieldData(int docId) {
		return super.docFieldData(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#createFieldData()
	 */
	@Override
	protected IntDocFieldData createFieldData() {
		return new IntDocFieldData(this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#stringValue(int)
	 */
	@Override
	public String stringValue(int docId) {
		return Integer.toString(value(docId));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#forEachValue(cn.com.rebirth.search.core.index.field.data.FieldData.StringValueProc)
	 */
	@Override
	public void forEachValue(StringValueProc proc) {
		for (int i = 1; i < values.length; i++) {
			proc.onValue(Integer.toString(values[i]));
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#byteValue(int)
	 */
	@Override
	public byte byteValue(int docId) {
		return (byte) value(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#shortValue(int)
	 */
	@Override
	public short shortValue(int docId) {
		return (short) value(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#intValue(int)
	 */
	@Override
	public int intValue(int docId) {
		return value(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#longValue(int)
	 */
	@Override
	public long longValue(int docId) {
		return (long) value(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#floatValue(int)
	 */
	@Override
	public float floatValue(int docId) {
		return (float) value(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#doubleValue(int)
	 */
	@Override
	public double doubleValue(int docId) {
		return (double) value(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#type()
	 */
	@Override
	public FieldDataType type() {
		return FieldDataType.DefaultTypes.INT;
	}

	/**
	 * For each value.
	 *
	 * @param proc the proc
	 */
	public void forEachValue(ValueProc proc) {
		for (int i = 1; i < values.length; i++) {
			proc.onValue(values[i]);
		}
	}

	/**
	 * The Interface ValueProc.
	 *
	 * @author l.xue.nong
	 */
	public static interface ValueProc {

		/**
		 * On value.
		 *
		 * @param value the value
		 */
		void onValue(int value);
	}

	/**
	 * For each value in doc.
	 *
	 * @param docId the doc id
	 * @param proc the proc
	 */
	public abstract void forEachValueInDoc(int docId, ValueInDocProc proc);

	/**
	 * The Interface ValueInDocProc.
	 *
	 * @author l.xue.nong
	 */
	public static interface ValueInDocProc {

		/**
		 * On value.
		 *
		 * @param docId the doc id
		 * @param value the value
		 */
		void onValue(int docId, int value);

		/**
		 * On missing.
		 *
		 * @param docId the doc id
		 */
		void onMissing(int docId);
	}

	/**
	 * Load.
	 *
	 * @param reader the reader
	 * @param field the field
	 * @return the int field data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static IntFieldData load(IndexReader reader, String field) throws IOException {
		return FieldDataLoader.load(reader, field, new IntTypeLoader());
	}

	/**
	 * The Class IntTypeLoader.
	 *
	 * @author l.xue.nong
	 */
	static class IntTypeLoader extends FieldDataLoader.FreqsTypeLoader<IntFieldData> {

		/** The terms. */
		private final TIntArrayList terms = new TIntArrayList();

		/**
		 * Instantiates a new int type loader.
		 */
		IntTypeLoader() {
			super();

			terms.add(0);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#collectTerm(java.lang.String)
		 */
		@Override
		public void collectTerm(String term) {
			terms.add(FieldCache.NUMERIC_UTILS_INT_PARSER.parseInt(term));
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#buildSingleValue(java.lang.String, int[])
		 */
		@Override
		public IntFieldData buildSingleValue(String field, int[] ordinals) {
			return new SingleValueIntFieldData(field, ordinals, terms.toArray());
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#buildMultiValue(java.lang.String, int[][])
		 */
		@Override
		public IntFieldData buildMultiValue(String field, int[][] ordinals) {
			return new MultiValueIntFieldData(field, ordinals, terms.toArray());
		}
	}
}