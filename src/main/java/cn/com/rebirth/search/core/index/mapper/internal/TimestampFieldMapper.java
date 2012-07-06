/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TimestampFieldMapper.java 2012-7-6 14:30:36 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.internal;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.joda.FormatDateTimeFormatter;
import cn.com.rebirth.commons.joda.Joda;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.core.index.mapper.InternalMapper;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperBuilders;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.mapper.RootMapper;
import cn.com.rebirth.search.core.index.mapper.core.DateFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.LongFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.TypeParsers;

/**
 * The Class TimestampFieldMapper.
 *
 * @author l.xue.nong
 */
public class TimestampFieldMapper extends DateFieldMapper implements InternalMapper, RootMapper {

	/** The Constant NAME. */
	public static final String NAME = "_timestamp";

	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "_timestamp";

	/** The Constant DEFAULT_DATE_TIME_FORMAT. */
	public static final String DEFAULT_DATE_TIME_FORMAT = "dateOptionalTime";

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends DateFieldMapper.Defaults {

		/** The Constant NAME. */
		public static final String NAME = "_timestamp";

		/** The Constant STORE. */
		public static final Field.Store STORE = Field.Store.NO;

		/** The Constant INDEX. */
		public static final Field.Index INDEX = Field.Index.NOT_ANALYZED;

		/** The Constant ENABLED. */
		public static final boolean ENABLED = false;

		/** The Constant PATH. */
		public static final String PATH = null;

		/** The Constant DATE_TIME_FORMATTER. */
		public static final FormatDateTimeFormatter DATE_TIME_FORMATTER = Joda.forPattern(DEFAULT_DATE_TIME_FORMAT);
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends NumberFieldMapper.Builder<Builder, TimestampFieldMapper> {

		/** The enabled. */
		private boolean enabled = Defaults.ENABLED;

		/** The path. */
		private String path = Defaults.PATH;

		/** The date time formatter. */
		private FormatDateTimeFormatter dateTimeFormatter = Defaults.DATE_TIME_FORMATTER;

		/**
		 * Instantiates a new builder.
		 */
		public Builder() {
			super(Defaults.NAME);
			store = Defaults.STORE;
			index = Defaults.INDEX;
		}

		/**
		 * Enabled.
		 *
		 * @param enabled the enabled
		 * @return the builder
		 */
		public Builder enabled(boolean enabled) {
			this.enabled = enabled;
			return builder;
		}

		/**
		 * Path.
		 *
		 * @param path the path
		 * @return the builder
		 */
		public Builder path(String path) {
			this.path = path;
			return builder;
		}

		/**
		 * Date time formatter.
		 *
		 * @param dateTimeFormatter the date time formatter
		 * @return the builder
		 */
		public Builder dateTimeFormatter(FormatDateTimeFormatter dateTimeFormatter) {
			this.dateTimeFormatter = dateTimeFormatter;
			return builder;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.Builder#build(cn.com.rebirth.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public TimestampFieldMapper build(BuilderContext context) {
			boolean parseUpperInclusive = Defaults.PARSE_UPPER_INCLUSIVE;
			if (context.indexSettings() != null) {
				parseUpperInclusive = context.indexSettings().getAsBoolean("index.mapping.date.parse_upper_inclusive",
						Defaults.PARSE_UPPER_INCLUSIVE);
			}
			return new TimestampFieldMapper(store, index, enabled, path, dateTimeFormatter, parseUpperInclusive);
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
			TimestampFieldMapper.Builder builder = MapperBuilders.timestamp();
			TypeParsers.parseField(builder, builder.name, node, parserContext);
			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String fieldName = Strings.toUnderscoreCase(entry.getKey());
				Object fieldNode = entry.getValue();
				if (fieldName.equals("enabled")) {
					builder.enabled(XContentMapValues.nodeBooleanValue(fieldNode));
				} else if (fieldName.equals("path")) {
					builder.path(fieldNode.toString());
				} else if (fieldName.equals("format")) {
					builder.dateTimeFormatter(TypeParsers.parseDateTimeFormatter(builder.name(), fieldNode.toString()));
				}
			}
			return builder;
		}
	}

