/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TruncateTokenFilter.java 2012-7-6 14:29:31 l.xue.nong$$
 */

package org.apache.lucene.analysis.miscellaneous;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

/**
 * The Class TruncateTokenFilter.
 *
 * @author l.xue.nong
 */
public class TruncateTokenFilter extends TokenFilter {

	/** The term attribute. */
	private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);

	/** The size. */
	private final int size;

	/**
	 * Instantiates a new truncate token filter.
	 *
	 * @param in the in
	 * @param size the size
	 */
	public TruncateTokenFilter(TokenStream in, int size) {
		super(in);
		this.size = size;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.TokenStream#incrementToken()
	 */
	@Override
	public final boolean incrementToken() throws IOException {
		if (input.incrementToken()) {
			final int length = termAttribute.length();
			if (length > size) {
				termAttribute.setLength(size);
			}
			return true;
		} else {
			return false;
		}
	}
}
