/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GeoDistanceSortParser.java 2012-7-6 14:28:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.sort;

import org.apache.lucene.search.SortField;

import cn.com.rebirth.commons.unit.DistanceUnit;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldMapper;
import cn.com.rebirth.search.core.index.search.geo.GeoDistance;
import cn.com.rebirth.search.core.index.search.geo.GeoDistanceDataComparator;
import cn.com.rebirth.search.core.index.search.geo.GeoHashUtils;
import cn.com.rebirth.search.core.index.search.geo.GeoUtils;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class GeoDistanceSortParser.
 *
 * @author l.xue.nong
 */
public class GeoDistanceSortParser implements SortParser {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.sort.SortParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { "_geo_distance", "_geoDistance" };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.sort.SortParser#parse(cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public SortField parse(XContentParser parser, SearchContext context) throws Exception {
		String fieldName = null;
		double lat = Double.NaN;
		double lon = Double.NaN;
		DistanceUnit unit = DistanceUnit.KILOMETERS;
		GeoDistance geoDistance = GeoDistance.ARC;
		boolean reverse = false;

		boolean normalizeLon = true;
		boolean normalizeLat = true;

		XContentParser.Token token;
		String currentName = parser.currentName();
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentName = parser.currentName();
			} else if (token == XContentParser.Token.START_ARRAY) {
				token = parser.nextToken();
				lon = parser.doubleValue();
				token = parser.nextToken();
				lat = parser.doubleValue();
				while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {

				}
				fieldName = currentName;
			} else if (token == XContentParser.Token.START_OBJECT) {

				fieldName = currentName;
				while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
					if (token == XContentParser.Token.FIELD_NAME) {
						currentName = parser.currentName();
					} else if (token.isValue()) {
						if (currentName.equals(GeoPointFieldMapper.Names.LAT)) {
							lat = parser.doubleValue();
						} else if (currentName.equals(GeoPointFieldMapper.Names.LON)) {
							lon = parser.doubleValue();
						} else if (currentName.equals(GeoPointFieldMapper.Names.GEOHASH)) {
							double[] values = GeoHashUtils.decode(parser.text());
							lat = values[0];
							lon = values[1];
						}
					}
				}
			} else if (token.isValue()) {
				if ("reverse".equals(currentName)) {
					reverse = parser.booleanValue();
				} else if ("order".equals(currentName)) {
					reverse = "desc".equals(parser.text());
				} else if (currentName.equals("unit")) {
					unit = DistanceUnit.fromString(parser.text());
				} else if (currentName.equals("distance_type") || currentName.equals("distanceType")) {
					geoDistance = GeoDistance.fromString(parser.text());
				} else if ("normalize".equals(currentName)) {
					normalizeLat = parser.booleanValue();
					normalizeLon = parser.booleanValue();
				} else {

					String value = parser.text();
					int comma = value.indexOf(',');
					if (comma != -1) {
						lat = Double.parseDouble(value.substring(0, comma).trim());
						lon = Double.parseDouble(value.substring(comma + 1).trim());
					} else {
						double[] values = GeoHashUtils.decode(value);
						lat = values[0];
						lon = values[1];
					}

					fieldName = currentName;
				}
			}
		}

		if (normalizeLat) {
			lat = GeoUtils.normalizeLat(lat);
		}
		if (normalizeLon) {
			lon = GeoUtils.normalizeLon(lon);
		}

		return new SortField(fieldName, GeoDistanceDataComparator.comparatorSource(fieldName, lat, lon, unit,
				geoDistance, context.fieldDataCache(), context.mapperService()), reverse);
	}
}