	/** The enabled. */
	private boolean enabled;

	/** The path. */
	private final String path;

	/**
	 * Instantiates a new timestamp field mapper.
	 */
	public TimestampFieldMapper() {
		this(Defaults.STORE, Defaults.INDEX, Defaults.ENABLED, Defaults.PATH, Defaults.DATE_TIME_FORMATTER,
				Defaults.PARSE_UPPER_INCLUSIVE);
	}

	/**
	 * Instantiates a new timestamp field mapper.
	 *
	 * @param store the store
	 * @param index the index
	 * @param enabled the enabled
	 * @param path the path
	 * @param dateTimeFormatter the date time formatter
	 * @param parseUpperInclusive the parse upper inclusive
	 */
	protected TimestampFieldMapper(Field.Store store, Field.Index index, boolean enabled, String path,
			FormatDateTimeFormatter dateTimeFormatter, boolean parseUpperInclusive) {
		super(new Names(Defaults.NAME, Defaults.NAME, Defaults.NAME, Defaults.NAME), dateTimeFormatter,
				Defaults.PRECISION_STEP, Defaults.FUZZY_FACTOR, index, store, Defaults.BOOST, Defaults.OMIT_NORMS,
				Defaults.OMIT_TERM_FREQ_AND_POSITIONS, Defaults.NULL_VALUE, TimeUnit.MILLISECONDS, parseUpperInclusive);
		this.enabled = enabled;
		this.path = path;
	}

	/**
	 * Enabled.
	 *
	 * @return true, if successful
	 */
	public boolean enabled() {
		return this.enabled;
	}

	/**
	 * Path.
	 *
	 * @return the string
	 */
	public String path() {
		return this.path;
	}

	/**
	 * Date time formatter.
	 *
	 * @return the format date time formatter
	 */
	public FormatDateTimeFormatter dateTimeFormatter() {
		return this.dateTimeFormatter;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.DateFieldMapper#valueForSearch(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public Object valueForSearch(Fieldable field) {
		return value(field);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.DateFieldMapper#valueAsString(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public String valueAsString(Fieldable field) {
		Long value = value(field);
		if (value == null) {
			return null;
		}
		return value.toString();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.RootMapper#validate(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void validate(ParseContext context) throws MapperParsingException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.RootMapper#preParse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void preParse(ParseContext context) throws IOException {
		super.parse(context);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.RootMapper#postParse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void postParse(ParseContext context) throws IOException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#parse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void parse(ParseContext context) throws IOException {

	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.RootMapper#includeInObject()
	 */
	@Override
	public boolean includeInObject() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.DateFieldMapper#parseCreateField(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	protected Fieldable parseCreateField(ParseContext context) throws IOException {
		if (enabled) {
			long timestamp = context.sourceToParse().timestamp();
			if (!indexed() && !stored()) {
				context.ignoredValue(names.indexName(), String.valueOf(timestamp));
				return null;
			}
			return new LongFieldMapper.CustomLongNumericField(this, timestamp);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.DateFieldMapper#contentType()
	 */
	@Override
	protected String contentType() {
		return CONTENT_TYPE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {

		if (index == Defaults.INDEX && store == Defaults.STORE && enabled == Defaults.ENABLED && path == Defaults.PATH
				&& dateTimeFormatter.format().equals(Defaults.DATE_TIME_FORMATTER.format())) {
			return builder;
		}
		builder.startObject(CONTENT_TYPE);
		if (index != Defaults.INDEX) {
			builder.field("index", index.name().toLowerCase());
		}
		if (store != Defaults.STORE) {
			builder.field("store", store.name().toLowerCase());
		}
		if (enabled != Defaults.ENABLED) {
			builder.field("enabled", enabled);
		}
		if (path != Defaults.PATH) {
			builder.field("path", path);
		}
		if (!dateTimeFormatter.format().equals(Defaults.DATE_TIME_FORMATTER.format())) {
			builder.field("format", dateTimeFormatter.format());
		}
		builder.endObject();
		return builder;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.DateFieldMapper#merge(cn.com.rebirth.search.core.index.mapper.Mapper, cn.com.rebirth.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {

	}
}
