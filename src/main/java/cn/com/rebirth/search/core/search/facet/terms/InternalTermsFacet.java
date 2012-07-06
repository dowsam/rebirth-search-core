/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InternalTermsFacet.java 2012-3-29 15:00:48 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.terms;

import java.util.List;

import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.InternalFacet;
import cn.com.rebirth.search.core.search.facet.terms.bytes.InternalByteTermsFacet;
import cn.com.rebirth.search.core.search.facet.terms.doubles.InternalDoubleTermsFacet;
import cn.com.rebirth.search.core.search.facet.terms.floats.InternalFloatTermsFacet;
import cn.com.rebirth.search.core.search.facet.terms.ints.InternalIntTermsFacet;
import cn.com.rebirth.search.core.search.facet.terms.ip.InternalIpTermsFacet;
import cn.com.rebirth.search.core.search.facet.terms.longs.InternalLongTermsFacet;
import cn.com.rebirth.search.core.search.facet.terms.shorts.InternalShortTermsFacet;
import cn.com.rebirth.search.core.search.facet.terms.strings.InternalStringTermsFacet;


/**
 * The Class InternalTermsFacet.
 *
 * @author l.xue.nong
 */
public abstract class InternalTermsFacet implements TermsFacet, InternalFacet {

	
	/**
	 * Register streams.
	 */
	public static void registerStreams() {
		InternalStringTermsFacet.registerStream();
		InternalLongTermsFacet.registerStream();
		InternalDoubleTermsFacet.registerStream();
		InternalIntTermsFacet.registerStream();
		InternalFloatTermsFacet.registerStream();
		InternalShortTermsFacet.registerStream();
		InternalByteTermsFacet.registerStream();
		InternalIpTermsFacet.registerStream();
	}

	
	/**
	 * Reduce.
	 *
	 * @param name the name
	 * @param facets the facets
	 * @return the facet
	 */
	public abstract Facet reduce(String name, List<Facet> facets);
}
