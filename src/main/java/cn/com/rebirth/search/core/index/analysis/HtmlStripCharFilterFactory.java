/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core HtmlStripCharFilterFactory.java 2012-3-29 15:00:58 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.CharStream;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.inject.assistedinject.Assisted;
import cn.com.rebirth.search.commons.lucene.analysis.HTMLStripCharFilter;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

import com.google.common.collect.ImmutableSet;


/**
 * A factory for creating HtmlStripCharFilter objects.
 */
public class HtmlStripCharFilterFactory extends AbstractCharFilterFactory {

	
	/** The escaped tags. */
	private final ImmutableSet<String> escapedTags;

	
	/** The read ahead limit. */
	private final int readAheadLimit;

	
	/**
	 * Instantiates a new html strip char filter factory.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param name the name
	 * @param settings the settings
	 */
	@Inject
	public HtmlStripCharFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name,
			@Assisted Settings settings) {
		super(index, indexSettings, name);
		this.readAheadLimit = settings.getAsInt("read_ahead", HTMLStripCharFilter.DEFAULT_READ_AHEAD);
		String[] escapedTags = settings.getAsArray("escaped_tags");
		if (escapedTags.length > 0) {
			this.escapedTags = ImmutableSet.copyOf(escapedTags);
		} else {
			this.escapedTags = null;
		}
	}

	
	/**
	 * Escaped tags.
	 *
	 * @return the immutable set
	 */
	public ImmutableSet<String> escapedTags() {
		return escapedTags;
	}

	
	/**
	 * Read ahead limit.
	 *
	 * @return the int
	 */
	public int readAheadLimit() {
		return readAheadLimit;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.analysis.CharFilterFactory#create(org.apache.lucene.analysis.CharStream)
	 */
	@Override
	public CharStream create(CharStream tokenStream) {
		return new HTMLStripCharFilter(tokenStream, escapedTags, readAheadLimit);
	}
}
