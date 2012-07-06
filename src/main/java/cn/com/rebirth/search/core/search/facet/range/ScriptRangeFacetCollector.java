/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ScriptRangeFacetCollector.java 2012-3-29 15:02:36 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.range;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class ScriptRangeFacetCollector.
 *
 * @author l.xue.nong
 */
public class ScriptRangeFacetCollector extends AbstractFacetCollector {

	
	/** The key script. */
	private final SearchScript keyScript;

	
	/** The value script. */
	private final SearchScript valueScript;

	
	/** The entries. */
	private final RangeFacet.Entry[] entries;

	
	/**
	 * Instantiates a new script range facet collector.
	 *
	 * @param facetName the facet name
	 * @param scriptLang the script lang
	 * @param keyScript the key script
	 * @param valueScript the value script
	 * @param params the params
	 * @param entries the entries
	 * @param context the context
	 */
	public ScriptRangeFacetCollector(String facetName, String scriptLang, String keyScript, String valueScript,
			Map<String, Object> params, RangeFacet.Entry[] entries, SearchContext context) {
		super(facetName);
		this.keyScript = context.scriptService().search(context.lookup(), scriptLang, keyScript, params);
		this.valueScript = context.scriptService().search(context.lookup(), scriptLang, valueScript, params);
		this.entries = entries;
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
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		keyScript.setNextDocId(doc);
		valueScript.setNextDocId(doc);
		double key = keyScript.runAsDouble();
		double value = valueScript.runAsDouble();

		for (RangeFacet.Entry entry : entries) {
			if (key >= entry.getFrom() && key < entry.getTo()) {
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
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		return new InternalRangeFacet(facetName, entries);
	}
}
