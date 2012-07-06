/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FloatFieldData.java 2012-7-6 14:28:51 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.floats;

import gnu.trove.list.array.TFloatArrayList;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;

import cn.com.rebirth.search.commons.RamUsage;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.NumericFieldData;
import cn.com.rebirth.search.core.index.field.data.FieldData.StringValueProc;
import cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader;

/**
 * The Class FloatFieldData.
 *
 * @author l.xue.nong
 */
public abstract class FloatFieldData extends NumericFieldData<FloatDocFieldData> {

	/** The Constant EMPTY_FLOAT_ARRAY. */
	static final float[] EMPTY_FLOAT_ARRAY = new float[0];

	/** The values. */
	protected final float[] values;

	/**
	 * Instantiates a new float field data.
	 *
	 * @param fieldName the field name
	 * @param values the values
	 */
	protected FloatFieldData(String fieldName, float[] values) {
		super(fieldName);
		this.values = values;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#computeSizeInBytes()
	 */
	@Override
	protected long computeSizeInBytes() {
		return RamUsage.NUM_BYTES_FLOAT * values.length + RamUsage.NUM_BYTES_ARRAY_HEADER;
	}

	/**
	 * Values.
	 *
	 * @return the float[]
	 */
	public final float[] values() {
		return this.values;
	}

	/**
	 * Value.
	 *
	 * @param docId the doc id
	 * @return the float
	 */
	abstract public float value(int docId);

	/**
	 * Values.
	 *
	 * @param docId the doc id
	 * @return the float[]
	 */
	abstract public float[] values(int docId);

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#docFieldData(int)
	 */
	@Override
	public FloatDocFieldData docFieldData(int docId) {
		return super.docFieldData(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#createFieldData()
	 */
	@Override
	protected FloatDocFieldData createFieldData() {
		return new FloatDocFieldData(this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#stringValue(int)
	 */
	@Override
	public String stringValue(int docId) {
		return Float.toString(value(docId));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#forEachValue(cn.com.rebirth.search.core.index.field.data.FieldData.StringValueProc)
	 */
	@Override
	public void forEachValue(StringValueProc proc) {
		for (int i = 1; i < values.length; i++) {
			proc.onValue(Float.toString(values[i]));
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
		return (int) value(docId);
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
		return value(docId);
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
		return FieldDataType.DefaultTypes.FLOAT;
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
		void onValue(float value);
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
		void onValue(int docId, float value);

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
	 * @return the float field data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static FloatFieldData load(IndexReader reader, String field) throws IOException {
		return FieldDataLoader.load(reader, field, new FloatTypeLoader());
	}

	/**
	 * The Class FloatTypeLoader.
	 *
	 * @author l.xue.nong
	 */
	static class FloatTypeLoader extends FieldDataLoader.FreqsTypeLoader<FloatFieldData> {

		/** The terms. */
		private final TFloatArrayList terms = new TFloatArrayList();

		/**
		 * Instantiates a new float type loader.
		 */
		FloatTypeLoader() {
			super();

			terms.add(0);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#collectTerm(java.lang.String)
		 */
		@Override
		public void collectTerm(String term) {
			terms.add(FieldCache.NUMERIC_UTILS_FLOAT_PARSER.parseFloat(term));
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#buildSingleValue(java.lang.String, int[])
		 */
		@Override
		public FloatFieldData buildSingleValue(String field, int[] ordinals) {
			return new SingleValueFloatFieldData(field, ordinals, terms.toArray());
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#buildMultiValue(java.lang.String, int[][])
		 */
		@Override
		public FloatFieldData buildMultiValue(String field, int[][] ordinals) {
			return new MultiValueFloatFieldData(field, ordinals, terms.toArray());
		}
	}
}