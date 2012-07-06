/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FieldDataType.java 2012-7-6 14:29:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.bytes.ByteFieldDataType;
import cn.com.rebirth.search.core.index.field.data.doubles.DoubleFieldDataType;
import cn.com.rebirth.search.core.index.field.data.floats.FloatFieldDataType;
import cn.com.rebirth.search.core.index.field.data.ints.IntFieldDataType;
import cn.com.rebirth.search.core.index.field.data.longs.LongFieldDataType;
import cn.com.rebirth.search.core.index.field.data.shorts.ShortFieldDataType;
import cn.com.rebirth.search.core.index.field.data.strings.StringFieldDataType;
import cn.com.rebirth.search.index.field.data.ExtendedFieldComparatorSource;

/**
 * The Interface FieldDataType.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public interface FieldDataType<T extends FieldData> {

	/**
	 * The Class DefaultTypes.
	 *
	 * @author l.xue.nong
	 */
	public static final class DefaultTypes {

		/** The Constant STRING. */
		public static final StringFieldDataType STRING = new StringFieldDataType();

		/** The Constant BYTE. */
		public static final ByteFieldDataType BYTE = new ByteFieldDataType();

		/** The Constant SHORT. */
		public static final ShortFieldDataType SHORT = new ShortFieldDataType();

		/** The Constant INT. */
		public static final IntFieldDataType INT = new IntFieldDataType();

		/** The Constant LONG. */
		public static final LongFieldDataType LONG = new LongFieldDataType();

		/** The Constant FLOAT. */
		public static final FloatFieldDataType FLOAT = new FloatFieldDataType();

		/** The Constant DOUBLE. */
		public static final DoubleFieldDataType DOUBLE = new DoubleFieldDataType();
	}

	/**
	 * New field comparator source.
	 *
	 * @param cache the cache
	 * @param missing the missing
	 * @return the extended field comparator source
	 */
	ExtendedFieldComparatorSource newFieldComparatorSource(FieldDataCache cache, @Nullable String missing);

	/**
	 * Load.
	 *
	 * @param reader the reader
	 * @param fieldName the field name
	 * @return the t
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	T load(IndexReader reader, String fieldName) throws IOException;

}
