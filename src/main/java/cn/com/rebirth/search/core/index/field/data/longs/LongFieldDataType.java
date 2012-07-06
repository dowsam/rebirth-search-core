/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core LongFieldDataType.java 2012-7-6 14:29:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.longs;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.SortField;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.index.field.data.ExtendedFieldComparatorSource;

/**
 * The Class LongFieldDataType.
 *
 * @author l.xue.nong
 */
public class LongFieldDataType implements FieldDataType<LongFieldData> {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldDataType#newFieldComparatorSource(cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache, java.lang.String)
	 */
	@Override
	public ExtendedFieldComparatorSource newFieldComparatorSource(final FieldDataCache cache, final String missing) {
		if (missing == null) {
			return new ExtendedFieldComparatorSource() {
				@Override
				public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
						throws IOException {
					return new LongFieldDataComparator(numHits, fieldname, cache);
				}

				@Override
				public int reducedType() {
					return SortField.LONG;
				}
			};
		}
		if (missing.equals("_last")) {
			return new ExtendedFieldComparatorSource() {
				@Override
				public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
						throws IOException {
					return new LongFieldDataMissingComparator(numHits, fieldname, cache, reversed ? Long.MIN_VALUE
							: Long.MAX_VALUE);
				}

				@Override
				public int reducedType() {
					return SortField.LONG;
				}
			};
		}
		if (missing.equals("_first")) {
			return new ExtendedFieldComparatorSource() {
				@Override
				public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
						throws IOException {
					return new LongFieldDataMissingComparator(numHits, fieldname, cache, reversed ? Long.MAX_VALUE
							: Long.MIN_VALUE);
				}

				@Override
				public int reducedType() {
					return SortField.LONG;
				}
			};
		}
		return new ExtendedFieldComparatorSource() {
			@Override
			public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
					throws IOException {
				return new LongFieldDataMissingComparator(numHits, fieldname, cache, Long.parseLong(missing));
			}

			@Override
			public int reducedType() {
				return SortField.LONG;
			}
		};
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldDataType#load(org.apache.lucene.index.IndexReader, java.lang.String)
	 */
	@Override
	public LongFieldData load(IndexReader reader, String fieldName) throws IOException {
		return LongFieldData.load(reader, fieldName);
	}
}
