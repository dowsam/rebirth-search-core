/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MappingCharFilterFactory.java 2012-3-29 15:01:09 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.CharStream;
import org.apache.lucene.analysis.MappingCharFilter;
import org.apache.lucene.analysis.NormalizeCharMap;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * A factory for creating MappingCharFilter objects.
 */
@AnalysisSettingsRequired
public class MappingCharFilterFactory extends AbstractCharFilterFactory {

	
	/** The norm map. */
	private final NormalizeCharMap normMap;

	
	/**
	 * Instantiates a new mapping char filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public MappingCharFilterFactory(Index index, @IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name);

		List<String> rules = Analysis.getWordList(env, settings, "mappings");
		if (rules == null) {
			throw new RestartIllegalArgumentException(
					"mapping requires either `mappings` or `mappings_path` to be configured");
		}

		normMap = new NormalizeCharMap();
		parseRules(rules, normMap);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.CharFilterFactory#create(org.apache.lucene.analysis.CharStream)
	 */
	@Override
	public CharStream create(CharStream tokenStream) {
		return new MappingCharFilter(normMap, tokenStream);
	}

	
	
	/** The rule pattern. */
	private static Pattern rulePattern = Pattern.compile("(.*)\\s*=>\\s*(.*)\\s*$");

	
	/**
	 * Parses the rules.
	 *
	 * @param rules the rules
	 * @param map the map
	 */
	private void parseRules(List<String> rules, NormalizeCharMap map) {
		for (String rule : rules) {
			Matcher m = rulePattern.matcher(rule);
			if (!m.find())
				throw new RuntimeException("Invalid Mapping Rule : [" + rule + "]");
			String lhs = parseString(m.group(1).trim());
			String rhs = parseString(m.group(2).trim());
			if (lhs == null || rhs == null)
				throw new RuntimeException("Invalid Mapping Rule : [" + rule + "]. Illegal mapping.");
			map.add(lhs, rhs);
		}
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
