/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AbstractSearchScript.java 2012-3-29 15:02:20 l.xue.nong$$
 */


package cn.com.rebirth.search.core.script;

import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.search.core.search.lookup.DocLookup;
import cn.com.rebirth.search.core.search.lookup.FieldsLookup;
import cn.com.rebirth.search.core.search.lookup.SearchLookup;
import cn.com.rebirth.search.core.search.lookup.SourceLookup;


/**
 * The Class AbstractSearchScript.
 *
 * @author l.xue.nong
 */
public abstract class AbstractSearchScript extends AbstractExecutableScript implements SearchScript {

	
	/** The lookup. */
	private SearchLookup lookup;

	
	/** The score. */
	private float score = Float.NaN;

	
	/**
	 * Score.
	 *
	 * @return the float
	 */
	protected final float score() {
		return score;
	}

	
	/**
	 * Doc.
	 *
	 * @return the doc lookup
	 */
	protected final DocLookup doc() {
		return lookup.doc();
	}

	
	/**
	 * Source.
	 *
	 * @return the source lookup
	 */
	protected final SourceLookup source() {
		return lookup.source();
	}

	
	/**
	 * Fields.
	 *
	 * @return the fields lookup
	 */
	protected final FieldsLookup fields() {
		return lookup.fields();
	}

	
	/**
	 * Sets the lookup.
	 *
	 * @param lookup the new lookup
	 */
	void setLookup(SearchLookup lookup) {
		this.lookup = lookup;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.SearchScript#setScorer(org.apache.lucene.search.Scorer)
	 */
	@Override
	public void setScorer(Scorer scorer) {
		lookup.setScorer(scorer);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.SearchScript#setNextReader(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public void setNextReader(IndexReader reader) {
		lookup.setNextReader(reader);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.SearchScript#setNextDocId(int)
	 */
	@Override
	public void setNextDocId(int doc) {
		lookup.setNextDocId(doc);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.SearchScript#setNextSource(java.util.Map)
	 */
	@Override
	public void setNextSource(Map<String, Object> source) {
		lookup.source().setNextSource(source);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.SearchScript#setNextScore(float)
	 */
	@Override
	public void setNextScore(float score) {
		this.score = score;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.SearchScript#runAsFloat()
	 */
	@Override
	public float runAsFloat() {
		return ((Number) run()).floatValue();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.SearchScript#runAsLong()
	 */
	@Override
	public long runAsLong() {
		return ((Number) run()).longValue();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.SearchScript#runAsDouble()
	 */
	@Override
	public double runAsDouble() {
		return ((Number) run()).doubleValue();
	}
}