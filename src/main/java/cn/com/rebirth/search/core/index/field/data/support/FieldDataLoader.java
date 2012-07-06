/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FieldDataLoader.java 2012-3-29 15:01:07 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.support;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.util.StringHelper;

import cn.com.rebirth.search.core.index.field.data.FieldData;


/**
 * The Class FieldDataLoader.
 *
 * @author l.xue.nong
 */
public class FieldDataLoader {

    
    /**
     * Load.
     *
     * @param <T> the generic type
     * @param reader the reader
     * @param field the field
     * @param loader the loader
     * @return the t
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static <T extends FieldData> T load(IndexReader reader, String field, TypeLoader<T> loader) throws IOException {

        loader.init();

        field = StringHelper.intern(field);
        ArrayList<int[]> ordinals = new ArrayList<int[]>();
        int[] idx = new int[reader.maxDoc()];
        ordinals.add(new int[reader.maxDoc()]);

        int t = 1;  

        TermDocs termDocs = reader.termDocs();
        TermEnum termEnum = reader.terms(new Term(field));
        try {
            do {
                Term term = termEnum.term();
                if (term == null || term.field() != field) break;
                loader.collectTerm(term.text());
                termDocs.seek(termEnum);
                while (termDocs.next()) {
                    int doc = termDocs.doc();
                    int[] ordinal;
                    if (idx[doc] >= ordinals.size()) {
                        ordinal = new int[reader.maxDoc()];
                        ordinals.add(ordinal);
                    } else {
                        ordinal = ordinals.get(idx[doc]);
                    }
                    ordinal[doc] = t;
                    idx[doc]++;
                }
                t++;
            } while (termEnum.next());
        } catch (RuntimeException e) {
            if (e.getClass().getName().endsWith("StopFillCacheException")) {
                
            } else {
                throw e;
            }
        } finally {
            termDocs.close();
            termEnum.close();
        }

        if (ordinals.size() == 1) {
            return loader.buildSingleValue(field, ordinals.get(0));
        } else {
            int[][] nativeOrdinals = new int[ordinals.size()][];
            for (int i = 0; i < nativeOrdinals.length; i++) {
                nativeOrdinals[i] = ordinals.get(i);
            }
            return loader.buildMultiValue(field, nativeOrdinals);
        }
    }

    
    /**
     * The Interface TypeLoader.
     *
     * @param <T> the generic type
     * @author l.xue.nong
     */
    public static interface TypeLoader<T extends FieldData> {

        
        /**
         * Inits the.
         */
        void init();

        
        /**
         * Collect term.
         *
         * @param term the term
         */
        void collectTerm(String term);

        
        /**
         * Builds the single value.
         *
         * @param fieldName the field name
         * @param ordinals the ordinals
         * @return the t
         */
        T buildSingleValue(String fieldName, int[] ordinals);

        
        /**
         * Builds the multi value.
         *
         * @param fieldName the field name
         * @param ordinals the ordinals
         * @return the t
         */
        T buildMultiValue(String fieldName, int[][] ordinals);
    }

    
    /**
     * The Class FreqsTypeLoader.
     *
     * @param <T> the generic type
     * @author l.xue.nong
     */
    public static abstract class FreqsTypeLoader<T extends FieldData> implements TypeLoader<T> {

        
        /**
         * Instantiates a new freqs type loader.
         */
        protected FreqsTypeLoader() {
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.core.index.field.data.support.FieldDataLoader.TypeLoader#init()
         */
        @Override
        public void init() {
        }
    }
}
