/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ScriptHistogramFacetCollector.java 2012-3-29 15:02:35 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.histogram.unbounded;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.commons.trove.ExtTLongObjectHashMap;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class ScriptHistogramFacetCollector.
 *
 * @author l.xue.nong
 */
public class ScriptHistogramFacetCollector extends AbstractFacetCollector {

	
	/** The key script. */
	private final SearchScript keyScript;

	
	/** The value script. */
	private final SearchScript valueScript;

	
	/** The interval. */
	private final long interval;

	
	/** The comparator type. */
	private final HistogramFacet.ComparatorType comparatorType;

	
	/** The entries. */
	final ExtTLongObjectHashMap<InternalFullHistogramFacet.FullEntry> entries = CacheRecycler.popLongObjectMap();

	
	/**
	 * Instantiates a new script histogram facet collector.
	 *
	 * @param facetName the facet name
	 * @param scriptLang the script lang
	 * @param keyScript the key script
	 * @param valueScript the value script
	 * @param params the params
	 * @param interval the interval
	 * @param comparatorType the comparator type
	 * @param context the context
	 */
	public ScriptHistogramFacetCollector(String facetName, String scriptLang, String keyScript, String valueScript,
			Map<String, Object> params, long interval, HistogramFacet.ComparatorType comparatorType,
			SearchContext context) {
		super(facetName);
		this.keyScript = context.scriptService().search(context.lookup(), scriptLang, keyScript, params);
		this.valueScript = context.scriptService().search(context.lookup(), scriptLang, valueScript, params);
		this.interval = interval > 0 ? interval : 0;
		this.comparatorType = comparatorType;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		keyScript.setNextDocId(doc);
		valueScript.setNextDocId(doc);
		long bucket;
		if (interval == 0) {
			bucket = keyScript.runAsLong();
		} else {
			bucket = bucket(keyScript.runAsDouble(), interval);
		}
		double value = valueScript.runAsDouble();

		InternalFullHistogramFacet.FullEntry entry = entries.get(bucket);
		if (entry == null) {
			entry = new InternalFullHistogramFacet.FullEntry(bucket, 1, value, value, 1, value);
			entries.put(bucket, entry);
		} else {
			entry.count++;
			entry.totalCount++;
			entry.total += value;
			if (value < entry.min) {
				entry.min = value;
			}
			if (value > entry.max) {
				entry.max = value;
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#setScorer(org.apache.lucene.search.Scorer)
	 */
	@Override
	public void setScorer(Scorer scorer) throws IOException {
		keyScript.setScorer(scorer);
		valueScript.setScorer(scorer);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		keyScript.setNextReader(reader);
		valueScript.setNextReader(reader);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		return new InternalFullHistogramFacet(facetName, comparatorType, entries, true);
	}

	
	/**
	 * Bucket.
	 *
	 * @param value the value
	 * @param interval the interval
	 * @return the long
	 */
	public static long bucket(double value, long interval) {
		return (((long) (value / interval)) * interval);
	}
}