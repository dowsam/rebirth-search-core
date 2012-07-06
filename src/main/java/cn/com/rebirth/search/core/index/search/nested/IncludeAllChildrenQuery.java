/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IncludeAllChildrenQuery.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.search.nested;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.FixedBitSet;

import cn.com.rebirth.search.commons.lucene.docset.FixedBitDocSet;

/**
 * The Class IncludeAllChildrenQuery.
 *
 * @author l.xue.nong
 */
public class IncludeAllChildrenQuery extends Query {

	/** The parent filter. */
	private final Filter parentFilter;

	/** The parent query. */
	private final Query parentQuery;

	/** The orig parent query. */
	private final Query origParentQuery;

	/**
	 * Instantiates a new include all children query.
	 *
	 * @param parentQuery the parent query
	 * @param parentFilter the parent filter
	 */
	public IncludeAllChildrenQuery(Query parentQuery, Filter parentFilter) {
		this.origParentQuery = parentQuery;
		this.parentQuery = parentQuery;
		this.parentFilter = parentFilter;
	}

	/**
	 * Instantiates a new include all children query.
	 *
	 * @param origParentQuery the orig parent query
	 * @param parentQuery the parent query
	 * @param parentFilter the parent filter
	 */
	IncludeAllChildrenQuery(Query origParentQuery, Query parentQuery, Filter parentFilter) {
		this.origParentQuery = origParentQuery;
		this.parentQuery = parentQuery;
		this.parentFilter = parentFilter;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Query#createWeight(org.apache.lucene.search.Searcher)
	 */
	@Override
	public Weight createWeight(Searcher searcher) throws IOException {
		return new IncludeAllChildrenWeight(parentQuery, parentQuery.createWeight(searcher), parentFilter);
	}

	/**
	 * The Class IncludeAllChildrenWeight.
	 *
	 * @author l.xue.nong
	 */
	static class IncludeAllChildrenWeight extends Weight {

		/** The parent query. */
		private final Query parentQuery;

		/** The parent weight. */
		private final Weight parentWeight;

		/** The parents filter. */
		private final Filter parentsFilter;

		/**
		 * Instantiates a new include all children weight.
		 *
		 * @param parentQuery the parent query
		 * @param parentWeight the parent weight
		 * @param parentsFilter the parents filter
		 */
		IncludeAllChildrenWeight(Query parentQuery, Weight parentWeight, Filter parentsFilter) {
			this.parentQuery = parentQuery;
			this.parentWeight = parentWeight;
			this.parentsFilter = parentsFilter;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Weight#getQuery()
		 */
		@Override
		public Query getQuery() {
			return parentQuery;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Weight#getValue()
		 */
		@Override
		public float getValue() {
			return parentWeight.getValue();
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Weight#sumOfSquaredWeights()
		 */
		@Override
		public float sumOfSquaredWeights() throws IOException {
			return parentWeight.sumOfSquaredWeights() * parentQuery.getBoost() * parentQuery.getBoost();
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Weight#normalize(float)
		 */
		@Override
		public void normalize(float norm) {
			parentWeight.normalize(norm * parentQuery.getBoost());
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Weight#scorer(org.apache.lucene.index.IndexReader, boolean, boolean)
		 */
		@Override
		public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder, boolean topScorer) throws IOException {
			final Scorer parentScorer = parentWeight.scorer(reader, true, false);

			if (parentScorer == null) {
				return null;
			}

			final int firstParentDoc = parentScorer.nextDoc();
			if (firstParentDoc == DocIdSetIterator.NO_MORE_DOCS) {

				return null;
			}

			DocIdSet parents = parentsFilter.getDocIdSet(reader);
			if (parents == null) {

				return null;
			}
			if (parents instanceof FixedBitDocSet) {
				parents = ((FixedBitDocSet) parents).set();
			}
			if (!(parents instanceof FixedBitSet)) {
				throw new IllegalStateException("parentFilter must return OpenBitSet; got " + parents);
			}

			return new IncludeAllChildrenScorer(this, parentScorer, (FixedBitSet) parents, firstParentDoc);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Weight#explain(org.apache.lucene.index.IndexReader, int)
		 */
		@Override
		public Explanation explain(IndexReader reader, int doc) throws IOException {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Weight#scoresDocsOutOfOrder()
		 */
		@Override
		public boolean scoresDocsOutOfOrder() {
			return false;
		}
	}

	/**
	 * The Class IncludeAllChildrenScorer.
	 *
	 * @author l.xue.nong
	 */
	static class IncludeAllChildrenScorer extends Scorer {

		/** The parent scorer. */
		private final Scorer parentScorer;

		/** The parent bits. */
		private final FixedBitSet parentBits;

		/** The current child pointer. */
		private int currentChildPointer = -1;

		/** The current parent pointer. */
		private int currentParentPointer = -1;

		/** The current doc. */
		private int currentDoc = -1;

		/**
		 * Instantiates a new include all children scorer.
		 *
		 * @param weight the weight
		 * @param parentScorer the parent scorer
		 * @param parentBits the parent bits
		 * @param currentParentPointer the current parent pointer
		 */
		IncludeAllChildrenScorer(Weight weight, Scorer parentScorer, FixedBitSet parentBits, int currentParentPointer) {
			super(weight);
			this.parentScorer = parentScorer;
			this.parentBits = parentBits;
			this.currentParentPointer = currentParentPointer;
			if (currentParentPointer == 0) {
				currentChildPointer = 0;
			} else {
				this.currentChildPointer = parentBits.prevSetBit(currentParentPointer - 1);
				if (currentChildPointer == -1) {

					currentChildPointer = 0;
				} else {
					currentChildPointer++;
				}
			}

			currentDoc = currentChildPointer;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Scorer#visitSubScorers(org.apache.lucene.search.Query, org.apache.lucene.search.BooleanClause.Occur, org.apache.lucene.search.Scorer.ScorerVisitor)
		 */
		@Override
		protected void visitSubScorers(Query parent, BooleanClause.Occur relationship,
				ScorerVisitor<Query, Query, Scorer> visitor) {
			super.visitSubScorers(parent, relationship, visitor);
			parentScorer.visitScorers(visitor);
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.DocIdSetIterator#nextDoc()
		 */
		@Override
		public int nextDoc() throws IOException {
			if (currentParentPointer == NO_MORE_DOCS) {
				return (currentDoc = NO_MORE_DOCS);
			}

			if (currentChildPointer == currentParentPointer) {

				currentDoc = currentParentPointer;
				currentParentPointer = parentScorer.nextDoc();
				if (currentParentPointer != NO_MORE_DOCS) {
					currentChildPointer = parentBits.prevSetBit(currentParentPointer - 1);
					if (currentChildPointer == -1) {

						currentChildPointer = currentParentPointer;
					} else {
						currentChildPointer++;
					}
				}
			} else {
				currentDoc = currentChildPointer++;
			}

			assert currentDoc != -1;
			return currentDoc;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.DocIdSetIterator#advance(int)
		 */
		@Override
		public int advance(int target) throws IOException {
			if (target == NO_MORE_DOCS) {
				return (currentDoc = NO_MORE_DOCS);
			}

			if (target == 0) {
				return nextDoc();
			}

			currentParentPointer = parentScorer.advance(target);
			if (currentParentPointer == NO_MORE_DOCS) {
				return (currentDoc = NO_MORE_DOCS);
			}
			if (currentParentPointer == 0) {
				currentChildPointer = 0;
			} else {
				currentChildPointer = parentBits.prevSetBit(currentParentPointer - 1);
				if (currentChildPointer == -1) {

					currentChildPointer = 0;
				} else {
					currentChildPointer++;
				}
			}

			currentDoc = currentChildPointer;

			return currentDoc;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Scorer#score()
		 */
		@Override
		public float score() throws IOException {
			return parentScorer.score();
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.DocIdSetIterator#docID()
		 */
		@Override
		public int docID() {
			return currentDoc;
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Query#extractTerms(java.util.Set)
	 */
	@Override
	public void extractTerms(Set<Term> terms) {
		parentQuery.extractTerms(terms);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Query#rewrite(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public Query rewrite(IndexReader reader) throws IOException {
		final Query parentRewrite = parentQuery.rewrite(reader);
		if (parentRewrite != parentQuery) {
			Query rewritten = new IncludeAllChildrenQuery(parentQuery, parentRewrite, parentFilter);
			rewritten.setBoost(getBoost());
			return rewritten;
		} else {
			return this;
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Query#toString(java.lang.String)
	 */
	@Override
	public String toString(String field) {
		return "IncludeAllChildrenQuery (" + parentQuery.toString() + ")";
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Query#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object _other) {
		if (_other instanceof IncludeAllChildrenQuery) {
			final IncludeAllChildrenQuery other = (IncludeAllChildrenQuery) _other;
			return origParentQuery.equals(other.origParentQuery) && parentFilter.equals(other.parentFilter);
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Query#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 1;
		hash = prime * hash + origParentQuery.hashCode();
		hash = prime * hash + parentFilter.hashCode();
		return hash;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Query#clone()
	 */
	@Override
	public Object clone() {
		return new IncludeAllChildrenQuery((Query) origParentQuery.clone(), parentFilter);
	}
}
