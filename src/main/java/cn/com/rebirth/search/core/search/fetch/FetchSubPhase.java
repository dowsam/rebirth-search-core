/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FetchSubPhase.java 2012-7-6 14:29:55 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.fetch;

import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.internal.InternalSearchHit;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.Maps;

/**
 * The Interface FetchSubPhase.
 *
 * @author l.xue.nong
 */
public interface FetchSubPhase {

	/**
	 * The Class HitContext.
	 *
	 * @author l.xue.nong
	 */
	public static class HitContext {

		/** The hit. */
		private InternalSearchHit hit;

		/** The top level reader. */
		private IndexReader topLevelReader;

		/** The top level doc id. */
		private int topLevelDocId;

		/** The reader. */
		private IndexReader reader;

		/** The doc id. */
		private int docId;

		/** The doc. */
		private Document doc;

		/** The cache. */
		private Map<String, Object> cache;

		/**
		 * Reset.
		 *
		 * @param hit the hit
		 * @param reader the reader
		 * @param docId the doc id
		 * @param topLevelReader the top level reader
		 * @param topLevelDocId the top level doc id
		 * @param doc the doc
		 */
		public void reset(InternalSearchHit hit, IndexReader reader, int docId, IndexReader topLevelReader,
				int topLevelDocId, Document doc) {
			this.hit = hit;
			this.reader = reader;
			this.docId = docId;
			this.topLevelReader = topLevelReader;
			this.topLevelDocId = topLevelDocId;
			this.doc = doc;
		}

		/**
		 * Hit.
		 *
		 * @return the internal search hit
		 */
		public InternalSearchHit hit() {
			return hit;
		}

		/**
		 * Reader.
		 *
		 * @return the index reader
		 */
		public IndexReader reader() {
			return reader;
		}

		/**
		 * Doc id.
		 *
		 * @return the int
		 */
		public int docId() {
			return docId;
		}

		/**
		 * Top level reader.
		 *
		 * @return the index reader
		 */
		public IndexReader topLevelReader() {
			return topLevelReader;
		}

		/**
		 * Top level doc id.
		 *
		 * @return the int
		 */
		public int topLevelDocId() {
			return topLevelDocId;
		}

		/**
		 * Doc.
		 *
		 * @return the document
		 */
		public Document doc() {
			return doc;
		}

		/**
		 * Cache.
		 *
		 * @return the map
		 */
		public Map<String, Object> cache() {
			if (cache == null) {
				cache = Maps.newHashMap();
			}
			return cache;
		}
	}

	/**
	 * Parses the elements.
	 *
	 * @return the map< string,? extends search parse element>
	 */
	Map<String, ? extends SearchParseElement> parseElements();

	/**
	 * Hit execution needed.
	 *
	 * @param context the context
	 * @return true, if successful
	 */
	boolean hitExecutionNeeded(SearchContext context);

	/**
	 * Hit execute.
	 *
	 * @param context the context
	 * @param hitContext the hit context
	 * @throws RebirthException the rebirth exception
	 */
	void hitExecute(SearchContext context, HitContext hitContext) throws RebirthException;

	/**
	 * Hits execution needed.
	 *
	 * @param context the context
	 * @return true, if successful
	 */
	boolean hitsExecutionNeeded(SearchContext context);

	/**
	 * Hits execute.
	 *
	 * @param context the context
	 * @param hits the hits
	 * @throws RebirthException the rebirth exception
	 */
	void hitsExecute(SearchContext context, InternalSearchHit[] hits) throws RebirthException;
}
