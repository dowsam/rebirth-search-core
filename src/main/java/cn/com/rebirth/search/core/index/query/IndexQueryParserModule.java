/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexQueryParserModule.java 2012-7-6 14:29:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.util.LinkedList;
import java.util.Map;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Scopes;
import cn.com.rebirth.search.commons.inject.assistedinject.FactoryProvider;
import cn.com.rebirth.search.commons.inject.multibindings.MapBinder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * The Class IndexQueryParserModule.
 *
 * @author l.xue.nong
 */
public class IndexQueryParserModule extends AbstractModule {

	/**
	 * The Class QueryParsersProcessor.
	 *
	 * @author l.xue.nong
	 */
	public static class QueryParsersProcessor {

		/**
		 * Process x content query parsers.
		 *
		 * @param bindings the bindings
		 */
		public void processXContentQueryParsers(XContentQueryParsersBindings bindings) {

		}

		/**
		 * The Class XContentQueryParsersBindings.
		 *
		 * @author l.xue.nong
		 */
		public static class XContentQueryParsersBindings {

			/** The binder. */
			private final MapBinder<String, QueryParserFactory> binder;

			/** The group settings. */
			private final Map<String, Settings> groupSettings;

			/**
			 * Instantiates a new x content query parsers bindings.
			 *
			 * @param binder the binder
			 * @param groupSettings the group settings
			 */
			public XContentQueryParsersBindings(MapBinder<String, QueryParserFactory> binder,
					Map<String, Settings> groupSettings) {
				this.binder = binder;
				this.groupSettings = groupSettings;
			}

			/**
			 * Binder.
			 *
			 * @return the map binder
			 */
			public MapBinder<String, QueryParserFactory> binder() {
				return binder;
			}

			/**
			 * Group settings.
			 *
			 * @return the map
			 */
			public Map<String, Settings> groupSettings() {
				return groupSettings;
			}

			/**
			 * Process x content query parser.
			 *
			 * @param name the name
			 * @param xcontentQueryParser the xcontent query parser
			 */
			public void processXContentQueryParser(String name, Class<? extends QueryParser> xcontentQueryParser) {
				if (!groupSettings.containsKey(name)) {
					binder.addBinding(name)
							.toProvider(FactoryProvider.newFactory(QueryParserFactory.class, xcontentQueryParser))
							.in(Scopes.SINGLETON);
				}
			}
		}

		/**
		 * Process x content filter parsers.
		 *
		 * @param bindings the bindings
		 */
		public void processXContentFilterParsers(XContentFilterParsersBindings bindings) {

		}

		/**
		 * The Class XContentFilterParsersBindings.
		 *
		 * @author l.xue.nong
		 */
		public static class XContentFilterParsersBindings {

			/** The binder. */
			private final MapBinder<String, FilterParserFactory> binder;

			/** The group settings. */
			private final Map<String, Settings> groupSettings;

			/**
			 * Instantiates a new x content filter parsers bindings.
			 *
			 * @param binder the binder
			 * @param groupSettings the group settings
			 */
			public XContentFilterParsersBindings(MapBinder<String, FilterParserFactory> binder,
					Map<String, Settings> groupSettings) {
				this.binder = binder;
				this.groupSettings = groupSettings;
			}

			/**
			 * Binder.
			 *
			 * @return the map binder
			 */
			public MapBinder<String, FilterParserFactory> binder() {
				return binder;
			}

			/**
			 * Group settings.
			 *
			 * @return the map
			 */
			public Map<String, Settings> groupSettings() {
				return groupSettings;
			}

			/**
			 * Process x content query filter.
			 *
			 * @param name the name
			 * @param xcontentFilterParser the xcontent filter parser
			 */
			public void processXContentQueryFilter(String name, Class<? extends FilterParser> xcontentFilterParser) {
				if (!groupSettings.containsKey(name)) {
					binder.addBinding(name)
							.toProvider(FactoryProvider.newFactory(FilterParserFactory.class, xcontentFilterParser))
							.in(Scopes.SINGLETON);
				}
			}
		}
	}

	/** The settings. */
	private final Settings settings;

	/** The processors. */
	private final LinkedList<QueryParsersProcessor> processors = Lists.newLinkedList();

