/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GeoPointFieldDataType.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.geo;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.SortField;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.strings.StringOrdValFieldDataComparator;
import cn.com.rebirth.search.index.field.data.ExtendedFieldComparatorSource;

/**
 * The Class GeoPointFieldDataType.
 *
 * @author l.xue.nong
 */
public class GeoPointFieldDataType implements FieldDataType<GeoPointFieldData> {

	/** The Constant TYPE. */
	public static final GeoPointFieldDataType TYPE = new GeoPointFieldDataType();

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldDataType#newFieldComparatorSource(cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache, java.lang.String)
	 */
	@Override
	public ExtendedFieldComparatorSource newFieldComparatorSource(final FieldDataCache cache, final String missing) {
		return new ExtendedFieldComparatorSource() {
			@Override
			public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
					throws IOException {
				return new StringOrdValFieldDataComparator(numHits, fieldname, sortPos, reversed, cache);
			}

			@Override
			public int reducedType() {
				return SortField.STRING;
			}
		};
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.field.data.FieldDataType#load(org.apache.lucene.index.IndexReader, java.lang.String)
	 */
	@Override
	public GeoPointFieldData load(IndexReader reader, String fieldName) throws IOException {
		return GeoPointFieldData.load(reader, fieldName);
	}
}
