/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NumericFieldData.java 2012-7-6 14:29:22 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data;

/**
 * The Class NumericFieldData.
 *
 * @param <Doc> the generic type
 * @author l.xue.nong
 */
public abstract class NumericFieldData<Doc extends NumericDocFieldData> extends FieldData<Doc> {

	/**
	 * Instantiates a new numeric field data.
	 *
	 * @param fieldName the field name
	 */
	protected NumericFieldData(String fieldName) {
		super(fieldName);
	}

	/**
	 * Int value.
	 *
	 * @param docId the doc id
	 * @return the int
	 */
	public abstract int intValue(int docId);

	/**
	 * Long value.
	 *
	 * @param docId the doc id
	 * @return the long
	 */
	public abstract long longValue(int docId);

	/**
	 * Float value.
	 *
	 * @param docId the doc id
	 * @return the float
	 */
	public abstract float floatValue(int docId);

	/**
	 * Double value.
	 *
	 * @param docId the doc id
	 * @return the double
	 */
	public abstract double doubleValue(int docId);

	/**
	 * Byte value.
	 *
	 * @param docId the doc id
	 * @return the byte
	 */
	public byte byteValue(int docId) {
		return (byte) intValue(docId);
	}

	/**
	 * Short value.
	 *
	 * @param docId the doc id
	 * @return the short
	 */
	public short shortValue(int docId) {
		return (short) intValue(docId);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldData#docFieldData(int)
	 */
	@Override
	public Doc docFieldData(int docId) {
		return super.docFieldData(docId);
	}

	/**
	 * Double values.
	 *
	 * @param docId the doc id
	 * @return the double[]
	 */
	public abstract double[] doubleValues(int docId);

	/**
	 * For each value in doc.
	 *
	 * @param docId the doc id
	 * @param proc the proc
	 */
	public abstract void forEachValueInDoc(int docId, DoubleValueInDocProc proc);

	/**
	 * For each value in doc.
	 *
	 * @param docId the doc id
	 * @param proc the proc
	 */
	public abstract void forEachValueInDoc(int docId, MissingDoubleValueInDocProc proc);

	/**
	 * For each value in doc.
	 *
	 * @param docId the doc id
	 * @param proc the proc
	 */
	public abstract void forEachValueInDoc(int docId, LongValueInDocProc proc);

	/**
	 * For each value in doc.
	 *
	 * @param docId the doc id
	 * @param proc the proc
	 */
	public abstract void forEachValueInDoc(int docId, MissingLongValueInDocProc proc);

	/**
	 * The Interface DoubleValueInDocProc.
	 *
	 * @author l.xue.nong
	 */
	public static interface DoubleValueInDocProc {

		/**
		 * On value.
		 *
		 * @param docId the doc id
		 * @param value the value
		 */
		void onValue(int docId, double value);
	}

	/**
	 * The Interface LongValueInDocProc.
	 *
	 * @author l.xue.nong
	 */
	public static interface LongValueInDocProc {

		/**
		 * On value.
		 *
		 * @param docId the doc id
		 * @param value the value
		 */
		void onValue(int docId, long value);
	}

	/**
	 * The Interface MissingLongValueInDocProc.
	 *
	 * @author l.xue.nong
	 */
	public static interface MissingLongValueInDocProc {

		/**
		 * On value.
		 *
		 * @param docId the doc id
		 * @param value the value
		 */
		void onValue(int docId, long value);

		/**
		 * On missing.
		 *
		 * @param docId the doc id
		 */
		void onMissing(int docId);
	}

	/**
	 * The Interface MissingDoubleValueInDocProc.
	 *
	 * @author l.xue.nong
	 */
	public static interface MissingDoubleValueInDocProc {

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
}
