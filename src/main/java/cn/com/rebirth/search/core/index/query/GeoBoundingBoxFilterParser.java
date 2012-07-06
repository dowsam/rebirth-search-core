/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GeoBoundingBoxFilterParser.java 2012-3-29 15:01:41 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Filter;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldMapper;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;
import cn.com.rebirth.search.core.index.search.geo.GeoHashUtils;
import cn.com.rebirth.search.core.index.search.geo.GeoUtils;
import cn.com.rebirth.search.core.index.search.geo.InMemoryGeoBoundingBoxFilter;
import cn.com.rebirth.search.core.index.search.geo.IndexedGeoBoundingBoxFilter;
import cn.com.rebirth.search.core.index.search.geo.Point;


/**
 * The Class GeoBoundingBoxFilterParser.
 *
 * @author l.xue.nong
 */
public class GeoBoundingBoxFilterParser implements FilterParser {

	
	/** The Constant NAME. */
	public static final String NAME = "geo_bbox";

	
	/**
	 * Instantiates a new geo bounding box filter parser.
	 */
	@Inject
	public GeoBoundingBoxFilterParser() {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.FilterParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, "geoBbox", "geo_bounding_box", "geoBoundingBox" };
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.FilterParser#parse(cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		boolean cache = false;
		CacheKeyFilter.Key cacheKey = null;
		String fieldName = null;
		Point topLeft = new Point();
		Point bottomRight = new Point();

		String filterName = null;
		String currentFieldName = null;
		XContentParser.Token token;
		boolean normalizeLon = true;
		boolean normalizeLat = true;

		String type = "memory";

		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				fieldName = currentFieldName;

				while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
					if (token == XContentParser.Token.FIELD_NAME) {
						currentFieldName = parser.currentName();
					} else if (token == XContentParser.Token.START_ARRAY) {
						Point point = null;
						if ("top_left".equals(currentFieldName) || "topLeft".equals(currentFieldName)) {
							point = topLeft;
						} else if ("bottom_right".equals(currentFieldName) || "bottomRight".equals(currentFieldName)) {
							point = bottomRight;
						}

						if (point != null) {
							token = parser.nextToken();
							point.lon = parser.doubleValue();
							token = parser.nextToken();
							point.lat = parser.doubleValue();
							while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {

							}
						}
					} else if (token == XContentParser.Token.START_OBJECT) {
						Point point = null;
						if ("top_left".equals(currentFieldName) || "topLeft".equals(currentFieldName)) {
							point = topLeft;
						} else if ("bottom_right".equals(currentFieldName) || "bottomRight".equals(currentFieldName)) {
							point = bottomRight;
						}

						if (point != null) {
							while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
								if (token == XContentParser.Token.FIELD_NAME) {
									currentFieldName = parser.currentName();
								} else if (token.isValue()) {
									if (currentFieldName.equals(GeoPointFieldMapper.Names.LAT)) {
										point.lat = parser.doubleValue();
									} else if (currentFieldName.equals(GeoPointFieldMapper.Names.LON)) {
										point.lon = parser.doubleValue();
									} else if (currentFieldName.equals(GeoPointFieldMapper.Names.GEOHASH)) {
										double[] values = GeoHashUtils.decode(parser.text());
										point.lat = values[0];
										point.lon = values[1];
									}
								}
							}
						}
					} else if (token.isValue()) {
						if ("field".equals(currentFieldName)) {
							fieldName = parser.text();
						} else {
							Point point = null;
							if ("top_left".equals(currentFieldName) || "topLeft".equals(currentFieldName)) {
								point = topLeft;
							} else if ("bottom_right".equals(currentFieldName)
									|| "bottomRight".equals(currentFieldName)) {
								point = bottomRight;
							}

							if (point != null) {
								String value = parser.text();
								int comma = value.indexOf(',');
								if (comma != -1) {
									point.lat = Double.parseDouble(value.substring(0, comma).trim());
									point.lon = Double.parseDouble(value.substring(comma + 1).trim());
								} else {
									double[] values = GeoHashUtils.decode(value);
									point.lat = values[0];
									point.lon = values[1];
								}
							}
						}
					}
				}
			} else if (token.isValue()) {
				if ("_name".equals(currentFieldName)) {
					filterName = parser.text();
				} else if ("_cache".equals(currentFieldName)) {
					cache = parser.booleanValue();
				} else if ("_cache_key".equals(currentFieldName) || "_cacheKey".equals(currentFieldName)) {
					cacheKey = new CacheKeyFilter.Key(parser.text());
				} else if ("normalize".equals(currentFieldName)) {
					normalizeLat = parser.booleanValue();
					normalizeLon = parser.booleanValue();
				} else if ("type".equals(currentFieldName)) {
					type = parser.text();
				} else {
					throw new QueryParsingException(parseContext.index(), "[qeo_bbox] filter does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		if (normalizeLat) {
			topLeft.lat = GeoUtils.normalizeLat(topLeft.lat);
			bottomRight.lat = GeoUtils.normalizeLat(bottomRight.lat);
		}
		if (normalizeLon) {
			topLeft.lon = GeoUtils.normalizeLon(topLeft.lon);
			bottomRight.lon = GeoUtils.normalizeLon(bottomRight.lon);
		}

		MapperService.SmartNameFieldMappers smartMappers = parseContext.smartFieldMappers(fieldName);
		if (smartMappers == null || !smartMappers.hasMapper()) {
			throw new QueryParsingException(parseContext.index(), "failed to find geo_point field [" + fieldName + "]");
		}
		FieldMapper mapper = smartMappers.mapper();
		if (!(mapper instanceof GeoPointFieldMapper.GeoStringFieldMapper)) {
			throw new QueryParsingException(parseContext.index(), "field [" + fieldName + "] is not a geo_point field");
		}
		GeoPointFieldMapper geoMapper = ((GeoPointFieldMapper.GeoStringFieldMapper) mapper).geoMapper();

		fieldName = mapper.names().indexName();

		Filter filter;
		if ("indexed".equals(type)) {
			filter = IndexedGeoBoundingBoxFilter.create(topLeft, bottomRight, geoMapper);
		} else if ("memory".equals(type)) {
			filter = new InMemoryGeoBoundingBoxFilter(topLeft, bottomRight, fieldName, parseContext.indexCache()
					.fieldData());
		} else {
			throw new QueryParsingException(parseContext.index(), "geo bounding box type [" + type
					+ "] not supported, either 'indexed' or 'memory' are allowed");
		}

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
