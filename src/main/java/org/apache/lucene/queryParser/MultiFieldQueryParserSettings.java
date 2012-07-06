/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MultiFieldQueryParserSettings.java 2012-7-6 14:29:15 l.xue.nong$$
 */

package org.apache.lucene.queryParser;

import gnu.trove.map.hash.TObjectFloatHashMap;

import java.util.List;

/**
 * The Class MultiFieldQueryParserSettings.
 *
 * @author l.xue.nong
 */
public class MultiFieldQueryParserSettings extends QueryParserSettings {

	/** The fields. */
	List<String> fields = null;

	/** The boosts. */
	TObjectFloatHashMap<String> boosts = null;

	/** The tie breaker. */
	float tieBreaker = 0.0f;

	/** The use dis max. */
	boolean useDisMax = true;

	/**
	 * Fields.
	 *
	 * @return the list
	 */
	public List<String> fields() {
		return fields;
	}

	/**
	 * Fields.
	 *
	 * @param fields the fields
	 */
	public void fields(List<String> fields) {
		this.fields = fields;
	}

	/**
	 * Boosts.
	 *
	 * @return the t object float hash map
	 */
	public TObjectFloatHashMap<String> boosts() {
		return boosts;
	}

	/**
	 * Boosts.
	 *
	 * @param boosts the boosts
	 */
	public void boosts(TObjectFloatHashMap<String> boosts) {
		this.boosts = boosts;
	}

	/**
	 * Tie breaker.
	 *
	 * @return the float
	 */
	public float tieBreaker() {
		return tieBreaker;
	}

	/**
	 * Tie breaker.
	 *
	 * @param tieBreaker the tie breaker
	 */
	public void tieBreaker(float tieBreaker) {
		this.tieBreaker = tieBreaker;
	}

	/**
	 * Use dis max.
	 *
	 * @return true, if successful
	 */
	public boolean useDisMax() {
		return useDisMax;
	}

	/**
	 * Use dis max.
	 *
	 * @param useDisMax the use dis max
	 */
	public void useDisMax(boolean useDisMax) {
		this.useDisMax = useDisMax;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.queryParser.QueryParserSettings#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (fields == null || fields.size() == 1)
			return super.equals(o);

		if (!super.equals(o))
			return false;

		MultiFieldQueryParserSettings that = (MultiFieldQueryParserSettings) o;

		if (Float.compare(that.tieBreaker, tieBreaker) != 0)
			return false;
		if (useDisMax != that.useDisMax)
			return false;
		if (boosts != null ? !boosts.equals(that.boosts) : that.boosts != null)
			return false;
		if (fields != null ? !fields.equals(that.fields) : that.fields != null)
			return false;

		return true;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.queryParser.QueryParserSettings#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (fields != null ? fields.hashCode() : 0);
		result = 31 * result + (boosts != null ? boosts.hashCode() : 0);
		result = 31 * result + (tieBreaker != +0.0f ? Float.floatToIntBits(tieBreaker) : 0);
		result = 31 * result + (useDisMax ? 1 : 0);
		return result;
	}
}
