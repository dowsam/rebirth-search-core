/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GeoPointFieldMapper.java 2012-7-6 14:29:09 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.geo;

import static cn.com.rebirth.search.core.index.mapper.MapperBuilders.doubleField;
import static cn.com.rebirth.search.core.index.mapper.MapperBuilders.stringField;
import static cn.com.rebirth.search.core.index.mapper.core.TypeParsers.parsePathType;
import static cn.com.rebirth.search.core.index.mapper.core.TypeParsers.parseStore;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Field;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.mapper.ContentPath;
import cn.com.rebirth.search.core.index.mapper.FieldMapperListener;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ObjectMapperListener;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.DoubleFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.StringFieldMapper;
import cn.com.rebirth.search.core.index.mapper.object.ArrayValueMapperParser;
import cn.com.rebirth.search.core.index.search.geo.GeoHashUtils;
import cn.com.rebirth.search.core.index.search.geo.GeoUtils;
import cn.com.rebirth.search.index.analysis.NamedAnalyzer;

/**
 * The Class GeoPointFieldMapper.
 *
 * @author l.xue.nong
 */
public class GeoPointFieldMapper implements Mapper, ArrayValueMapperParser {

	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "geo_point";

	/**
	 * The Class Names.
	 *
	 * @author l.xue.nong
	 */
	public static class Names {

		/** The Constant LAT. */
		public static final String LAT = "lat";

		/** The Constant LAT_SUFFIX. */
		public static final String LAT_SUFFIX = "." + LAT;

		/** The Constant LON. */
		public static final String LON = "lon";

		/** The Constant LON_SUFFIX. */
		public static final String LON_SUFFIX = "." + LON;

		/** The Constant GEOHASH. */
		public static final String GEOHASH = "geohash";

