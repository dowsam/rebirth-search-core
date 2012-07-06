/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GeoDistanceFilterParser.java 2012-3-29 15:00:59 l.xue.nong$$
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
import cn.com.rebirth.search.core.index.search.geo.GeoDistanceFilter;
import cn.com.rebirth.search.core.index.search.geo.GeoHashUtils;
import cn.com.rebirth.search.core.index.search.geo.GeoUtils;


/**
 * The Class GeoDistanceFilterParser.
 *
 * @author l.xue.nong
 */
public class GeoDistanceFilterParser implements FilterParser {

	
	/** The Constant NAME. */
	public static final String NAME = "geo_distance";

	
	/**
	 * Instantiates a new geo distance filter parser.
	 */
	@Inject
	public GeoDistanceFilterParser() {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.FilterParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, "geoDistance" };
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.FilterParser#parse(cn.com.summall.search.core.index.query.QueryParseContext)
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
		double distance = 0;
		Object vDistance = null;
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
						} else {
							throw new QueryParsingException(parseContext.index(),
									"[geo_distance] filter does not support [" + currentFieldName + "]");
						}
					}
				}
			} else if (token.isValue()) {
				if (currentFieldName.equals("distance")) {
					if (token == XContentParser.Token.VALUE_STRING) {
						vDistance = parser.text(); 
					} else {
						vDistance = parser.numberValue(); 
					}
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

		if (vDistance instanceof Number) {
			distance = unit.toMiles(((Number) vDistance).doubleValue());
		} else {
			distance = DistanceUnit.parse((String) vDistance, unit, DistanceUnit.MILES);
		}
		distance = geoDistance.normalize(distance, DistanceUnit.MILES);

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

		Filter filter = new GeoDistanceFilter(lat, lon, distance, geoDistance, fieldName, geoMapper, parseContext
				.indexCache().fieldData(), optimizeBbox);
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
