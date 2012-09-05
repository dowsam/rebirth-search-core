/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core Mapper.java 2012-7-6 14:29:54 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.ToXContent;
import cn.com.rebirth.search.core.index.analysis.AnalysisService;

import com.google.common.collect.ImmutableMap;

/**
 * The Interface Mapper.
 *
 * @author l.xue.nong
 */
public interface Mapper extends ToXContent {

	/** The Constant EMPTY_ARRAY. */
	public static final Mapper[] EMPTY_ARRAY = new Mapper[0];

	/**
	 * The Class BuilderContext.
	 *
	 * @author l.xue.nong
	 */
	public static class BuilderContext {

		/** The index settings. */
		private final Settings indexSettings;

		/** The content path. */
		private final ContentPath contentPath;

		/**
		 * Instantiates a new builder context.
		 *
		 * @param indexSettings the index settings
		 * @param contentPath the content path
		 */
		public BuilderContext(@Nullable Settings indexSettings, ContentPath contentPath) {
			this.contentPath = contentPath;
			this.indexSettings = indexSettings;
		}

		/**
		 * Path.
		 *
		 * @return the content path
		 */
		public ContentPath path() {
			return this.contentPath;
		}

		/**
		 * Index settings.
		 *
		 * @return the settings
		 */
		@Nullable
		public Settings indexSettings() {
			return this.indexSettings;
		}
	}

	/**
	 * The Class Builder.
	 *
	 * @param <T> the generic type
	 * @param <Y> the generic type
	 * @author l.xue.nong
	 */
	@SuppressWarnings("rawtypes")
	public static abstract class Builder<T extends Builder, Y extends Mapper> {

		/** The name. */
		public String name;

		/** The builder. */
		protected T builder;

		/**
		 * Instantiates a new builder.
		 *
		 * @param name the name
		 */
		protected Builder(String name) {
			this.name = name;
		}

		/**
		 * Name.
		 *
		 * @return the string
		 */
		public String name() {
			return this.name;
		}

		/**
		 * Builds the.
		 *
		 * @param context the context
		 * @return the y
		 */
		public abstract Y build(BuilderContext context);
	}

	/**
	 * The Interface TypeParser.
	 *
	 * @author l.xue.nong
	 */
	public interface TypeParser {

		/**
		 * The Class ParserContext.
		 *
		 * @author l.xue.nong
		 */
		public static class ParserContext {

			/** The analysis service. */
			private final AnalysisService analysisService;

			/** The type parsers. */
			private final ImmutableMap<String, TypeParser> typeParsers;

			/**
			 * Instantiates a new parser context.
			 *
			 * @param analysisService the analysis service
			 * @param typeParsers the type parsers
			 */
			public ParserContext(AnalysisService analysisService, ImmutableMap<String, TypeParser> typeParsers) {
				this.analysisService = analysisService;
				this.typeParsers = typeParsers;
			}

			/**
			 * Analysis service.
			 *
			 * @return the analysis service
			 */
			public AnalysisService analysisService() {
				return analysisService;
			}

			/**
			 * Type parser.
			 *
			 * @param type the type
			 * @return the type parser
			 */
			public TypeParser typeParser(String type) {
				return typeParsers.get(Strings.toUnderscoreCase(type));
			}
		}

		/**
		 * Parses the.
		 *
		 * @param name the name
		 * @param node the node
		 * @param parserContext the parser context
		 * @return the mapper. builder
		 * @throws MapperParsingException the mapper parsing exception
		 */
		@SuppressWarnings("rawtypes")
		Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext)
				throws MapperParsingException;
	}

	/**
	 * Name.
	 *
	 * @return the string
	 */
	String name();

	/**
	 * Parses the.
	 *
	 * @param context the context
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void parse(ParseContext context) throws IOException;

	/**
	 * Merge.
	 *
	 * @param mergeWith the merge with
	 * @param mergeContext the merge context
	 * @throws MergeMappingException the merge mapping exception
	 */
	void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException;

	/**
	 * Traverse.
	 *
	 * @param fieldMapperListener the field mapper listener
	 */
	void traverse(FieldMapperListener fieldMapperListener);

	/**
	 * Traverse.
	 *
	 * @param objectMapperListener the object mapper listener
	 */
	void traverse(ObjectMapperListener objectMapperListener);

	/**
	 * Close.
	 */
	void close();
}
