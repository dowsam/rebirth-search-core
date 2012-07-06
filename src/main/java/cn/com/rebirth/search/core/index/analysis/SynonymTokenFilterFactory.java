/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SynonymTokenFilterFactory.java 2012-7-6 14:29:03 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.synonym.SolrSynonymParser;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.synonym.WordnetSynonymParser;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.indices.analysis.IndicesAnalysisService;

/**
 * A factory for creating SynonymTokenFilter objects.
 */
@AnalysisSettingsRequired
public class SynonymTokenFilterFactory extends AbstractTokenFilterFactory {

	/** The synonym map. */
	private final SynonymMap synonymMap;

	/** The ignore case. */
	private final boolean ignoreCase;

	/**
	 * Instantiates a new synonym token filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param indicesAnalysisService the indices analysis service
	 * @param tokenizerFactories the tokenizer factories
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public SynonymTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, Environment env,
			IndicesAnalysisService indicesAnalysisService, Map<String, TokenizerFactoryFactory> tokenizerFactories,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);

		Reader rulesReader = null;
		if (settings.getAsArray("synonyms", null) != null) {
			List<String> rules = Analysis.getWordList(env, settings, "synonyms");
			StringBuilder sb = new StringBuilder();
			for (String line : rules) {
				sb.append(line).append(System.getProperty("line.separator"));
			}
			rulesReader = new StringReader(sb.toString());
		} else if (settings.get("synonyms_path") != null) {
			rulesReader = Analysis.getReaderFromFile(env, settings, "synonyms_path");
		} else {
			throw new RebirthIllegalArgumentException(
					"synonym requires either `synonyms` or `synonyms_path` to be configured");
		}

		this.ignoreCase = settings.getAsBoolean("ignore_case", false);
		boolean expand = settings.getAsBoolean("expand", true);

		String tokenizerName = settings.get("tokenizer", "whitespace");

		TokenizerFactoryFactory tokenizerFactoryFactory = tokenizerFactories.get(tokenizerName);
		if (tokenizerFactoryFactory == null) {
			tokenizerFactoryFactory = indicesAnalysisService.tokenizerFactoryFactory(tokenizerName);
		}
		if (tokenizerFactoryFactory == null) {
			throw new RebirthIllegalArgumentException("failed to find tokenizer [" + tokenizerName
					+ "] for synonym token filter");
		}
		final TokenizerFactory tokenizerFactory = tokenizerFactoryFactory.create(tokenizerName, settings);

		Analyzer analyzer = new ReusableAnalyzerBase() {
			@Override
			protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
				Tokenizer tokenizer = tokenizerFactory == null ? new WhitespaceTokenizer(Lucene.ANALYZER_VERSION,
						reader) : tokenizerFactory.create(reader);
				TokenStream stream = ignoreCase ? new LowerCaseFilter(Lucene.ANALYZER_VERSION, tokenizer) : tokenizer;
				return new TokenStreamComponents(tokenizer, stream);
			}
		};

		try {
			SynonymMap.Builder parser = null;

			if ("wordnet".equalsIgnoreCase(settings.get("format"))) {
				parser = new WordnetSynonymParser(true, expand, analyzer);
				((WordnetSynonymParser) parser).add(rulesReader);
			} else {
				parser = new SolrSynonymParser(true, expand, analyzer);
				((SolrSynonymParser) parser).add(rulesReader);
			}

			synonymMap = parser.build();
		} catch (Exception e) {
			throw new RebirthIllegalArgumentException("failed to build synonyms", e);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.TokenFilterFactory#create(org.apache.lucene.analysis.TokenStream)
	 */
	@Override
	public TokenStream create(TokenStream tokenStream) {

		return synonymMap.fst == null ? tokenStream : new SynonymFilter(tokenStream, synonymMap, ignoreCase);
	}
}