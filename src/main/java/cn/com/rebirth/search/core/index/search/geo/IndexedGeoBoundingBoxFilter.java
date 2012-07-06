/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexedGeoBoundingBoxFilter.java 2012-7-6 14:30:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.search.geo;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.FixedBitSet;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.search.commons.lucene.docset.DocSets;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldMapper;

/**
 * The Class IndexedGeoBoundingBoxFilter.
 *
 * @author l.xue.nong
 */
public class IndexedGeoBoundingBoxFilter {

	/**
	 * Creates the.
	 *
	 * @param topLeft the top left
	 * @param bottomRight the bottom right
	 * @param fieldMapper the field mapper
	 * @return the filter
	 */
	public static Filter create(Point topLeft, Point bottomRight, GeoPointFieldMapper fieldMapper) {
		if (!fieldMapper.isEnableLatLon()) {
			throw new RebirthIllegalArgumentException("lat/lon is not enabled (indexed) for field ["
					+ fieldMapper.name() + "], can't use indexed filter on it");
		}

		if (topLeft.lon > bottomRight.lon) {
			return new LeftGeoBoundingBoxFilter(topLeft, bottomRight, fieldMapper);
		} else {
			return new RightGeoBoundingBoxFilter(topLeft, bottomRight, fieldMapper);
		}
	}

	/**
	 * The Class LeftGeoBoundingBoxFilter.
	 *
	 * @author l.xue.nong
	 */
	static class LeftGeoBoundingBoxFilter extends Filter {

		/** The lon filter1. */
		final Filter lonFilter1;

		/** The lon filter2. */
		final Filter lonFilter2;

		/** The lat filter. */
		final Filter latFilter;

		/**
		 * Instantiates a new left geo bounding box filter.
		 *
		 * @param topLeft the top left
		 * @param bottomRight the bottom right
		 * @param fieldMapper the field mapper
		 */
		public LeftGeoBoundingBoxFilter(Point topLeft, Point bottomRight, GeoPointFieldMapper fieldMapper) {
			lonFilter1 = fieldMapper.lonMapper().rangeFilter(null, bottomRight.lon, true, true);
			lonFilter2 = fieldMapper.lonMapper().rangeFilter(topLeft.lon, null, true, true);
			latFilter = fieldMapper.latMapper().rangeFilter(bottomRight.lat, topLeft.lat, true, true);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Filter#getDocIdSet(org.apache.lucene.index.IndexReader)
		 */
		@Override
		public FixedBitSet getDocIdSet(IndexReader reader) throws IOException {
			FixedBitSet main;
			DocIdSet set = lonFilter1.getDocIdSet(reader);
			if (set == null || set == DocIdSet.EMPTY_DOCIDSET) {
				main = null;
			} else {
				main = (FixedBitSet) set;
			}

			set = lonFilter2.getDocIdSet(reader);
			if (set == null || set == DocIdSet.EMPTY_DOCIDSET) {
				if (main == null) {
					return null;
				} else {

				}
			} else {
				if (main == null) {
					main = (FixedBitSet) set;
				} else {
					main.or((FixedBitSet) set);
				}
			}

			set = latFilter.getDocIdSet(reader);
			if (set == null || set == DocIdSet.EMPTY_DOCIDSET) {
				return null;
			}
			DocSets.and(main, set);
			return main;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			LeftGeoBoundingBoxFilter that = (LeftGeoBoundingBoxFilter) o;

			if (latFilter != null ? !latFilter.equals(that.latFilter) : that.latFilter != null)
				return false;
			if (lonFilter1 != null ? !lonFilter1.equals(that.lonFilter1) : that.lonFilter1 != null)
				return false;
			if (lonFilter2 != null ? !lonFilter2.equals(that.lonFilter2) : that.lonFilter2 != null)
				return false;

			return true;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			int result = lonFilter1 != null ? lonFilter1.hashCode() : 0;
			result = 31 * result + (lonFilter2 != null ? lonFilter2.hashCode() : 0);
			result = 31 * result + (latFilter != null ? latFilter.hashCode() : 0);
			return result;
		}
	}

	/**
	 * The Class RightGeoBoundingBoxFilter.
	 *
	 * @author l.xue.nong
	 */
	static class RightGeoBoundingBoxFilter extends Filter {

		/** The lon filter. */
		final Filter lonFilter;

		/** The lat filter. */
		final Filter latFilter;

		/**
		 * Instantiates a new right geo bounding box filter.
		 *
		 * @param topLeft the top left
		 * @param bottomRight the bottom right
		 * @param fieldMapper the field mapper
		 */
		public RightGeoBoundingBoxFilter(Point topLeft, Point bottomRight, GeoPointFieldMapper fieldMapper) {
			lonFilter = fieldMapper.lonMapper().rangeFilter(topLeft.lon, bottomRight.lon, true, true);
			latFilter = fieldMapper.latMapper().rangeFilter(bottomRight.lat, topLeft.lat, true, true);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Filter#getDocIdSet(org.apache.lucene.index.IndexReader)
		 */
		@Override
		public FixedBitSet getDocIdSet(IndexReader reader) throws IOException {
			FixedBitSet main;
			DocIdSet set = lonFilter.getDocIdSet(reader);
			if (set == null || set == DocIdSet.EMPTY_DOCIDSET) {
				return null;
			}
			main = (FixedBitSet) set;
			set = latFilter.getDocIdSet(reader);
			if (set == null || set == DocIdSet.EMPTY_DOCIDSET) {
				return null;
			}
			DocSets.and(main, set);
			return main;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			RightGeoBoundingBoxFilter that = (RightGeoBoundingBoxFilter) o;

			if (latFilter != null ? !latFilter.equals(that.latFilter) : that.latFilter != null)
				return false;
			if (lonFilter != null ? !lonFilter.equals(that.lonFilter) : that.lonFilter != null)
				return false;

			return true;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			int result = lonFilter != null ? lonFilter.hashCode() : 0;
			result = 31 * result + (latFilter != null ? latFilter.hashCode() : 0);
			return result;
		}
	}
}
