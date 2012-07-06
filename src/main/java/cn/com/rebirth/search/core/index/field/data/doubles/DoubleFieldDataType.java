/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DoubleFieldDataType.java 2012-7-6 14:30:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.field.data.doubles;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.SortField;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.index.field.data.ExtendedFieldComparatorSource;

/**
 * The Class DoubleFieldDataType.
 *
 * @author l.xue.nong
 */
public class DoubleFieldDataType implements FieldDataType<DoubleFieldData> {

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
					return new DoubleFieldDataComparator(numHits, fieldname, cache);
				}

				@Override
				public int reducedType() {
					return SortField.DOUBLE;
				}
			};
		}
		if (missing.equals("_last")) {
			return new ExtendedFieldComparatorSource() {
				@Override
				public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
						throws IOException {
					return new DoubleFieldDataMissingComparator(numHits, fieldname, cache,
							reversed ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
				}

				@Override
				public int reducedType() {
					return SortField.DOUBLE;
				}
			};
		}
		if (missing.equals("_first")) {
			return new ExtendedFieldComparatorSource() {
				@Override
				public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
						throws IOException {
					return new DoubleFieldDataMissingComparator(numHits, fieldname, cache,
							reversed ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
				}

				@Override
				public int reducedType() {
					return SortField.DOUBLE;
				}
			};
		}
		return new ExtendedFieldComparatorSource() {
			@Override
			public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
					throws IOException {
				return new DoubleFieldDataMissingComparator(numHits, fieldname, cache, Double.parseDouble(missing));
			}

			@Override
			public int reducedType() {
				return SortField.DOUBLE;
			}
		};
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldDataType#load(org.apache.lucene.index.IndexReader, java.lang.String)
	 */
	@Override
	public DoubleFieldData load(IndexReader reader, String fieldName) throws IOException {
		return DoubleFieldData.load(reader, fieldName);
	}
}
