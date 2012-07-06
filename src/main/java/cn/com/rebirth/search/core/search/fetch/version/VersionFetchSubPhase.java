/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core VersionFetchSubPhase.java 2012-3-29 15:01:51 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.fetch.version;

import java.util.Map;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.search.commons.lucene.uid.UidField;
import cn.com.rebirth.search.core.index.mapper.internal.UidFieldMapper;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.fetch.FetchSubPhase;
import cn.com.rebirth.search.core.search.internal.InternalSearchHit;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.ImmutableMap;


/**
 * The Class VersionFetchSubPhase.
 *
 * @author l.xue.nong
 */
public class VersionFetchSubPhase implements FetchSubPhase {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.fetch.FetchSubPhase#parseElements()
	 */
	@Override
	public Map<String, ? extends SearchParseElement> parseElements() {
		return ImmutableMap.of("version", new VersionParseElement());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.fetch.FetchSubPhase#hitsExecutionNeeded(cn.com.summall.search.core.search.internal.SearchContext)
	 */
	@Override
	public boolean hitsExecutionNeeded(SearchContext context) {
		return false;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.fetch.FetchSubPhase#hitsExecute(cn.com.summall.search.core.search.internal.SearchContext, cn.com.summall.search.core.search.internal.InternalSearchHit[])
	 */
	@Override
	public void hitsExecute(SearchContext context, InternalSearchHit[] hits) throws RestartException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.fetch.FetchSubPhase#hitExecutionNeeded(cn.com.summall.search.core.search.internal.SearchContext)
	 */
	@Override
	public boolean hitExecutionNeeded(SearchContext context) {
		return context.version();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.fetch.FetchSubPhase#hitExecute(cn.com.summall.search.core.search.internal.SearchContext, cn.com.summall.search.core.search.fetch.FetchSubPhase.HitContext)
	 */
	@Override
	public void hitExecute(SearchContext context, HitContext hitContext) throws RestartException {
		
		
		
		long version = UidField.loadVersion(hitContext.reader(),
				UidFieldMapper.TERM_FACTORY.createTerm(hitContext.doc().get(UidFieldMapper.NAME)));
		if (version < 0) {
			version = -1;
		}
		hitContext.hit().version(version);
	}
}
