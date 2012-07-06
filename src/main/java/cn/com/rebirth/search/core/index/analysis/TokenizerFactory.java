/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TokenizerFactory.java 2012-3-29 15:01:44 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.Tokenizer;

import java.io.Reader;


/**
 * A factory for creating Tokenizer objects.
 */
public interface TokenizerFactory {

    /**
     * Name.
     *
     * @return the string
     */
    String name();

    /**
     * Creates the.
     *
     * @param reader the reader
     * @return the tokenizer
     */
    Tokenizer create(Reader reader);
}
