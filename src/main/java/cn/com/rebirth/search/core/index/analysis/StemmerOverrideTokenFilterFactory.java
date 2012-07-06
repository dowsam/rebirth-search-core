/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core StemmerOverrideTokenFilterFactory.java 2012-3-29 15:01:06 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.StemmerOverrideFilter;
import org.apache.lucene.util.Version;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;


/**
 * A factory for creating StemmerOverrideTokenFilter objects.
 */
@AnalysisSettingsRequired
public class StemmerOverrideTokenFilterFactory extends AbstractTokenFilterFactory {

	
	/** The dictionary. */
	private final Map<String, String> dictionary;

	
	/**
	 * Instantiates a new stemmer override token filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public StemmerOverrideTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);

		List<String> rules = Analysis.getWordList(env, settings, "rules");
		if (rules == null) {
			throw new RestartIllegalArgumentException(
					"stemmer override filter requires either `rules` or `rules_path` to be configured");
		}
		dictionary = new HashMap<String, String>();
		parseRules(rules, dictionary, "=>");
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
	 */
	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new StemmerOverrideFilter(Version.LUCENE_32, tokenStream, dictionary);
	}

	
	/**
	 * Parses the rules.
	 *
	 * @param rules the rules
	 * @param rulesMap the rules map
	 * @param mappingSep the mapping sep
	 */
	static void parseRules(List<String> rules, Map<String, String> rulesMap, String mappingSep) {
		for (String rule : rules) {
			String key, override;
			List<String> mapping = Strings.splitSmart(rule, mappingSep, false);
			if (mapping.size() == 2) {
				key = mapping.get(0).trim();
				override = mapping.get(1).trim();
			} else {
				throw new RuntimeException("Invalid Keyword override Rule:" + rule);
			}

			if (key.isEmpty() || override.isEmpty()) {
				throw new RuntimeException("Invalid Keyword override Rule:" + rule);
			} else {
				rulesMap.put(key, override);
			}
		}
	}

}
