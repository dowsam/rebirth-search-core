/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RootObjectMapper.java 2012-7-6 14:30:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.object;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.com.rebirth.commons.joda.FormatDateTimeFormatter;
import cn.com.rebirth.commons.joda.Joda;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.core.index.mapper.ContentPath;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.mapper.core.DateFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.TypeParsers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * The Class RootObjectMapper.
 *
 * @author l.xue.nong
 */
public class RootObjectMapper extends ObjectMapper {

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults {

		/** The Constant DYNAMIC_DATE_TIME_FORMATTERS. */
		public static final FormatDateTimeFormatter[] DYNAMIC_DATE_TIME_FORMATTERS = new FormatDateTimeFormatter[] {
				DateFieldMapper.Defaults.DATE_TIME_FORMATTER, Joda.forPattern("yyyy/MM/dd HH:mm:ss||yyyy/MM/dd") };

		/** The Constant DATE_DETECTION. */
		public static final boolean DATE_DETECTION = true;

		/** The Constant NUMERIC_DETECTION. */
		public static final boolean NUMERIC_DETECTION = false;
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends ObjectMapper.Builder<Builder, RootObjectMapper> {

		/** The dynamic templates. */
		protected final List<DynamicTemplate> dynamicTemplates = newArrayList();

		/** The seen date formats. */
		protected Set<String> seenDateFormats = Sets.newHashSet();

		/** The dynamic date time formatters. */
		protected List<FormatDateTimeFormatter> dynamicDateTimeFormatters = newArrayList();

		/** The date detection. */
		protected boolean dateDetection = Defaults.DATE_DETECTION;

		/** The numeric detection. */
		protected boolean numericDetection = Defaults.NUMERIC_DETECTION;

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
		 * No dynamic date time formatter.
		 *
		 * @return the builder
		 */
		public Builder noDynamicDateTimeFormatter() {
			this.dynamicDateTimeFormatters = null;
			return builder;
		}

		/**
		 * Dynamic date time formatter.
		 *
		 * @param dateTimeFormatters the date time formatters
		 * @return the builder
		 */
		public Builder dynamicDateTimeFormatter(Iterable<FormatDateTimeFormatter> dateTimeFormatters) {
			for (FormatDateTimeFormatter dateTimeFormatter : dateTimeFormatters) {
				if (!seenDateFormats.contains(dateTimeFormatter.format())) {
					seenDateFormats.add(dateTimeFormatter.format());
					this.dynamicDateTimeFormatters.add(dateTimeFormatter);
				}
			}
			return builder;
		}

		/**
		 * Adds the.
		 *
		 * @param dynamicTemplate the dynamic template
		 * @return the builder
		 */
		public Builder add(DynamicTemplate dynamicTemplate) {
			this.dynamicTemplates.add(dynamicTemplate);
			return this;
		}

		/**
		 * Adds the.
		 *
		 * @param dynamicTemplate the dynamic template
		 * @return the builder
		 */
		public Builder add(DynamicTemplate... dynamicTemplate) {
			for (DynamicTemplate template : dynamicTemplate) {
				this.dynamicTemplates.add(template);
			}
			return this;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.object.ObjectMapper.Builder#createMapper(java.lang.String, java.lang.String, boolean, cn.com.rebirth.search.core.index.mapper.object.ObjectMapper.Nested, cn.com.rebirth.search.core.index.mapper.object.ObjectMapper.Dynamic, cn.com.rebirth.search.core.index.mapper.ContentPath.Type, java.util.Map)
		 */
		@Override
		protected ObjectMapper createMapper(String name, String fullPath, boolean enabled, Nested nested,
				Dynamic dynamic, ContentPath.Type pathType, Map<String, Mapper> mappers) {
			assert !nested.isNested();
			FormatDateTimeFormatter[] dates = null;
			if (dynamicDateTimeFormatters == null) {
				dates = new FormatDateTimeFormatter[0];
			} else if (dynamicDateTimeFormatters.isEmpty()) {

				dates = Defaults.DYNAMIC_DATE_TIME_FORMATTERS;
			} else {
				dates = dynamicDateTimeFormatters
						.toArray(new FormatDateTimeFormatter[dynamicDateTimeFormatters.size()]);
			}

			if (dynamic == null) {
				dynamic = Dynamic.TRUE;
			}
			return new RootObjectMapper(name, enabled, dynamic, pathType, mappers, dates,
					dynamicTemplates.toArray(new DynamicTemplate[dynamicTemplates.size()]), dateDetection,
					numericDetection);
		}
	}

	/**
	 * The Class TypeParser.
	 *
	 * @author l.xue.nong
	 */
	public static class TypeParser extends ObjectMapper.TypeParser {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.object.ObjectMapper.TypeParser#createBuilder(java.lang.String)
		 */
		@Override
		protected ObjectMapper.Builder createBuilder(String name) {
			return new Builder(name);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.object.ObjectMapper.TypeParser#processField(cn.com.rebirth.search.core.index.mapper.object.ObjectMapper.Builder, java.lang.String, java.lang.Object)
		 */
		@Override
		protected void processField(ObjectMapper.Builder builder, String fieldName, Object fieldNode) {
			if (fieldName.equals("date_formats") || fieldName.equals("dynamic_date_formats")) {
				List<FormatDateTimeFormatter> dateTimeFormatters = newArrayList();
				if (fieldNode instanceof List) {
					for (Object node1 : (List) fieldNode) {
						dateTimeFormatters.add(TypeParsers.parseDateTimeFormatter(fieldName, node1));
					}
				} else if ("none".equals(fieldNode.toString())) {
					dateTimeFormatters = null;
				} else {
					dateTimeFormatters.add(TypeParsers.parseDateTimeFormatter(fieldName, fieldNode));
				}
				if (dateTimeFormatters == null) {
					((Builder) builder).noDynamicDateTimeFormatter();
				} else {
					((Builder) builder).dynamicDateTimeFormatter(dateTimeFormatters);
				}
			} else if (fieldName.equals("dynamic_templates")) {

				List tmplNodes = (List) fieldNode;
				for (Object tmplNode : tmplNodes) {
					Map<String, Object> tmpl = (Map<String, Object>) tmplNode;
					if (tmpl.size() != 1) {
						throw new MapperParsingException("A dynamic template must be defined with a name");
					}
					Map.Entry<String, Object> entry = tmpl.entrySet().iterator().next();
					((Builder) builder).add(DynamicTemplate.parse(entry.getKey(),
							(Map<String, Object>) entry.getValue()));
				}
			} else if (fieldName.equals("date_detection")) {
				((Builder) builder).dateDetection = XContentMapValues.nodeBooleanValue(fieldNode);
			} else if (fieldName.equals("numeric_detection")) {
				((Builder) builder).numericDetection = XContentMapValues.nodeBooleanValue(fieldNode);
			}
		}
	}

	/** The dynamic date time formatters. */
	private final FormatDateTimeFormatter[] dynamicDateTimeFormatters;

	/** The date detection. */
	private final boolean dateDetection;

	/** The numeric detection. */
	private final boolean numericDetection;

	/** The dynamic templates. */
	private volatile DynamicTemplate dynamicTemplates[];

	/**
	 * Instantiates a new root object mapper.
	 *
	 * @param name the name
	 * @param enabled the enabled
	 * @param dynamic the dynamic
	 * @param pathType the path type
	 * @param mappers the mappers
	 * @param dynamicDateTimeFormatters the dynamic date time formatters
	 * @param dynamicTemplates the dynamic templates
	 * @param dateDetection the date detection
	 * @param numericDetection the numeric detection
	 */
	RootObjectMapper(String name, boolean enabled, Dynamic dynamic, ContentPath.Type pathType,
			Map<String, Mapper> mappers, FormatDateTimeFormatter[] dynamicDateTimeFormatters,
			DynamicTemplate dynamicTemplates[], boolean dateDetection, boolean numericDetection) {
		super(name, name, enabled, Nested.NO, dynamic, pathType, mappers);
		this.dynamicTemplates = dynamicTemplates;
		this.dynamicDateTimeFormatters = dynamicDateTimeFormatters;
		this.dateDetection = dateDetection;
		this.numericDetection = numericDetection;
	}

	/**
	 * Date detection.
	 *
	 * @return true, if successful
	 */
	public boolean dateDetection() {
		return this.dateDetection;
	}

	/**
	 * Numeric detection.
	 *
	 * @return true, if successful
	 */
	public boolean numericDetection() {
		return this.numericDetection;
	}

	/**
	 * Dynamic date time formatters.
	 *
	 * @return the format date time formatter[]
	 */
	public FormatDateTimeFormatter[] dynamicDateTimeFormatters() {
		return dynamicDateTimeFormatters;
	}

	/**
	 * Find template builder.
	 *
	 * @param context the context
	 * @param name the name
	 * @param dynamicType the dynamic type
	 * @return the mapper. builder
	 */
	public Mapper.Builder findTemplateBuilder(ParseContext context, String name, String dynamicType) {
		return findTemplateBuilder(context, name, dynamicType, dynamicType);
	}

	/**
	 * Find template builder.
	 *
	 * @param context the context
	 * @param name the name
	 * @param dynamicType the dynamic type
	 * @param matchType the match type
	 * @return the mapper. builder
	 */
	public Mapper.Builder findTemplateBuilder(ParseContext context, String name, String dynamicType, String matchType) {
		DynamicTemplate dynamicTemplate = findTemplate(context.path(), name, matchType);
		if (dynamicTemplate == null) {
			return null;
		}
		Mapper.TypeParser.ParserContext parserContext = context.docMapperParser().parserContext();
		String mappingType = dynamicTemplate.mappingType(dynamicType);
		Mapper.TypeParser typeParser = parserContext.typeParser(mappingType);
		if (typeParser == null) {
			throw new MapperParsingException("failed to find type parsed [" + mappingType + "] for [" + name + "]");
		}
		return typeParser.parse(name, dynamicTemplate.mappingForName(name, dynamicType), parserContext);
	}

	/**
	 * Find template.
	 *
	 * @param path the path
	 * @param name the name
	 * @param matchType the match type
	 * @return the dynamic template
	 */
	public DynamicTemplate findTemplate(ContentPath path, String name, String matchType) {
		for (DynamicTemplate dynamicTemplate : dynamicTemplates) {
			if (dynamicTemplate.match(path, name, matchType)) {
				return dynamicTemplate;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.object.ObjectMapper#doMerge(cn.com.rebirth.search.core.index.mapper.object.ObjectMapper, cn.com.rebirth.search.core.index.mapper.MergeContext)
	 */
	@Override
	protected void doMerge(ObjectMapper mergeWith, MergeContext mergeContext) {
		RootObjectMapper mergeWithObject = (RootObjectMapper) mergeWith;
		if (!mergeContext.mergeFlags().simulate()) {

			List<DynamicTemplate> mergedTemplates = Lists.newArrayList(Arrays.asList(this.dynamicTemplates));
			for (DynamicTemplate template : mergeWithObject.dynamicTemplates) {
				boolean replaced = false;
				for (int i = 0; i < mergedTemplates.size(); i++) {
					if (mergedTemplates.get(i).name().equals(template.name())) {
						mergedTemplates.set(i, template);
						replaced = true;
					}
				}
				if (!replaced) {
					mergedTemplates.add(template);
				}
			}
			this.dynamicTemplates = mergedTemplates.toArray(new DynamicTemplate[mergedTemplates.size()]);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.object.ObjectMapper#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, ToXContent.Params params) throws IOException {
		if (dynamicDateTimeFormatters != Defaults.DYNAMIC_DATE_TIME_FORMATTERS) {
			if (dynamicDateTimeFormatters.length > 0) {
				builder.startArray("dynamic_date_formats");
				for (FormatDateTimeFormatter dateTimeFormatter : dynamicDateTimeFormatters) {
					builder.value(dateTimeFormatter.format());
				}
				builder.endArray();
			}
		}

		if (dynamicTemplates != null && dynamicTemplates.length > 0) {
			builder.startArray("dynamic_templates");
			for (DynamicTemplate dynamicTemplate : dynamicTemplates) {
				builder.startObject();
				builder.field(dynamicTemplate.name());
				builder.map(dynamicTemplate.conf());
				builder.endObject();
			}
			builder.endArray();
		}

		if (dateDetection != Defaults.DATE_DETECTION) {
			builder.field("date_detection", dateDetection);
		}
		if (numericDetection != Defaults.NUMERIC_DETECTION) {
			builder.field("numeric_detection", numericDetection);
		}
	}
}
