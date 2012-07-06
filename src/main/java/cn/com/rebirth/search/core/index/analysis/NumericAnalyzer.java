/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NumericAnalyzer.java 2012-3-29 15:01:42 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.io.Reader;


/**
 * The Class NumericAnalyzer.
 *
 * @param <T> the generic type
 * @author l.xue.nong
 */
public abstract class NumericAnalyzer<T extends NumericTokenizer> extends Analyzer {

    /* (non-Javadoc)
     * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
     */
    @Override
    public final TokenStream tokenStream(String fieldName, Reader reader) {
        try {
            return createNumericTokenizer(reader, new char[32]);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create numeric tokenizer", e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.analysis.Analyzer#reusableTokenStream(java.lang.String, java.io.Reader)
     */
    @Override
    public final TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
        Holder holder = (Holder) getPreviousTokenStream();
        if (holder == null) {
            char[] buffer = new char[120];
            holder = new Holder(createNumericTokenizer(reader, buffer), buffer);
            setPreviousTokenStream(holder);
        } else {
            holder.tokenizer.reset(reader, holder.buffer);
        }
        return holder.tokenizer;
    }

    /**
     * Creates the numeric tokenizer.
     *
     * @param reader the reader
     * @param buffer the buffer
     * @return the t
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected abstract T createNumericTokenizer(Reader reader, char[] buffer) throws IOException;

    /**
     * The Class Holder.
     *
     * @author l.xue.nong
     */
    private static final class Holder {
        
        /** The tokenizer. */
        final NumericTokenizer tokenizer;
        
        /** The buffer. */
        final char[] buffer;

        /**
         * Instantiates a new holder.
         *
         * @param tokenizer the tokenizer
         * @param buffer the buffer
         */
        private Holder(NumericTokenizer tokenizer, char[] buffer) {
            this.tokenizer = tokenizer;
            this.buffer = buffer;
        }
    }
}
