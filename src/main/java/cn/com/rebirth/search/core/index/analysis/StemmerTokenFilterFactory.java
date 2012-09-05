/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StemmerTokenFilterFactory.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ar.ArabicStemFilter;
import org.apache.lucene.analysis.br.BrazilianStemFilter;
import org.apache.lucene.analysis.cz.CzechStemFilter;
import org.apache.lucene.analysis.de.GermanLightStemFilter;
import org.apache.lucene.analysis.de.GermanMinimalStemFilter;
import org.apache.lucene.analysis.el.GreekStemFilter;
import org.apache.lucene.analysis.en.EnglishMinimalStemFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.es.SpanishLightStemFilter;
import org.apache.lucene.analysis.fi.FinnishLightStemFilter;
import org.apache.lucene.analysis.fr.FrenchLightStemFilter;
import org.apache.lucene.analysis.fr.FrenchMinimalStemFilter;
import org.apache.lucene.analysis.hi.HindiStemFilter;
import org.apache.lucene.analysis.hu.HungarianLightStemFilter;
import org.apache.lucene.analysis.id.IndonesianStemFilter;
import org.apache.lucene.analysis.it.ItalianLightStemFilter;
import org.apache.lucene.analysis.pt.PortugueseLightStemFilter;
import org.apache.lucene.analysis.pt.PortugueseMinimalStemFilter;
import org.apache.lucene.analysis.pt.PortugueseStemFilter;
import org.apache.lucene.analysis.ru.RussianLightStemFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.sv.SwedishLightStemFilter;
import org.tartarus.snowball.ext.ArmenianStemmer;
import org.tartarus.snowball.ext.BasqueStemmer;
import org.tartarus.snowball.ext.CatalanStemmer;
import org.tartarus.snowball.ext.DanishStemmer;
import org.tartarus.snowball.ext.DutchStemmer;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.FinnishStemmer;
import org.tartarus.snowball.ext.FrenchStemmer;
import org.tartarus.snowball.ext.German2Stemmer;
import org.tartarus.snowball.ext.GermanStemmer;
import org.tartarus.snowball.ext.HungarianStemmer;
import org.tartarus.snowball.ext.ItalianStemmer;
import org.tartarus.snowball.ext.KpStemmer;
import org.tartarus.snowball.ext.LovinsStemmer;
import org.tartarus.snowball.ext.NorwegianStemmer;
import org.tartarus.snowball.ext.PorterStemmer;
import org.tartarus.snowball.ext.PortugueseStemmer;
import org.tartarus.snowball.ext.RomanianStemmer;
import org.tartarus.snowball.ext.RussianStemmer;
import org.tartarus.snowball.ext.SpanishStemmer;
import org.tartarus.snowball.ext.SwedishStemmer;
import org.tartarus.snowball.ext.TurkishStemmer;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * A factory for creating StemmerTokenFilter objects.
 */
public class StemmerTokenFilterFactory extends AbstractTokenFilterFactory {

	/** The language. */
	private String language;

