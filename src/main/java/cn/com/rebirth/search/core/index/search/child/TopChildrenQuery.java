/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TopChildrenQuery.java 2012-3-29 15:01:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.search.child;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.ToStringUtils;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.exception.RestartIllegalStateException;
import cn.com.rebirth.search.commons.BytesWrap;
import cn.com.rebirth.search.commons.lucene.search.EmptyScorer;
import cn.com.rebirth.search.core.search.internal.ScopePhase;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class TopChildrenQuery.
 *
 * @author l.xue.nong
 */
@SuppressWarnings("deprecation")
public class TopChildrenQuery extends Query implements ScopePhase.TopDocsPhase {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5873329809716453746L;

	/**
	 * The Enum ScoreType.
	 *
	 * @author l.xue.nong
	 */
	public static enum ScoreType {

		/** The MAX. */
		MAX,

		/** The AVG. */
		AVG,

		/** The SUM. */
		SUM;

		/**
		 * From string.
		 *
		 * @param type the type
		 * @return the score type
		 */
		public static ScoreType fromString(String type) {
			if ("max".equals(type)) {
				return MAX;
			} else if ("avg".equals(type)) {
				return AVG;
			} else if ("sum".equals(type)) {
				return SUM;
			}
			throw new RestartIllegalArgumentException("No score type for child query [" + type + "] found");
		}
	}

	/** The query. */
	private Query query;

	/** The scope. */
	private String scope;

	/** The parent type. */
	private String parentType;

	/** The child type. */
	private String childType;

	/** The score type. */
	private ScoreType scoreType;

	/** The factor. */
	private int factor;

	/** The incremental factor. */
	private int incrementalFactor;

	/** The parent docs. */
	private Map<Object, ParentDoc[]> parentDocs;

	/** The num hits. */
	private int numHits = 0;

