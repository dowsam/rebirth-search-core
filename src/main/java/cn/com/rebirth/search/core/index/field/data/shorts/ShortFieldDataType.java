/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShortFieldDataType.java 2012-7-6 14:30:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.shorts;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.SortField;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.index.field.data.ExtendedFieldComparatorSource;

/**
 * The Class ShortFieldDataType.
 *
 * @author l.xue.nong
 */
public class ShortFieldDataType implements FieldDataType<ShortFieldData> {

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
					return new ShortFieldDataComparator(numHits, fieldname, cache);
				}

				@Override
				public int reducedType() {
					return SortField.SHORT;
				}
			};
		}
		if (missing.equals("_last")) {
			return new ExtendedFieldComparatorSource() {
				@Override
				public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
						throws IOException {
					return new ShortFieldDataMissingComparator(numHits, fieldname, cache, reversed ? Short.MIN_VALUE
							: Short.MAX_VALUE);
				}

				@Override
				public int reducedType() {
					return SortField.SHORT;
				}
			};
		}
		if (missing.equals("_first")) {
			return new ExtendedFieldComparatorSource() {
				@Override
				public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
						throws IOException {
					return new ShortFieldDataMissingComparator(numHits, fieldname, cache, reversed ? Short.MAX_VALUE
							: Short.MIN_VALUE);
				}

				@Override
				public int reducedType() {
					return SortField.SHORT;
				}
			};
		}
		return new ExtendedFieldComparatorSource() {
			@Override
			public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
					throws IOException {
				return new ShortFieldDataMissingComparator(numHits, fieldname, cache, Short.parseShort(missing));
			}

			@Override
			public int reducedType() {
				return SortField.SHORT;
			}
		};
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldDataType#load(org.apache.lucene.index.IndexReader, java.lang.String)
	 */
	@Override
	public ShortFieldData load(IndexReader reader, String fieldName) throws IOException {
		return ShortFieldData.load(reader, fieldName);
	}
}
