/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MatchedFiltersFetchSubPhase.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.fetch.matchedfilters;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.search.commons.lucene.docset.DocSet;
import cn.com.rebirth.search.commons.lucene.docset.DocSets;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.fetch.FetchSubPhase;
import cn.com.rebirth.search.core.search.internal.InternalSearchHit;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * The Class MatchedFiltersFetchSubPhase.
 *
 * @author l.xue.nong
 */
public class MatchedFiltersFetchSubPhase implements FetchSubPhase {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.fetch.FetchSubPhase#parseElements()
	 */
	@Override
	public Map<String, ? extends SearchParseElement> parseElements() {
		return ImmutableMap.of();
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
		return !context.parsedQuery().namedFilters().isEmpty();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.fetch.FetchSubPhase#hitExecute(cn.com.rebirth.search.core.search.internal.SearchContext, cn.com.rebirth.search.core.search.fetch.FetchSubPhase.HitContext)
	 */
	@Override
	public void hitExecute(SearchContext context, HitContext hitContext) throws RebirthException {
		List<String> matchedFilters = Lists.newArrayListWithCapacity(2);
		for (Map.Entry<String, Filter> entry : context.parsedQuery().namedFilters().entrySet()) {
			String name = entry.getKey();
			Filter filter = entry.getValue();
			try {
				DocIdSet docIdSet = filter.getDocIdSet(hitContext.reader());
				if (docIdSet != null) {
					DocSet docSet = DocSets.convert(hitContext.reader(), docIdSet);
					if (docSet.get(hitContext.docId())) {
						matchedFilters.add(name);
					}
				}
			} catch (IOException e) {

			}
		}
		hitContext.hit().matchedFilters(matchedFilters.toArray(new String[matchedFilters.size()]));
	}
}