	/**
	 * Instantiates a new stemmer token filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public StemmerTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		this.language = Strings.capitalize(settings.get("language", settings.get("name", "porter")));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
	 */
	@Override
	public TokenStream create(TokenStream tokenStream) {
		if ("arabic".equalsIgnoreCase(language)) {
			return new ArabicStemFilter(tokenStream);
		} else if ("armenian".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new ArmenianStemmer());
		} else if ("basque".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new BasqueStemmer());
		} else if ("brazilian".equalsIgnoreCase(language)) {
			return new BrazilianStemFilter(tokenStream);
		} else if ("catalan".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new CatalanStemmer());
		} else if ("czech".equalsIgnoreCase(language)) {
			return new CzechStemFilter(tokenStream);
		} else if ("danish".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new DanishStemmer());
		} else if ("dutch".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new DutchStemmer());
		} else if ("english".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new EnglishStemmer());
		} else if ("finnish".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new FinnishStemmer());
		} else if ("french".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new FrenchStemmer());
		} else if ("german".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new GermanStemmer());
		} else if ("german2".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new German2Stemmer());
		} else if ("hungarian".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new HungarianStemmer());
		} else if ("italian".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new ItalianStemmer());
		} else if ("kp".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new KpStemmer());
		} else if ("kstem".equalsIgnoreCase(language)) {
			return new KStemFilter(tokenStream);
		} else if ("lovins".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new LovinsStemmer());
		} else if ("norwegian".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new NorwegianStemmer());
		} else if ("porter".equalsIgnoreCase(language)) {
			return new PorterStemFilter(tokenStream);
		} else if ("porter2".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new PorterStemmer());
		} else if ("portuguese".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new PortugueseStemmer());
		} else if ("romanian".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new RomanianStemmer());
		} else if ("russian".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new RussianStemmer());
		} else if ("spanish".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new SpanishStemmer());
		} else if ("swedish".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new SwedishStemmer());
		} else if ("turkish".equalsIgnoreCase(language)) {
			return new SnowballFilter(tokenStream, new TurkishStemmer());
		} else if ("minimal_english".equalsIgnoreCase(language) || "minimalEnglish".equalsIgnoreCase(language)) {
			return new EnglishMinimalStemFilter(tokenStream);
		} else if ("possessive_english".equalsIgnoreCase(language) || "possessiveEnglish".equalsIgnoreCase(language)) {
			return new EnglishPossessiveFilter(tokenStream);
		} else if ("light_finish".equalsIgnoreCase(language) || "lightFinish".equalsIgnoreCase(language)) {
			return new FinnishLightStemFilter(tokenStream);
		} else if ("light_french".equalsIgnoreCase(language) || "lightFrench".equalsIgnoreCase(language)) {
			return new FrenchLightStemFilter(tokenStream);
		} else if ("minimal_french".equalsIgnoreCase(language) || "minimalFrench".equalsIgnoreCase(language)) {
			return new FrenchMinimalStemFilter(tokenStream);
		} else if ("light_german".equalsIgnoreCase(language) || "lightGerman".equalsIgnoreCase(language)) {
			return new GermanLightStemFilter(tokenStream);
		} else if ("minimal_german".equalsIgnoreCase(language) || "minimalGerman".equalsIgnoreCase(language)) {
			return new GermanMinimalStemFilter(tokenStream);
		} else if ("hindi".equalsIgnoreCase(language)) {
			return new HindiStemFilter(tokenStream);
		} else if ("light_hungarian".equalsIgnoreCase(language) || "lightHungarian".equalsIgnoreCase(language)) {
			return new HungarianLightStemFilter(tokenStream);
		} else if ("indonesian".equalsIgnoreCase(language)) {
			return new IndonesianStemFilter(tokenStream);
		} else if ("light_italian".equalsIgnoreCase(language) || "lightItalian".equalsIgnoreCase(language)) {
			return new ItalianLightStemFilter(tokenStream);
		} else if ("light_portuguese".equalsIgnoreCase(language) || "lightPortuguese".equalsIgnoreCase(language)) {
			return new PortugueseLightStemFilter(tokenStream);
		} else if ("minimal_portuguese".equalsIgnoreCase(language) || "minimalPortuguese".equalsIgnoreCase(language)) {
			return new PortugueseMinimalStemFilter(tokenStream);
		} else if ("portuguese".equalsIgnoreCase(language)) {
			return new PortugueseStemFilter(tokenStream);
		} else if ("light_russian".equalsIgnoreCase(language) || "lightRussian".equalsIgnoreCase(language)) {
			return new RussianLightStemFilter(tokenStream);
		} else if ("light_spanish".equalsIgnoreCase(language) || "lightSpanish".equalsIgnoreCase(language)) {
			return new SpanishLightStemFilter(tokenStream);
		} else if ("light_swedish".equalsIgnoreCase(language) || "lightSwedish".equalsIgnoreCase(language)) {
			return new SwedishLightStemFilter(tokenStream);
		} else if ("greek".equalsIgnoreCase(language)) {
			return new GreekStemFilter(tokenStream);
		}
		return new SnowballFilter(tokenStream, language);
	}

}
