/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexAliasesService.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.aliases;

import java.io.IOException;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilterClause;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.compress.CompressedString;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.XContentFactory;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.XBooleanFilter;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.query.IndexQueryParserService;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.indices.AliasFilterParsingException;
import cn.com.rebirth.search.core.indices.InvalidAliasNameException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;

/**
 * The Class IndexAliasesService.
 *
 * @author l.xue.nong
 */
public class IndexAliasesService extends AbstractIndexComponent implements Iterable<IndexAlias> {

	/** The index query parser. */
	private final IndexQueryParserService indexQueryParser;

	/** The aliases. */
	private volatile ImmutableMap<String, IndexAlias> aliases = ImmutableMap.of();

	/** The mutex. */
	private final Object mutex = new Object();

	/**
	 * Instantiates a new index aliases service.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param indexQueryParser the index query parser
	 */
	@Inject
	public IndexAliasesService(Index index, @IndexSettings Settings indexSettings,
			IndexQueryParserService indexQueryParser) {
		super(index, indexSettings);
		this.indexQueryParser = indexQueryParser;
	}

	/**
	 * Checks for alias.
	 *
	 * @param alias the alias
	 * @return true, if successful
	 */
	public boolean hasAlias(String alias) {
		return aliases.containsKey(alias);
	}

	/**
	 * Alias.
	 *
	 * @param alias the alias
	 * @return the index alias
	 */
	public IndexAlias alias(String alias) {
		return aliases.get(alias);
	}

	/**
	 * Adds the.
	 *
	 * @param alias the alias
	 * @param filter the filter
	 */
	public void add(String alias, @Nullable CompressedString filter) {
		add(new IndexAlias(alias, filter, parse(alias, filter)));
	}

	/**
	 * Alias filter.
	 *
	 * @param aliases the aliases
	 * @return the filter
	 */
	public Filter aliasFilter(String... aliases) {
		if (aliases == null || aliases.length == 0) {
			return null;
		}
		if (aliases.length == 1) {
			IndexAlias indexAlias = alias(aliases[0]);
			if (indexAlias == null) {

				throw new InvalidAliasNameException(index, aliases[0], "Unknown alias name was passed to alias Filter");
			}
			return indexAlias.parsedFilter();
		} else {

			XBooleanFilter combined = new XBooleanFilter();
			for (String alias : aliases) {
				IndexAlias indexAlias = alias(alias);
				if (indexAlias == null) {

					throw new InvalidAliasNameException(index, aliases[0],
							"Unknown alias name was passed to alias Filter");
				}
				if (indexAlias.parsedFilter() != null) {
					combined.add(new FilterClause(indexAlias.parsedFilter(), BooleanClause.Occur.SHOULD));
				} else {

					return null;
				}
			}
			if (combined.getShouldFilters().size() == 0) {
				return null;
			}
			if (combined.getShouldFilters().size() == 1) {
				return combined.getShouldFilters().get(0);
			}
			return combined;
		}
	}

	/**
	 * Adds the.
	 *
	 * @param indexAlias the index alias
	 */
	private void add(IndexAlias indexAlias) {
		synchronized (mutex) {
			aliases = MapBuilder.newMapBuilder(aliases).put(indexAlias.alias(), indexAlias).immutableMap();
		}
	}

	/**
	 * Removes the.
	 *
	 * @param alias the alias
	 */
	public void remove(String alias) {
		synchronized (mutex) {
			aliases = MapBuilder.newMapBuilder(aliases).remove(alias).immutableMap();
		}
	}

	/**
	 * Parses the.
	 *
	 * @param alias the alias
	 * @param filter the filter
	 * @return the filter
	 */
	private Filter parse(String alias, CompressedString filter) {
		if (filter == null) {
			return null;
		}
		try {
			byte[] filterSource = filter.uncompressed();
			XContentParser parser = XContentFactory.xContent(filterSource).createParser(filterSource);
			try {
				return indexQueryParser.parseInnerFilter(parser);
			} finally {
				parser.close();
			}
		} catch (IOException ex) {
			throw new AliasFilterParsingException(index, alias, "Invalid alias filter", ex);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public UnmodifiableIterator<IndexAlias> iterator() {
		return aliases.values().iterator();
	}
}
