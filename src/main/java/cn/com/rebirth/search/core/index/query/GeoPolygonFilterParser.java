/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GeoPolygonFilterParser.java 2012-3-29 15:02:22 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.search.Filter;

import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldDataType;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldMapper;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;
import cn.com.rebirth.search.core.index.search.geo.GeoHashUtils;
import cn.com.rebirth.search.core.index.search.geo.GeoPolygonFilter;
import cn.com.rebirth.search.core.index.search.geo.GeoUtils;
import cn.com.rebirth.search.core.index.search.geo.Point;

import com.google.common.collect.Lists;


/**
 * The Class GeoPolygonFilterParser.
 *
 * @author l.xue.nong
 */
public class GeoPolygonFilterParser implements FilterParser {

	
	/** The Constant NAME. */
	public static final String NAME = "geo_polygon";

	
	/**
	 * Instantiates a new geo polygon filter parser.
	 */
	@Inject
	public GeoPolygonFilterParser() {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.FilterParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, "geoPolygon" };
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
		List<Point> points = Lists.newArrayList();

		boolean normalizeLon = true;
		boolean normalizeLat = true;

		String filterName = null;
		String currentFieldName = null;
		XContentParser.Token token;

		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				fieldName = currentFieldName;

				while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
					if (token == XContentParser.Token.FIELD_NAME) {
						currentFieldName = parser.currentName();
					} else if (token == XContentParser.Token.START_ARRAY) {
						if ("points".equals(currentFieldName)) {
							while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
								if (token == XContentParser.Token.FIELD_NAME) {
									currentFieldName = parser.currentName();
								} else if (token == XContentParser.Token.START_ARRAY) {
									Point point = new Point();
									token = parser.nextToken();
									point.lon = parser.doubleValue();
									token = parser.nextToken();
									point.lat = parser.doubleValue();
									while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {

									}
									points.add(point);
								} else if (token == XContentParser.Token.START_OBJECT) {
									Point point = new Point();
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
									points.add(point);
								} else if (token.isValue()) {
									Point point = new Point();
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
									points.add(point);
								}
							}
						} else {
							throw new QueryParsingException(parseContext.index(),
									"[geo_polygon] filter does not support [" + currentFieldName + "]");
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
				} else {
					throw new QueryParsingException(parseContext.index(), "[geo_polygon] filter does not support ["
							+ currentFieldName + "]");
				}
			}
		}

		if (points.isEmpty()) {
			throw new QueryParsingException(parseContext.index(), "no points defined for geo_polygon filter");
		}

		for (Point point : points) {
			if (normalizeLat) {
				point.lat = GeoUtils.normalizeLat(point.lat);
			}
			if (normalizeLon) {
				point.lon = GeoUtils.normalizeLon(point.lon);
			}
		}

		MapperService.SmartNameFieldMappers smartMappers = parseContext.smartFieldMappers(fieldName);
		if (smartMappers == null || !smartMappers.hasMapper()) {
			throw new QueryParsingException(parseContext.index(), "failed to find geo_point field [" + fieldName + "]");
		}
		FieldMapper mapper = smartMappers.mapper();
		if (mapper.fieldDataType() != GeoPointFieldDataType.TYPE) {
			throw new QueryParsingException(parseContext.index(), "field [" + fieldName + "] is not a geo_point field");
		}
		fieldName = mapper.names().indexName();

		Filter filter = new GeoPolygonFilter(points.toArray(new Point[points.size()]), fieldName, parseContext
				.indexCache().fieldData());
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
