/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MultiValueStringFieldData.java 2012-3-29 15:02:04 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.strings;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.search.commons.RamUsage;


/**
 * The Class MultiValueStringFieldData.
 *
 * @author l.xue.nong
 */
public class MultiValueStringFieldData extends StringFieldData {

	
	/** The Constant VALUE_CACHE_SIZE. */
	private static final int VALUE_CACHE_SIZE = 100;

	
	/** The values cache. */
	private static ThreadLocal<ThreadLocals.CleanableValue<String[][]>> valuesCache = new ThreadLocal<ThreadLocals.CleanableValue<String[][]>>() {
		@Override
		protected ThreadLocals.CleanableValue<String[][]> initialValue() {
			String[][] value = new String[VALUE_CACHE_SIZE][];
			for (int i = 0; i < value.length; i++) {
				value[i] = new String[i];
			}
			return new ThreadLocals.CleanableValue<java.lang.String[][]>(value);
		}
	};

	
	
	/** The ordinals. */
	private final int[][] ordinals;

	
	/**
	 * Instantiates a new multi value string field data.
	 *
	 * @param fieldName the field name
	 * @param ordinals the ordinals
	 * @param values the values
	 */
	public MultiValueStringFieldData(String fieldName, int[][] ordinals, String[] values) {
		super(fieldName, values);
		this.ordinals = ordinals;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.strings.StringFieldData#computeSizeInBytes()
	 */
	@Override
	protected long computeSizeInBytes() {
		long size = super.computeSizeInBytes();
		size += RamUsage.NUM_BYTES_ARRAY_HEADER; 
		for (int[] ordinal : ordinals) {
			size += RamUsage.NUM_BYTES_INT * ordinal.length + RamUsage.NUM_BYTES_ARRAY_HEADER;
		}
		return size;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.FieldData#multiValued()
	 */
	@Override
	public boolean multiValued() {
		return true;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.FieldData#hasValue(int)
	 */
	@Override
	public boolean hasValue(int docId) {
		for (int[] ordinal : ordinals) {
			if (ordinal[docId] != 0) {
				return true;
			}
		}
		return false;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.FieldData#forEachValueInDoc(int, cn.com.summall.search.core.index.field.data.FieldData.StringValueInDocProc)
	 */
	@Override
	public void forEachValueInDoc(int docId, StringValueInDocProc proc) {
		boolean found = false;
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				found = true;
				proc.onValue(docId, values[loc]);
			}
		}
		if (!found) {
			proc.onMissing(docId);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.FieldData#forEachOrdinalInDoc(int, cn.com.summall.search.core.index.field.data.FieldData.OrdinalInDocProc)
	 */
	@Override
	public void forEachOrdinalInDoc(int docId, OrdinalInDocProc proc) {
		boolean found = false;
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				found = true;
				proc.onOrdinal(docId, loc);
			}
		}
		if (!found) {
			proc.onOrdinal(docId, 0);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.strings.StringFieldData#value(int)
	 */
	@Override
	public String value(int docId) {
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				return values[loc];
			}
		}
		return null;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.field.data.strings.StringFieldData#values(int)
	 */
	@Override
	public String[] values(int docId) {
		int length = 0;
		for (int[] ordinal : ordinals) {
			if (ordinal[docId] != 0) {
				length++;
			}
		}
		if (length == 0) {
			return Strings.EMPTY_ARRAY;
		}
		String[] strings;
		if (length < VALUE_CACHE_SIZE) {
			strings = valuesCache.get().get()[length];
		} else {
			strings = new String[length];
		}
		int i = 0;
		for (int[] ordinal : ordinals) {
			int loc = ordinal[docId];
			if (loc != 0) {
				strings[i++] = values[loc];
			}
		}
		return strings;
	}
}