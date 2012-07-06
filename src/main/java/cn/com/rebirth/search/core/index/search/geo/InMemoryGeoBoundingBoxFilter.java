/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InMemoryGeoBoundingBoxFilter.java 2012-3-29 15:00:56 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.search.geo;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;

import cn.com.rebirth.search.commons.lucene.docset.GetDocSet;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldData;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldDataType;


/**
 * The Class InMemoryGeoBoundingBoxFilter.
 *
 * @author l.xue.nong
 */
public class InMemoryGeoBoundingBoxFilter extends Filter {

	
	/** The top left. */
	private final Point topLeft;

	
	/** The bottom right. */
	private final Point bottomRight;

	
	/** The field name. */
	private final String fieldName;

	
	/** The field data cache. */
	private final FieldDataCache fieldDataCache;

	
	/**
	 * Instantiates a new in memory geo bounding box filter.
	 *
	 * @param topLeft the top left
	 * @param bottomRight the bottom right
	 * @param fieldName the field name
	 * @param fieldDataCache the field data cache
	 */
	public InMemoryGeoBoundingBoxFilter(Point topLeft, Point bottomRight, String fieldName,
			FieldDataCache fieldDataCache) {
		this.topLeft = topLeft;
		this.bottomRight = bottomRight;
		this.fieldName = fieldName;
		this.fieldDataCache = fieldDataCache;
	}

	
	/**
	 * Top left.
	 *
	 * @return the point
	 */
	public Point topLeft() {
		return topLeft;
	}

	
	/**
	 * Bottom right.
	 *
	 * @return the point
	 */
	public Point bottomRight() {
		return bottomRight;
	}

	
	/**
	 * Field name.
	 *
	 * @return the string
	 */
	public String fieldName() {
		return fieldName;
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Filter#getDocIdSet(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
		final GeoPointFieldData fieldData = (GeoPointFieldData) fieldDataCache.cache(GeoPointFieldDataType.TYPE,
				reader, fieldName);

		
		if (topLeft.lon > bottomRight.lon) {
			return new Meridian180GeoBoundingBoxDocSet(reader.maxDoc(), fieldData, topLeft, bottomRight);
		} else {
			return new GeoBoundingBoxDocSet(reader.maxDoc(), fieldData, topLeft, bottomRight);
		}
	}

	
	/**
	 * The Class Meridian180GeoBoundingBoxDocSet.
	 *
	 * @author l.xue.nong
	 */
	public static class Meridian180GeoBoundingBoxDocSet extends GetDocSet {

		
		/** The field data. */
		private final GeoPointFieldData fieldData;

		
		/** The top left. */
		private final Point topLeft;

		
		/** The bottom right. */
		private final Point bottomRight;

		
		/**
		 * Instantiates a new meridian180 geo bounding box doc set.
		 *
		 * @param maxDoc the max doc
		 * @param fieldData the field data
		 * @param topLeft the top left
		 * @param bottomRight the bottom right
		 */
		public Meridian180GeoBoundingBoxDocSet(int maxDoc, GeoPointFieldData fieldData, Point topLeft, Point bottomRight) {
			super(maxDoc);
			this.fieldData = fieldData;
			this.topLeft = topLeft;
			this.bottomRight = bottomRight;
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.search.DocIdSet#isCacheable()
		 */
		@Override
		public boolean isCacheable() {
			
			
			
			return false;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.lucene.docset.DocSet#get(int)
		 */
		@Override
		public boolean get(int doc) {
			if (!fieldData.hasValue(doc)) {
				return false;
			}

			if (fieldData.multiValued()) {
				double[] lats = fieldData.latValues(doc);
				double[] lons = fieldData.lonValues(doc);
				for (int i = 0; i < lats.length; i++) {
					double lat = lats[i];
					double lon = lons[i];
					if (((topLeft.lon <= lon || bottomRight.lon >= lon))
							&& (topLeft.lat >= lat && bottomRight.lat <= lat)) {
						return true;
					}
				}
			} else {
				double lat = fieldData.latValue(doc);
				double lon = fieldData.lonValue(doc);

				if (((topLeft.lon <= lon || bottomRight.lon >= lon)) && (topLeft.lat >= lat && bottomRight.lat <= lat)) {
					return true;
				}
			}
			return false;
		}
	}

	
	/**
	 * The Class GeoBoundingBoxDocSet.
	 *
	 * @author l.xue.nong
	 */
	public static class GeoBoundingBoxDocSet extends GetDocSet {

		
		/** The field data. */
		private final GeoPointFieldData fieldData;

		
		/** The top left. */
		private final Point topLeft;

		
		/** The bottom right. */
		private final Point bottomRight;

		
		/**
		 * Instantiates a new geo bounding box doc set.
		 *
		 * @param maxDoc the max doc
		 * @param fieldData the field data
		 * @param topLeft the top left
		 * @param bottomRight the bottom right
		 */
		public GeoBoundingBoxDocSet(int maxDoc, GeoPointFieldData fieldData, Point topLeft, Point bottomRight) {
			super(maxDoc);
			this.fieldData = fieldData;
			this.topLeft = topLeft;
			this.bottomRight = bottomRight;
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.search.DocIdSet#isCacheable()
		 */
		@Override
		public boolean isCacheable() {
			
			
			
			return false;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.lucene.docset.DocSet#get(int)
		 */
		@Override
		public boolean get(int doc) {
			if (!fieldData.hasValue(doc)) {
				return false;
			}

			if (fieldData.multiValued()) {
				double[] lats = fieldData.latValues(doc);
				double[] lons = fieldData.lonValues(doc);
				for (int i = 0; i < lats.length; i++) {
					if (topLeft.lon <= lons[i] && bottomRight.lon >= lons[i] && topLeft.lat >= lats[i]
							&& bottomRight.lat <= lats[i]) {
						return true;
					}
				}
			} else {
				double lat = fieldData.latValue(doc);
				double lon = fieldData.lonValue(doc);

				if (topLeft.lon <= lon && bottomRight.lon >= lon && topLeft.lat >= lat && bottomRight.lat <= lat) {
					return true;
				}
			}
			return false;
		}
	}
}
