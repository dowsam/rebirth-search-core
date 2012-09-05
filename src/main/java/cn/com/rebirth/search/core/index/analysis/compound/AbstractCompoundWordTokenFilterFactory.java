/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AbstractCompoundWordTokenFilterFactory.java 2012-7-6 14:29:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis.compound;

import java.util.Set;

import org.apache.lucene.analysis.compound.CompoundWordTokenFilterBase;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.analysis.AbstractTokenFilterFactory;
import cn.com.rebirth.search.core.index.analysis.Analysis;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * A factory for creating AbstractCompoundWordTokenFilter objects.
 */
public abstract class AbstractCompoundWordTokenFilterFactory extends AbstractTokenFilterFactory {

	/** The min word size. */
	protected final int minWordSize;

	/** The min subword size. */
	protected final int minSubwordSize;

	/** The max subword size. */
	protected final int maxSubwordSize;

	/** The only longest match. */
	protected final boolean onlyLongestMatch;

	/** The word list. */
	protected final Set<?> wordList;

	/**
	 * Instantiates a new abstract compound word token filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param env the env
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public AbstractCompoundWordTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, Environment env,
			@Assisted String name, @Assisted Settings settings) {
		super(index, indexSettings, name, settings);

		minWordSize = settings.getAsInt("min_word_size", CompoundWordTokenFilterBase.DEFAULT_MIN_WORD_SIZE);
		minSubwordSize = settings.getAsInt("min_subword_size", CompoundWordTokenFilterBase.DEFAULT_MIN_SUBWORD_SIZE);
		maxSubwordSize = settings.getAsInt("max_subword_size", CompoundWordTokenFilterBase.DEFAULT_MAX_SUBWORD_SIZE);
		onlyLongestMatch = settings.getAsBoolean("only_longest_match", false);
		wordList = Analysis.getWordSet(env, settings, "word_list", version);
		if (wordList == null) {
			throw new RebirthIllegalArgumentException("word_list must be provided for [" + name
					+ "], either as a path to a file, or directly");
		}
	}
}