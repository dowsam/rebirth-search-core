/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchScript.java 2012-7-6 14:29:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.script;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import java.util.Map;

/**
 * The Interface SearchScript.
 *
 * @author l.xue.nong
 */
public interface SearchScript extends ExecutableScript {

	/**
	 * Sets the scorer.
	 *
	 * @param scorer the new scorer
	 */
	void setScorer(Scorer scorer);

	/**
	 * Sets the next reader.
	 *
	 * @param reader the new next reader
	 */
	void setNextReader(IndexReader reader);

	/**
	 * Sets the next doc id.
	 *
	 * @param doc the new next doc id
	 */
	void setNextDocId(int doc);

	/**
	 * Sets the next source.
	 *
	 * @param source the source
	 */
	void setNextSource(Map<String, Object> source);

	/**
	 * Sets the next score.
	 *
	 * @param score the new next score
	 */
	void setNextScore(float score);

	/**
	 * Run as float.
	 *
	 * @return the float
	 */
	float runAsFloat();

	/**
	 * Run as long.
	 *
	 * @return the long
	 */
	long runAsLong();

	/**
	 * Run as double.
	 *
	 * @return the double
	 */
	double runAsDouble();
}