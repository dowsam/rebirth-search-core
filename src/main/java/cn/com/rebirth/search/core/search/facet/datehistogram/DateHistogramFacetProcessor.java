/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DateHistogramFacetProcessor.java 2012-7-6 14:30:20 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.datehistogram;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.joda.time.Chronology;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.joda.TimeZoneRounding;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetCollector;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.facet.FacetProcessor;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.ImmutableMap;

/**
 * The Class DateHistogramFacetProcessor.
 *
 * @author l.xue.nong
 */
public class DateHistogramFacetProcessor extends AbstractComponent implements FacetProcessor {

	/** The date field parsers. */
	private final ImmutableMap<String, DateFieldParser> dateFieldParsers;

	/**
	 * Instantiates a new date histogram facet processor.
	 *
	 * @param settings the settings
	 */
	@Inject
	public DateHistogramFacetProcessor(Settings settings) {
		super(settings);
		InternalDateHistogramFacet.registerStreams();

		dateFieldParsers = MapBuilder.<String, DateFieldParser> newMapBuilder()
				.put("year", new DateFieldParser.YearOfCentury()).put("1y", new DateFieldParser.YearOfCentury())
				.put("month", new DateFieldParser.MonthOfYear()).put("1m", new DateFieldParser.MonthOfYear())
				.put("week", new DateFieldParser.WeekOfWeekyear()).put("1w", new DateFieldParser.WeekOfWeekyear())
				.put("day", new DateFieldParser.DayOfMonth()).put("1d", new DateFieldParser.DayOfMonth())
				.put("hour", new DateFieldParser.HourOfDay()).put("1h", new DateFieldParser.HourOfDay())
				.put("minute", new DateFieldParser.MinuteOfHour()).put("1m", new DateFieldParser.MinuteOfHour())
				.put("second", new DateFieldParser.SecondOfMinute()).put("1s", new DateFieldParser.SecondOfMinute())
				.immutableMap();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#types()
	 */
	@Override
	public String[] types() {
		return new String[] { DateHistogramFacet.TYPE, "dateHistogram" };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#parse(java.lang.String, cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public FacetCollector parse(String facetName, XContentParser parser, SearchContext context) throws IOException {
		String keyField = null;
		String valueField = null;
		String valueScript = null;
		String scriptLang = null;
		Map<String, Object> params = null;
		String interval = null;
		DateTimeZone preZone = DateTimeZone.UTC;
		DateTimeZone postZone = DateTimeZone.UTC;
		boolean preZoneAdjustLargeInterval = false;
		long preOffset = 0;
		long postOffset = 0;
		float factor = 1.0f;
		Chronology chronology = ISOChronology.getInstanceUTC();
		DateHistogramFacet.ComparatorType comparatorType = DateHistogramFacet.ComparatorType.TIME;
		XContentParser.Token token;
		String fieldName = null;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				fieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("params".equals(fieldName)) {
					params = parser.map();
				}
			} else if (token.isValue()) {
				if ("field".equals(fieldName)) {
					keyField = parser.text();
				} else if ("key_field".equals(fieldName) || "keyField".equals(fieldName)) {
					keyField = parser.text();
				} else if ("value_field".equals(fieldName) || "valueField".equals(fieldName)) {
					valueField = parser.text();
				} else if ("interval".equals(fieldName)) {
					interval = parser.text();
				} else if ("time_zone".equals(fieldName) || "timeZone".equals(fieldName)) {
					preZone = parseZone(parser, token);
				} else if ("pre_zone".equals(fieldName) || "preZone".equals(fieldName)) {
					preZone = parseZone(parser, token);
				} else if ("pre_zone_adjust_large_interval".equals(fieldName)
						|| "preZoneAdjustLargeInterval".equals(fieldName)) {
					preZoneAdjustLargeInterval = parser.booleanValue();
				} else if ("post_zone".equals(fieldName) || "postZone".equals(fieldName)) {
					postZone = parseZone(parser, token);
				} else if ("pre_offset".equals(fieldName) || "preOffset".equals(fieldName)) {
					preOffset = parseOffset(parser.text());
				} else if ("post_offset".equals(fieldName) || "postOffset".equals(fieldName)) {
					postOffset = parseOffset(parser.text());
				} else if ("factor".equals(fieldName)) {
					factor = parser.floatValue();
				} else if ("value_script".equals(fieldName) || "valueScript".equals(fieldName)) {
					valueScript = parser.text();
				} else if ("order".equals(fieldName) || "comparator".equals(fieldName)) {
					comparatorType = DateHistogramFacet.ComparatorType.fromString(parser.text());
				} else if ("lang".equals(fieldName)) {
					scriptLang = parser.text();
				}
			}
		}

		if (keyField == null) {
			throw new FacetPhaseExecutionException(facetName,
					"key field is required to be set for histogram facet, either using [field] or using [key_field]");
		}

		FieldMapper mapper = context.smartNameFieldMapper(keyField);
		if (mapper == null) {
			throw new FacetPhaseExecutionException(facetName, "(key) field [" + keyField + "] not found");
		}
		if (mapper.fieldDataType() != FieldDataType.DefaultTypes.LONG) {
			throw new FacetPhaseExecutionException(facetName, "(key) field [" + keyField + "] is not of type date");
		}

		if (interval == null) {
			throw new FacetPhaseExecutionException(facetName, "[interval] is required to be set for histogram facet");
		}

