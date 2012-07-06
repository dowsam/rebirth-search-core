/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IntFieldDataType.java 2012-7-6 14:28:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.ints;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.SortField;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.index.field.data.ExtendedFieldComparatorSource;

/**
 * The Class IntFieldDataType.
 *
 * @author l.xue.nong
 */
public class IntFieldDataType implements FieldDataType<IntFieldData> {

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
					return new IntFieldDataComparator(numHits, fieldname, cache);
				}

				@Override
				public int reducedType() {
					return SortField.INT;
				}
			};
		}
		if (missing.equals("_last")) {
			return new ExtendedFieldComparatorSource() {
				@Override
				public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
						throws IOException {
					return new IntFieldDataMissingComparator(numHits, fieldname, cache, reversed ? Integer.MIN_VALUE
							: Integer.MAX_VALUE);
				}

				@Override
				public int reducedType() {
					return SortField.INT;
				}
			};
		}
		if (missing.equals("_first")) {
			return new ExtendedFieldComparatorSource() {
				@Override
				public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
						throws IOException {
					return new IntFieldDataMissingComparator(numHits, fieldname, cache, reversed ? Integer.MAX_VALUE
							: Integer.MIN_VALUE);
				}

				@Override
				public int reducedType() {
					return SortField.INT;
				}
			};
		}
		return new ExtendedFieldComparatorSource() {
			@Override
			public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
					throws IOException {
				return new IntFieldDataMissingComparator(numHits, fieldname, cache, Integer.parseInt(missing));
			}

			@Override
			public int reducedType() {
				return SortField.INT;
			}
		};
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldDataType#load(org.apache.lucene.index.IndexReader, java.lang.String)
	 */
	@Override
	public IntFieldData load(IndexReader reader, String fieldName) throws IOException {
		return IntFieldData.load(reader, fieldName);
	}
}
