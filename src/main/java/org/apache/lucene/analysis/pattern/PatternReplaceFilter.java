/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PatternReplaceFilter.java 2012-7-6 14:30:24 l.xue.nong$$
 */

package org.apache.lucene.analysis.pattern;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Class PatternReplaceFilter.
 *
 * @author l.xue.nong
 */
public final class PatternReplaceFilter extends TokenFilter {

	/** The replacement. */
	private final String replacement;

	/** The all. */
	private final boolean all;

	/** The term att. */
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

	/** The m. */
	private final Matcher m;

	/**
	 * Instantiates a new pattern replace filter.
	 *
	 * @param in the in
	 * @param p the p
	 * @param replacement the replacement
	 * @param all the all
	 */
	public PatternReplaceFilter(TokenStream in, Pattern p, String replacement, boolean all) {
		super(in);
		this.replacement = (null == replacement) ? "" : replacement;
		this.all = all;
		this.m = p.matcher(termAtt);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.TokenStream#incrementToken()
	 */
	@Override
	public boolean incrementToken() throws IOException {
		if (!input.incrementToken())
			return false;

		m.reset();
		if (m.find()) {

			String transformed = all ? m.replaceAll(replacement) : m.replaceFirst(replacement);
			termAtt.setEmpty().append(transformed);
		}

		return true;
	}

}