		TimeZoneRounding.Builder tzRoundingBuilder;
		DateFieldParser fieldParser = dateFieldParsers.get(interval);
		if (fieldParser != null) {
			tzRoundingBuilder = TimeZoneRounding.builder(fieldParser.parse(chronology));
		} else {

			tzRoundingBuilder = TimeZoneRounding.builder(TimeValue.parseTimeValue(interval, null));
		}

		TimeZoneRounding tzRounding = tzRoundingBuilder.preZone(preZone).postZone(postZone)
				.preZoneAdjustLargeInterval(preZoneAdjustLargeInterval).preOffset(preOffset).postOffset(postOffset)
				.factor(factor).build();

		if (valueScript != null) {
			return new ValueScriptDateHistogramFacetCollector(facetName, keyField, scriptLang, valueScript, params,
					tzRounding, comparatorType, context);
		} else if (valueField == null) {
			return new CountDateHistogramFacetCollector(facetName, keyField, tzRounding, comparatorType, context);
		} else {
			return new ValueDateHistogramFacetCollector(facetName, keyField, valueField, tzRounding, comparatorType,
					context);
		}
	}

	/**
	 * Parses the offset.
	 *
	 * @param offset the offset
	 * @return the long
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private long parseOffset(String offset) throws IOException {
		if (offset.charAt(0) == '-') {
			return -TimeValue.parseTimeValue(offset.substring(1), null).millis();
		}
		return TimeValue.parseTimeValue(offset, null).millis();
	}

	/**
	 * Parses the zone.
	 *
	 * @param parser the parser
	 * @param token the token
	 * @return the date time zone
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private DateTimeZone parseZone(XContentParser parser, XContentParser.Token token) throws IOException {
		if (token == XContentParser.Token.VALUE_NUMBER) {
			return DateTimeZone.forOffsetHours(parser.intValue());
		} else {
			String text = parser.text();
			int index = text.indexOf(':');
			if (index != -1) {

				return DateTimeZone.forOffsetHoursMinutes(Integer.parseInt(text.substring(0, index)),
						Integer.parseInt(text.substring(index + 1)));
			} else {

				return DateTimeZone.forID(text);
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetProcessor#reduce(java.lang.String, java.util.List)
	 */
	@Override
	public Facet reduce(String name, List<Facet> facets) {
		InternalDateHistogramFacet first = (InternalDateHistogramFacet) facets.get(0);
		return first.reduce(name, facets);
	}

	/**
	 * The Interface DateFieldParser.
	 *
	 * @author l.xue.nong
	 */
	static interface DateFieldParser {

		/**
		 * Parses the.
		 *
		 * @param chronology the chronology
		 * @return the date time field
		 */
		DateTimeField parse(Chronology chronology);

		/**
		 * The Class WeekOfWeekyear.
		 *
		 * @author l.xue.nong
		 */
		static class WeekOfWeekyear implements DateFieldParser {

			/* (non-Javadoc)
			 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacetProcessor.DateFieldParser#parse(org.joda.time.Chronology)
			 */
			@Override
			public DateTimeField parse(Chronology chronology) {
				return chronology.weekOfWeekyear();
			}
		}

		/**
		 * The Class YearOfCentury.
		 *
		 * @author l.xue.nong
		 */
		static class YearOfCentury implements DateFieldParser {

			/* (non-Javadoc)
			 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacetProcessor.DateFieldParser#parse(org.joda.time.Chronology)
			 */
			@Override
			public DateTimeField parse(Chronology chronology) {
				return chronology.yearOfCentury();
			}
		}

		/**
		 * The Class MonthOfYear.
		 *
		 * @author l.xue.nong
		 */
		static class MonthOfYear implements DateFieldParser {

			/* (non-Javadoc)
			 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacetProcessor.DateFieldParser#parse(org.joda.time.Chronology)
			 */
			@Override
			public DateTimeField parse(Chronology chronology) {
				return chronology.monthOfYear();
			}
		}

		/**
		 * The Class DayOfMonth.
		 *
		 * @author l.xue.nong
		 */
		static class DayOfMonth implements DateFieldParser {

			/* (non-Javadoc)
			 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacetProcessor.DateFieldParser#parse(org.joda.time.Chronology)
			 */
			@Override
			public DateTimeField parse(Chronology chronology) {
				return chronology.dayOfMonth();
			}
		}

		/**
		 * The Class HourOfDay.
		 *
		 * @author l.xue.nong
		 */
		static class HourOfDay implements DateFieldParser {

			/* (non-Javadoc)
			 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacetProcessor.DateFieldParser#parse(org.joda.time.Chronology)
			 */
			@Override
			public DateTimeField parse(Chronology chronology) {
				return chronology.hourOfDay();
			}
		}

		/**
		 * The Class MinuteOfHour.
		 *
		 * @author l.xue.nong
		 */
		static class MinuteOfHour implements DateFieldParser {

			/* (non-Javadoc)
			 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacetProcessor.DateFieldParser#parse(org.joda.time.Chronology)
			 */
			@Override
			public DateTimeField parse(Chronology chronology) {
				return chronology.minuteOfHour();
			}
		}

		/**
		 * The Class SecondOfMinute.
		 *
		 * @author l.xue.nong
		 */
		static class SecondOfMinute implements DateFieldParser {

			/* (non-Javadoc)
			 * @see cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacetProcessor.DateFieldParser#parse(org.joda.time.Chronology)
			 */
			@Override
			public DateTimeField parse(Chronology chronology) {
				return chronology.secondOfMinute();
			}
		}
	}
}
