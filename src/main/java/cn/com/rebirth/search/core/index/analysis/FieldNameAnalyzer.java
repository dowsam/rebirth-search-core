/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FieldNameAnalyzer.java 2012-7-6 14:29:00 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import com.google.common.collect.ImmutableMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Fieldable;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * The Class FieldNameAnalyzer.
 *
 * @author l.xue.nong
 */
public final class FieldNameAnalyzer extends Analyzer {

	/** The analyzers. */
	private final ImmutableMap<String, Analyzer> analyzers;

	/** The default analyzer. */
	private final Analyzer defaultAnalyzer;

	/**
	 * Instantiates a new field name analyzer.
	 *
	 * @param analyzers the analyzers
	 * @param defaultAnalyzer the default analyzer
	 */
	public FieldNameAnalyzer(Map<String, Analyzer> analyzers, Analyzer defaultAnalyzer) {
		this.analyzers = ImmutableMap.copyOf(analyzers);
		this.defaultAnalyzer = defaultAnalyzer;
	}

	/**
	 * Analyzers.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, Analyzer> analyzers() {
		return analyzers;
	}

	/**
	 * Default analyzer.
	 *
	 * @return the analyzer
	 */
	public Analyzer defaultAnalyzer() {
		return defaultAnalyzer;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
	 */
	@Override
	public final TokenStream tokenStream(String fieldName, Reader reader) {
		return getAnalyzer(fieldName).tokenStream(fieldName, reader);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.Analyzer#reusableTokenStream(java.lang.String, java.io.Reader)
	 */
	@Override
	public final TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
		return getAnalyzer(fieldName).reusableTokenStream(fieldName, reader);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.Analyzer#getPositionIncrementGap(java.lang.String)
	 */
	@Override
	public int getPositionIncrementGap(String fieldName) {
		return getAnalyzer(fieldName).getPositionIncrementGap(fieldName);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.Analyzer#getOffsetGap(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public int getOffsetGap(Fieldable field) {
		return getAnalyzer(field.name()).getOffsetGap(field);
	}

	/**
	 * Gets the analyzer.
	 *
	 * @param name the name
	 * @return the analyzer
	 */
	private Analyzer getAnalyzer(String name) {
		Analyzer analyzer = analyzers.get(name);
		if (analyzer != null) {
			return analyzer;
		}
		return defaultAnalyzer;
	}
}