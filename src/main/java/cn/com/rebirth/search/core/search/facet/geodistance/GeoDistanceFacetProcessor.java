/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GeoDistanceFacetProcessor.java 2012-3-29 15:01:12 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.geodistance;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.DistanceUnit;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldMapper;
import cn.com.rebirth.search.core.index.search.geo.GeoDistance;
import cn.com.rebirth.search.core.index.search.geo.GeoHashUtils;
import cn.com.rebirth.search.core.index.search.geo.GeoUtils;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetCollector;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.facet.FacetProcessor;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.Lists;


/**
 * The Class GeoDistanceFacetProcessor.
 *
 * @author l.xue.nong
 */
public class GeoDistanceFacetProcessor extends AbstractComponent implements FacetProcessor {

	
	/**
	 * Instantiates a new geo distance facet processor.
	 *
	 * @param settings the settings
	 */
	@Inject
	public GeoDistanceFacetProcessor(Settings settings) {
		super(settings);
		InternalGeoDistanceFacet.registerStreams();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetProcessor#types()
	 */
	@Override
	public String[] types() {
		return new String[] { GeoDistanceFacet.TYPE, "geoDistance" };
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetProcessor#parse(java.lang.String, cn.com.summall.search.commons.xcontent.XContentParser, cn.com.summall.search.core.search.internal.SearchContext)
	 */
	@Override
	public FacetCollector parse(String facetName, XContentParser parser, SearchContext context) throws IOException {
		String fieldName = null;
		String valueFieldName = null;
		String valueScript = null;
		String scriptLang = null;
		Map<String, Object> params = null;
		double lat = Double.NaN;
		double lon = Double.NaN;
		DistanceUnit unit = DistanceUnit.KILOMETERS;
		GeoDistance geoDistance = GeoDistance.ARC;
		List<GeoDistanceFacet.Entry> entries = Lists.newArrayList();

		boolean normalizeLon = true;
		boolean normalizeLat = true;

		XContentParser.Token token;
		String currentName = parser.currentName();

		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentName = parser.currentName();
			} else if (token == XContentParser.Token.START_ARRAY) {
				if ("ranges".equals(currentName) || "entries".equals(currentName)) {
					
					
					
					
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						double from = Double.NEGATIVE_INFINITY;
						double to = Double.POSITIVE_INFINITY;
						while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
							if (token == XContentParser.Token.FIELD_NAME) {
								currentName = parser.currentName();
							} else if (token.isValue()) {
								if ("from".equals(currentName)) {
									from = parser.doubleValue();
								} else if ("to".equals(currentName)) {
									to = parser.doubleValue();
								}
							}
						}
						entries.add(new GeoDistanceFacet.Entry(from, to, 0, 0, 0, Double.POSITIVE_INFINITY,
								Double.NEGATIVE_INFINITY));
					}
				} else {
					token = parser.nextToken();
					lon = parser.doubleValue();
					token = parser.nextToken();
					lat = parser.doubleValue();
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {

					}
					fieldName = currentName;
				}
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("params".equals(currentName)) {
					params = parser.map();
				} else {
					
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
				}
			} else if (token.isValue()) {
				if (currentName.equals("unit")) {
					unit = DistanceUnit.fromString(parser.text());
				} else if (currentName.equals("distance_type") || currentName.equals("distanceType")) {
					geoDistance = GeoDistance.fromString(parser.text());
				} else if ("value_field".equals(currentName) || "valueField".equals(currentName)) {
					valueFieldName = parser.text();
				} else if ("value_script".equals(currentName) || "valueScript".equals(currentName)) {
					valueScript = parser.text();
				} else if ("lang".equals(currentName)) {
					scriptLang = parser.text();
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

		if (Double.isNaN(lat) || Double.isNaN(lon)) {
			throw new FacetPhaseExecutionException(facetName, "lat/lon not set for geo_distance facet");
		}

		if (entries.isEmpty()) {
			throw new FacetPhaseExecutionException(facetName, "no ranges defined for geo_distance facet");
		}

		if (normalizeLat) {
			lat = GeoUtils.normalizeLat(lat);
		}
		if (normalizeLon) {
			lon = GeoUtils.normalizeLon(lon);
		}

		if (valueFieldName != null) {
			return new ValueGeoDistanceFacetCollector(facetName, fieldName, lat, lon, unit, geoDistance,
					entries.toArray(new GeoDistanceFacet.Entry[entries.size()]), context, valueFieldName);
		}

		if (valueScript != null) {
			return new ScriptGeoDistanceFacetCollector(facetName, fieldName, lat, lon, unit, geoDistance,
					entries.toArray(new GeoDistanceFacet.Entry[entries.size()]), context, scriptLang, valueScript,
					params);
		}

		return new GeoDistanceFacetCollector(facetName, fieldName, lat, lon, unit, geoDistance,
				entries.toArray(new GeoDistanceFacet.Entry[entries.size()]), context);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetProcessor#reduce(java.lang.String, java.util.List)
	 */
	@Override
	public Facet reduce(String name, List<Facet> facets) {
		InternalGeoDistanceFacet agg = null;
		for (Facet facet : facets) {
			InternalGeoDistanceFacet geoDistanceFacet = (InternalGeoDistanceFacet) facet;
			if (agg == null) {
				agg = geoDistanceFacet;
			} else {
				for (int i = 0; i < geoDistanceFacet.entries.length; i++) {
					GeoDistanceFacet.Entry aggEntry = agg.entries[i];
					GeoDistanceFacet.Entry currentEntry = geoDistanceFacet.entries[i];
					aggEntry.count += currentEntry.count;
					aggEntry.totalCount += currentEntry.totalCount;
					aggEntry.total += currentEntry.total;
					if (currentEntry.min < aggEntry.min) {
						aggEntry.min = currentEntry.min;
					}
					if (currentEntry.max > aggEntry.max) {
						aggEntry.max = currentEntry.max;
					}
				}
			}
		}
		return agg;
	}
}
