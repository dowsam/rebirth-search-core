/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ScanContext.java 2012-3-29 15:02:05 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.scan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocs;

import cn.com.rebirth.search.commons.lucene.docset.AllDocSet;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.Maps;


/**
 * The Class ScanContext.
 *
 * @author l.xue.nong
 */
public class ScanContext {

	
	/** The reader states. */
	private final Map<IndexReader, ReaderState> readerStates = Maps.newHashMap();

	
	/**
	 * Clear.
	 */
	public void clear() {
		readerStates.clear();
	}

	
	/**
	 * Execute.
	 *
	 * @param context the context
	 * @return the top docs
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public TopDocs execute(SearchContext context) throws IOException {
		ScanCollector collector = new ScanCollector(readerStates, context.from(), context.size(), context.trackScores());
		Query query = new FilteredQuery(context.query(), new ScanFilter(readerStates, collector));
		try {
			context.searcher().search(query, collector);
		} catch (ScanCollector.StopCollectingException e) {
			
		}
		return collector.topDocs();
	}

	
	/**
	 * The Class ScanCollector.
	 *
	 * @author l.xue.nong
	 */
	static class ScanCollector extends Collector {

		
		/** The reader states. */
		private final Map<IndexReader, ReaderState> readerStates;

		
		/** The from. */
		private final int from;

		
		/** The to. */
		private final int to;

		
		/** The docs. */
		private final ArrayList<ScoreDoc> docs;

		
		/** The track scores. */
		private final boolean trackScores;

		
		/** The scorer. */
		private Scorer scorer;

		
		/** The doc base. */
		private int docBase;

		
		/** The counter. */
		private int counter;

		
		/** The current reader. */
		private IndexReader currentReader;

		
		/** The reader state. */
		private ReaderState readerState;

		
		/**
		 * Instantiates a new scan collector.
		 *
		 * @param readerStates the reader states
		 * @param from the from
		 * @param size the size
		 * @param trackScores the track scores
		 */
		ScanCollector(Map<IndexReader, ReaderState> readerStates, int from, int size, boolean trackScores) {
			this.readerStates = readerStates;
			this.from = from;
			this.to = from + size;
			this.trackScores = trackScores;
			this.docs = new ArrayList<ScoreDoc>(size);
		}

		
		/**
		 * Inc counter.
		 *
		 * @param count the count
		 */
		void incCounter(int count) {
			this.counter += count;
		}

		
		/**
		 * Top docs.
		 *
		 * @return the top docs
		 */
		public TopDocs topDocs() {
			return new TopDocs(docs.size(), docs.toArray(new ScoreDoc[docs.size()]), 0f);
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Collector#setScorer(org.apache.lucene.search.Scorer)
		 */
		@Override
		public void setScorer(Scorer scorer) throws IOException {
			this.scorer = scorer;
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Collector#collect(int)
		 */
		@Override
		public void collect(int doc) throws IOException {
			if (counter >= from) {
				docs.add(new ScoreDoc(docBase + doc, trackScores ? scorer.score() : 0f));
			}
			readerState.count++;
			counter++;
			if (counter >= to) {
				throw StopCollectingException;
			}
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Collector#setNextReader(org.apache.lucene.index.IndexReader, int)
		 */
		@Override
		public void setNextReader(IndexReader reader, int docBase) throws IOException {
			
			
			
			if (currentReader != null && !readerStates.containsKey(currentReader)) {
				assert readerState != null;
				readerState.done = true;
				readerStates.put(currentReader, readerState);
			}
			this.currentReader = reader;
			this.docBase = docBase;
			this.readerState = new ReaderState();
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Collector#acceptsDocsOutOfOrder()
		 */
		@Override
		public boolean acceptsDocsOutOfOrder() {
			return true;
		}

		
		/** The Constant StopCollectingException. */
		public static final RuntimeException StopCollectingException = new StopCollectingException();

		
		/**
		 * The Class StopCollectingException.
		 *
		 * @author l.xue.nong
		 */
		static class StopCollectingException extends RuntimeException {

			
			/* (non-Javadoc)
			 * @see java.lang.Throwable#fillInStackTrace()
			 */
			@Override
			public Throwable fillInStackTrace() {
				return null;
			}
		}
	}

	
	/**
	 * The Class ScanFilter.
	 *
	 * @author l.xue.nong
	 */
	public static class ScanFilter extends Filter {

		
		/** The reader states. */
		private final Map<IndexReader, ReaderState> readerStates;

		
		/** The scan collector. */
		private final ScanCollector scanCollector;

		
		/**
		 * Instantiates a new scan filter.
		 *
		 * @param readerStates the reader states
		 * @param scanCollector the scan collector
		 */
		public ScanFilter(Map<IndexReader, ReaderState> readerStates, ScanCollector scanCollector) {
			this.readerStates = readerStates;
			this.scanCollector = scanCollector;
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Filter#getDocIdSet(org.apache.lucene.index.IndexReader)
		 */
		@Override
		public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
			ReaderState readerState = readerStates.get(reader);
			if (readerState != null && readerState.done) {
				scanCollector.incCounter(readerState.count);
				return null;
			}
			return new AllDocSet(reader.maxDoc());
		}
	}

	
	/**
	 * The Class ReaderState.
	 *
	 * @author l.xue.nong
	 */
	static class ReaderState {

		
		/** The count. */
		public int count;

		
		/** The done. */
		public boolean done;
	}
}