	/**
	 * Instantiates a new top children query.
	 *
	 * @param query the query
	 * @param scope the scope
	 * @param childType the child type
	 * @param parentType the parent type
	 * @param scoreType the score type
	 * @param factor the factor
	 * @param incrementalFactor the incremental factor
	 */
	public TopChildrenQuery(Query query, String scope, String childType, String parentType, ScoreType scoreType,
			int factor, int incrementalFactor) {
		this.query = query;
		this.scope = scope;
		this.childType = childType;
		this.parentType = parentType;
		this.scoreType = scoreType;
		this.factor = factor;
		this.incrementalFactor = incrementalFactor;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.internal.ScopePhase#query()
	 */
	@Override
	public Query query() {
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.internal.ScopePhase#scope()
	 */
	@Override
	public String scope() {
		return scope;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.internal.ScopePhase#clear()
	 */
	@Override
	public void clear() {
		parentDocs = null;
		numHits = 0;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.internal.ScopePhase.TopDocsPhase#numHits()
	 */
	@Override
	public int numHits() {
		return numHits;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.internal.ScopePhase.TopDocsPhase#factor()
	 */
	@Override
	public int factor() {
		return this.factor;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.internal.ScopePhase.TopDocsPhase#incrementalFactor()
	 */
	@Override
	public int incrementalFactor() {
		return this.incrementalFactor;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.internal.ScopePhase.TopDocsPhase#processResults(org.apache.lucene.search.TopDocs, cn.com.summall.search.core.search.internal.SearchContext)
	 */
	@Override
	public void processResults(TopDocs topDocs, SearchContext context) {
		Map<Object, TIntObjectHashMap<ParentDoc>> parentDocsPerReader = new HashMap<Object, TIntObjectHashMap<ParentDoc>>();
		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			int readerIndex = context.searcher().readerIndex(scoreDoc.doc);
			IndexReader subReader = context.searcher().subReaders()[readerIndex];
			int subDoc = scoreDoc.doc - context.searcher().docStarts()[readerIndex];

			BytesWrap parentId = context.idCache().reader(subReader).parentIdByDoc(parentType, subDoc);
			if (parentId == null) {

				continue;
			}

			for (IndexReader indexReader : context.searcher().subReaders()) {
				int parentDocId = context.idCache().reader(indexReader).docById(parentType, parentId);
				if (parentDocId != -1 && !indexReader.isDeleted(parentDocId)) {

					TIntObjectHashMap<ParentDoc> readerParentDocs = parentDocsPerReader.get(indexReader
							.getCoreCacheKey());
					if (readerParentDocs == null) {
						readerParentDocs = new TIntObjectHashMap<ParentDoc>();
						parentDocsPerReader.put(indexReader.getCoreCacheKey(), readerParentDocs);
					}

					ParentDoc parentDoc = readerParentDocs.get(parentDocId);
					if (parentDoc == null) {
						numHits++;
						parentDoc = new ParentDoc();
						parentDoc.docId = parentDocId;
						parentDoc.count = 1;
						parentDoc.maxScore = scoreDoc.score;
						parentDoc.sumScores = scoreDoc.score;
						readerParentDocs.put(parentDocId, parentDoc);
					} else {
						parentDoc.count++;
						parentDoc.sumScores += scoreDoc.score;
						if (scoreDoc.score > parentDoc.maxScore) {
							parentDoc.maxScore = scoreDoc.score;
						}
					}
				}
			}
		}

		this.parentDocs = new HashMap<Object, ParentDoc[]>();
		for (Map.Entry<Object, TIntObjectHashMap<ParentDoc>> entry : parentDocsPerReader.entrySet()) {
			ParentDoc[] values = entry.getValue().values(new ParentDoc[entry.getValue().size()]);
			Arrays.sort(values, PARENT_DOC_COMP);
			parentDocs.put(entry.getKey(), values);
		}
	}

	/** The Constant PARENT_DOC_COMP. */
	private static final ParentDocComparator PARENT_DOC_COMP = new ParentDocComparator();

	/**
	 * The Class ParentDocComparator.
	 *
	 * @author l.xue.nong
	 */
	static class ParentDocComparator implements Comparator<ParentDoc> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(ParentDoc o1, ParentDoc o2) {
			return o1.docId - o2.docId;
		}
	}

	/**
	 * The Class ParentDoc.
	 *
	 * @author l.xue.nong
	 */
	public static class ParentDoc {

		/** The doc id. */
		public int docId;

		/** The count. */
		public int count;

		/** The max score. */
		public float maxScore = Float.NaN;

		/** The sum scores. */
		public float sumScores = 0;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Query#rewrite(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public Query rewrite(IndexReader reader) throws IOException {
		Query newQ = query.rewrite(reader);
		if (newQ == query)
			return this;
		TopChildrenQuery bq = (TopChildrenQuery) this.clone();
		bq.query = newQ;
		return bq;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Query#extractTerms(java.util.Set)
	 */
	@Override
	public void extractTerms(Set<Term> terms) {
		query.extractTerms(terms);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Query#createWeight(org.apache.lucene.search.Searcher)
	 */
	@Override
	public Weight createWeight(Searcher searcher) throws IOException {
		if (parentDocs != null) {
			return new ParentWeight(searcher, query.weight(searcher));
		}
		return query.weight(searcher);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Query#toString(java.lang.String)
	 */
	public String toString(String field) {
		StringBuilder sb = new StringBuilder();
		sb.append("score_child[").append(childType).append("/").append(parentType).append("](")
				.append(query.toString(field)).append(')');
		sb.append(ToStringUtils.boost(getBoost()));
		return sb.toString();
	}

	/**
	 * The Class ParentWeight.
	 *
	 * @author l.xue.nong
	 */
	class ParentWeight extends Weight {

		/** The searcher. */
		final Searcher searcher;

		/** The query weight. */
		final Weight queryWeight;

		/**
		 * Instantiates a new parent weight.
		 *
		 * @param searcher the searcher
		 * @param queryWeight the query weight
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public ParentWeight(Searcher searcher, Weight queryWeight) throws IOException {
			this.searcher = searcher;
			this.queryWeight = queryWeight;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Weight#getQuery()
		 */
		public Query getQuery() {
			return TopChildrenQuery.this;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Weight#getValue()
		 */
		public float getValue() {
			return getBoost();
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Weight#sumOfSquaredWeights()
		 */
		@Override
		public float sumOfSquaredWeights() throws IOException {
			float sum = queryWeight.sumOfSquaredWeights();
			sum *= getBoost() * getBoost();
			return sum;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Weight#normalize(float)
		 */
		@Override
		public void normalize(float norm) {

		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Weight#scorer(org.apache.lucene.index.IndexReader, boolean, boolean)
		 */
		@Override
		public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder, boolean topScorer) throws IOException {
			ParentDoc[] readerParentDocs = parentDocs.get(reader.getCoreCacheKey());
			if (readerParentDocs != null) {
				return new ParentScorer(getSimilarity(searcher), readerParentDocs);
			}
			return new EmptyScorer(getSimilarity(searcher));
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Weight#explain(org.apache.lucene.index.IndexReader, int)
		 */
		@Override
		public Explanation explain(IndexReader reader, int doc) throws IOException {
			return new Explanation(getBoost(), "not implemented yet...");
		}
	}

	/**
	 * The Class ParentScorer.
	 *
	 * @author l.xue.nong
	 */
	class ParentScorer extends Scorer {

		/** The docs. */
		private final ParentDoc[] docs;

		/** The index. */
		private int index = -1;

		/**
		 * Instantiates a new parent scorer.
		 *
		 * @param similarity the similarity
		 * @param docs the docs
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		private ParentScorer(Similarity similarity, ParentDoc[] docs) throws IOException {
			super(similarity);
			this.docs = docs;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.DocIdSetIterator#docID()
		 */
		@Override
		public int docID() {
			if (index >= docs.length) {
				return NO_MORE_DOCS;
			}
			return docs[index].docId;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.DocIdSetIterator#advance(int)
		 */
		@Override
		public int advance(int target) throws IOException {
			int doc;
			while ((doc = nextDoc()) < target) {
			}
			return doc;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.DocIdSetIterator#nextDoc()
		 */
		@Override
		public int nextDoc() throws IOException {
			if (++index >= docs.length) {
				return NO_MORE_DOCS;
			}
			return docs[index].docId;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Scorer#score()
		 */
		@Override
		public float score() throws IOException {
			if (scoreType == ScoreType.MAX) {
				return docs[index].maxScore;
			} else if (scoreType == ScoreType.AVG) {
				return docs[index].sumScores / docs[index].count;
			} else if (scoreType == ScoreType.SUM) {
				return docs[index].sumScores;
			}
			throw new RestartIllegalStateException("No support for score type [" + scoreType + "]");
		}
	}
}
