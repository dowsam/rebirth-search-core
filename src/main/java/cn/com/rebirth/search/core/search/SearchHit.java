/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchHit.java 2012-7-6 14:30:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search;

import java.util.Map;

import org.apache.lucene.search.Explanation;

import cn.com.rebirth.commons.exception.RebirthParseException;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.xcontent.ToXContent;
import cn.com.rebirth.search.core.search.highlight.HighlightField;

/**
 * The Interface SearchHit.
 *
 * @author l.xue.nong
 */
public interface SearchHit extends Streamable, ToXContent, Iterable<SearchHitField> {

	/**
	 * Score.
	 *
	 * @return the float
	 */
	float score();

	/**
	 * Gets the score.
	 *
	 * @return the score
	 */
	float getScore();

	/**
	 * Index.
	 *
	 * @return the string
	 */
	String index();

	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	String getIndex();

	/**
	 * Id.
	 *
	 * @return the string
	 */
	String id();

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	String getId();

	/**
	 * Type.
	 *
	 * @return the string
	 */
	String type();

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	String getType();

	/**
	 * Version.
	 *
	 * @return the long
	 */
	long version();

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	long getVersion();

	/**
	 * Source.
	 *
	 * @return the byte[]
	 */
	byte[] source();

	/**
	 * Checks if is source empty.
	 *
	 * @return true, if is source empty
	 */
	boolean isSourceEmpty();

	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	Map<String, Object> getSource();

	/**
	 * Source as string.
	 *
	 * @return the string
	 */
	String sourceAsString();

	/**
	 * Source as map.
	 *
	 * @return the map
	 * @throws RebirthParseException the rebirth parse exception
	 */
	Map<String, Object> sourceAsMap() throws RebirthParseException;

	/**
	 * Explanation.
	 *
	 * @return the explanation
	 */
	Explanation explanation();

	/**
	 * Gets the explanation.
	 *
	 * @return the explanation
	 */
	Explanation getExplanation();

	/**
	 * Field.
	 *
	 * @param fieldName the field name
	 * @return the search hit field
	 */
	public SearchHitField field(String fieldName);

	/**
	 * Fields.
	 *
	 * @return the map
	 */
	Map<String, SearchHitField> fields();

	/**
	 * Gets the fields.
	 *
	 * @return the fields
	 */
	Map<String, SearchHitField> getFields();

	/**
	 * Highlight fields.
	 *
	 * @return the map
	 */
	Map<String, HighlightField> highlightFields();

	/**
	 * Gets the highlight fields.
	 *
	 * @return the highlight fields
	 */
	Map<String, HighlightField> getHighlightFields();

	/**
	 * Sort values.
	 *
	 * @return the object[]
	 */
	Object[] sortValues();

	/**
	 * Gets the sort values.
	 *
	 * @return the sort values
	 */
	Object[] getSortValues();

	/**
	 * Matched filters.
	 *
	 * @return the string[]
	 */
	String[] matchedFilters();

	/**
	 * Gets the matched filters.
	 *
	 * @return the matched filters
	 */
	String[] getMatchedFilters();

	/**
	 * Shard.
	 *
	 * @return the search shard target
	 */
	SearchShardTarget shard();

	/**
	 * Gets the shard.
	 *
	 * @return the shard
	 */
	SearchShardTarget getShard();
}