		/** The Constant GEOHASH_SUFFIX. */
		public static final String GEOHASH_SUFFIX = "." + GEOHASH;
	}

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults {

		/** The Constant PATH_TYPE. */
		public static final ContentPath.Type PATH_TYPE = ContentPath.Type.FULL;

		/** The Constant STORE. */
		public static final Field.Store STORE = Field.Store.NO;

		/** The Constant ENABLE_LATLON. */
		public static final boolean ENABLE_LATLON = false;

		/** The Constant ENABLE_GEOHASH. */
		public static final boolean ENABLE_GEOHASH = false;

		/** The Constant PRECISION. */
		public static final int PRECISION = GeoHashUtils.PRECISION;

		/** The Constant NORMALIZE_LAT. */
		public static final boolean NORMALIZE_LAT = true;

		/** The Constant NORMALIZE_LON. */
		public static final boolean NORMALIZE_LON = true;

		/** The Constant VALIDATE_LAT. */
		public static final boolean VALIDATE_LAT = true;

		/** The Constant VALIDATE_LON. */
		public static final boolean VALIDATE_LON = true;
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends Mapper.Builder<Builder, GeoPointFieldMapper> {

		/** The path type. */
		private ContentPath.Type pathType = Defaults.PATH_TYPE;

		/** The enable geo hash. */
		private boolean enableGeoHash = Defaults.ENABLE_GEOHASH;

		/** The enable lat lon. */
		private boolean enableLatLon = Defaults.ENABLE_LATLON;

		/** The precision step. */
		private Integer precisionStep;

		/** The precision. */
		private int precision = Defaults.PRECISION;

		/** The store. */
		private Field.Store store = Defaults.STORE;

		/** The validate lat. */
		boolean validateLat = Defaults.VALIDATE_LAT;

		/** The validate lon. */
		boolean validateLon = Defaults.VALIDATE_LON;

		/** The normalize lat. */
		boolean normalizeLat = Defaults.NORMALIZE_LAT;

		/** The normalize lon. */
		boolean normalizeLon = Defaults.NORMALIZE_LON;

		/**
		 * Instantiates a new builder.
		 *
		 * @param name the name
		 */
		public Builder(String name) {
			super(name);
			this.builder = this;
		}

		/**
		 * Path type.
		 *
		 * @param pathType the path type
		 * @return the builder
		 */
		public Builder pathType(ContentPath.Type pathType) {
			this.pathType = pathType;
			return this;
		}

		/**
		 * Enable geo hash.
		 *
		 * @param enableGeoHash the enable geo hash
		 * @return the builder
		 */
		public Builder enableGeoHash(boolean enableGeoHash) {
			this.enableGeoHash = enableGeoHash;
			return this;
		}

		/**
		 * Enable lat lon.
		 *
		 * @param enableLatLon the enable lat lon
		 * @return the builder
		 */
		public Builder enableLatLon(boolean enableLatLon) {
			this.enableLatLon = enableLatLon;
			return this;
		}

		/**
		 * Precision step.
		 *
		 * @param precisionStep the precision step
		 * @return the builder
		 */
		public Builder precisionStep(int precisionStep) {
			this.precisionStep = precisionStep;
			return this;
		}

		/**
		 * Precision.
		 *
		 * @param precision the precision
		 * @return the builder
		 */
		public Builder precision(int precision) {
			this.precision = precision;
			return this;
		}

		/**
		 * Store.
		 *
		 * @param store the store
		 * @return the builder
		 */
		public Builder store(Field.Store store) {
			this.store = store;
			return this;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.Builder#build(cn.com.rebirth.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public GeoPointFieldMapper build(BuilderContext context) {
			ContentPath.Type origPathType = context.path().pathType();
			context.path().pathType(pathType);

			GeoStringFieldMapper geoStringMapper = new GeoStringFieldMapper.Builder(name)
					.index(Field.Index.NOT_ANALYZED).omitNorms(true).omitTermFreqAndPositions(true).includeInAll(false)
					.store(store).build(context);

			DoubleFieldMapper latMapper = null;
			DoubleFieldMapper lonMapper = null;

			context.path().add(name);
			if (enableLatLon) {
				NumberFieldMapper.Builder latMapperBuilder = doubleField(Names.LAT).includeInAll(false);
				NumberFieldMapper.Builder lonMapperBuilder = doubleField(Names.LON).includeInAll(false);
				if (precisionStep != null) {
					latMapperBuilder.precisionStep(precisionStep);
					lonMapperBuilder.precisionStep(precisionStep);
				}
				latMapper = (DoubleFieldMapper) latMapperBuilder.includeInAll(false).store(store).build(context);
				lonMapper = (DoubleFieldMapper) lonMapperBuilder.includeInAll(false).store(store).build(context);
			}
			StringFieldMapper geohashMapper = null;
			if (enableGeoHash) {
				geohashMapper = stringField(Names.GEOHASH).index(Field.Index.NOT_ANALYZED).includeInAll(false)
						.omitNorms(true).omitTermFreqAndPositions(true).build(context);
			}
			context.path().remove();

			context.path().pathType(origPathType);

			return new GeoPointFieldMapper(name, pathType, enableLatLon, enableGeoHash, precisionStep, precision,
					latMapper, lonMapper, geohashMapper, geoStringMapper, validateLon, validateLat, normalizeLon,
					normalizeLat);
		}
	}

	/**
	 * The Class TypeParser.
	 *
	 * @author l.xue.nong
	 */
	public static class TypeParser implements Mapper.TypeParser {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.TypeParser#parse(java.lang.String, java.util.Map, cn.com.rebirth.search.core.index.mapper.Mapper.TypeParser.ParserContext)
		 */
		@Override
		public Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext)
				throws MapperParsingException {
			Builder builder = new Builder(name);

			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String fieldName = Strings.toUnderscoreCase(entry.getKey());
				Object fieldNode = entry.getValue();
				if (fieldName.equals("path")) {
					builder.pathType(parsePathType(name, fieldNode.toString()));
				} else if (fieldName.equals("store")) {
					builder.store(parseStore(name, fieldNode.toString()));
				} else if (fieldName.equals("lat_lon")) {
					builder.enableLatLon(XContentMapValues.nodeBooleanValue(fieldNode));
				} else if (fieldName.equals("geohash")) {
					builder.enableGeoHash(XContentMapValues.nodeBooleanValue(fieldNode));
				} else if (fieldName.equals("precision_step")) {
					builder.precisionStep(XContentMapValues.nodeIntegerValue(fieldNode));
				} else if (fieldName.equals("geohash_precision")) {
					builder.precision(XContentMapValues.nodeIntegerValue(fieldNode));
				} else if (fieldName.equals("validate")) {
					builder.validateLat = XContentMapValues.nodeBooleanValue(fieldNode);
					builder.validateLon = XContentMapValues.nodeBooleanValue(fieldNode);
				} else if (fieldName.equals("validate_lon")) {
					builder.validateLon = XContentMapValues.nodeBooleanValue(fieldNode);
				} else if (fieldName.equals("validate_lat")) {
					builder.validateLat = XContentMapValues.nodeBooleanValue(fieldNode);
				} else if (fieldName.equals("normalize")) {
					builder.normalizeLat = XContentMapValues.nodeBooleanValue(fieldNode);
					builder.normalizeLon = XContentMapValues.nodeBooleanValue(fieldNode);
				} else if (fieldName.equals("normalize_lat")) {
					builder.normalizeLat = XContentMapValues.nodeBooleanValue(fieldNode);
				} else if (fieldName.equals("normalize_lon")) {
					builder.normalizeLon = XContentMapValues.nodeBooleanValue(fieldNode);
				}
			}
			return builder;
		}
	}

	/** The name. */
	private final String name;

	/** The path type. */
	private final ContentPath.Type pathType;

	/** The enable lat lon. */
	private final boolean enableLatLon;

	/** The enable geo hash. */
	private final boolean enableGeoHash;

	/** The precision step. */
	private final Integer precisionStep;

	/** The precision. */
	private final int precision;

	/** The lat mapper. */
	private final DoubleFieldMapper latMapper;

	/** The lon mapper. */
	private final DoubleFieldMapper lonMapper;

	/** The geohash mapper. */
	private final StringFieldMapper geohashMapper;

	/** The geo string mapper. */
	private final GeoStringFieldMapper geoStringMapper;

	/** The validate lon. */
	private final boolean validateLon;

	/** The validate lat. */
	private final boolean validateLat;

	/** The normalize lon. */
	private final boolean normalizeLon;

	/** The normalize lat. */
	private final boolean normalizeLat;

	/**
	 * Instantiates a new geo point field mapper.
	 *
	 * @param name the name
	 * @param pathType the path type
	 * @param enableLatLon the enable lat lon
	 * @param enableGeoHash the enable geo hash
	 * @param precisionStep the precision step
	 * @param precision the precision
	 * @param latMapper the lat mapper
	 * @param lonMapper the lon mapper
	 * @param geohashMapper the geohash mapper
	 * @param geoStringMapper the geo string mapper
	 * @param validateLon the validate lon
	 * @param validateLat the validate lat
	 * @param normalizeLon the normalize lon
	 * @param normalizeLat the normalize lat
	 */
	public GeoPointFieldMapper(String name, ContentPath.Type pathType, boolean enableLatLon, boolean enableGeoHash,
			Integer precisionStep, int precision, DoubleFieldMapper latMapper, DoubleFieldMapper lonMapper,
			StringFieldMapper geohashMapper, GeoStringFieldMapper geoStringMapper, boolean validateLon,
			boolean validateLat, boolean normalizeLon, boolean normalizeLat) {
		this.name = name;
		this.pathType = pathType;
		this.enableLatLon = enableLatLon;
		this.enableGeoHash = enableGeoHash;
		this.precisionStep = precisionStep;
		this.precision = precision;

		this.latMapper = latMapper;
		this.lonMapper = lonMapper;
		this.geoStringMapper = geoStringMapper;
		this.geohashMapper = geohashMapper;

		this.geoStringMapper.geoMapper = this;

		this.validateLat = validateLat;
		this.validateLon = validateLon;

		this.normalizeLat = normalizeLat;
		this.normalizeLon = normalizeLon;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#name()
	 */
	@Override
	public String name() {
		return this.name;
	}

	/**
	 * Lat mapper.
	 *
	 * @return the double field mapper
	 */
	public DoubleFieldMapper latMapper() {
		return latMapper;
	}

	/**
	 * Lon mapper.
	 *
	 * @return the double field mapper
	 */
	public DoubleFieldMapper lonMapper() {
		return lonMapper;
	}

	/**
	 * Checks if is enable lat lon.
	 *
	 * @return true, if is enable lat lon
	 */
	public boolean isEnableLatLon() {
		return enableLatLon;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#parse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void parse(ParseContext context) throws IOException {
		ContentPath.Type origPathType = context.path().pathType();
		context.path().pathType(pathType);
		context.path().add(name);

		XContentParser.Token token = context.parser().currentToken();
		if (token == XContentParser.Token.START_ARRAY) {
			token = context.parser().nextToken();
			if (token == XContentParser.Token.START_ARRAY) {

				while (token != XContentParser.Token.END_ARRAY) {
					token = context.parser().nextToken();
					double lon = context.parser().doubleValue();
					token = context.parser().nextToken();
					double lat = context.parser().doubleValue();
					while ((token = context.parser().nextToken()) != XContentParser.Token.END_ARRAY) {

					}
					parseLatLon(context, lat, lon);
					token = context.parser().nextToken();
				}
			} else {

				if (token == XContentParser.Token.VALUE_NUMBER) {
					double lon = context.parser().doubleValue();
					token = context.parser().nextToken();
					double lat = context.parser().doubleValue();
					while ((token = context.parser().nextToken()) != XContentParser.Token.END_ARRAY) {

					}
					parseLatLon(context, lat, lon);
				} else {
					while (token != XContentParser.Token.END_ARRAY) {
						if (token == XContentParser.Token.START_OBJECT) {
							parseObjectLatLon(context);
						} else if (token == XContentParser.Token.VALUE_STRING) {
							parseStringLatLon(context);
						}
						token = context.parser().nextToken();
					}
				}
			}
		} else if (token == XContentParser.Token.START_OBJECT) {
			parseObjectLatLon(context);
		} else if (token == XContentParser.Token.VALUE_STRING) {
			parseStringLatLon(context);
		}

		context.path().remove();
		context.path().pathType(origPathType);
	}

	/**
	 * Parses the string lat lon.
	 *
	 * @param context the context
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void parseStringLatLon(ParseContext context) throws IOException {
		String value = context.parser().text();
		int comma = value.indexOf(',');
		if (comma != -1) {
			double lat = Double.parseDouble(value.substring(0, comma).trim());
			double lon = Double.parseDouble(value.substring(comma + 1).trim());
			parseLatLon(context, lat, lon);
		} else {
			parseGeohash(context, value);
		}
	}

	/**
	 * Parses the object lat lon.
	 *
	 * @param context the context
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void parseObjectLatLon(ParseContext context) throws IOException {
		XContentParser.Token token;
		String currentName = context.parser().currentName();
		Double lat = null;
		Double lon = null;
		String geohash = null;
		while ((token = context.parser().nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentName = context.parser().currentName();
			} else if (token.isValue()) {
				if (currentName.equals(Names.LAT)) {
					lat = context.parser().doubleValue();
				} else if (currentName.equals(Names.LON)) {
					lon = context.parser().doubleValue();
				} else if (currentName.equals(Names.GEOHASH)) {
					geohash = context.parser().text();
				}
			}
		}
		if (geohash != null) {
			parseGeohash(context, geohash);
		} else if (lat != null && lon != null) {
			parseLatLon(context, lat, lon);
		}
	}

	/**
	 * Parses the lat lon.
	 *
	 * @param context the context
	 * @param lat the lat
	 * @param lon the lon
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void parseLatLon(ParseContext context, double lat, double lon) throws IOException {
		if (normalizeLon) {
			lon = GeoUtils.normalizeLon(lon);
		}
		if (normalizeLat) {
			lat = GeoUtils.normalizeLat(lat);
		}

		if (validateLat) {
			if (lat > 90.0 || lat < -90.0) {
				throw new RebirthIllegalArgumentException("illegal latitude value [" + lat + "] for " + name);
			}
		}
		if (validateLon) {
			if (lon > 180.0 || lon < -180) {
				throw new RebirthIllegalArgumentException("illegal longitude value [" + lon + "] for " + name);
			}
		}

		context.externalValue(Double.toString(lat) + ',' + Double.toString(lon));
		geoStringMapper.parse(context);
		if (enableGeoHash) {
			context.externalValue(GeoHashUtils.encode(lat, lon, precision));
			geohashMapper.parse(context);
		}
		if (enableLatLon) {
			context.externalValue(lat);
			latMapper.parse(context);
			context.externalValue(lon);
			lonMapper.parse(context);
		}
	}

	/**
	 * Parses the geohash.
	 *
	 * @param context the context
	 * @param geohash the geohash
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void parseGeohash(ParseContext context, String geohash) throws IOException {
		double[] values = GeoHashUtils.decode(geohash);
		double lat = values[0];
		double lon = values[1];

		if (normalizeLon) {
			lon = GeoUtils.normalizeLon(lon);
		}
		if (normalizeLat) {
			lat = GeoUtils.normalizeLat(lat);
		}

		if (validateLat) {
			if (lat > 90.0 || lat < -90.0) {
				throw new RebirthIllegalArgumentException("illegal latitude value [" + lat + "] for " + name);
			}
		}
		if (validateLon) {
			if (lon > 180.0 || lon < -180) {
				throw new RebirthIllegalArgumentException("illegal longitude value [" + lon + "] for " + name);
			}
		}

		context.externalValue(Double.toString(lat) + ',' + Double.toString(lon));
		geoStringMapper.parse(context);
		if (enableGeoHash) {
			context.externalValue(geohash);
			geohashMapper.parse(context);
		}
		if (enableLatLon) {
			context.externalValue(lat);
			latMapper.parse(context);
			context.externalValue(lon);
			lonMapper.parse(context);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#close()
	 */
	@Override
	public void close() {
		if (latMapper != null) {
			latMapper.close();
		}
		if (lonMapper != null) {
			lonMapper.close();
		}
		if (geohashMapper != null) {
			geohashMapper.close();
		}
		if (geoStringMapper != null) {
			geoStringMapper.close();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#merge(cn.com.rebirth.search.core.index.mapper.Mapper, cn.com.rebirth.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#traverse(cn.com.rebirth.search.core.index.mapper.FieldMapperListener)
	 */
	@Override
	public void traverse(FieldMapperListener fieldMapperListener) {
		geoStringMapper.traverse(fieldMapperListener);
		if (enableGeoHash) {
			geohashMapper.traverse(fieldMapperListener);
		}
		if (enableLatLon) {
			latMapper.traverse(fieldMapperListener);
			lonMapper.traverse(fieldMapperListener);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.Mapper#traverse(cn.com.rebirth.search.core.index.mapper.ObjectMapperListener)
	 */
	@Override
	public void traverse(ObjectMapperListener objectMapperListener) {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(name);
		builder.field("type", CONTENT_TYPE);
		if (pathType != Defaults.PATH_TYPE) {
			builder.field("path", pathType.name().toLowerCase());
		}
		if (enableLatLon != Defaults.ENABLE_LATLON) {
			builder.field("lat_lon", enableLatLon);
		}
		if (enableGeoHash != Defaults.ENABLE_GEOHASH) {
			builder.field("geohash", enableGeoHash);
		}
		if (geoStringMapper.store() != Defaults.STORE) {
			builder.field("store", geoStringMapper.store().name().toLowerCase());
		}
		if (precision != Defaults.PRECISION) {
			builder.field("geohash_precision", precision);
		}
		if (precisionStep != null) {
			builder.field("precision_step", precisionStep);
		}
		if (!validateLat && !validateLon) {
			builder.field("validate", false);
		} else {
			if (validateLat != Defaults.VALIDATE_LAT) {
				builder.field("validate_lat", validateLat);
			}
			if (validateLon != Defaults.VALIDATE_LON) {
				builder.field("validate_lon", validateLon);
			}
		}
		if (!normalizeLat && !normalizeLon) {
			builder.field("normalize", false);
		} else {
			if (normalizeLat != Defaults.NORMALIZE_LAT) {
				builder.field("normalize_lat", normalizeLat);
			}
			if (normalizeLon != Defaults.NORMALIZE_LON) {
				builder.field("normalize_lon", normalizeLon);
			}
		}

		builder.endObject();
		return builder;
	}

	/**
	 * The Class GeoStringFieldMapper.
	 *
	 * @author l.xue.nong
	 */
	public static class GeoStringFieldMapper extends StringFieldMapper {

		/**
		 * The Class Builder.
		 *
		 * @author l.xue.nong
		 */
		public static class Builder extends AbstractFieldMapper.OpenBuilder<Builder, StringFieldMapper> {

			/** The null value. */
			protected String nullValue = Defaults.NULL_VALUE;

			/**
			 * Instantiates a new builder.
			 *
			 * @param name the name
			 */
			public Builder(String name) {
				super(name);
				builder = this;
			}

			/**
			 * Null value.
			 *
			 * @param nullValue the null value
			 * @return the builder
			 */
			public Builder nullValue(String nullValue) {
				this.nullValue = nullValue;
				return this;
			}

			/* (non-Javadoc)
			 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#includeInAll(java.lang.Boolean)
			 */
			@Override
			public Builder includeInAll(Boolean includeInAll) {
				this.includeInAll = includeInAll;
				return this;
			}

			/* (non-Javadoc)
			 * @see cn.com.rebirth.search.core.index.mapper.Mapper.Builder#build(cn.com.rebirth.search.core.index.mapper.Mapper.BuilderContext)
			 */
			@Override
			public GeoStringFieldMapper build(BuilderContext context) {
				GeoStringFieldMapper fieldMapper = new GeoStringFieldMapper(buildNames(context), index, store,
						termVector, boost, omitNorms, omitTermFreqAndPositions, nullValue, indexAnalyzer,
						searchAnalyzer);
				fieldMapper.includeInAll(includeInAll);
				return fieldMapper;
			}
		}

		/** The geo mapper. */
		GeoPointFieldMapper geoMapper;

		/**
		 * Instantiates a new geo string field mapper.
		 *
		 * @param names the names
		 * @param index the index
		 * @param store the store
		 * @param termVector the term vector
		 * @param boost the boost
		 * @param omitNorms the omit norms
		 * @param omitTermFreqAndPositions the omit term freq and positions
		 * @param nullValue the null value
		 * @param indexAnalyzer the index analyzer
		 * @param searchAnalyzer the search analyzer
		 */
		public GeoStringFieldMapper(Names names, Field.Index index, Field.Store store, Field.TermVector termVector,
				float boost, boolean omitNorms, boolean omitTermFreqAndPositions, String nullValue,
				NamedAnalyzer indexAnalyzer, NamedAnalyzer searchAnalyzer) {
			super(names, index, store, termVector, boost, omitNorms, omitTermFreqAndPositions, nullValue,
					indexAnalyzer, searchAnalyzer);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#fieldDataType()
		 */
		@Override
		public FieldDataType fieldDataType() {
			return GeoPointFieldDataType.TYPE;
		}

		/**
		 * Geo mapper.
		 *
		 * @return the geo point field mapper
		 */
		public GeoPointFieldMapper geoMapper() {
			return geoMapper;
		}
	}
}
