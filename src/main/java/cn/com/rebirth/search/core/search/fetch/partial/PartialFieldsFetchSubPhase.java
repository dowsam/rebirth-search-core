/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PartialFieldsFetchSubPhase.java 2012-3-29 15:02:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.fetch.partial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.search.SearchHitField;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.fetch.FetchSubPhase;
import cn.com.rebirth.search.core.search.internal.InternalSearchHit;
import cn.com.rebirth.search.core.search.internal.InternalSearchHitField;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.ImmutableMap;


/**
 * The Class PartialFieldsFetchSubPhase.
 *
 * @author l.xue.nong
 */
public class PartialFieldsFetchSubPhase implements FetchSubPhase {

	
	/**
	 * Instantiates a new partial fields fetch sub phase.
	 */
	@Inject
	public PartialFieldsFetchSubPhase() {

	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.fetch.FetchSubPhase#parseElements()
	 */
	@Override
	public Map<String, ? extends SearchParseElement> parseElements() {
		ImmutableMap.Builder<String, SearchParseElement> parseElements = ImmutableMap.builder();
		parseElements.put("partial_fields", new PartialFieldsParseElement()).put("partialFields",
				new PartialFieldsParseElement());
		return parseElements.build();
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
		return context.hasPartialFields();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.fetch.FetchSubPhase#hitExecute(cn.com.summall.search.core.search.internal.SearchContext, cn.com.summall.search.core.search.fetch.FetchSubPhase.HitContext)
	 */
	@Override
	public void hitExecute(SearchContext context, HitContext hitContext) throws RestartException {
		for (PartialFieldsContext.PartialField field : context.partialFields().fields()) {
			Object value = context.lookup().source().filter(field.includes(), field.excludes());

			if (hitContext.hit().fieldsOrNull() == null) {
				hitContext.hit().fields(new HashMap<String, SearchHitField>(2));
			}

			SearchHitField hitField = hitContext.hit().fields().get(field.name());
			if (hitField == null) {
				hitField = new InternalSearchHitField(field.name(), new ArrayList<Object>(2));
				hitContext.hit().fields().put(field.name(), hitField);
			}
			hitField.values().add(value);
		}
	}
}
