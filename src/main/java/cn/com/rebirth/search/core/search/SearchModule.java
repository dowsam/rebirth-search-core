/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SearchModule.java 2012-3-29 15:00:49 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.Module;
import cn.com.rebirth.search.commons.inject.SpawnModules;
import cn.com.rebirth.search.core.search.action.SearchServiceTransportAction;
import cn.com.rebirth.search.core.search.controller.SearchPhaseController;
import cn.com.rebirth.search.core.search.dfs.DfsPhase;
import cn.com.rebirth.search.core.search.facet.FacetModule;
import cn.com.rebirth.search.core.search.fetch.FetchPhase;
import cn.com.rebirth.search.core.search.fetch.explain.ExplainFetchSubPhase;
import cn.com.rebirth.search.core.search.fetch.matchedfilters.MatchedFiltersFetchSubPhase;
import cn.com.rebirth.search.core.search.fetch.partial.PartialFieldsFetchSubPhase;
import cn.com.rebirth.search.core.search.fetch.script.ScriptFieldsFetchSubPhase;
import cn.com.rebirth.search.core.search.fetch.version.VersionFetchSubPhase;
import cn.com.rebirth.search.core.search.highlight.HighlightPhase;
import cn.com.rebirth.search.core.search.query.QueryPhase;

import com.google.common.collect.ImmutableList;

/**
 * The Class SearchModule.
 *
 * @author l.xue.nong
 */
public class SearchModule extends AbstractModule implements SpawnModules {

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.SpawnModules#spawnModules()
	 */
	@Override
	public Iterable<? extends Module> spawnModules() {
		return ImmutableList.of(new TransportSearchModule(), new FacetModule());
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(DfsPhase.class).asEagerSingleton();
		bind(QueryPhase.class).asEagerSingleton();
		bind(SearchService.class).asEagerSingleton();
		bind(SearchPhaseController.class).asEagerSingleton();

		bind(FetchPhase.class).asEagerSingleton();
		bind(ExplainFetchSubPhase.class).asEagerSingleton();
		bind(ScriptFieldsFetchSubPhase.class).asEagerSingleton();
		bind(PartialFieldsFetchSubPhase.class).asEagerSingleton();
		bind(VersionFetchSubPhase.class).asEagerSingleton();
		bind(MatchedFiltersFetchSubPhase.class).asEagerSingleton();
		bind(HighlightPhase.class).asEagerSingleton();

		bind(SearchServiceTransportAction.class).asEagerSingleton();
	}
}
