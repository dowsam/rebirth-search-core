/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NumericDoubleTokenizer.java 2012-3-29 15:01:04 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.NumericTokenStream;

import java.io.IOException;
import java.io.Reader;


/**
 * The Class NumericDoubleTokenizer.
 *
 * @author l.xue.nong
 */
public class NumericDoubleTokenizer extends NumericTokenizer {

    /**
     * Instantiates a new numeric double tokenizer.
     *
     * @param reader the reader
     * @param precisionStep the precision step
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public NumericDoubleTokenizer(Reader reader, int precisionStep) throws IOException {
        super(reader, new NumericTokenStream(precisionStep), null);
    }

    /**
     * Instantiates a new numeric double tokenizer.
     *
     * @param reader the reader
     * @param precisionStep the precision step
     * @param buffer the buffer
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public NumericDoubleTokenizer(Reader reader, int precisionStep, char[] buffer) throws IOException {
        super(reader, new NumericTokenStream(precisionStep), buffer, null);
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.analysis.NumericTokenizer#setValue(org.apache.lucene.analysis.NumericTokenStream, java.lang.String)
     */
    @Override
    protected void setValue(NumericTokenStream tokenStream, String value) {
        tokenStream.setDoubleValue(Double.parseDouble(value));
    }
}