	/** The queries. */
	private final Map<String, Class<? extends QueryParser>> queries = Maps.newHashMap();

	/** The filters. */
	private final Map<String, Class<? extends FilterParser>> filters = Maps.newHashMap();

	/**
	 * Instantiates a new index query parser module.
	 *
	 * @param settings the settings
	 */
	public IndexQueryParserModule(Settings settings) {
		this.settings = settings;
	}

	/**
	 * Adds the query parser.
	 *
	 * @param name the name
	 * @param queryParser the query parser
	 */
	public void addQueryParser(String name, Class<? extends QueryParser> queryParser) {
		queries.put(name, queryParser);
	}

	/**
	 * Adds the filter parser.
	 *
	 * @param name the name
	 * @param filterParser the filter parser
	 */
	public void addFilterParser(String name, Class<? extends FilterParser> filterParser) {
		filters.put(name, filterParser);
	}

	/**
	 * Adds the processor.
	 *
	 * @param processor the processor
	 * @return the index query parser module
	 */
	public IndexQueryParserModule addProcessor(QueryParsersProcessor processor) {
		processors.addFirst(processor);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {

		bind(IndexQueryParserService.class).asEagerSingleton();

		MapBinder<String, QueryParserFactory> queryBinder = MapBinder.newMapBinder(binder(), String.class,
				QueryParserFactory.class);
		Map<String, Settings> xContentQueryParserGroups = settings
				.getGroups(IndexQueryParserService.Defaults.QUERY_PREFIX);
		for (Map.Entry<String, Settings> entry : xContentQueryParserGroups.entrySet()) {
			String qName = entry.getKey();
			Settings qSettings = entry.getValue();
			Class<? extends QueryParser> type = qSettings.getAsClass("type", null);
			if (type == null) {
				throw new IllegalArgumentException("Query Parser [" + qName + "] must be provided with a type");
			}
			queryBinder
					.addBinding(qName)
					.toProvider(
							FactoryProvider.newFactory(QueryParserFactory.class, qSettings.getAsClass("type", null)))
					.in(Scopes.SINGLETON);
		}

		QueryParsersProcessor.XContentQueryParsersBindings xContentQueryParsersBindings = new QueryParsersProcessor.XContentQueryParsersBindings(
				queryBinder, xContentQueryParserGroups);
		for (QueryParsersProcessor processor : processors) {
			processor.processXContentQueryParsers(xContentQueryParsersBindings);
		}

		for (Map.Entry<String, Class<? extends QueryParser>> entry : queries.entrySet()) {
			queryBinder.addBinding(entry.getKey())
					.toProvider(FactoryProvider.newFactory(QueryParserFactory.class, entry.getValue()))
					.in(Scopes.SINGLETON);
		}

		MapBinder<String, FilterParserFactory> filterBinder = MapBinder.newMapBinder(binder(), String.class,
				FilterParserFactory.class);
		Map<String, Settings> xContentFilterParserGroups = settings
				.getGroups(IndexQueryParserService.Defaults.FILTER_PREFIX);
		for (Map.Entry<String, Settings> entry : xContentFilterParserGroups.entrySet()) {
			String fName = entry.getKey();
			Settings fSettings = entry.getValue();
			Class<? extends FilterParser> type = fSettings.getAsClass("type", null);
			if (type == null) {
				throw new IllegalArgumentException("Filter Parser [" + fName + "] must be provided with a type");
			}
			filterBinder
					.addBinding(fName)
					.toProvider(
							FactoryProvider.newFactory(FilterParserFactory.class, fSettings.getAsClass("type", null)))
					.in(Scopes.SINGLETON);
		}

		QueryParsersProcessor.XContentFilterParsersBindings xContentFilterParsersBindings = new QueryParsersProcessor.XContentFilterParsersBindings(
				filterBinder, xContentFilterParserGroups);
		for (QueryParsersProcessor processor : processors) {
			processor.processXContentFilterParsers(xContentFilterParsersBindings);
		}

		for (Map.Entry<String, Class<? extends FilterParser>> entry : filters.entrySet()) {
			filterBinder.addBinding(entry.getKey())
					.toProvider(FactoryProvider.newFactory(FilterParserFactory.class, entry.getValue()))
					.in(Scopes.SINGLETON);
		}
	}
}
