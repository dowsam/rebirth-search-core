/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShortFieldData.java 2012-7-6 14:28:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.shorts;

import gnu.trove.list.array.TShortArrayList;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;

import cn.com.rebirth.search.commons.RamUsage;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.NumericFieldData;
import cn.com.rebirth.search.core.index.field.data.FieldData.StringValueProc;
import cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader;

/**
 * The Class ShortFieldData.
 *
 * @author l.xue.nong
 */
public abstract class ShortFieldData extends NumericFieldData<ShortDocFieldData> {

	/** The Constant EMPTY_SHORT_ARRAY. */
	static final short[] EMPTY_SHORT_ARRAY = new short[0];

	/** The values. */
	protected final short[] values;

	/**
	 * Instantiates a new short field data.
	 *
	 * @param fieldName the field name
	 * @param values the values
	 */
	protected ShortFieldData(String fieldName, short[] values) {
		super(fieldName);
		this.values = values;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#computeSizeInBytes()
	 */
	@Override
	protected long computeSizeInBytes() {
		return RamUsage.NUM_BYTES_SHORT * values.length + RamUsage.NUM_BYTES_ARRAY_HEADER;
	}

	/**
	 * Values.
	 *
	 * @return the short[]
	 */
	public final short[] values() {
		return this.values;
	}

	/**
	 * Value.
	 *
	 * @param docId the doc id
	 * @return the short
	 */
	abstract public short value(int docId);

	/**
	 * Values.
	 *
	 * @param docId the doc id
	 * @return the short[]
	 */
	abstract public short[] values(int docId);

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#docFieldData(int)
	 */
	@Override
	public ShortDocFieldData docFieldData(int docId) {
		return super.docFieldData(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#createFieldData()
	 */
	@Override
	protected ShortDocFieldData createFieldData() {
		return new ShortDocFieldData(this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#forEachValue(cn.com.rebirth.search.core.index.field.data.FieldData.StringValueProc)
	 */
	@Override
	public void forEachValue(StringValueProc proc) {
		for (int i = 1; i < values.length; i++) {
			proc.onValue(Short.toString(values[i]));
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#stringValue(int)
	 */
	@Override
	public String stringValue(int docId) {
		return Short.toString(value(docId));
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
		return value(docId);
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
		return (double) value(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#type()
	 */
	@Override
	public FieldDataType type() {
		return FieldDataType.DefaultTypes.SHORT;
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
		void onValue(short value);
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
		void onValue(int docId, short value);

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
	 * @return the short field data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ShortFieldData load(IndexReader reader, String field) throws IOException {
		return FieldDataLoader.load(reader, field, new ShortTypeLoader());
	}

	/**
	 * The Class ShortTypeLoader.
	 *
	 * @author l.xue.nong
	 */
	static class ShortTypeLoader extends FieldDataLoader.FreqsTypeLoader<ShortFieldData> {

		/** The terms. */
		private final TShortArrayList terms = new TShortArrayList();

		/**
		 * Instantiates a new short type loader.
		 */
		ShortTypeLoader() {
			super();

			terms.add((short) 0);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#collectTerm(java.lang.String)
		 */
		@Override
		public void collectTerm(String term) {
			terms.add((short) FieldCache.NUMERIC_UTILS_INT_PARSER.parseInt(term));
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#buildSingleValue(java.lang.String, int[])
		 */
		@Override
		public ShortFieldData buildSingleValue(String field, int[] ordinals) {
			return new SingleValueShortFieldData(field, ordinals, terms.toArray());
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#buildMultiValue(java.lang.String, int[][])
		 */
		@Override
		public ShortFieldData buildMultiValue(String field, int[][] ordinals) {
			return new MultiValueShortFieldData(field, ordinals, terms.toArray());
		}
	}
}