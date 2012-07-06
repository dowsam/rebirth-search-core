/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AbstractTokenizerFactory.java 2012-7-6 14:30:42 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.util.Version;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * A factory for creating AbstractTokenizer objects.
 */
public abstract class AbstractTokenizerFactory extends AbstractIndexComponent implements TokenizerFactory {

	/** The name. */
	private final String name;

	/** The version. */
	protected final Version version;

	/**
	 * Instantiates a new abstract tokenizer factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	public AbstractTokenizerFactory(Index index, @IndexSettings Settings indexSettings, String name, Settings settings) {
		super(index, indexSettings);
		this.name = name;
		this.version = Lucene.parseVersion(settings.get("version"), Lucene.ANALYZER_VERSION);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.TokenizerFactory#name()
	 */
	@Override
	public String name() {
		return this.name;
	}
}
