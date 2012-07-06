/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ScriptTermsStringFieldFacetCollector.java 2012-3-29 15:00:58 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.terms.strings;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.commons.collect.BoundedTreeSet;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.terms.support.EntryPriorityQueue;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;


/**
 * The Class ScriptTermsStringFieldFacetCollector.
 *
 * @author l.xue.nong
 */
public class ScriptTermsStringFieldFacetCollector extends AbstractFacetCollector {

	
	/** The comparator type. */
	private final InternalStringTermsFacet.ComparatorType comparatorType;

	
	/** The size. */
	private final int size;

	
	/** The number of shards. */
	private final int numberOfShards;

	
	/** The script. */
	private final SearchScript script;

	
	/** The matcher. */
	private final Matcher matcher;

	
	/** The excluded. */
	private final ImmutableSet<String> excluded;

	
	/** The facets. */
	private final TObjectIntHashMap<String> facets;

	
	/** The missing. */
	private int missing;

	
	/** The total. */
	private int total;

	
	/**
	 * Instantiates a new script terms string field facet collector.
	 *
	 * @param facetName the facet name
	 * @param size the size
	 * @param comparatorType the comparator type
	 * @param context the context
	 * @param excluded the excluded
	 * @param pattern the pattern
	 * @param scriptLang the script lang
	 * @param script the script
	 * @param params the params
	 */
	public ScriptTermsStringFieldFacetCollector(String facetName, int size,
			InternalStringTermsFacet.ComparatorType comparatorType, SearchContext context,
			ImmutableSet<String> excluded, Pattern pattern, String scriptLang, String script, Map<String, Object> params) {
		super(facetName);
		this.size = size;
		this.comparatorType = comparatorType;
		this.numberOfShards = context.numberOfShards();
		this.script = context.scriptService().search(context.lookup(), scriptLang, script, params);

		this.excluded = excluded;
		this.matcher = pattern != null ? pattern.matcher("") : null;

		this.facets = CacheRecycler.popObjectIntMap();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#setScorer(org.apache.lucene.search.Scorer)
	 */
	@Override
	public void setScorer(Scorer scorer) throws IOException {
		script.setScorer(scorer);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		script.setNextReader(reader);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		script.setNextDocId(doc);
		Object o = script.run();
		if (o == null) {
			missing++;
			return;
		}
		if (o instanceof Iterable) {
			boolean found = false;
			for (Object o1 : ((Iterable) o)) {
				String value = o1.toString();
				if (match(value)) {
					found = true;
					facets.adjustOrPutValue(value, 1, 1);
					total++;
				}
			}
			if (!found) {
				missing++;
			}
		} else if (o instanceof Object[]) {
			boolean found = false;
			for (Object o1 : ((Object[]) o)) {
				String value = o1.toString();
				if (match(value)) {
					found = true;
					facets.adjustOrPutValue(value, 1, 1);
					total++;
				}
			}
			if (!found) {
				missing++;
			}
		} else {
			String value = o.toString();
			if (match(value)) {
				facets.adjustOrPutValue(value, 1, 1);
				total++;
			} else {
				missing++;
			}
		}
	}

	
	/**
	 * Match.
	 *
	 * @param value the value
	 * @return true, if successful
	 */
	private boolean match(String value) {
		if (excluded != null && excluded.contains(value)) {
			return false;
		}
		if (matcher != null && !matcher.reset(value).matches()) {
			return false;
		}
		return true;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		if (facets.isEmpty()) {
			CacheRecycler.pushObjectIntMap(facets);
			return new InternalStringTermsFacet(facetName, comparatorType, size,
					ImmutableList.<InternalStringTermsFacet.StringEntry> of(), missing, total);
		} else {
			if (size < EntryPriorityQueue.LIMIT) {
				EntryPriorityQueue ordered = new EntryPriorityQueue(size, comparatorType.comparator());
				for (TObjectIntIterator<String> it = facets.iterator(); it.hasNext();) {
					it.advance();
					ordered.insertWithOverflow(new InternalStringTermsFacet.StringEntry(it.key(), it.value()));
				}
				InternalStringTermsFacet.StringEntry[] list = new InternalStringTermsFacet.StringEntry[ordered.size()];
				for (int i = ordered.size() - 1; i >= 0; i--) {
					list[i] = ((InternalStringTermsFacet.StringEntry) ordered.pop());
				}
				CacheRecycler.pushObjectIntMap(facets);
				return new InternalStringTermsFacet(facetName, comparatorType, size, Arrays.asList(list), missing,
						total);
			} else {
				BoundedTreeSet<InternalStringTermsFacet.StringEntry> ordered = new BoundedTreeSet<InternalStringTermsFacet.StringEntry>(
						comparatorType.comparator(), size);
				for (TObjectIntIterator<String> it = facets.iterator(); it.hasNext();) {
					it.advance();
					ordered.add(new InternalStringTermsFacet.StringEntry(it.key(), it.value()));
				}
				CacheRecycler.pushObjectIntMap(facets);
				return new InternalStringTermsFacet(facetName, comparatorType, size, ordered, missing, total);
			}
		}
	}
}
