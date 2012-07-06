/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core WordDelimiterIterator.java 2012-7-6 14:29:42 l.xue.nong$$
 */

package org.apache.lucene.analysis.miscellaneous;

import static org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter.*;

/**
 * The Class WordDelimiterIterator.
 *
 * @author l.xue.nong
 */
public final class WordDelimiterIterator {

	/** The Constant DONE. */
	public static final int DONE = -1;

	/** The Constant DEFAULT_WORD_DELIM_TABLE. */
	public static final byte[] DEFAULT_WORD_DELIM_TABLE;

	/** The text. */
	char text[];

	/** The length. */
	int length;

	/** The start bounds. */
	int startBounds;

	/** The end bounds. */
	int endBounds;

	/** The current. */
	int current;

	/** The end. */
	int end;

	/** The has final possessive. */
	private boolean hasFinalPossessive = false;

	/** The split on case change. */
	final boolean splitOnCaseChange;

	/** The split on numerics. */
	final boolean splitOnNumerics;

	/** The stem english possessive. */
	final boolean stemEnglishPossessive;

	/** The char type table. */
	private final byte[] charTypeTable;

	/** The skip possessive. */
	private boolean skipPossessive = false;

	static {
		byte[] tab = new byte[256];
		for (int i = 0; i < 256; i++) {
			byte code = 0;
			if (Character.isLowerCase(i)) {
				code |= LOWER;
			} else if (Character.isUpperCase(i)) {
				code |= UPPER;
			} else if (Character.isDigit(i)) {
				code |= DIGIT;
			}
			if (code == 0) {
				code = SUBWORD_DELIM;
			}
			tab[i] = code;
		}
		DEFAULT_WORD_DELIM_TABLE = tab;
	}

	/**
	 * Instantiates a new word delimiter iterator.
	 *
	 * @param charTypeTable the char type table
	 * @param splitOnCaseChange the split on case change
	 * @param splitOnNumerics the split on numerics
	 * @param stemEnglishPossessive the stem english possessive
	 */
	WordDelimiterIterator(byte[] charTypeTable, boolean splitOnCaseChange, boolean splitOnNumerics,
			boolean stemEnglishPossessive) {
		this.charTypeTable = charTypeTable;
		this.splitOnCaseChange = splitOnCaseChange;
		this.splitOnNumerics = splitOnNumerics;
		this.stemEnglishPossessive = stemEnglishPossessive;
	}

	/**
	 * Next.
	 *
	 * @return the int
	 */
	int next() {
		current = end;
		if (current == DONE) {
			return DONE;
		}

		if (skipPossessive) {
			current += 2;
			skipPossessive = false;
		}

		int lastType = 0;

		while (current < endBounds && (isSubwordDelim(lastType = charType(text[current])))) {
			current++;
		}

		if (current >= endBounds) {
			return end = DONE;
		}

		for (end = current + 1; end < endBounds; end++) {
			int type = charType(text[end]);
			if (isBreak(lastType, type)) {
				break;
			}
			lastType = type;
		}

		if (end < endBounds - 1 && endsWithPossessive(end + 2)) {
			skipPossessive = true;
		}

		return end;
	}

	/**
	 * Type.
	 *
	 * @return the int
	 */
	int type() {
		if (end == DONE) {
			return 0;
		}

		int type = charType(text[current]);
		switch (type) {

		case LOWER:
		case UPPER:
			return ALPHA;
		default:
			return type;
		}
	}

	/**
	 * Sets the text.
	 *
	 * @param text the text
	 * @param length the length
	 */
	void setText(char text[], int length) {
		this.text = text;
		this.length = this.endBounds = length;
		current = startBounds = end = 0;
		skipPossessive = hasFinalPossessive = false;
		setBounds();
	}

	/**
	 * Checks if is break.
	 *
	 * @param lastType the last type
	 * @param type the type
	 * @return true, if is break
	 */
	private boolean isBreak(int lastType, int type) {
		if ((type & lastType) != 0) {
			return false;
		}

		if (!splitOnCaseChange && isAlpha(lastType) && isAlpha(type)) {

			return false;
		} else if (isUpper(lastType) && isAlpha(type)) {

			return false;
		} else if (!splitOnNumerics && ((isAlpha(lastType) && isDigit(type)) || (isDigit(lastType) && isAlpha(type)))) {

			return false;
		}

		return true;
	}

	/**
	 * Checks if is single word.
	 *
	 * @return true, if is single word
	 */
	boolean isSingleWord() {
		if (hasFinalPossessive) {
			return current == startBounds && end == endBounds - 2;
		} else {
			return current == startBounds && end == endBounds;
		}
	}

	/**
	 * Sets the bounds.
	 */
	private void setBounds() {
		while (startBounds < length && (isSubwordDelim(charType(text[startBounds])))) {
			startBounds++;
		}

		while (endBounds > startBounds && (isSubwordDelim(charType(text[endBounds - 1])))) {
			endBounds--;
		}
		if (endsWithPossessive(endBounds)) {
			hasFinalPossessive = true;
		}
		current = startBounds;
	}

	/**
	 * Ends with possessive.
	 *
	 * @param pos the pos
	 * @return true, if successful
	 */
	private boolean endsWithPossessive(int pos) {
		return (stemEnglishPossessive && pos > 2 && text[pos - 2] == '\''
				&& (text[pos - 1] == 's' || text[pos - 1] == 'S') && isAlpha(charType(text[pos - 3])) && (pos == endBounds || isSubwordDelim(charType(text[pos]))));
	}

	/**
	 * Char type.
	 *
	 * @param ch the ch
	 * @return the int
	 */
	private int charType(int ch) {
		if (ch < charTypeTable.length) {
			return charTypeTable[ch];
		}
		return getType(ch);
	}

	/**
	 * Gets the type.
	 *
	 * @param ch the ch
	 * @return the type
	 */
	public static byte getType(int ch) {
		switch (Character.getType(ch)) {
		case Character.UPPERCASE_LETTER:
			return UPPER;
		case Character.LOWERCASE_LETTER:
			return LOWER;

		case Character.TITLECASE_LETTER:
		case Character.MODIFIER_LETTER:
		case Character.OTHER_LETTER:
		case Character.NON_SPACING_MARK:
		case Character.ENCLOSING_MARK:
		case Character.COMBINING_SPACING_MARK:
			return ALPHA;

		case Character.DECIMAL_DIGIT_NUMBER:
		case Character.LETTER_NUMBER:
		case Character.OTHER_NUMBER:
			return DIGIT;

		case Character.SURROGATE:
			return ALPHA | DIGIT;

		default:
			return SUBWORD_DELIM;
		}
	}
}