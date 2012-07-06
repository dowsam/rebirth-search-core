/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ScriptStatisticalFacetCollector.java 2012-7-6 14:30:23 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.statistical;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class ScriptStatisticalFacetCollector.
 *
 * @author l.xue.nong
 */
public class ScriptStatisticalFacetCollector extends AbstractFacetCollector {

	/** The script. */
	private final SearchScript script;

	/** The min. */
	private double min = Double.POSITIVE_INFINITY;

	/** The max. */
	private double max = Double.NEGATIVE_INFINITY;

	/** The total. */
	private double total = 0;

	/** The sum of squares. */
	private double sumOfSquares = 0.0;

	/** The count. */
	private long count;

	/**
	 * Instantiates a new script statistical facet collector.
	 *
	 * @param facetName the facet name
	 * @param scriptLang the script lang
	 * @param script the script
	 * @param params the params
	 * @param context the context
	 */
	public ScriptStatisticalFacetCollector(String facetName, String scriptLang, String script,
			Map<String, Object> params, SearchContext context) {
		super(facetName);
		this.script = context.scriptService().search(context.lookup(), scriptLang, script, params);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		script.setNextDocId(doc);
		double value = script.runAsDouble();
		if (value < min) {
			min = value;
		}
		if (value > max) {
			max = value;
		}
		sumOfSquares += value * value;
		total += value;
		count++;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#setScorer(org.apache.lucene.search.Scorer)
	 */
	@Override
	public void setScorer(Scorer scorer) throws IOException {
		script.setScorer(scorer);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		script.setNextReader(reader);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		return new InternalStatisticalFacet(facetName, min, max, total, sumOfSquares, count);
	}
}