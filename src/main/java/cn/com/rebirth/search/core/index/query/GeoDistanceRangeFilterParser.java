/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GeoDistanceRangeFilterParser.java 2012-7-6 14:28:59 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Filter;

import cn.com.rebirth.commons.unit.DistanceUnit;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldDataType;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldMapper;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;
import cn.com.rebirth.search.core.index.search.geo.GeoDistance;
import cn.com.rebirth.search.core.index.search.geo.GeoDistanceRangeFilter;
import cn.com.rebirth.search.core.index.search.geo.GeoHashUtils;
import cn.com.rebirth.search.core.index.search.geo.GeoUtils;

/**
 * The Class GeoDistanceRangeFilterParser.
 *
 * @author l.xue.nong
 */
public class GeoDistanceRangeFilterParser implements FilterParser {

	/** The Constant NAME. */
	public static final String NAME = "geo_distance_range";

	/**
	 * Instantiates a new geo distance range filter parser.
	 */
	@Inject
	public GeoDistanceRangeFilterParser() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.FilterParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, "geoDistanceRange" };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.FilterParser#parse(cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		XContentParser.Token token;

		boolean cache = false;
		CacheKeyFilter.Key cacheKey = null;
		String filterName = null;
		String currentFieldName = null;
		double lat = 0;
		double lon = 0;
		String fieldName = null;
		Object vFrom = null;
		Object vTo = null;
		boolean includeLower = true;
		boolean includeUpper = true;
		DistanceUnit unit = DistanceUnit.KILOMETERS;
		GeoDistance geoDistance = GeoDistance.ARC;
		String optimizeBbox = "memory";
		boolean normalizeLon = true;
		boolean normalizeLat = true;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_ARRAY) {
				token = parser.nextToken();
				lon = parser.doubleValue();
				token = parser.nextToken();
				lat = parser.doubleValue();
				while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {

				}
				fieldName = currentFieldName;
			} else if (token == XContentParser.Token.START_OBJECT) {

				String currentName = parser.currentName();
				fieldName = currentFieldName;
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
				if (currentFieldName.equals("from")) {
					if (token == XContentParser.Token.VALUE_NULL) {
					} else if (token == XContentParser.Token.VALUE_STRING) {
						vFrom = parser.text();
					} else {
						vFrom = parser.numberValue();
					}
				} else if (currentFieldName.equals("to")) {
					if (token == XContentParser.Token.VALUE_NULL) {
					}
					if (token == XContentParser.Token.VALUE_STRING) {
						vTo = parser.text();
					} else {
						vTo = parser.numberValue();
					}
				} else if ("include_lower".equals(currentFieldName) || "includeLower".equals(currentFieldName)) {
					includeLower = parser.booleanValue();
				} else if ("include_upper".equals(currentFieldName) || "includeUpper".equals(currentFieldName)) {
					includeUpper = parser.booleanValue();
				} else if ("gt".equals(currentFieldName)) {
					if (token == XContentParser.Token.VALUE_NULL) {
					} else if (token == XContentParser.Token.VALUE_STRING) {
						vFrom = parser.text();
					} else {
						vFrom = parser.numberValue();
					}
					includeLower = false;
				} else if ("gte".equals(currentFieldName) || "ge".equals(currentFieldName)) {
					if (token == XContentParser.Token.VALUE_NULL) {
					} else if (token == XContentParser.Token.VALUE_STRING) {
						vFrom = parser.text();
					} else {
						vFrom = parser.numberValue();
					}
					includeLower = true;
				} else if ("lt".equals(currentFieldName)) {
					if (token == XContentParser.Token.VALUE_NULL) {
					}
					if (token == XContentParser.Token.VALUE_STRING) {
						vTo = parser.text();
					} else {
						vTo = parser.numberValue();
					}
					includeUpper = false;
				} else if ("lte".equals(currentFieldName) || "le".equals(currentFieldName)) {
					if (token == XContentParser.Token.VALUE_NULL) {
					}
					if (token == XContentParser.Token.VALUE_STRING) {
						vTo = parser.text();
					} else {
						vTo = parser.numberValue();
					}
					includeUpper = true;
				} else if (currentFieldName.equals("unit")) {
					unit = DistanceUnit.fromString(parser.text());
				} else if (currentFieldName.equals("distance_type") || currentFieldName.equals("distanceType")) {
					geoDistance = GeoDistance.fromString(parser.text());
				} else if (currentFieldName.endsWith(GeoPointFieldMapper.Names.LAT_SUFFIX)) {
					lat = parser.doubleValue();
					fieldName = currentFieldName.substring(0, currentFieldName.length()
							- GeoPointFieldMapper.Names.LAT_SUFFIX.length());
				} else if (currentFieldName.endsWith(GeoPointFieldMapper.Names.LON_SUFFIX)) {
					lon = parser.doubleValue();
					fieldName = currentFieldName.substring(0, currentFieldName.length()
							- GeoPointFieldMapper.Names.LON_SUFFIX.length());
				} else if (currentFieldName.endsWith(GeoPointFieldMapper.Names.GEOHASH_SUFFIX)) {
					double[] values = GeoHashUtils.decode(parser.text());
					lat = values[0];
					lon = values[1];
					fieldName = currentFieldName.substring(0, currentFieldName.length()
							- GeoPointFieldMapper.Names.GEOHASH_SUFFIX.length());
				} else if ("_name".equals(currentFieldName)) {
					filterName = parser.text();
				} else if ("_cache".equals(currentFieldName)) {
					cache = parser.booleanValue();
				} else if ("_cache_key".equals(currentFieldName) || "_cacheKey".equals(currentFieldName)) {
					cacheKey = new CacheKeyFilter.Key(parser.text());
				} else if ("optimize_bbox".equals(currentFieldName) || "optimizeBbox".equals(currentFieldName)) {
					optimizeBbox = parser.textOrNull();
				} else if ("normalize".equals(currentFieldName)) {
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
					fieldName = currentFieldName;
				}
			}
		}

		double from;
		double to;
		if (vFrom instanceof Number) {
			from = unit.toMiles(((Number) vFrom).doubleValue());
		} else {
			from = DistanceUnit.parse((String) vFrom, unit, DistanceUnit.MILES);
		}
		from = geoDistance.normalize(from, DistanceUnit.MILES);
		if (vTo instanceof Number) {
			to = unit.toMiles(((Number) vTo).doubleValue());
		} else {
			to = DistanceUnit.parse((String) vTo, unit, DistanceUnit.MILES);
		}
		to = geoDistance.normalize(to, DistanceUnit.MILES);

		if (normalizeLat) {
			lat = GeoUtils.normalizeLat(lat);
		}
		if (normalizeLon) {
			lon = GeoUtils.normalizeLon(lon);
		}

		MapperService.SmartNameFieldMappers smartMappers = parseContext.smartFieldMappers(fieldName);
		if (smartMappers == null || !smartMappers.hasMapper()) {
			throw new QueryParsingException(parseContext.index(), "failed to find geo_point field [" + fieldName + "]");
		}
		FieldMapper mapper = smartMappers.mapper();
		if (mapper.fieldDataType() != GeoPointFieldDataType.TYPE) {
			throw new QueryParsingException(parseContext.index(), "field [" + fieldName + "] is not a geo_point field");
		}
		GeoPointFieldMapper geoMapper = ((GeoPointFieldMapper.GeoStringFieldMapper) mapper).geoMapper();
		fieldName = mapper.names().indexName();

		Filter filter = new GeoDistanceRangeFilter(lat, lon, from, to, includeLower, includeUpper, geoDistance,
				fieldName, geoMapper, parseContext.indexCache().fieldData(), optimizeBbox);
		if (cache) {
			filter = parseContext.cacheFilter(filter, cacheKey);
		}
		filter = QueryParsers.wrapSmartNameFilter(filter, smartMappers, parseContext);
		if (filterName != null) {
			parseContext.addNamedFilter(filterName, filter);
		}
		return filter;
	}
}
