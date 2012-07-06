/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FacetProcessors.java 2012-3-29 15:01:12 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet;

import java.util.Set;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.search.commons.inject.Inject;

import com.google.common.collect.ImmutableMap;


/**
 * The Class FacetProcessors.
 *
 * @author l.xue.nong
 */
public class FacetProcessors {

	
	/** The processors. */
	private final ImmutableMap<String, FacetProcessor> processors;

	
	/**
	 * Instantiates a new facet processors.
	 *
	 * @param processors the processors
	 */
	@Inject
	public FacetProcessors(Set<FacetProcessor> processors) {
		MapBuilder<String, FacetProcessor> builder = MapBuilder.newMapBuilder();
		for (FacetProcessor processor : processors) {
			for (String type : processor.types()) {
				builder.put(type, processor);
			}
		}
		this.processors = builder.immutableMap();
	}

	
	/**
	 * Processor.
	 *
	 * @param type the type
	 * @return the facet processor
	 */
	public FacetProcessor processor(String type) {
		return processors.get(type);
	}
}
