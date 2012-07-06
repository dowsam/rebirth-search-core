/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SingleValueStringFieldData.java 2012-3-29 15:00:46 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.strings;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.search.commons.RamUsage;


/**
 * The Class SingleValueStringFieldData.
 *
 * @author l.xue.nong
 */
public class SingleValueStringFieldData extends StringFieldData {

	
	/** The values cache. */
	private static ThreadLocal<ThreadLocals.CleanableValue<String[]>> valuesCache = new ThreadLocal<ThreadLocals.CleanableValue<String[]>>() {
		@Override
		protected ThreadLocals.CleanableValue<String[]> initialValue() {
			return new ThreadLocals.CleanableValue<String[]>(new String[1]);
		}
	};

	
	
	/** The ordinals. */
	private final int[] ordinals;

	
	/**
	 * Instantiates a new single value string field data.
	 *
	 * @param fieldName the field name
	 * @param ordinals the ordinals
	 * @param values the values
	 */
	public SingleValueStringFieldData(String fieldName, int[] ordinals, String[] values) {
		super(fieldName, values);
		this.ordinals = ordinals;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.strings.StringFieldData#computeSizeInBytes()
	 */
	@Override
	protected long computeSizeInBytes() {
		return super.computeSizeInBytes() + RamUsage.NUM_BYTES_INT * ordinals.length + RamUsage.NUM_BYTES_ARRAY_HEADER;
	}

	
	/**
	 * Ordinals.
	 *
	 * @return the int[]
	 */
	int[] ordinals() {
		return ordinals;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.FieldData#forEachOrdinalInDoc(int, cn.com.summall.search.core.index.field.data.FieldData.OrdinalInDocProc)
	 */
	@Override
	public void forEachOrdinalInDoc(int docId, OrdinalInDocProc proc) {
		proc.onOrdinal(docId, ordinals[docId]);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.FieldData#multiValued()
	 */
	@Override
	public boolean multiValued() {
		return false;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.FieldData#hasValue(int)
	 */
	@Override
	public boolean hasValue(int docId) {
		return ordinals[docId] != 0;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.FieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.FieldData.StringValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, StringValueInDocProc proc) {
		int loc = ordinals[docId];
		if (loc == 0) {
			proc.onMissing(docId);
			return;
		}
		proc.onValue(docId, values[loc]);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.strings.StringFieldData#value(int)
	 */
	@Override
	public String value(int docId) {
		return values[ordinals[docId]];
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.strings.StringFieldData#values(int)
	 */
	@Override
	public String[] values(int docId) {
		int loc = ordinals[docId];
		if (loc == 0) {
			return Strings.EMPTY_ARRAY;
		}
		String[] ret = valuesCache.get().get();
		ret[0] = values[loc];
		return ret;
	}
}
