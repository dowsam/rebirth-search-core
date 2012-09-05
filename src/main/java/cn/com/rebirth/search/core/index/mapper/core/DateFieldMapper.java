/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DateFieldMapper.java 2012-7-6 14:29:53 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.core;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.NumericRangeFilter;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.NumericUtils;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.joda.DateMathParser;
import cn.com.rebirth.commons.joda.FormatDateTimeFormatter;
import cn.com.rebirth.commons.joda.Joda;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.search.commons.Numbers;
import cn.com.rebirth.search.core.index.analysis.NumericDateAnalyzer;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperBuilders;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.query.QueryParseContext;
import cn.com.rebirth.search.core.index.search.NumericRangeFieldDataFilter;
import cn.com.rebirth.search.index.analysis.NamedAnalyzer;

/**
 * The Class DateFieldMapper.
 *
 * @author l.xue.nong
 */
public class DateFieldMapper extends NumberFieldMapper<Long> {

	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "date";

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends NumberFieldMapper.Defaults {

		/** The Constant DATE_TIME_FORMATTER. */
		public static final FormatDateTimeFormatter DATE_TIME_FORMATTER = Joda.forPattern("dateOptionalTime");

		/** The Constant NULL_VALUE. */
		public static final String NULL_VALUE = null;

		/** The Constant TIME_UNIT. */
		public static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;

		/** The Constant PARSE_UPPER_INCLUSIVE. */
		public static final boolean PARSE_UPPER_INCLUSIVE = true;
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends NumberFieldMapper.Builder<Builder, DateFieldMapper> {

		/** The time unit. */
		protected TimeUnit timeUnit = Defaults.TIME_UNIT;

		/** The null value. */
		protected String nullValue = Defaults.NULL_VALUE;

		/** The date time formatter. */
		protected FormatDateTimeFormatter dateTimeFormatter = Defaults.DATE_TIME_FORMATTER;

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
		 * Time unit.
		 *
		 * @param timeUnit the time unit
		 * @return the builder
		 */
		public Builder timeUnit(TimeUnit timeUnit) {
			this.timeUnit = timeUnit;
			return this;
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

		/**
		 * Date time formatter.
		 *
		 * @param dateTimeFormatter the date time formatter
		 * @return the builder
		 */
		public Builder dateTimeFormatter(FormatDateTimeFormatter dateTimeFormatter) {
			this.dateTimeFormatter = dateTimeFormatter;
			return this;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.Builder#build(cn.com.rebirth.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public DateFieldMapper build(BuilderContext context) {
			boolean parseUpperInclusive = Defaults.PARSE_UPPER_INCLUSIVE;
			if (context.indexSettings() != null) {
				parseUpperInclusive = context.indexSettings().getAsBoolean("index.mapping.date.parse_upper_inclusive",
						Defaults.PARSE_UPPER_INCLUSIVE);
			}
			DateFieldMapper fieldMapper = new DateFieldMapper(buildNames(context), dateTimeFormatter, precisionStep,
					fuzzyFactor, index, store, boost, omitNorms, omitTermFreqAndPositions, nullValue, timeUnit,
					parseUpperInclusive);
			fieldMapper.includeInAll(includeInAll);
			return fieldMapper;
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
			DateFieldMapper.Builder builder = MapperBuilders.dateField(name);
			TypeParsers.parseNumberField(builder, name, node, parserContext);
			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String propName = Strings.toUnderscoreCase(entry.getKey());
				Object propNode = entry.getValue();
				if (propName.equals("null_value")) {
					builder.nullValue(propNode.toString());
				} else if (propName.equals("format")) {
					builder.dateTimeFormatter(TypeParsers.parseDateTimeFormatter(propName, propNode));
				} else if (propName.equals("numeric_resolution")) {
					builder.timeUnit(TimeUnit.valueOf(propNode.toString().toUpperCase()));
				}
			}
			return builder;
		}
	}

	/** The date time formatter. */
	protected final FormatDateTimeFormatter dateTimeFormatter;

	/** The parse upper inclusive. */
	private final boolean parseUpperInclusive;

	/** The date math parser. */
	private final DateMathParser dateMathParser;

	/** The null value. */
	private String nullValue;

	/** The time unit. */
	protected final TimeUnit timeUnit;

	/**
	 * Instantiates a new date field mapper.
	 *
	 * @param names the names
	 * @param dateTimeFormatter the date time formatter
	 * @param precisionStep the precision step
	 * @param fuzzyFactor the fuzzy factor
	 * @param index the index
	 * @param store the store
	 * @param boost the boost
	 * @param omitNorms the omit norms
	 * @param omitTermFreqAndPositions the omit term freq and positions
	 * @param nullValue the null value
	 * @param timeUnit the time unit
	 * @param parseUpperInclusive the parse upper inclusive
	 */
	protected DateFieldMapper(Names names, FormatDateTimeFormatter dateTimeFormatter, int precisionStep,
			String fuzzyFactor, Field.Index index, Field.Store store, float boost, boolean omitNorms,
			boolean omitTermFreqAndPositions, String nullValue, TimeUnit timeUnit, boolean parseUpperInclusive) {
		super(names, precisionStep, fuzzyFactor, index, store, boost, omitNorms, omitTermFreqAndPositions,
				new NamedAnalyzer("_date/" + precisionStep, new NumericDateAnalyzer(precisionStep,
						dateTimeFormatter.parser())), new NamedAnalyzer("_date/max", new NumericDateAnalyzer(
						Integer.MAX_VALUE, dateTimeFormatter.parser())));
		this.dateTimeFormatter = dateTimeFormatter;
		this.nullValue = nullValue;
		this.timeUnit = timeUnit;
		this.parseUpperInclusive = parseUpperInclusive;
		this.dateMathParser = new DateMathParser(dateTimeFormatter, timeUnit);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#parseFuzzyFactor(java.lang.String)
	 */
	@Override
	protected double parseFuzzyFactor(String fuzzyFactor) {
		if (fuzzyFactor == null) {
			return 1.0d;
		}
		try {
			return TimeValue.parseTimeValue(fuzzyFactor, null).millis();
		} catch (Exception e) {
			return Double.parseDouble(fuzzyFactor);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#maxPrecisionStep()
	 */
	@Override
	protected int maxPrecisionStep() {
		return 64;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#value(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public Long value(Fieldable field) {
		byte[] value = field.getBinaryValue();
		if (value == null) {
			return null;
		}
		return Numbers.bytesToLong(value);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#valueFromString(java.lang.String)
	 */
	@Override
	public Long valueFromString(String value) {
		return parseStringValue(value);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#valueForSearch(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public Object valueForSearch(Fieldable field) {
		return valueAsString(field);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#valueAsString(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public String valueAsString(Fieldable field) {
		Long value = value(field);
		if (value == null) {
			return null;
		}
		return dateTimeFormatter.printer().print(value);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#indexedValue(java.lang.String)
	 */
	@Override
	public String indexedValue(String value) {
		return NumericUtils.longToPrefixCoded(dateTimeFormatter.parser().parseMillis(value));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#fuzzyQuery(java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public Query fuzzyQuery(String value, String minSim, int prefixLength, int maxExpansions) {
		long iValue = dateMathParser.parse(value, System.currentTimeMillis());
		long iSim;
		try {
			iSim = TimeValue.parseTimeValue(minSim, null).millis();
		} catch (Exception e) {

			iSim = (long) Double.parseDouble(minSim);
		}
		return NumericRangeQuery.newLongRange(names.indexName(), precisionStep, iValue - iSim, iValue + iSim, true,
				true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#fuzzyQuery(java.lang.String, double, int, int)
	 */
	@Override
	public Query fuzzyQuery(String value, double minSim, int prefixLength, int maxExpansions) {
		long iValue = dateMathParser.parse(value, System.currentTimeMillis());
		long iSim = (long) (minSim * dFuzzyFactor);
		return NumericRangeQuery.newLongRange(names.indexName(), precisionStep, iValue - iSim, iValue + iSim, true,
				true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#fieldQuery(java.lang.String, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query fieldQuery(String value, @Nullable QueryParseContext context) {
		long now = context == null ? System.currentTimeMillis() : context.nowInMillis();
		long lValue = dateMathParser.parse(value, now);
		return NumericRangeQuery.newLongRange(names.indexName(), precisionStep, lValue, lValue, true, true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#rangeQuery(java.lang.String, java.lang.String, boolean, boolean, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query rangeQuery(String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper,
			@Nullable QueryParseContext context) {
		long now = context == null ? System.currentTimeMillis() : context.nowInMillis();
		return NumericRangeQuery.newLongRange(names.indexName(), precisionStep, lowerTerm == null ? null
				: dateMathParser.parse(lowerTerm, now), upperTerm == null ? null
				: (includeUpper && parseUpperInclusive) ? dateMathParser.parseUpperInclusive(upperTerm, now)
						: dateMathParser.parse(upperTerm, now), includeLower, includeUpper);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#fieldFilter(java.lang.String, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter fieldFilter(String value, @Nullable QueryParseContext context) {
		long now = context == null ? System.currentTimeMillis() : context.nowInMillis();
		long lValue = dateMathParser.parse(value, now);
		return NumericRangeFilter.newLongRange(names.indexName(), precisionStep, lValue, lValue, true, true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#rangeFilter(java.lang.String, java.lang.String, boolean, boolean, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter rangeFilter(String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper,
			@Nullable QueryParseContext context) {
		long now = context == null ? System.currentTimeMillis() : context.nowInMillis();
		return NumericRangeFilter.newLongRange(names.indexName(), precisionStep, lowerTerm == null ? null
				: dateMathParser.parse(lowerTerm, now), upperTerm == null ? null
				: (includeUpper && parseUpperInclusive) ? dateMathParser.parseUpperInclusive(upperTerm, now)
						: dateMathParser.parse(upperTerm, now), includeLower, includeUpper);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#rangeFilter(cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache, java.lang.String, java.lang.String, boolean, boolean, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter rangeFilter(FieldDataCache fieldDataCache, String lowerTerm, String upperTerm, boolean includeLower,
			boolean includeUpper, @Nullable QueryParseContext context) {
		long now = context == null ? System.currentTimeMillis() : context.nowInMillis();
		return NumericRangeFieldDataFilter.newLongRange(fieldDataCache, names.indexName(), lowerTerm == null ? null
				: dateMathParser.parse(lowerTerm, now), upperTerm == null ? null
				: (includeUpper && parseUpperInclusive) ? dateMathParser.parseUpperInclusive(upperTerm, now)
						: dateMathParser.parse(upperTerm, now), includeLower, includeUpper);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#customBoost()
	 */
	@Override
	protected boolean customBoost() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#parseCreateField(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	protected Fieldable parseCreateField(ParseContext context) throws IOException {
		String dateAsString = null;
		Long value = null;
		float boost = this.boost;
		if (context.externalValueSet()) {
			Object externalValue = context.externalValue();
			if (externalValue instanceof Number) {
				value = ((Number) externalValue).longValue();
			} else {
				dateAsString = (String) externalValue;
				if (dateAsString == null) {
					dateAsString = nullValue;
				}
			}
		} else {
			XContentParser parser = context.parser();
			XContentParser.Token token = parser.currentToken();
			if (token == XContentParser.Token.VALUE_NULL) {
				dateAsString = nullValue;
			} else if (token == XContentParser.Token.VALUE_NUMBER) {
				value = parser.longValue();
			} else if (token == XContentParser.Token.START_OBJECT) {
				String currentFieldName = null;
				while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
					if (token == XContentParser.Token.FIELD_NAME) {
						currentFieldName = parser.currentName();
					} else {
						if ("value".equals(currentFieldName) || "_value".equals(currentFieldName)) {
							if (token == XContentParser.Token.VALUE_NULL) {
								dateAsString = nullValue;
							} else if (token == XContentParser.Token.VALUE_NUMBER) {
								value = parser.longValue();
							} else {
								dateAsString = parser.text();
							}
						} else if ("boost".equals(currentFieldName) || "_boost".equals(currentFieldName)) {
							boost = parser.floatValue();
						}
					}
				}
			} else {
				dateAsString = parser.text();
			}
		}

		if (value != null) {
			LongFieldMapper.CustomLongNumericField field = new LongFieldMapper.CustomLongNumericField(this,
					timeUnit.toMillis(value));
			field.setBoost(boost);
			return field;
		}

		if (dateAsString == null) {
			return null;
		}
		if (context.includeInAll(includeInAll, this)) {
			context.allEntries().addText(names.fullName(), dateAsString, boost);
		}

		value = parseStringValue(dateAsString);
		LongFieldMapper.CustomLongNumericField field = new LongFieldMapper.CustomLongNumericField(this, value);
		field.setBoost(boost);
		return field;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#fieldDataType()
	 */
	@Override
	public FieldDataType fieldDataType() {
		return FieldDataType.DefaultTypes.LONG;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#contentType()
	 */
	@Override
	protected String contentType() {
		return CONTENT_TYPE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#merge(cn.com.rebirth.search.core.index.mapper.Mapper, cn.com.rebirth.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
		super.merge(mergeWith, mergeContext);
		if (!this.getClass().equals(mergeWith.getClass())) {
			return;
		}
		if (!mergeContext.mergeFlags().simulate()) {
			this.nullValue = ((DateFieldMapper) mergeWith).nullValue;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#doXContentBody(cn.com.rebirth.search.commons.xcontent.XContentBuilder)
	 */
	@Override
	protected void doXContentBody(XContentBuilder builder) throws IOException {
		super.doXContentBody(builder);
		if (index != Defaults.INDEX) {
			builder.field("index", index.name().toLowerCase());
		}
		if (store != Defaults.STORE) {
			builder.field("store", store.name().toLowerCase());
		}
		if (termVector != Defaults.TERM_VECTOR) {
			builder.field("term_vector", termVector.name().toLowerCase());
		}
		if (omitNorms != Defaults.OMIT_NORMS) {
			builder.field("omit_norms", omitNorms);
		}
		if (omitTermFreqAndPositions != Defaults.OMIT_TERM_FREQ_AND_POSITIONS) {
			builder.field("omit_term_freq_and_positions", omitTermFreqAndPositions);
		}
		if (precisionStep != Defaults.PRECISION_STEP) {
			builder.field("precision_step", precisionStep);
		}
		if (fuzzyFactor != Defaults.FUZZY_FACTOR) {
			builder.field("fuzzy_factor", fuzzyFactor);
		}
		builder.field("format", dateTimeFormatter.format());
		if (nullValue != null) {
			builder.field("null_value", nullValue);
		}
		if (includeInAll != null) {
			builder.field("include_in_all", includeInAll);
		}
		if (timeUnit != Defaults.TIME_UNIT) {
			builder.field("numeric_resolution", timeUnit.name().toLowerCase());
		}
	}

	/**
	 * Parses the string value.
	 *
	 * @param value the value
	 * @return the long
	 */
	private long parseStringValue(String value) {
		try {
			return dateTimeFormatter.parser().parseMillis(value);
		} catch (RuntimeException e) {
			try {
				long time = Long.parseLong(value);
				return timeUnit.toMillis(time);
			} catch (NumberFormatException e1) {
				throw new MapperParsingException("failed to parse date field [" + value + "], tried both date format ["
						+ dateTimeFormatter.format() + "], and timestamp number", e);
			}
		}
	}
}