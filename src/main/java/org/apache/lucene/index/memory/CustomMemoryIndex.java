/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CustomMemoryIndex.java 2012-3-29 15:04:16 l.xue.nong$$
 */
package org.apache.lucene.index.memory;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.index.TermVectorMapper;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.Constants;





/**
 * The Class CustomMemoryIndex.
 *
 * @author l.xue.nong
 */
@SuppressWarnings("deprecation")
public class CustomMemoryIndex implements Serializable {

    
    /** The fields. */
    private final HashMap<String, Info> fields = new HashMap<String, Info>();

    
    /** The sorted fields. */
    private transient Map.Entry<String, Info>[] sortedFields;

    
    /** The stride. */
    private final int stride;

    
    /** The Constant docBoost. */
    private static final float docBoost = 1.0f;

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 2782195016849084649L;

    /** The Constant DEBUG. */
    private static final boolean DEBUG = false;

    
    /** The Constant termComparator. */
    private static final Comparator<Object> termComparator = new Comparator<Object>() {
        public int compare(Object o1, Object o2) {
            if (o1 instanceof Map.Entry<?, ?>) o1 = ((Map.Entry<?, ?>) o1).getKey();
            if (o2 instanceof Map.Entry<?, ?>) o2 = ((Map.Entry<?, ?>) o2).getKey();
            if (o1 == o2) return 0;
            return ((String) o1).compareTo((String) o2);
        }
    };

    
    /**
     * Instantiates a new custom memory index.
     */
    public CustomMemoryIndex() {
        this(false);
    }

    
    /**
     * Instantiates a new custom memory index.
     *
     * @param storeOffsets the store offsets
     */
    private CustomMemoryIndex(boolean storeOffsets) {
        this.stride = storeOffsets ? 3 : 1;
    }

    
    /**
     * Adds the field.
     *
     * @param fieldName the field name
     * @param text the text
     * @param analyzer the analyzer
     */
    public void addField(String fieldName, String text, Analyzer analyzer) {
        if (fieldName == null)
            throw new IllegalArgumentException("fieldName must not be null");
        if (text == null)
            throw new IllegalArgumentException("text must not be null");
        if (analyzer == null)
            throw new IllegalArgumentException("analyzer must not be null");

        TokenStream stream = analyzer.tokenStream(fieldName,
                new StringReader(text));

        addField(fieldName, stream);
    }

    
    /**
     * Keyword token stream.
     *
     * @param <T> the generic type
     * @param keywords the keywords
     * @return the token stream
     */
    public <T> TokenStream keywordTokenStream(final Collection<T> keywords) {
        
        if (keywords == null)
            throw new IllegalArgumentException("keywords must not be null");

        return new TokenStream() {
            private Iterator<T> iter = keywords.iterator();
            private int start = 0;
            private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
            private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

            @Override
            public boolean incrementToken() {
                if (!iter.hasNext()) return false;

                T obj = iter.next();
                if (obj == null)
                    throw new IllegalArgumentException("keyword must not be null");

                String term = obj.toString();
                clearAttributes();
                termAtt.setEmpty().append(term);
                offsetAtt.setOffset(start, start + termAtt.length());
                start += term.length() + 1; 
                return true;
            }
        };
    }

    
    /**
     * Adds the field.
     *
     * @param fieldName the field name
     * @param stream the stream
     */
    public void addField(String fieldName, TokenStream stream) {
        addField(fieldName, stream, 1.0f);
    }

    
    /**
     * Adds the field.
     *
     * @param fieldName the field name
     * @param stream the stream
     * @param boost the boost
     */
    public void addField(String fieldName, TokenStream stream, float boost) {
        try {
            if (fieldName == null)
                throw new IllegalArgumentException("fieldName must not be null");
            if (stream == null)
                throw new IllegalArgumentException("token stream must not be null");
            if (boost <= 0.0f)
                throw new IllegalArgumentException("boost factor must be greater than 0.0");

            HashMap<String, ArrayIntList> terms = new HashMap<String, ArrayIntList>();
            int numTokens = 0;
            int numOverlapTokens = 0;
            int pos = -1;

            
            if (fields.get(fieldName) != null) {
                Info info = fields.get(fieldName);
                terms = info.terms;
                numTokens = info.numTokens;
                numOverlapTokens = info.numOverlapTokens;
                pos = info.pos;
            } else {
                terms = new HashMap<String, ArrayIntList>();
            }

            CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
            PositionIncrementAttribute posIncrAttribute = stream.addAttribute(PositionIncrementAttribute.class);
            OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
            stream.reset();
            while (stream.incrementToken()) {
                String term = termAtt.toString();
                if (term.length() == 0) continue; 

                numTokens++;
                final int posIncr = posIncrAttribute.getPositionIncrement();
                if (posIncr == 0)
                    numOverlapTokens++;
                pos += posIncr;

                ArrayIntList positions = terms.get(term);
                if (positions == null) { 
                    positions = new ArrayIntList(stride);
                    terms.put(term, positions);
                }
                if (stride == 1) {
                    positions.add(pos);
                } else {
                    positions.add(pos, offsetAtt.startOffset(), offsetAtt.endOffset());
                }
            }
            stream.end();

            
            if (numTokens > 0) {
                boost = boost * docBoost; 
                fields.put(fieldName, new Info(terms, numTokens, numOverlapTokens, boost, pos));
                sortedFields = null;    
            }
        } catch (IOException e) { 
            throw new RuntimeException(e);
        } finally {
            try {
                if (stream != null) stream.close();
            } catch (IOException e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    
    /**
     * Creates the searcher.
     *
     * @return the index searcher
     */
    public IndexSearcher createSearcher() {
        MemoryIndexReader reader = new MemoryIndexReader();
        IndexSearcher searcher = new IndexSearcher(reader); 
        reader.setSearcher(searcher); 
        return searcher;
    }

    
    /**
     * Search.
     *
     * @param query the query
     * @return the float
     */
    public float search(Query query) {
        if (query == null)
            throw new IllegalArgumentException("query must not be null");

        Searcher searcher = createSearcher();
        try {
            final float[] scores = new float[1]; 
            searcher.search(query, new Collector() {
                private Scorer scorer;

                @Override
                public void collect(int doc) throws IOException {
                    scores[0] = scorer.score();
                }

                @Override
                public void setScorer(Scorer scorer) throws IOException {
                    this.scorer = scorer;
                }

                @Override
                public boolean acceptsDocsOutOfOrder() {
                    return true;
                }

                @Override
                public void setNextReader(IndexReader reader, int docBase) {
                }
            });
            float score = scores[0];
            return score;
        } catch (IOException e) { 
            throw new RuntimeException(e);
        } finally {
            
            
        }
    }

    
    /**
     * Gets the memory size.
     *
     * @return the memory size
     */
    public int getMemorySize() {
        
        int PTR = VM.PTR;
        int INT = VM.INT;
        int size = 0;
        size += VM.sizeOfObject(2 * PTR + INT); 
        if (sortedFields != null) size += VM.sizeOfObjectArray(sortedFields.length);

        size += VM.sizeOfHashMap(fields.size());
        for (Map.Entry<String, Info> entry : fields.entrySet()) { 
            Info info = entry.getValue();
            size += VM.sizeOfObject(2 * INT + 3 * PTR); 
            if (info.sortedTerms != null) size += VM.sizeOfObjectArray(info.sortedTerms.length);

            int len = info.terms.size();
            size += VM.sizeOfHashMap(len);
            Iterator<Map.Entry<String, ArrayIntList>> iter2 = info.terms.entrySet().iterator();
            while (--len >= 0) { 
                Map.Entry<String, ArrayIntList> e = iter2.next();
                size += VM.sizeOfObject(PTR + 3 * INT); 

                ArrayIntList positions = e.getValue();
                size += VM.sizeOfArrayIntList(positions.size());
            }
        }
        return size;
    }

    /**
     * Num positions.
     *
     * @param positions the positions
     * @return the int
     */
    private int numPositions(ArrayIntList positions) {
        return positions.size() / stride;
    }

    
    /**
     * Sort fields.
     */
    private void sortFields() {
        if (sortedFields == null) sortedFields = sort(fields);
    }

    
    /**
     * Sort.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the map
     * @return the map. entry[]
     */
    private static <K, V> Map.Entry<K, V>[] sort(HashMap<K, V> map) {
        int size = map.size();
        @SuppressWarnings("unchecked")
        Map.Entry<K, V>[] entries = new Map.Entry[size];

        Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            entries[i] = iter.next();
        }

        if (size > 1) ArrayUtil.quickSort(entries, termComparator);
        return entries;
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(256);
        sortFields();
        int sumChars = 0;
        int sumPositions = 0;
        int sumTerms = 0;

        for (int i = 0; i < sortedFields.length; i++) {
            Map.Entry<String, Info> entry = sortedFields[i];
            String fieldName = entry.getKey();
            Info info = entry.getValue();
            info.sortTerms();
            result.append(fieldName + ":\n");

            int numChars = 0;
            int numPositions = 0;
            for (int j = 0; j < info.sortedTerms.length; j++) {
                Map.Entry<String, ArrayIntList> e = info.sortedTerms[j];
                String term = e.getKey();
                ArrayIntList positions = e.getValue();
                result.append("\t'" + term + "':" + numPositions(positions) + ":");
                result.append(positions.toString(stride)); 
                result.append("\n");
                numPositions += numPositions(positions);
                numChars += term.length();
            }

            result.append("\tterms=" + info.sortedTerms.length);
            result.append(", positions=" + numPositions);
            result.append(", Kchars=" + (numChars / 1000.0f));
            result.append("\n");
            sumPositions += numPositions;
            sumChars += numChars;
            sumTerms += info.sortedTerms.length;
        }

        result.append("\nfields=" + sortedFields.length);
        result.append(", terms=" + sumTerms);
        result.append(", positions=" + sumPositions);
        result.append(", Kchars=" + (sumChars / 1000.0f));
        return result.toString();
    }


    
    
    

    
    /**
     * The Class Info.
     *
     * @author l.xue.nong
     */
    private static final class Info implements Serializable {

        
        /** The terms. */
        private final HashMap<String, ArrayIntList> terms;

        
        /** The sorted terms. */
        private transient Map.Entry<String, ArrayIntList>[] sortedTerms;

        
        /** The num tokens. */
        private final int numTokens;

        
        /** The num overlap tokens. */
        private final int numOverlapTokens;

        
        /** The boost. */
        private final float boost;

        /** The pos. */
        private final int pos;

        
        /** The template. */
        public transient Term template;

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 2882195016849084649L;

        /**
         * Instantiates a new info.
         *
         * @param terms the terms
         * @param numTokens the num tokens
         * @param numOverlapTokens the num overlap tokens
         * @param boost the boost
         * @param pos the pos
         */
        public Info(HashMap<String, ArrayIntList> terms, int numTokens, int numOverlapTokens, float boost, int pos) {
            this.terms = terms;
            this.numTokens = numTokens;
            this.numOverlapTokens = numOverlapTokens;
            this.boost = boost;
            this.pos = pos;
        }

        
        /**
         * Sort terms.
         */
        public void sortTerms() {
            if (sortedTerms == null) sortedTerms = sort(terms);
        }

        
        /**
         * Gets the positions.
         *
         * @param term the term
         * @return the positions
         */
        public ArrayIntList getPositions(String term) {
            return terms.get(term);
        }

        
        /**
         * Gets the positions.
         *
         * @param pos the pos
         * @return the positions
         */
        public ArrayIntList getPositions(int pos) {
            return sortedTerms[pos].getValue();
        }

        /**
         * Gets the boost.
         *
         * @return the boost
         */
        public float getBoost() {
            return boost;
        }

    }


    
    
    

    
    /**
     * The Class ArrayIntList.
     *
     * @author l.xue.nong
     */
    private static final class ArrayIntList implements Serializable {

        /** The elements. */
        private int[] elements;
        
        /** The size. */
        private int size = 0;

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 2282195016849084649L;

        /**
         * Instantiates a new array int list.
         */
        @SuppressWarnings("unused")
		public ArrayIntList() {
            this(10);
        }

        /**
         * Instantiates a new array int list.
         *
         * @param initialCapacity the initial capacity
         */
        public ArrayIntList(int initialCapacity) {
            elements = new int[initialCapacity];
        }

        /**
         * Adds the.
         *
         * @param elem the elem
         */
        public void add(int elem) {
            if (size == elements.length) ensureCapacity(size + 1);
            elements[size++] = elem;
        }

        /**
         * Adds the.
         *
         * @param pos the pos
         * @param start the start
         * @param end the end
         */
        public void add(int pos, int start, int end) {
            if (size + 3 > elements.length) ensureCapacity(size + 3);
            elements[size] = pos;
            elements[size + 1] = start;
            elements[size + 2] = end;
            size += 3;
        }

        /**
         * Gets the.
         *
         * @param index the index
         * @return the int
         */
        public int get(int index) {
            if (index >= size) throwIndex(index);
            return elements[index];
        }

        /**
         * Size.
         *
         * @return the int
         */
        public int size() {
            return size;
        }

        /**
         * To array.
         *
         * @param stride the stride
         * @return the int[]
         */
        public int[] toArray(int stride) {
            int[] arr = new int[size() / stride];
            if (stride == 1) {
                System.arraycopy(elements, 0, arr, 0, size); 
            } else {
                for (int i = 0, j = 0; j < size; i++, j += stride) arr[i] = elements[j];
            }
            return arr;
        }

        /**
         * Ensure capacity.
         *
         * @param minCapacity the min capacity
         */
        private void ensureCapacity(int minCapacity) {
            int newCapacity = Math.max(minCapacity, (elements.length * 3) / 2 + 1);
            int[] newElements = new int[newCapacity];
            System.arraycopy(elements, 0, newElements, 0, size);
            elements = newElements;
        }

        /**
         * Throw index.
         *
         * @param index the index
         */
        private void throwIndex(int index) {
            throw new IndexOutOfBoundsException("index: " + index
                    + ", size: " + size);
        }

        
        /**
         * To string.
         *
         * @param stride the stride
         * @return the string
         */
        public String toString(int stride) {
            int s = size() / stride;
            int len = Math.min(10, s); 
            StringBuilder buf = new StringBuilder(4 * len);
            buf.append("[");
            for (int i = 0; i < len; i++) {
                buf.append(get(i * stride));
                if (i < len - 1) buf.append(", ");
            }
            if (len != s) buf.append(", ..."); 
            buf.append("]");
            return buf.toString();
        }
    }


    
    
    
    /** The Constant MATCH_ALL_TERM. */
    private static final Term MATCH_ALL_TERM = new Term("");

    
    /**
     * The Class MemoryIndexReader.
     *
     * @author l.xue.nong
     */
    private final class MemoryIndexReader extends IndexReader {

        /** The searcher. */
        private Searcher searcher; 

        /**
         * Instantiates a new memory index reader.
         */
        private MemoryIndexReader() {
            super(); 
            readerFinishedListeners = Collections.synchronizedSet(new HashSet<ReaderFinishedListener>());
        }

        /**
         * Gets the info.
         *
         * @param fieldName the field name
         * @return the info
         */
        private Info getInfo(String fieldName) {
            return fields.get(fieldName);
        }

        /**
         * Gets the info.
         *
         * @param pos the pos
         * @return the info
         */
        private Info getInfo(int pos) {
            return sortedFields[pos].getValue();
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#docFreq(org.apache.lucene.index.Term)
         */
        @Override
        public int docFreq(Term term) {
            Info info = getInfo(term.field());
            int freq = 0;
            if (info != null) freq = info.getPositions(term.text()) != null ? 1 : 0;
            if (DEBUG) System.err.println("MemoryIndexReader.docFreq: " + term + ", freq:" + freq);
            return freq;
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#terms()
         */
        @Override
        public TermEnum terms() {
            if (DEBUG) System.err.println("MemoryIndexReader.terms()");
            return terms(MATCH_ALL_TERM);
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#terms(org.apache.lucene.index.Term)
         */
        @Override
        public TermEnum terms(Term term) {
            if (DEBUG) System.err.println("MemoryIndexReader.terms: " + term);

            int i; 
            int j; 

            sortFields();
            if (sortedFields.length == 1 && sortedFields[0].getKey() == term.field()) {
                j = 0; 
            } else {
                j = Arrays.binarySearch(sortedFields, term.field(), termComparator);
            }

            if (j < 0) { 
                j = -j - 1;
                i = 0;
                if (j < sortedFields.length) getInfo(j).sortTerms();
            } else { 
                Info info = getInfo(j);
                info.sortTerms();
                i = Arrays.binarySearch(info.sortedTerms, term.text(), termComparator);
                if (i < 0) { 
                    i = -i - 1;
                    if (i >= info.sortedTerms.length) { 
                        j++;
                        i = 0;
                        if (j < sortedFields.length) getInfo(j).sortTerms();
                    }
                }
            }
            final int ix = i;
            final int jx = j;

            return new TermEnum() {

                private int srtTermsIdx = ix; 
                private int srtFldsIdx = jx; 

                @Override
                public boolean next() {
                    if (DEBUG) System.err.println("TermEnum.next");
                    if (srtFldsIdx >= sortedFields.length) return false;
                    Info info = getInfo(srtFldsIdx);
                    if (++srtTermsIdx < info.sortedTerms.length) return true;

                    
                    srtFldsIdx++;
                    srtTermsIdx = 0;
                    if (srtFldsIdx >= sortedFields.length) return false;
                    getInfo(srtFldsIdx).sortTerms();
                    return true;
                }

                @Override
                public Term term() {
                    if (DEBUG) System.err.println("TermEnum.term: " + srtTermsIdx);
                    if (srtFldsIdx >= sortedFields.length) return null;
                    Info info = getInfo(srtFldsIdx);
                    if (srtTermsIdx >= info.sortedTerms.length) return null;

                    return createTerm(info, srtFldsIdx, info.sortedTerms[srtTermsIdx].getKey());
                }

                @Override
                public int docFreq() {
                    if (DEBUG) System.err.println("TermEnum.docFreq");
                    if (srtFldsIdx >= sortedFields.length) return 0;
                    Info info = getInfo(srtFldsIdx);
                    if (srtTermsIdx >= info.sortedTerms.length) return 0;
                    return numPositions(info.getPositions(srtTermsIdx));
                }

                @Override
                public void close() {
                    if (DEBUG) System.err.println("TermEnum.close");
                }

                
                private Term createTerm(Info info, int pos, String text) {
                    
                    Term template = info.template;
                    if (template == null) { 
                        String fieldName = sortedFields[pos].getKey();
                        template = new Term(fieldName);
                        info.template = template;
                    }

                    return template.createTerm(text);
                }

            };
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#termPositions()
         */
        @Override
        public TermPositions termPositions() {
            if (DEBUG) System.err.println("MemoryIndexReader.termPositions");

            return new TermPositions() {

                private boolean hasNext;
                private int cursor = 0;
                private ArrayIntList current;
                private Term term;

                public void seek(Term term) {
                    this.term = term;
                    if (DEBUG) System.err.println(".seek: " + term);
                    if (term == null) {
                        hasNext = true;  
                    } else {
                        Info info = getInfo(term.field());
                        current = info == null ? null : info.getPositions(term.text());
                        hasNext = (current != null);
                        cursor = 0;
                    }
                }

                public void seek(TermEnum termEnum) {
                    if (DEBUG) System.err.println(".seekEnum");
                    seek(termEnum.term());
                }

                public int doc() {
                    if (DEBUG) System.err.println(".doc");
                    return 0;
                }

                public int freq() {
                    int freq = current != null ? numPositions(current) : (term == null ? 1 : 0);
                    if (DEBUG) System.err.println(".freq: " + freq);
                    return freq;
                }

                public boolean next() {
                    if (DEBUG) System.err.println(".next: " + current + ", oldHasNext=" + hasNext);
                    boolean next = hasNext;
                    hasNext = false;
                    return next;
                }

                public int read(int[] docs, int[] freqs) {
                    if (DEBUG) System.err.println(".read: " + docs.length);
                    if (!hasNext) return 0;
                    hasNext = false;
                    docs[0] = 0;
                    freqs[0] = freq();
                    return 1;
                }

                public boolean skipTo(int target) {
                    if (DEBUG) System.err.println(".skipTo: " + target);
                    return next();
                }

                public void close() {
                    if (DEBUG) System.err.println(".close");
                }

                public int nextPosition() { 
                    int pos = current.get(cursor);
                    cursor += stride;
                    if (DEBUG) System.err.println(".nextPosition: " + pos);
                    return pos;
                }

                
                public int getPayloadLength() {
                    throw new UnsupportedOperationException();
                }

                
                public byte[] getPayload(byte[] data, int offset) throws IOException {
                    throw new UnsupportedOperationException();
                }

                public boolean isPayloadAvailable() {
                    
                    return false;
                }

            };
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#termDocs()
         */
        @Override
        public TermDocs termDocs() {
            if (DEBUG) System.err.println("MemoryIndexReader.termDocs");
            return termPositions();
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#getTermFreqVectors(int)
         */
        @Override
        public TermFreqVector[] getTermFreqVectors(int docNumber) {
            if (DEBUG) System.err.println("MemoryIndexReader.getTermFreqVectors");
            TermFreqVector[] vectors = new TermFreqVector[fields.size()];

            Iterator<String> iter = fields.keySet().iterator();
            for (int i = 0; i < vectors.length; i++) {
                vectors[i] = getTermFreqVector(docNumber, iter.next());
            }
            return vectors;
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#getTermFreqVector(int, org.apache.lucene.index.TermVectorMapper)
         */
        @Override
        public void getTermFreqVector(int docNumber, TermVectorMapper mapper) throws IOException {
            if (DEBUG) System.err.println("MemoryIndexReader.getTermFreqVectors");

            
            for (final String fieldName : fields.keySet()) {
                getTermFreqVector(docNumber, fieldName, mapper);
            }
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#getTermFreqVector(int, java.lang.String, org.apache.lucene.index.TermVectorMapper)
         */
        @Override
        public void getTermFreqVector(int docNumber, String field, TermVectorMapper mapper) throws IOException {
            if (DEBUG) System.err.println("MemoryIndexReader.getTermFreqVector");
            final Info info = getInfo(field);
            if (info == null) {
                return;
            }
            info.sortTerms();
            mapper.setExpectations(field, info.sortedTerms.length, stride != 1, true);
            for (int i = info.sortedTerms.length; --i >= 0; ) {

                ArrayIntList positions = info.sortedTerms[i].getValue();
                int size = positions.size();
                org.apache.lucene.index.TermVectorOffsetInfo[] offsets =
                        new org.apache.lucene.index.TermVectorOffsetInfo[size / stride];

                for (int k = 0, j = 1; j < size; k++, j += stride) {
                    int start = positions.get(j);
                    int end = positions.get(j + 1);
                    offsets[k] = new org.apache.lucene.index.TermVectorOffsetInfo(start, end);
                }
                mapper.map(info.sortedTerms[i].getKey(),
                        numPositions(info.sortedTerms[i].getValue()),
                        offsets, (info.sortedTerms[i].getValue()).toArray(stride));
            }
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#getTermFreqVector(int, java.lang.String)
         */
        @Override
        public TermFreqVector getTermFreqVector(int docNumber, final String fieldName) {
            if (DEBUG) System.err.println("MemoryIndexReader.getTermFreqVector");
            final Info info = getInfo(fieldName);
            if (info == null) return null; 
            info.sortTerms();

            return new TermPositionVector() {

                private final Map.Entry<String, ArrayIntList>[] sortedTerms = info.sortedTerms;

                public String getField() {
                    return fieldName;
                }

                public int size() {
                    return sortedTerms.length;
                }

                public String[] getTerms() {
                    String[] terms = new String[sortedTerms.length];
                    for (int i = sortedTerms.length; --i >= 0; ) {
                        terms[i] = sortedTerms[i].getKey();
                    }
                    return terms;
                }

                public int[] getTermFrequencies() {
                    int[] freqs = new int[sortedTerms.length];
                    for (int i = sortedTerms.length; --i >= 0; ) {
                        freqs[i] = numPositions(sortedTerms[i].getValue());
                    }
                    return freqs;
                }

                public int indexOf(String term) {
                    int i = Arrays.binarySearch(sortedTerms, term, termComparator);
                    return i >= 0 ? i : -1;
                }

                public int[] indexesOf(String[] terms, int start, int len) {
                    int[] indexes = new int[len];
                    for (int i = 0; i < len; i++) {
                        indexes[i] = indexOf(terms[start++]);
                    }
                    return indexes;
                }

                
                public int[] getTermPositions(int index) {
                    return sortedTerms[index].getValue().toArray(stride);
                }

                
                public org.apache.lucene.index.TermVectorOffsetInfo[] getOffsets(int index) {
                    if (stride == 1) return null; 

                    ArrayIntList positions = sortedTerms[index].getValue();
                    int size = positions.size();
                    org.apache.lucene.index.TermVectorOffsetInfo[] offsets =
                            new org.apache.lucene.index.TermVectorOffsetInfo[size / stride];

                    for (int i = 0, j = 1; j < size; i++, j += stride) {
                        int start = positions.get(j);
                        int end = positions.get(j + 1);
                        offsets[i] = new org.apache.lucene.index.TermVectorOffsetInfo(start, end);
                    }
                    return offsets;
                }

            };
        }

        /**
         * Gets the similarity.
         *
         * @return the similarity
         */
        private Similarity getSimilarity() {
            if (searcher != null) return searcher.getSimilarity();
            return Similarity.getDefault();
        }

        /**
         * Sets the searcher.
         *
         * @param searcher the new searcher
         */
        private void setSearcher(Searcher searcher) {
            this.searcher = searcher;
        }

        
        /** The cached norms. */
        private byte[] cachedNorms;
        
        /** The cached field name. */
        private String cachedFieldName;
        
        /** The cached similarity. */
        private Similarity cachedSimilarity;

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#norms(java.lang.String)
         */
        @Override
        public byte[] norms(String fieldName) {
            byte[] norms = cachedNorms;
            Similarity sim = getSimilarity();
            if (fieldName != cachedFieldName || sim != cachedSimilarity) { 
                Info info = getInfo(fieldName);
                int numTokens = info != null ? info.numTokens : 0;
                int numOverlapTokens = info != null ? info.numOverlapTokens : 0;
                float boost = info != null ? info.getBoost() : 1.0f;
                FieldInvertState invertState = new FieldInvertState(0, numTokens, numOverlapTokens, 0, boost);
                float n = sim.computeNorm(fieldName, invertState);
                byte norm = sim.encodeNormValue(n);
                norms = new byte[]{norm};

                
                cachedNorms = norms;
                cachedFieldName = fieldName;
                cachedSimilarity = sim;
                if (DEBUG)
                    System.err.println("MemoryIndexReader.norms: " + fieldName + ":" + n + ":" + norm + ":" + numTokens);
            }
            return norms;
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#norms(java.lang.String, byte[], int)
         */
        @Override
        public void norms(String fieldName, byte[] bytes, int offset) {
            if (DEBUG) System.err.println("MemoryIndexReader.norms*: " + fieldName);
            byte[] norms = norms(fieldName);
            System.arraycopy(norms, 0, bytes, offset, norms.length);
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#doSetNorm(int, java.lang.String, byte)
         */
        @Override
        protected void doSetNorm(int doc, String fieldName, byte value) {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#numDocs()
         */
        @Override
        public int numDocs() {
            if (DEBUG) System.err.println("MemoryIndexReader.numDocs");
            return fields.size() > 0 ? 1 : 0;
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#maxDoc()
         */
        @Override
        public int maxDoc() {
            if (DEBUG) System.err.println("MemoryIndexReader.maxDoc");
            return 1;
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#document(int)
         */
        @Override
        public Document document(int n) {
            if (DEBUG) System.err.println("MemoryIndexReader.document");
            return new Document(); 
        }

        
        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#document(int, org.apache.lucene.document.FieldSelector)
         */
        @Override
        public Document document(int n, FieldSelector fieldSelector) throws IOException {
            if (DEBUG) System.err.println("MemoryIndexReader.document");
            return new Document(); 
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#isDeleted(int)
         */
        @Override
        public boolean isDeleted(int n) {
            if (DEBUG) System.err.println("MemoryIndexReader.isDeleted");
            return false;
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#hasDeletions()
         */
        @Override
        public boolean hasDeletions() {
            if (DEBUG) System.err.println("MemoryIndexReader.hasDeletions");
            return false;
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#doDelete(int)
         */
        @Override
        protected void doDelete(int docNum) {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#doUndeleteAll()
         */
        @Override
        protected void doUndeleteAll() {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#doCommit(java.util.Map)
         */
        @Override
        protected void doCommit(Map<String, String> commitUserData) {
            if (DEBUG) System.err.println("MemoryIndexReader.doCommit");
        }

        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#doClose()
         */
        @Override
        protected void doClose() {
            if (DEBUG) System.err.println("MemoryIndexReader.doClose");
        }

        
        /* (non-Javadoc)
         * @see org.apache.lucene.index.IndexReader#getFieldNames(org.apache.lucene.index.IndexReader.FieldOption)
         */
        @Override
        public Collection<String> getFieldNames(FieldOption fieldOption) {
            if (DEBUG) System.err.println("MemoryIndexReader.getFieldNamesOption");
            if (fieldOption == FieldOption.UNINDEXED)
                return Collections.<String>emptySet();
            if (fieldOption == FieldOption.INDEXED_NO_TERMVECTOR)
                return Collections.<String>emptySet();
            if (fieldOption == FieldOption.TERMVECTOR_WITH_OFFSET && stride == 1)
                return Collections.<String>emptySet();
            if (fieldOption == FieldOption.TERMVECTOR_WITH_POSITION_OFFSET && stride == 1)
                return Collections.<String>emptySet();

            return Collections.unmodifiableSet(fields.keySet());
        }
    }


    
    
    
    /**
     * The Class VM.
     *
     * @author l.xue.nong
     */
    private static final class VM {

        /** The Constant PTR. */
        public static final int PTR = Constants.JRE_IS_64BIT ? 8 : 4;
        
        /** The Constant INT. */
        public static final int INT = 4;
        
        /** The Constant LOG_PTR. */
        private static final int LOG_PTR = (int) Math.round(log2(PTR));

        
        /** The Constant OBJECT_HEADER. */
        private static final int OBJECT_HEADER = 2 * PTR;

        /**
         * Instantiates a new vM.
         */
        private VM() {
        } 

        
        
        
        
        
        /**
         * Size of.
         *
         * @param n the n
         * @return the int
         */
        private static int sizeOf(int n) {
            return (((n - 1) >> LOG_PTR) + 1) << LOG_PTR;
        }

        /**
         * Size of object.
         *
         * @param n the n
         * @return the int
         */
        public static int sizeOfObject(int n) {
            return sizeOf(OBJECT_HEADER + n);
        }

        /**
         * Size of object array.
         *
         * @param len the len
         * @return the int
         */
        public static int sizeOfObjectArray(int len) {
            return sizeOfObject(INT + PTR * len);
        }

        /**
         * Size of int array.
         *
         * @param len the len
         * @return the int
         */
        public static int sizeOfIntArray(int len) {
            return sizeOfObject(INT + INT * len);
        }

        /**
         * Size of hash map.
         *
         * @param len the len
         * @return the int
         */
        public static int sizeOfHashMap(int len) {
            return sizeOfObject(4 * PTR + 4 * INT) + sizeOfObjectArray(len)
                    + len * sizeOfObject(3 * PTR + INT); 
        }

        
        /**
         * Size of array int list.
         *
         * @param len the len
         * @return the int
         */
        public static int sizeOfArrayIntList(int len) {
            return sizeOfObject(PTR + INT) + sizeOfIntArray(len);
        }

        
        /**
         * Log2.
         *
         * @param value the value
         * @return the double
         */
        private static double log2(double value) {
            return Math.log(value) / Math.log(2);
        }

    }

}
