/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ScopePhase.java 2012-7-6 14:30:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.internal;

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;

/**
 * The Interface ScopePhase.
 *
 * @author l.xue.nong
 */
public interface ScopePhase {

	/**
	 * Scope.
	 *
	 * @return the string
	 */
	String scope();

	/**
	 * Clear.
	 */
	void clear();

	/**
	 * Query.
	 *
	 * @return the query
	 */
	Query query();

	/**
	 * The Interface TopDocsPhase.
	 *
	 * @author l.xue.nong
	 */
	public interface TopDocsPhase extends ScopePhase {

		/**
		 * Process results.
		 *
		 * @param topDocs the top docs
		 * @param context the context
		 */
		void processResults(TopDocs topDocs, SearchContext context);

		/**
		 * Num hits.
		 *
		 * @return the int
		 */
		int numHits();

		/**
		 * Factor.
		 *
		 * @return the int
		 */
		int factor();

		/**
		 * Incremental factor.
		 *
		 * @return the int
		 */
		int incrementalFactor();
	}

	/**
	 * The Interface CollectorPhase.
	 *
	 * @author l.xue.nong
	 */
	public interface CollectorPhase extends ScopePhase {

		/**
		 * Requires processing.
		 *
		 * @return true, if successful
		 */
		boolean requiresProcessing();

		/**
		 * Collector.
		 *
		 * @return the collector
		 */
		Collector collector();

		/**
		 * Process collector.
		 *
		 * @param collector the collector
		 */
		void processCollector(Collector collector);
	}
}
