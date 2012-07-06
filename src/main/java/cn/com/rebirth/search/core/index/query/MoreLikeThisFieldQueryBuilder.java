/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MoreLikeThisFieldQueryBuilder.java 2012-7-6 14:29:05 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class MoreLikeThisFieldQueryBuilder.
 *
 * @author l.xue.nong
 */
public class MoreLikeThisFieldQueryBuilder extends BaseQueryBuilder {

	/** The name. */
	private final String name;

	/** The like text. */
	private String likeText;

	/** The percent terms to match. */
	private float percentTermsToMatch = -1;

	/** The min term freq. */
	private int minTermFreq = -1;

	/** The max query terms. */
	private int maxQueryTerms = -1;

	/** The stop words. */
	private String[] stopWords = null;

	/** The min doc freq. */
	private int minDocFreq = -1;

	/** The max doc freq. */
	private int maxDocFreq = -1;

	/** The min word len. */
	private int minWordLen = -1;

	/** The max word len. */
	private int maxWordLen = -1;

	/** The boost terms. */
	private float boostTerms = -1;

	/** The boost. */
	private float boost = -1;

	/** The analyzer. */
	private String analyzer;

	/**
	 * Instantiates a new more like this field query builder.
	 *
	 * @param name the name
	 */
	public MoreLikeThisFieldQueryBuilder(String name) {
		this.name = name;
	}

	/**
	 * Like text.
	 *
	 * @param likeText the like text
	 * @return the more like this field query builder
	 */
	public MoreLikeThisFieldQueryBuilder likeText(String likeText) {
		this.likeText = likeText;
		return this;
	}

	/**
	 * Percent terms to match.
	 *
	 * @param percentTermsToMatch the percent terms to match
	 * @return the more like this field query builder
	 */
	public MoreLikeThisFieldQueryBuilder percentTermsToMatch(float percentTermsToMatch) {
		this.percentTermsToMatch = percentTermsToMatch;
		return this;
	}

	/**
	 * Min term freq.
	 *
	 * @param minTermFreqy the min term freqy
	 * @return the more like this field query builder
	 */
	public MoreLikeThisFieldQueryBuilder minTermFreq(int minTermFreqy) {
		this.minTermFreq = minTermFreqy;
		return this;
	}

	/**
	 * Max query terms.
	 *
	 * @param maxQueryTerms the max query terms
	 * @return the more like this field query builder
	 */
	public MoreLikeThisFieldQueryBuilder maxQueryTerms(int maxQueryTerms) {
		this.maxQueryTerms = maxQueryTerms;
		return this;
	}

	/**
	 * Stop words.
	 *
	 * @param stopWords the stop words
	 * @return the more like this field query builder
	 */
	public MoreLikeThisFieldQueryBuilder stopWords(String... stopWords) {
		this.stopWords = stopWords;
		return this;
	}

	/**
	 * Min doc freq.
	 *
	 * @param minDocFreq the min doc freq
	 * @return the more like this field query builder
	 */
	public MoreLikeThisFieldQueryBuilder minDocFreq(int minDocFreq) {
		this.minDocFreq = minDocFreq;
		return this;
	}

	/**
	 * Max doc freq.
	 *
	 * @param maxDocFreq the max doc freq
	 * @return the more like this field query builder
	 */
	public MoreLikeThisFieldQueryBuilder maxDocFreq(int maxDocFreq) {
		this.maxDocFreq = maxDocFreq;
		return this;
	}

	/**
	 * Min word len.
	 *
	 * @param minWordLen the min word len
	 * @return the more like this field query builder
	 */
	public MoreLikeThisFieldQueryBuilder minWordLen(int minWordLen) {
		this.minWordLen = minWordLen;
		return this;
	}

	/**
	 * Max word len.
	 *
	 * @param maxWordLen the max word len
	 * @return the more like this field query builder
	 */
	public MoreLikeThisFieldQueryBuilder maxWordLen(int maxWordLen) {
		this.maxWordLen = maxWordLen;
		return this;
	}

	/**
	 * Boost terms.
	 *
	 * @param boostTerms the boost terms
	 * @return the more like this field query builder
	 */
	public MoreLikeThisFieldQueryBuilder boostTerms(float boostTerms) {
		this.boostTerms = boostTerms;
		return this;
	}

	/**
	 * Analyzer.
	 *
	 * @param analyzer the analyzer
	 * @return the more like this field query builder
	 */
	public MoreLikeThisFieldQueryBuilder analyzer(String analyzer) {
		this.analyzer = analyzer;
		return this;
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the more like this field query builder
	 */
	public MoreLikeThisFieldQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(MoreLikeThisFieldQueryParser.NAME);
		builder.startObject(name);
		if (likeText == null) {
			throw new QueryBuilderException("moreLikeThisField requires 'like_text' to be provided");
		}
		builder.field("like_text", likeText);
		if (percentTermsToMatch != -1) {
			builder.field("percent_terms_to_match", percentTermsToMatch);
		}
		if (minTermFreq != -1) {
			builder.field("min_term_freq", minTermFreq);
		}
		if (maxQueryTerms != -1) {
			builder.field("max_query_terms", maxQueryTerms);
		}
		if (stopWords != null && stopWords.length > 0) {
			builder.startArray("stop_words");
			for (String stopWord : stopWords) {
				builder.value(stopWord);
			}
			builder.endArray();
		}
		if (minDocFreq != -1) {
			builder.field("min_doc_freq", minDocFreq);
		}
		if (maxDocFreq != -1) {
			builder.field("max_doc_freq", maxDocFreq);
		}
		if (minWordLen != -1) {
			builder.field("min_word_len", minWordLen);
		}
		if (maxWordLen != -1) {
			builder.field("max_word_len", maxWordLen);
		}
		if (boostTerms != -1) {
			builder.field("boost_terms", boostTerms);
		}
		if (boost != -1) {
			builder.field("boost", boost);
		}
		if (analyzer != null) {
			builder.field("analyzer", analyzer);
		}
		builder.endObject();
		builder.endObject();
	}
}