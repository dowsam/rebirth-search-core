/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PathHierarchyTokenizerFactory.java 2012-7-6 14:29:22 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.path.PathHierarchyTokenizer;
import org.apache.lucene.analysis.path.ReversePathHierarchyTokenizer;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * A factory for creating PathHierarchyTokenizer objects.
 */
public class PathHierarchyTokenizerFactory extends AbstractTokenizerFactory {

	/** The buffer size. */
	private final int bufferSize;

	/** The delimiter. */
	private final char delimiter;

	/** The replacement. */
	private final char replacement;

	/** The skip. */
	private final int skip;

	/** The reverse. */
	private final boolean reverse;

	/**
	 * Instantiates a new path hierarchy tokenizer factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public PathHierarchyTokenizerFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name, settings);
		bufferSize = settings.getAsInt("buffer_size", 1024);
		String delimiter = settings.get("delimiter");
		if (delimiter == null) {
			this.delimiter = PathHierarchyTokenizer.DEFAULT_DELIMITER;
		} else if (delimiter.length() > 1) {
			throw new RebirthIllegalArgumentException("delimiter can only be a one char value");
		} else {
			this.delimiter = delimiter.charAt(0);
		}

		String replacement = settings.get("replacement");
		if (replacement == null) {
			this.replacement = this.delimiter;
		} else if (replacement.length() > 1) {
			throw new RebirthIllegalArgumentException("replacement can only be a one char value");
		} else {
			this.replacement = replacement.charAt(0);
		}
		this.skip = settings.getAsInt("skip", PathHierarchyTokenizer.DEFAULT_SKIP);
		this.reverse = settings.getAsBoolean("reverse", false);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.TokenizerFactory#create(java.io.Reader)
	 */
	@Override
	public Tokenizer create(Reader reader) {
		if (reverse) {
			return new ReversePathHierarchyTokenizer(reader, bufferSize, delimiter, replacement, skip);
		}
		return new PathHierarchyTokenizer(reader, bufferSize, delimiter, replacement, skip);
	}
}
