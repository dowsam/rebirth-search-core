/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core Analysis.java 2012-3-29 15:01:01 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.bg.BulgarianAnalyzer;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.ca.CatalanAnalyzer;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.da.DanishAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.eu.BasqueAnalyzer;
import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.analysis.fi.FinnishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.gl.GalicianAnalyzer;
import org.apache.lucene.analysis.hi.HindiAnalyzer;
import org.apache.lucene.analysis.hu.HungarianAnalyzer;
import org.apache.lucene.analysis.hy.ArmenianAnalyzer;
import org.apache.lucene.analysis.id.IndonesianAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;
import org.apache.lucene.util.Version;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.core.env.Environment;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;


/**
 * The Class Analysis.
 *
 * @author l.xue.nong
 */
public class Analysis {

	
	/**
	 * Checks if is no stopwords.
	 *
	 * @param settings the settings
	 * @return true, if is no stopwords
	 */
	public static boolean isNoStopwords(Settings settings) {
		String value = settings.get("stopwords");
		return value != null && "_none_".equals(value);
	}

	
	/**
	 * Parses the stem exclusion.
	 *
	 * @param settings the settings
	 * @param defaultStemExclusion the default stem exclusion
	 * @return the sets the
	 */
	public static Set<?> parseStemExclusion(Settings settings, Set<?> defaultStemExclusion) {
		String value = settings.get("stem_exclusion");
		if (value != null) {
			if ("_none_".equals(value)) {
				return ImmutableSet.of();
			} else {
				return ImmutableSet.copyOf(Strings.commaDelimitedListToSet(value));
			}
		}
		String[] stopWords = settings.getAsArray("stem_exclusion", null);
		if (stopWords != null) {
			return ImmutableSet.copyOf(Iterators.forArray(stopWords));
		} else {
			return defaultStemExclusion;
		}
	}

	
	/** The Constant namedStopWords. */
	public static final ImmutableMap<String, Set<?>> namedStopWords = MapBuilder.<String, Set<?>> newMapBuilder()
			.put("_arabic_", ArabicAnalyzer.getDefaultStopSet())
			.put("_armenian_", ArmenianAnalyzer.getDefaultStopSet())
			.put("_basque_", BasqueAnalyzer.getDefaultStopSet())
			.put("_brazilian_", BrazilianAnalyzer.getDefaultStopSet())
			.put("_bulgarian_", BulgarianAnalyzer.getDefaultStopSet())
			.put("_catalan_", CatalanAnalyzer.getDefaultStopSet()).put("_czech_", CzechAnalyzer.getDefaultStopSet())
			.put("_danish_", DanishAnalyzer.getDefaultStopSet()).put("_dutch_", DutchAnalyzer.getDefaultStopSet())
			.put("_english_", EnglishAnalyzer.getDefaultStopSet())
			.put("_finnish_", FinnishAnalyzer.getDefaultStopSet()).put("_french_", FrenchAnalyzer.getDefaultStopSet())
			.put("_galician_", GalicianAnalyzer.getDefaultStopSet())
			.put("_german_", GermanAnalyzer.getDefaultStopSet()).put("_greek_", GreekAnalyzer.getDefaultStopSet())
			.put("_hindi_", HindiAnalyzer.getDefaultStopSet())
			.put("_hungarian_", HungarianAnalyzer.getDefaultStopSet())
			.put("_indonesian_", IndonesianAnalyzer.getDefaultStopSet())
			.put("_italian_", ItalianAnalyzer.getDefaultStopSet())
			.put("_norwegian_", NorwegianAnalyzer.getDefaultStopSet())
			.put("_persian_", PersianAnalyzer.getDefaultStopSet())
			.put("_portuguese_", PortugueseAnalyzer.getDefaultStopSet())
			.put("_romanian_", RomanianAnalyzer.getDefaultStopSet())
			.put("_russian_", RussianAnalyzer.getDefaultStopSet())
			.put("_spanish_", SpanishAnalyzer.getDefaultStopSet())
			.put("_swedish_", SwedishAnalyzer.getDefaultStopSet())
			.put("_turkish_", TurkishAnalyzer.getDefaultStopSet()).immutableMap();

	
	/**
	 * Parses the articles.
	 *
	 * @param env the env
	 * @param settings the settings
	 * @param version the version
	 * @return the sets the
	 */
	public static Set<?> parseArticles(Environment env, Settings settings, Version version) {
		String value = settings.get("articles");
		if (value != null) {
			if ("_none_".equals(value)) {
				return CharArraySet.EMPTY_SET;
			} else {
				return new CharArraySet(version, Strings.commaDelimitedListToSet(value), settings.getAsBoolean(
						"articles_case", false));
			}
		}
		String[] articles = settings.getAsArray("articles", null);
		if (articles != null) {
			return new CharArraySet(version, Arrays.asList(articles), settings.getAsBoolean("articles_case", false));
		}
		CharArraySet pathLoadedArticles = getWordSet(env, settings, "articles", version);
		if (pathLoadedArticles != null) {
			return pathLoadedArticles;
		}

		return null;
	}

	
	/**
	 * Parses the stop words.
	 *
	 * @param env the env
	 * @param settings the settings
	 * @param defaultStopWords the default stop words
	 * @param version the version
	 * @return the sets the
	 */
	public static Set<?> parseStopWords(Environment env, Settings settings, Set<?> defaultStopWords, Version version) {
		String value = settings.get("stopwords");
		if (value != null) {
			if ("_none_".equals(value)) {
				return CharArraySet.EMPTY_SET;
			} else {
				return new CharArraySet(version, Strings.commaDelimitedListToSet(value), settings.getAsBoolean(
						"stopwords_case", false));
			}
		}
		String[] stopWords = settings.getAsArray("stopwords", null);
		if (stopWords != null) {
			CharArraySet setStopWords = new CharArraySet(version, stopWords.length, settings.getAsBoolean(
					"stopwords_case", false));
			for (String stopWord : stopWords) {
				if (namedStopWords.containsKey(stopWord)) {
					setStopWords.addAll(namedStopWords.get(stopWord));
				} else {
					setStopWords.add(stopWord);
				}
			}
			return setStopWords;
		}
		List<String> pathLoadedStopWords = getWordList(env, settings, "stopwords");
		if (pathLoadedStopWords != null) {
			CharArraySet setStopWords = new CharArraySet(version, pathLoadedStopWords.size(), settings.getAsBoolean(
					"stopwords_case", false));
			for (String stopWord : pathLoadedStopWords) {
				if (namedStopWords.containsKey(stopWord)) {
					setStopWords.addAll(namedStopWords.get(stopWord));
				} else {
					setStopWords.add(stopWord);
				}
			}
			return setStopWords;
		}

		return defaultStopWords;
	}

	
	/**
	 * Gets the word set.
	 *
	 * @param env the env
	 * @param settings the settings
	 * @param settingsPrefix the settings prefix
	 * @param version the version
	 * @return the word set
	 */
	public static CharArraySet getWordSet(Environment env, Settings settings, String settingsPrefix, Version version) {
		List<String> wordList = getWordList(env, settings, settingsPrefix);
		if (wordList == null) {
			return null;
		}
		return new CharArraySet(version, wordList, settings.getAsBoolean(settingsPrefix + "_case", false));
	}

	
	/**
	 * Gets the word list.
	 *
	 * @param env the env
	 * @param settings the settings
	 * @param settingPrefix the setting prefix
	 * @return the word list
	 */
	public static List<String> getWordList(Environment env, Settings settings, String settingPrefix) {
		String wordListPath = settings.get(settingPrefix + "_path", null);

		if (wordListPath == null) {
			String[] explicitWordList = settings.getAsArray(settingPrefix, null);
			if (explicitWordList == null) {
				return null;
			} else {
				return Arrays.asList(explicitWordList);
			}
		}

		URL wordListFile = env.resolveConfig(wordListPath);

		try {
			return loadWordList(new InputStreamReader(wordListFile.openStream(), Charsets.UTF_8), "#");
		} catch (IOException ioe) {
			String message = String.format("IOException while reading %s_path: %s", settingPrefix, ioe.getMessage());
			throw new RestartIllegalArgumentException(message);
		}
	}

	
	/**
	 * Load word list.
	 *
	 * @param reader the reader
	 * @param comment the comment
	 * @return the list
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static List<String> loadWordList(Reader reader, String comment) throws IOException {
		final List<String> result = new ArrayList<String>();
		BufferedReader br = null;
		try {
			if (reader instanceof BufferedReader) {
				br = (BufferedReader) reader;
			} else {
				br = new BufferedReader(reader);
			}
			String word = null;
			while ((word = br.readLine()) != null) {
				if (!Strings.hasText(word)) {
					continue;
				}
				if (!word.startsWith(comment)) {
					result.add(word.trim());
				}
			}
		} finally {
			if (br != null)
				br.close();
		}
		return result;
	}

	
	/**
	 * Gets the reader from file.
	 *
	 * @param env the env
	 * @param settings the settings
	 * @param settingPrefix the setting prefix
	 * @return the reader from file
	 */
	public static Reader getReaderFromFile(Environment env, Settings settings, String settingPrefix) {
		String filePath = settings.get(settingPrefix, null);

		if (filePath == null) {
			return null;
		}

		URL fileUrl = env.resolveConfig(filePath);

		Reader reader = null;
		try {
			reader = new InputStreamReader(fileUrl.openStream(), Charsets.UTF_8);
		} catch (IOException ioe) {
			String message = String.format("IOException while reading %s_path: %s", settingPrefix, ioe.getMessage());
			throw new RestartIllegalArgumentException(message);
		}

		return reader;
	}
}
