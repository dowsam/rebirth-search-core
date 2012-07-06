/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CharFilterFactory.java 2012-3-29 15:02:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.CharStream;


/**
 * A factory for creating CharFilter objects.
 */
public interface CharFilterFactory {

    /**
     * Name.
     *
     * @return the string
     */
    String name();

    /**
     * Creates the.
     *
     * @param tokenStream the token stream
     * @return the char stream
     */
    CharStream create(CharStream tokenStream);
}
