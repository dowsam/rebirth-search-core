/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ByteFieldData.java 2012-7-6 14:30:02 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.bytes;

import gnu.trove.list.array.TByteArrayList;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;

import cn.com.rebirth.search.commons.RamUsage;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.NumericFieldData;
import cn.com.rebirth.search.core.index.field.data.FieldData.StringValueProc;
import cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader;

/**
 * The Class ByteFieldData.
 *
 * @author l.xue.nong
 */
public abstract class ByteFieldData extends NumericFieldData<ByteDocFieldData> {

	/** The Constant EMPTY_BYTE_ARRAY. */
	static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	/** The values. */
	protected final byte[] values;

	/**
	 * Instantiates a new byte field data.
	 *
	 * @param fieldName the field name
	 * @param values the values
	 */
	protected ByteFieldData(String fieldName, byte[] values) {
		super(fieldName);
		this.values = values;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#computeSizeInBytes()
	 */
	@Override
	protected long computeSizeInBytes() {
		return 1 * values.length + RamUsage.NUM_BYTES_ARRAY_HEADER;
	}

	/**
	 * Values.
	 *
	 * @return the byte[]
	 */
	public final byte[] values() {
		return this.values;
	}

	/**
	 * Value.
	 *
	 * @param docId the doc id
	 * @return the byte
	 */
	abstract public byte value(int docId);

	/**
	 * Values.
	 *
	 * @param docId the doc id
	 * @return the byte[]
	 */
	abstract public byte[] values(int docId);

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#docFieldData(int)
	 */
	@Override
	public ByteDocFieldData docFieldData(int docId) {
		return super.docFieldData(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#createFieldData()
	 */
	@Override
	protected ByteDocFieldData createFieldData() {
		return new ByteDocFieldData(this);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#forEachValue(cn.com.rebirth.search.core.index.field.data.FieldData.StringValueProc)
	 */
	@Override
	public void forEachValue(StringValueProc proc) {
		for (int i = 1; i < values.length; i++) {
			proc.onValue(Byte.toString(values[i]));
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#stringValue(int)
	 */
	@Override
	public String stringValue(int docId) {
		return Byte.toString(value(docId));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData#byteValue(int)
	 */
	@Override
	public byte byteValue(int docId) {
		return value(docId);
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
		return FieldDataType.DefaultTypes.BYTE;
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
		void onValue(byte value);
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
		void onValue(int docId, byte value);

		/**
		 * On missing.
		 *
		 * @param docID the doc id
		 */
		void onMissing(int docID);
	}

	/**
	 * Load.
	 *
	 * @param reader the reader
	 * @param field the field
	 * @return the byte field data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ByteFieldData load(IndexReader reader, String field) throws IOException {
		return FieldDataLoader.load(reader, field, new ByteTypeLoader());
	}

	/**
	 * The Class ByteTypeLoader.
	 *
	 * @author l.xue.nong
	 */
	static class ByteTypeLoader extends FieldDataLoader.FreqsTypeLoader<ByteFieldData> {

		/** The terms. */
		private final TByteArrayList terms = new TByteArrayList();

		/**
		 * Instantiates a new byte type loader.
		 */
		ByteTypeLoader() {
			super();

			terms.add((byte) 0);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#collectTerm(java.lang.String)
		 */
		@Override
		public void collectTerm(String term) {
			terms.add((byte) FieldCache.NUMERIC_UTILS_INT_PARSER.parseInt(term));
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#buildSingleValue(java.lang.String, int[])
		 */
		@Override
		public ByteFieldData buildSingleValue(String field, int[] ordinals) {
			return new SingleValueByteFieldData(field, ordinals, terms.toArray());
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.support.FieldDataLoader.TypeLoader#buildMultiValue(java.lang.String, int[][])
		 */
		@Override
		public ByteFieldData buildMultiValue(String field, int[][] ordinals) {
			return new MultiValueByteFieldData(field, ordinals, terms.toArray());
		}
	}
}