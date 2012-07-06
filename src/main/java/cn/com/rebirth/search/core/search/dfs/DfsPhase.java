/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DfsPhase.java 2012-7-6 14:30:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.dfs;

import gnu.trove.set.hash.THashSet;

import java.util.Map;

import org.apache.lucene.index.Term;

import cn.com.rebirth.commons.thread.ThreadLocals;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.SearchPhase;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.ImmutableMap;

/**
 * The Class DfsPhase.
 *
 * @author l.xue.nong
 */
public class DfsPhase implements SearchPhase {

	/** The cached terms set. */
	private static ThreadLocal<ThreadLocals.CleanableValue<THashSet<Term>>> cachedTermsSet = new ThreadLocal<ThreadLocals.CleanableValue<THashSet<Term>>>() {
		@Override
		protected ThreadLocals.CleanableValue<THashSet<Term>> initialValue() {
			return new ThreadLocals.CleanableValue<THashSet<Term>>(new THashSet<Term>());
		}
	};

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhase#parseElements()
	 */
	@Override
	public Map<String, ? extends SearchParseElement> parseElements() {
		return ImmutableMap.of();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhase#preProcess(cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public void preProcess(SearchContext context) {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhase#execute(cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	public void execute(SearchContext context) {
		try {
			if (!context.queryRewritten()) {
				context.updateRewriteQuery(context.searcher().rewrite(context.query()));
			}

			THashSet<Term> termsSet = cachedTermsSet.get().get();
			termsSet.clear();
			context.query().extractTerms(termsSet);
			Term[] terms = termsSet.toArray(new Term[termsSet.size()]);
			int[] freqs = context.searcher().docFreqs(terms);

			context.dfsResult().termsAndFreqs(terms, freqs);
			context.dfsResult().maxDoc(context.searcher().getIndexReader().maxDoc());
		} catch (Exception e) {
			throw new DfsPhaseExecutionException(context, "", e);
		}
	}
}
