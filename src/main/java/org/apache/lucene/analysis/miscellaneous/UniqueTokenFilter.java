/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core UniqueTokenFilter.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package org.apache.lucene.analysis.miscellaneous;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.Version;

import java.io.IOException;

/**
 * The Class UniqueTokenFilter.
 *
 * @author l.xue.nong
 */
public class UniqueTokenFilter extends TokenFilter {

	/** The term attribute. */
	private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);

	/** The pos inc attribute. */
	private final PositionIncrementAttribute posIncAttribute = addAttribute(PositionIncrementAttribute.class);

	/** The previous. */
	private final CharArraySet previous = new CharArraySet(Version.LUCENE_31, 8, false);

	/** The only on same position. */
	private final boolean onlyOnSamePosition;

	/**
	 * Instantiates a new unique token filter.
	 *
	 * @param in the in
	 */
	public UniqueTokenFilter(TokenStream in) {
		this(in, false);
	}

	/**
	 * Instantiates a new unique token filter.
	 *
	 * @param in the in
	 * @param onlyOnSamePosition the only on same position
	 */
	public UniqueTokenFilter(TokenStream in, boolean onlyOnSamePosition) {
		super(in);
		this.onlyOnSamePosition = onlyOnSamePosition;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.TokenStream#incrementToken()
	 */
	@Override
	public final boolean incrementToken() throws IOException {
		while (input.incrementToken()) {
			final char term[] = termAttribute.buffer();
			final int length = termAttribute.length();

			boolean duplicate;
			if (onlyOnSamePosition) {
				final int posIncrement = posIncAttribute.getPositionIncrement();
				if (posIncrement > 0) {
					previous.clear();
				}

				duplicate = (posIncrement == 0 && previous.contains(term, 0, length));
			} else {
				duplicate = previous.contains(term, 0, length);
			}

			char saved[] = new char[length];
			System.arraycopy(term, 0, saved, 0, length);
			previous.add(saved);

			if (!duplicate) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.TokenFilter#reset()
	 */
	@Override
	public final void reset() throws IOException {
		super.reset();
		previous.clear();
	}
}
