/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StandardHtmlStripAnalyzer.java 2012-7-6 14:30:19 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;

/**
 * The Class StandardHtmlStripAnalyzer.
 *
 * @author l.xue.nong
 */
public class StandardHtmlStripAnalyzer extends StopwordAnalyzerBase {

	/**
	 * Instantiates a new standard html strip analyzer.
	 *
	 * @param version the version
	 */
	public StandardHtmlStripAnalyzer(Version version) {
		super(version, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.ReusableAnalyzerBase#createComponents(java.lang.String, java.io.Reader)
	 */
	@Override
	protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
		final StandardTokenizer src = new StandardTokenizer(matchVersion, reader);
		src.setMaxTokenLength(StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
		TokenStream tok = new StandardFilter(matchVersion, src);
		tok = new LowerCaseFilter(matchVersion, tok);
		tok = new StopFilter(matchVersion, tok, stopwords);
		return new TokenStreamComponents(src, tok) {
			@Override
			protected boolean reset(final Reader reader) throws IOException {
				src.setMaxTokenLength(StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
				return super.reset(reader);
			}
		};
	}

}