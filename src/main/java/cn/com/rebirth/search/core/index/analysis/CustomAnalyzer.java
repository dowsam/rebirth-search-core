/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CustomAnalyzer.java 2012-3-29 15:02:32 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.*;

import java.io.IOException;
import java.io.Reader;


/**
 * The Class CustomAnalyzer.
 *
 * @author l.xue.nong
 */
public final class CustomAnalyzer extends Analyzer implements PositionIncrementGapAnalyzer {

    /** The tokenizer factory. */
    private final TokenizerFactory tokenizerFactory;

    /** The char filters. */
    private final CharFilterFactory[] charFilters;

    /** The token filters. */
    private final TokenFilterFactory[] tokenFilters;

    /** The position increment gap. */
    private int positionIncrementGap = 0;

    /**
     * Instantiates a new custom analyzer.
     *
     * @param tokenizerFactory the tokenizer factory
     * @param charFilters the char filters
     * @param tokenFilters the token filters
     */
    public CustomAnalyzer(TokenizerFactory tokenizerFactory, CharFilterFactory[] charFilters, TokenFilterFactory[] tokenFilters) {
        this.tokenizerFactory = tokenizerFactory;
        this.charFilters = charFilters;
        this.tokenFilters = tokenFilters;
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.analysis.PositionIncrementGapAnalyzer#setPositionIncrementGap(int)
     */
    @Override
    public void setPositionIncrementGap(int positionIncrementGap) {
        this.positionIncrementGap = positionIncrementGap;
    }

    /**
     * Tokenizer factory.
     *
     * @return the tokenizer factory
     */
    public TokenizerFactory tokenizerFactory() {
        return tokenizerFactory;
    }

    /**
     * Token filters.
     *
     * @return the token filter factory[]
     */
    public TokenFilterFactory[] tokenFilters() {
        return tokenFilters;
    }

    /**
     * Char filters.
     *
     * @return the char filter factory[]
     */
    public CharFilterFactory[] charFilters() {
        return charFilters;
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.analysis.Analyzer#getPositionIncrementGap(java.lang.String)
     */
    @Override
    public int getPositionIncrementGap(String fieldName) {
        return this.positionIncrementGap;
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
     */
    @Override
    public final TokenStream tokenStream(String fieldName, Reader reader) {
        return buildHolder(reader).tokenStream;
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.analysis.Analyzer#reusableTokenStream(java.lang.String, java.io.Reader)
     */
    @Override
    public final TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
        Holder holder = (Holder) getPreviousTokenStream();
        if (holder == null) {
            holder = buildHolder(charFilterIfNeeded(reader));
            setPreviousTokenStream(holder);
        } else {
            holder.tokenizer.reset(charFilterIfNeeded(reader));
        }
        return holder.tokenStream;
    }

    /**
     * Builds the holder.
     *
     * @param input the input
     * @return the holder
     */
    private Holder buildHolder(Reader input) {
        Tokenizer tokenizer = tokenizerFactory.create(input);
        TokenStream tokenStream = tokenizer;
        for (TokenFilterFactory tokenFilter : tokenFilters) {
            tokenStream = tokenFilter.create(tokenStream);
        }
        return new Holder(tokenizer, tokenStream);
    }

    /**
     * Char filter if needed.
     *
     * @param reader the reader
     * @return the reader
     */
    private Reader charFilterIfNeeded(Reader reader) {
        if (charFilters != null && charFilters.length > 0) {
            CharStream charStream = CharReader.get(reader);
            for (CharFilterFactory charFilter : charFilters) {
                charStream = charFilter.create(charStream);
            }
            reader = charStream;
        }
        return reader;
    }

    /**
     * The Class Holder.
     *
     * @author l.xue.nong
     */
    static class Holder {
        
        /** The tokenizer. */
        final Tokenizer tokenizer;
        
        /** The token stream. */
        final TokenStream tokenStream;

        /**
         * Instantiates a new holder.
         *
         * @param tokenizer the tokenizer
         * @param tokenStream the token stream
         */
        private Holder(Tokenizer tokenizer, TokenStream tokenStream) {
            this.tokenizer = tokenizer;
            this.tokenStream = tokenStream;
        }
    }
}
