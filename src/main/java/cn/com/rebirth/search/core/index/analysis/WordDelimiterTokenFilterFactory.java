/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core WordDelimiterTokenFilterFactory.java 2012-7-6 14:29:03 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterIterator;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * A factory for creating WordDelimiterTokenFilter objects.
 */
public class WordDelimiterTokenFilterFactory extends AbstractTokenFilterFactory {

	/** The char type table. */
	private final byte[] charTypeTable;

	/** The generate word parts. */
	private final boolean generateWordParts;

	/** The generate number parts. */
	private final boolean generateNumberParts;

	/** The catenate words. */
	private final boolean catenateWords;

	/** The catenate numbers. */
	private final boolean catenateNumbers;

	/** The catenate all. */
	private final boolean catenateAll;

	/** The split on case change. */
	private final boolean splitOnCaseChange;

	/** The preserve original. */
	private final boolean preserveOriginal;

	/** The split on numerics. */
	private final boolean splitOnNumerics;

	/** The stem english possessive. */
	private final boolean stemEnglishPossessive;

	/** The proto words. */
	private final CharArraySet protoWords;

	/**
	 * Instantiates a new word delimiter token filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public WordDelimiterTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);

		List<String> charTypeTableValues = Analysis.getWordList(env, settings, "type_table");
		if (charTypeTableValues == null) {
			this.charTypeTable = WordDelimiterIterator.DEFAULT_WORD_DELIM_TABLE;
		} else {
			this.charTypeTable = parseTypes(charTypeTableValues);
		}

		this.generateWordParts = settings.getAsBoolean("generate_word_parts", true);

		this.generateNumberParts = settings.getAsBoolean("generate_number_parts", true);

		this.catenateWords = settings.getAsBoolean("catenate_words", false);

		this.catenateNumbers = settings.getAsBoolean("catenate_numbers", false);

		this.catenateAll = settings.getAsBoolean("catenate_all", false);

		this.splitOnCaseChange = settings.getAsBoolean("split_on_case_change", true);

		this.preserveOriginal = settings.getAsBoolean("preserve_original", false);

		this.splitOnNumerics = settings.getAsBoolean("split_on_numerics", true);

		this.stemEnglishPossessive = settings.getAsBoolean("stem_english_possessive", true);

		Set<?> protectedWords = Analysis.getWordSet(env, settings, "protected_words", version);
		this.protoWords = protectedWords == null ? null : CharArraySet.copy(Lucene.VERSION, protectedWords);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
	 */
	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new WordDelimiterFilter(tokenStream, WordDelimiterIterator.DEFAULT_WORD_DELIM_TABLE,
				generateWordParts ? 1 : 0, generateNumberParts ? 1 : 0, catenateWords ? 1 : 0, catenateNumbers ? 1 : 0,
				catenateAll ? 1 : 0, splitOnCaseChange ? 1 : 0, preserveOriginal ? 1 : 0, splitOnNumerics ? 1 : 0,
				stemEnglishPossessive ? 1 : 0, protoWords);
	}

	/** The type pattern. */
	private static Pattern typePattern = Pattern.compile("(.*)\\s*=>\\s*(.*)\\s*$");

	/**
	 * Parses the types.
	 *
	 * @param rules the rules
	 * @return the byte[]
	 */
	private byte[] parseTypes(Collection<String> rules) {
		SortedMap<Character, Byte> typeMap = new TreeMap<Character, Byte>();
		for (String rule : rules) {
			Matcher m = typePattern.matcher(rule);
			if (!m.find())
				throw new RuntimeException("Invalid Mapping Rule : [" + rule + "]");
			String lhs = parseString(m.group(1).trim());
			Byte rhs = parseType(m.group(2).trim());
			if (lhs.length() != 1)
				throw new RuntimeException("Invalid Mapping Rule : [" + rule + "]. Only a single character is allowed.");
			if (rhs == null)
				throw new RuntimeException("Invalid Mapping Rule : [" + rule + "]. Illegal type.");
			typeMap.put(lhs.charAt(0), rhs);
		}

		byte types[] = new byte[Math.max(typeMap.lastKey() + 1, WordDelimiterIterator.DEFAULT_WORD_DELIM_TABLE.length)];
		for (int i = 0; i < types.length; i++)
			types[i] = WordDelimiterIterator.getType(i);
		for (Map.Entry<Character, Byte> mapping : typeMap.entrySet())
			types[mapping.getKey()] = mapping.getValue();
		return types;
	}

	/**
	 * Parses the type.
	 *
	 * @param s the s
	 * @return the byte
	 */
	private Byte parseType(String s) {
		if (s.equals("LOWER"))
			return WordDelimiterFilter.LOWER;
		else if (s.equals("UPPER"))
			return WordDelimiterFilter.UPPER;
		else if (s.equals("ALPHA"))
			return WordDelimiterFilter.ALPHA;
		else if (s.equals("DIGIT"))
			return WordDelimiterFilter.DIGIT;
		else if (s.equals("ALPHANUM"))
			return WordDelimiterFilter.ALPHANUM;
		else if (s.equals("SUBWORD_DELIM"))
			return WordDelimiterFilter.SUBWORD_DELIM;
		else
			return null;
	}

	/** The out. */
	char[] out = new char[256];

	/**
	 * Parses the string.
	 *
	 * @param s the s
	 * @return the string
	 */
	private String parseString(String s) {
		int readPos = 0;
		int len = s.length();
		int writePos = 0;
		while (readPos < len) {
			char c = s.charAt(readPos++);
			if (c == '\\') {
				if (readPos >= len)
					throw new RuntimeException("Invalid escaped char in [" + s + "]");
				c = s.charAt(readPos++);
				switch (c) {
				case '\\':
					c = '\\';
					break;
				case 'n':
					c = '\n';
					break;
				case 't':
					c = '\t';
					break;
				case 'r':
					c = '\r';
					break;
				case 'b':
					c = '\b';
					break;
				case 'f':
					c = '\f';
					break;
				case 'u':
					if (readPos + 3 >= len)
						throw new RuntimeException("Invalid escaped char in [" + s + "]");
					c = (char) Integer.parseInt(s.substring(readPos, readPos + 4), 16);
					readPos += 4;
					break;
				}
			}
			out[writePos++] = c;
		}
		return new String(out, 0, writePos);
	}
}