/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FacetModule.java 2012-3-29 15:01:21 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet;

import java.util.List;

import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.multibindings.Multibinder;
import cn.com.rebirth.search.core.search.facet.datehistogram.DateHistogramFacetProcessor;
import cn.com.rebirth.search.core.search.facet.filter.FilterFacetProcessor;
import cn.com.rebirth.search.core.search.facet.geodistance.GeoDistanceFacetProcessor;
import cn.com.rebirth.search.core.search.facet.histogram.HistogramFacetProcessor;
import cn.com.rebirth.search.core.search.facet.query.QueryFacetProcessor;
import cn.com.rebirth.search.core.search.facet.range.RangeFacetProcessor;
import cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacetProcessor;
import cn.com.rebirth.search.core.search.facet.terms.TermsFacetProcessor;
import cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacetProcessor;

import com.google.common.collect.Lists;

/**
 * The Class FacetModule.
 *
 * @author l.xue.nong
 */
public class FacetModule extends AbstractModule {

	/** The processors. */
	private List<Class<? extends FacetProcessor>> processors = Lists.newArrayList();

	/**
	 * Instantiates a new facet module.
	 */
	public FacetModule() {
		processors.add(FilterFacetProcessor.class);
		processors.add(QueryFacetProcessor.class);
		processors.add(GeoDistanceFacetProcessor.class);
		processors.add(HistogramFacetProcessor.class);
		processors.add(DateHistogramFacetProcessor.class);
		processors.add(RangeFacetProcessor.class);
		processors.add(StatisticalFacetProcessor.class);
		processors.add(TermsFacetProcessor.class);
		processors.add(TermsStatsFacetProcessor.class);
	}

	/**
	 * Adds the facet processor.
	 *
	 * @param facetProcessor the facet processor
	 */
	public void addFacetProcessor(Class<? extends FacetProcessor> facetProcessor) {
		processors.add(facetProcessor);
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		Multibinder<FacetProcessor> multibinder = Multibinder.newSetBinder(binder(), FacetProcessor.class);
		for (Class<? extends FacetProcessor> processor : processors) {
			multibinder.addBinding().to(processor);
		}
		bind(FacetProcessors.class).asEagerSingleton();
		bind(FacetParseElement.class).asEagerSingleton();
		bind(FacetPhase.class).asEagerSingleton();
	}
}
