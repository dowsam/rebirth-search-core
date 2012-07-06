/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FieldData.java 2012-3-29 15:00:56 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data;

import java.io.IOException;

import javax.print.Doc;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.thread.ThreadLocals;


/**
 * The Class FieldData.
 *
 * @param <Doc> the generic type
 * @author l.xue.nong
 */
public abstract class FieldData<Doc extends DocFieldData> {

	
	/** The cached doc field data. */
	private final ThreadLocal<ThreadLocals.CleanableValue<Doc>> cachedDocFieldData = new ThreadLocal<ThreadLocals.CleanableValue<Doc>>() {
		@Override
		protected ThreadLocals.CleanableValue<Doc> initialValue() {
			return new ThreadLocals.CleanableValue<Doc>(createFieldData());
		}
	};

	
	/** The field name. */
	private final String fieldName;

	
	/** The size in bytes. */
	private long sizeInBytes = -1;

	
	/**
	 * Instantiates a new field data.
	 *
	 * @param fieldName the field name
	 */
	protected FieldData(String fieldName) {
		this.fieldName = fieldName;
	}

	
	/**
	 * Field name.
	 *
	 * @return the string
	 */
	public final String fieldName() {
		return fieldName;
	}

	
	/**
	 * Doc field data.
	 *
	 * @param docId the doc id
	 * @return the doc
	 */
	public Doc docFieldData(int docId) {
		Doc docFieldData = cachedDocFieldData.get().get();
		docFieldData.setDocId(docId);
		return docFieldData;
	}

	
	/**
	 * Size in bytes.
	 *
	 * @return the long
	 */
	public long sizeInBytes() {
		if (sizeInBytes == -1) {
			sizeInBytes = computeSizeInBytes();
		}
		return sizeInBytes;
	}

	
	/**
	 * Compute size in bytes.
	 *
	 * @return the long
	 */
	protected abstract long computeSizeInBytes();

	
	/**
	 * Creates the field data.
	 *
	 * @return the doc
	 */
	protected abstract Doc createFieldData();

	
	/**
	 * Multi valued.
	 *
	 * @return true, if successful
	 */
	public abstract boolean multiValued();

	
	/**
	 * Checks for value.
	 *
	 * @param docId the doc id
	 * @return true, if successful
	 */
	public abstract boolean hasValue(int docId);

	
	/**
	 * String value.
	 *
	 * @param docId the doc id
	 * @return the string
	 */
	public abstract String stringValue(int docId);

	
	/**
	 * For each value.
	 *
	 * @param proc the proc
	 */
	public abstract void forEachValue(StringValueProc proc);

	
	/**
	 * The Interface StringValueProc.
	 *
	 * @author l.xue.nong
	 */
	public static interface StringValueProc {

		
		/**
		 * On value.
		 *
		 * @param value the value
		 */
		void onValue(String value);
	}

	
	/**
	 * For each value in doc.
	 *
	 * @param docId the doc id
	 * @param proc the proc
	 */
	public abstract void forEachValueInDoc(int docId, StringValueInDocProc proc);

	
	/**
	 * The Interface StringValueInDocProc.
	 *
	 * @author l.xue.nong
	 */
	public static interface StringValueInDocProc {

		
		/**
		 * On value.
		 *
		 * @param docId the doc id
		 * @param value the value
		 */
		void onValue(int docId, String value);

		
		/**
		 * On missing.
		 *
		 * @param docId the doc id
		 */
		void onMissing(int docId);
	}

	
	/**
	 * For each ordinal in doc.
	 *
	 * @param docId the doc id
	 * @param proc the proc
	 */
	public abstract void forEachOrdinalInDoc(int docId, OrdinalInDocProc proc);

	
	/**
	 * The Interface OrdinalInDocProc.
	 *
	 * @author l.xue.nong
	 */
	public static interface OrdinalInDocProc {

		
		/**
		 * On ordinal.
		 *
		 * @param docId the doc id
		 * @param ordinal the ordinal
		 */
		void onOrdinal(int docId, int ordinal);
	}

	
	/**
	 * Type.
	 *
	 * @return the field data type
	 */
	public abstract FieldDataType type();

	
	/**
	 * Load.
	 *
	 * @param type the type
	 * @param reader the reader
	 * @param fieldName the field name
	 * @return the field data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static FieldData load(FieldDataType type, IndexReader reader, String fieldName) throws IOException {
		return type.load(reader, fieldName);
	}
}
