/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PatternTokenizerFactory.java 2012-7-6 14:30:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.pattern.PatternTokenizer;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.regex.Regex;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * A factory for creating PatternTokenizer objects.
 */
public class PatternTokenizerFactory extends AbstractTokenizerFactory {

	/** The pattern. */
	private final Pattern pattern;

	/** The group. */
	private final int group;

	/**
	 * Instantiates a new pattern tokenizer factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public PatternTokenizerFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);

		String sPattern = settings.get("pattern", "\\W+");
		if (sPattern == null) {
			throw new RebirthIllegalArgumentException("pattern is missing for [" + name
					+ "] tokenizer of type 'pattern'");
		}

		this.pattern = Regex.compile(sPattern, settings.get("flags"));
		this.group = settings.getAsInt("group", -1);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.TokenizerFactory#create(java.io.Reader)
	 */
	@Override
	public Tokenizer create(Reader reader) {
		try {
			return new PatternTokenizer(reader, pattern, group);
		} catch (IOException e) {
			throw new RebirthIllegalArgumentException("failed to create pattern tokenizer", e);
		}
	}
}