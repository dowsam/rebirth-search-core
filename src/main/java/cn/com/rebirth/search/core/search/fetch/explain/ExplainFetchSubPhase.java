/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ExplainFetchSubPhase.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.fetch.explain;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.fetch.FetchPhaseExecutionException;
import cn.com.rebirth.search.core.search.fetch.FetchSubPhase;
import cn.com.rebirth.search.core.search.internal.InternalSearchHit;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.ImmutableMap;

/**
 * The Class ExplainFetchSubPhase.
 *
 * @author l.xue.nong
 */
public class ExplainFetchSubPhase implements FetchSubPhase {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.fetch.FetchSubPhase#parseElements()
	 */
	@Override
	public Map<String, ? extends SearchParseElement> parseElements() {
		return ImmutableMap.of("explain", new ExplainParseElement());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.fetch.FetchSubPhase#hitsExecutionNeeded(cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public boolean hitsExecutionNeeded(SearchContext context) {
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.fetch.FetchSubPhase#hitsExecute(cn.com.rebirth.search.core.search.internal.SearchContext, cn.com.rebirth.search.core.search.internal.InternalSearchHit[])
	 */
	@Override
	public void hitsExecute(SearchContext context, InternalSearchHit[] hits) throws RebirthException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.fetch.FetchSubPhase#hitExecutionNeeded(cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public boolean hitExecutionNeeded(SearchContext context) {
		return context.explain();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.fetch.FetchSubPhase#hitExecute(cn.com.rebirth.search.core.search.internal.SearchContext, cn.com.rebirth.search.core.search.fetch.FetchSubPhase.HitContext)
	 */
	@Override
	public void hitExecute(SearchContext context, HitContext hitContext) throws RebirthException {
		try {

			hitContext.hit().explanation(context.searcher().explain(context.query(), hitContext.hit().docId()));
		} catch (IOException e) {
			throw new FetchPhaseExecutionException(context, "Failed to explain doc [" + hitContext.hit().type() + "#"
					+ hitContext.hit().id() + "]", e);
		}
	}
}
