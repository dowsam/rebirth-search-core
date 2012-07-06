/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BlockJoinQuery.java 2012-3-29 15:02:06 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.search.nested;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.FixedBitSet;

import cn.com.rebirth.search.commons.lucene.docset.FixedBitDocSet;
import cn.com.rebirth.search.commons.lucene.search.NoopCollector;




/**
 * The Class BlockJoinQuery.
 *
 * @author l.xue.nong
 */
public class BlockJoinQuery extends Query {

    
    /**
     * The Enum ScoreMode.
     *
     * @author l.xue.nong
     */
    public static enum ScoreMode {

/** The None. */
None, 
 
 /** The Avg. */
 Avg, 
 
 /** The Max. */
 Max, 
 
 /** The Total. */
 Total}

    ;

    
    /** The parents filter. */
    private final Filter parentsFilter;
    
    
    /** The child query. */
    private final Query childQuery;

    
    /** The child collector. */
    private Collector childCollector = NoopCollector.NOOP_COLLECTOR;

    
    /**
     * Sets the collector.
     *
     * @param collector the collector
     * @return the block join query
     */
    public BlockJoinQuery setCollector(Collector collector) {
        this.childCollector = collector;
        return this;
    }

    
    
    
    
    
    
    /** The orig child query. */
    private final Query origChildQuery;
    
    
    /** The score mode. */
    private final ScoreMode scoreMode;

    
    /**
     * Instantiates a new block join query.
     *
     * @param childQuery the child query
     * @param parentsFilter the parents filter
     * @param scoreMode the score mode
     */
    public BlockJoinQuery(Query childQuery, Filter parentsFilter, ScoreMode scoreMode) {
        super();
        this.origChildQuery = childQuery;
        this.childQuery = childQuery;
        this.parentsFilter = parentsFilter;
        this.scoreMode = scoreMode;
    }

    
    /**
     * Instantiates a new block join query.
     *
     * @param origChildQuery the orig child query
     * @param childQuery the child query
     * @param parentsFilter the parents filter
     * @param scoreMode the score mode
     */
    private BlockJoinQuery(Query origChildQuery, Query childQuery, Filter parentsFilter, ScoreMode scoreMode) {
        super();
        this.origChildQuery = origChildQuery;
        this.childQuery = childQuery;
        this.parentsFilter = parentsFilter;
        this.scoreMode = scoreMode;
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.Query#createWeight(org.apache.lucene.search.Searcher)
     */
    @Override
    public Weight createWeight(Searcher searcher) throws IOException {
        return new BlockJoinWeight(this, childQuery.createWeight(searcher), parentsFilter, scoreMode, childCollector);
    }

    
    /**
     * The Class BlockJoinWeight.
     *
     * @author l.xue.nong
     */
    private static class BlockJoinWeight extends Weight {
        
        
        /** The join query. */
        private final Query joinQuery;
        
        
        /** The child weight. */
        private final Weight childWeight;
        
        
        /** The parents filter. */
        private final Filter parentsFilter;
        
        
        /** The score mode. */
        private final ScoreMode scoreMode;
        
        
        /** The child collector. */
        private final Collector childCollector;

        
        /**
         * Instantiates a new block join weight.
         *
         * @param joinQuery the join query
         * @param childWeight the child weight
         * @param parentsFilter the parents filter
         * @param scoreMode the score mode
         * @param childCollector the child collector
         */
        public BlockJoinWeight(Query joinQuery, Weight childWeight, Filter parentsFilter, ScoreMode scoreMode, Collector childCollector) {
            super();
            this.joinQuery = joinQuery;
            this.childWeight = childWeight;
            this.parentsFilter = parentsFilter;
            this.scoreMode = scoreMode;
            this.childCollector = childCollector;
        }

        
        /* (non-Javadoc)
         * @see org.apache.lucene.search.Weight#getQuery()
         */
        @Override
        public Query getQuery() {
            return joinQuery;
        }

        
        /* (non-Javadoc)
         * @see org.apache.lucene.search.Weight#getValue()
         */
        @Override
        public float getValue() {
            return childWeight.getValue();
        }

        
        /* (non-Javadoc)
         * @see org.apache.lucene.search.Weight#sumOfSquaredWeights()
         */
        @Override
        public float sumOfSquaredWeights() throws IOException {
            return childWeight.sumOfSquaredWeights() * joinQuery.getBoost() * joinQuery.getBoost();
        }

        
        /* (non-Javadoc)
         * @see org.apache.lucene.search.Weight#normalize(float)
         */
        @Override
        public void normalize(float norm) {
            childWeight.normalize(norm * joinQuery.getBoost());
        }

        
        /* (non-Javadoc)
         * @see org.apache.lucene.search.Weight#scorer(org.apache.lucene.index.IndexReader, boolean, boolean)
         */
        @Override
        public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder, boolean topScorer) throws IOException {
            
            final Scorer childScorer = childWeight.scorer(reader, true, false);

            if (childScorer == null) {
                
                return null;
            }

            final int firstChildDoc = childScorer.nextDoc();
            if (firstChildDoc == DocIdSetIterator.NO_MORE_DOCS) {
                
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

            
            if (childCollector != null) {
                childCollector.setNextReader(reader, 0);
                childCollector.setScorer(childScorer);
            }

            return new BlockJoinScorer(this, childScorer, (FixedBitSet) parents, firstChildDoc, scoreMode, childCollector);
        }

        
        /* (non-Javadoc)
         * @see org.apache.lucene.search.Weight#explain(org.apache.lucene.index.IndexReader, int)
         */
        @Override
        public Explanation explain(IndexReader reader, int doc) throws IOException {
            
            throw new UnsupportedOperationException(getClass().getName() +
                    " cannot explain match on parent document");
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
     * The Class BlockJoinScorer.
     *
     * @author l.xue.nong
     */
    static class BlockJoinScorer extends Scorer {
        
        
        /** The child scorer. */
        private final Scorer childScorer;
        
        
        /** The parent bits. */
        private final FixedBitSet parentBits;
        
        
        /** The score mode. */
        private final ScoreMode scoreMode;
        
        
        /** The child collector. */
        private final Collector childCollector;
        
        
        /** The parent doc. */
        private int parentDoc = -1;
        
        
        /** The parent score. */
        private float parentScore;
        
        
        /** The next child doc. */
        private int nextChildDoc;

        
        /** The pending child docs. */
        private int[] pendingChildDocs = new int[5];
        
        
        /** The pending child scores. */
        private float[] pendingChildScores;
        
        
        /** The child doc upto. */
        private int childDocUpto;

        
        /**
         * Instantiates a new block join scorer.
         *
         * @param weight the weight
         * @param childScorer the child scorer
         * @param parentBits the parent bits
         * @param firstChildDoc the first child doc
         * @param scoreMode the score mode
         * @param childCollector the child collector
         */
        public BlockJoinScorer(Weight weight, Scorer childScorer, FixedBitSet parentBits, int firstChildDoc, ScoreMode scoreMode, Collector childCollector) {
            super(weight);
            
            this.parentBits = parentBits;
            this.childScorer = childScorer;
            this.scoreMode = scoreMode;
            this.childCollector = childCollector;
            if (scoreMode != ScoreMode.None) {
                pendingChildScores = new float[5];
            }
            nextChildDoc = firstChildDoc;
        }

        
        /* (non-Javadoc)
         * @see org.apache.lucene.search.Scorer#visitSubScorers(org.apache.lucene.search.Query, org.apache.lucene.search.BooleanClause.Occur, org.apache.lucene.search.Scorer.ScorerVisitor)
         */
        @Override
        public void visitSubScorers(Query parent, BooleanClause.Occur relationship,
                                    ScorerVisitor<Query, Query, Scorer> visitor) {
            super.visitSubScorers(parent, relationship, visitor);
            
            childScorer.visitScorers(visitor);
        }

        
        /**
         * Gets the child count.
         *
         * @return the child count
         */
        int getChildCount() {
            return childDocUpto;
        }

        
        /**
         * Swap child docs.
         *
         * @param other the other
         * @return the int[]
         */
        int[] swapChildDocs(int[] other) {
            final int[] ret = pendingChildDocs;
            if (other == null) {
                pendingChildDocs = new int[5];
            } else {
                pendingChildDocs = other;
            }
            return ret;
        }

        
        /**
         * Swap child scores.
         *
         * @param other the other
         * @return the float[]
         */
        float[] swapChildScores(float[] other) {
            if (scoreMode == ScoreMode.None) {
                throw new IllegalStateException("ScoreMode is None");
            }
            final float[] ret = pendingChildScores;
            if (other == null) {
                pendingChildScores = new float[5];
            } else {
                pendingChildScores = other;
            }
            return ret;
        }

        
        /* (non-Javadoc)
         * @see org.apache.lucene.search.DocIdSetIterator#nextDoc()
         */
        @Override
        public int nextDoc() throws IOException {
            

            if (nextChildDoc == NO_MORE_DOCS) {
                
                return parentDoc = NO_MORE_DOCS;
            }

            
            parentDoc = parentBits.nextSetBit(nextChildDoc);
            
            assert parentDoc != -1;

            float totalScore = 0;
            float maxScore = Float.NEGATIVE_INFINITY;

            childDocUpto = 0;
            do {
                
                if (pendingChildDocs.length == childDocUpto) {
                    pendingChildDocs = ArrayUtil.grow(pendingChildDocs);
                    if (scoreMode != ScoreMode.None) {
                        pendingChildScores = ArrayUtil.grow(pendingChildScores);
                    }
                }
                pendingChildDocs[childDocUpto] = nextChildDoc;
                if (scoreMode != ScoreMode.None) {
                    
                    final float childScore = childScorer.score();
                    pendingChildScores[childDocUpto] = childScore;
                    maxScore = Math.max(childScore, maxScore);
                    totalScore += childScore;
                }

                
                childCollector.collect(nextChildDoc);

                childDocUpto++;
                nextChildDoc = childScorer.nextDoc();
            } while (nextChildDoc < parentDoc);
            

            
            assert nextChildDoc != parentDoc;

            switch (scoreMode) {
                case Avg:
                    parentScore = totalScore / childDocUpto;
                    break;
                case Max:
                    parentScore = maxScore;
                    break;
                case Total:
                    parentScore = totalScore;
                    break;
                case None:
                    break;
            }

            
            return parentDoc;
        }

        
        /* (non-Javadoc)
         * @see org.apache.lucene.search.DocIdSetIterator#docID()
         */
        @Override
        public int docID() {
            return parentDoc;
        }

        
        /* (non-Javadoc)
         * @see org.apache.lucene.search.Scorer#score()
         */
        @Override
        public float score() throws IOException {
            return parentScore;
        }

        
        /* (non-Javadoc)
         * @see org.apache.lucene.search.DocIdSetIterator#advance(int)
         */
        @Override
        public int advance(int parentTarget) throws IOException {

            
            if (parentTarget == NO_MORE_DOCS) {
                return parentDoc = NO_MORE_DOCS;
            }

            if (parentTarget == 0) {
                
                
                
                
                
                
                return nextDoc();
            }

            final int prevParentDoc = parentBits.prevSetBit(parentTarget - 1);

            
            assert prevParentDoc >= parentDoc;
            if (prevParentDoc > nextChildDoc) {
                nextChildDoc = childScorer.advance(prevParentDoc);
                
                
                
            }

            
            assert nextChildDoc != prevParentDoc;

            final int nd = nextDoc();
            
            return nd;
        }
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.Query#extractTerms(java.util.Set)
     */
    @Override
    public void extractTerms(Set<Term> terms) {
        childQuery.extractTerms(terms);
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.Query#rewrite(org.apache.lucene.index.IndexReader)
     */
    @Override
    public Query rewrite(IndexReader reader) throws IOException {
        final Query childRewrite = childQuery.rewrite(reader);
        if (childRewrite != childQuery) {
            Query rewritten = new BlockJoinQuery(childQuery,
                    childRewrite,
                    parentsFilter,
                    scoreMode).setCollector(childCollector);
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
        return "BlockJoinQuery (" + childQuery.toString() + ")";
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.Query#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object _other) {
        if (_other instanceof BlockJoinQuery) {
            final BlockJoinQuery other = (BlockJoinQuery) _other;
            return origChildQuery.equals(other.origChildQuery) &&
                    parentsFilter.equals(other.parentsFilter) &&
                    scoreMode == other.scoreMode;
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
        hash = prime * hash + origChildQuery.hashCode();
        hash = prime * hash + scoreMode.hashCode();
        hash = prime * hash + parentsFilter.hashCode();
        return hash;
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.Query#clone()
     */
    @Override
    public Object clone() {
        return new BlockJoinQuery((Query) origChildQuery.clone(),
                parentsFilter,
                scoreMode).setCollector(childCollector);
    }
}
