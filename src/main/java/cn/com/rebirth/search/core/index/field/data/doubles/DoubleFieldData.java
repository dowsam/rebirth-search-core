/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DoubleFieldData.java 2012-7-6 14:30:17 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.doubles;

import gnu.trove.list.array.TDoubleArrayList;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;

import cn.com.rebirth.search.commons.RamUsage;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.NumericFieldData;
import cn.com.rebirth.search.core.index.field.data.FieldData.StringValueProc;
import cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader;

/**
 * The Class DoubleFieldData.
 *
 * @author l.xue.nong
 */
public abstract class DoubleFieldData extends NumericFieldData<DoubleDocFieldData> {

	/** The Constant EMPTY_DOUBLE_ARRAY. */
	public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

	/** The values. */
	protected final double[] values;

	/**
	 * Instantiates a new double field data.
	 *
	 * @param fieldName the field name
	 * @param values the values
	 */
	protected DoubleFieldData(String fieldName, double[] values) {
		super(fieldName);
		this.values = values;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#computeSizeInBytes()
	 */
	@Override
	protected long computeSizeInBytes() {
		return RamUsage.NUM_BYTES_DOUBLE * values.length + RamUsage.NUM_BYTES_ARRAY_HEADER;
	}

	/**
	 * Values.
	 *
	 * @return the double[]
	 */
	public final double[] values() {
		return this.values;
	}

	/**
	 * Value.
	 *
	 * @param docId the doc id
	 * @return the double
	 */
	abstract public double value(int docId);

	/**
	 * Values.
	 *
	 * @param docId the doc id
	 * @return the double[]
	 */
	abstract public double[] values(int docId);

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#docFieldData(int)
	 */
	@Override
	public DoubleDocFieldData docFieldData(int docId) {
		return super.docFieldData(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#createFieldData()
	 */
	@Override
	protected DoubleDocFieldData createFieldData() {
		return new DoubleDocFieldData(this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#stringValue(int)
	 */
	@Override
	public String stringValue(int docId) {
		return Double.toString(value(docId));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#forEachValue(cn.com.rebirth.search.core.index.field.data.FieldData.StringValueProc)
	 */
	@Override
	public void forEachValue(StringValueProc proc) {
		for (int i = 1; i < values.length; i++) {
			proc.onValue(Double.toString(values[i]));
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
		return (float) value(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#doubleValue(int)
	 */
	@Override
	public double doubleValue(int docId) {
		return value(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#type()
	 */
	@Override
	public FieldDataType type() {
		return FieldDataType.DefaultTypes.DOUBLE;
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
		void onValue(double value);
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
		void onValue(int docId, double value);

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
	 * @return the double field data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static DoubleFieldData load(IndexReader reader, String field) throws IOException {
		return FieldDataLoader.load(reader, field, new DoubleTypeLoader());
	}

	/**
	 * The Class DoubleTypeLoader.
	 *
	 * @author l.xue.nong
	 */
	static class DoubleTypeLoader extends FieldDataLoader.FreqsTypeLoader<DoubleFieldData> {

		/** The terms. */
		private final TDoubleArrayList terms = new TDoubleArrayList();

		/**
		 * Instantiates a new double type loader.
		 */
		DoubleTypeLoader() {
			super();

			terms.add(0);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#collectTerm(java.lang.String)
		 */
		@Override
		public void collectTerm(String term) {
			terms.add(FieldCache.NUMERIC_UTILS_DOUBLE_PARSER.parseDouble(term));
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#buildSingleValue(java.lang.String, int[])
		 */
		@Override
		public DoubleFieldData buildSingleValue(String field, int[] ordinals) {
			return new SingleValueDoubleFieldData(field, ordinals, terms.toArray());
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#buildMultiValue(java.lang.String, int[][])
		 */
		@Override
		public DoubleFieldData buildMultiValue(String field, int[][] ordinals) {
			return new MultiValueDoubleFieldData(field, ordinals, terms.toArray());
		}
	}
}