/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AbstractIndexAnalyzerProvider.java 2012-3-29 15:02:26 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.index.analysis.AnalyzerScope;


/**
 * The Class AbstractIndexAnalyzerProvider.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public abstract class AbstractIndexAnalyzerProvider<T extends Analyzer> extends AbstractIndexComponent implements
		AnalyzerProvider<T> {

	
	/** The name. */
	private final String name;

	
	/** The version. */
	protected final Version version;

	
	/**
	 * Instantiates a new abstract index analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	public AbstractIndexAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, String name,
			Settings settings) {
		super(index, indexSettings);
		this.name = name;
		this.version = Lucene.parseVersion(settings.get("version"), Lucene.ANALYZER_VERSION);
	}

	
	/**
	 * Instantiates a new abstract index analyzer provider.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param prefixSettings the prefix settings
	 * @param name the name
	 * @param settings the settings
	 */
	public AbstractIndexAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, String prefixSettings,
			String name, Settings settings) {
		super(index, indexSettings, prefixSettings);
		this.name = name;
		this.version = Lucene.parseVersion(settings.get("version"), Lucene.ANALYZER_VERSION);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.AnalyzerProvider#name()
	 */
	@Override
	public String name() {
		return this.name;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.AnalyzerProvider#scope()
	 */
	@Override
	public AnalyzerScope scope() {
		return AnalyzerScope.INDEX;
	}
}
