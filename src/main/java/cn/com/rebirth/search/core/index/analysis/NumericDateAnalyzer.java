/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NumericDateAnalyzer.java 2012-3-29 15:01:31 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.util.NumericUtils;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.Reader;


/**
 * The Class NumericDateAnalyzer.
 *
 * @author l.xue.nong
 */
public class NumericDateAnalyzer extends NumericAnalyzer<NumericDateTokenizer> {

    /** The precision step. */
    private final int precisionStep;

    /** The date time formatter. */
    private final DateTimeFormatter dateTimeFormatter;

    /**
     * Instantiates a new numeric date analyzer.
     *
     * @param dateTimeFormatter the date time formatter
     */
    public NumericDateAnalyzer(DateTimeFormatter dateTimeFormatter) {
        this(NumericUtils.PRECISION_STEP_DEFAULT, dateTimeFormatter);
    }

    /**
     * Instantiates a new numeric date analyzer.
     *
     * @param precisionStep the precision step
     * @param dateTimeFormatter the date time formatter
     */
    public NumericDateAnalyzer(int precisionStep, DateTimeFormatter dateTimeFormatter) {
        this.precisionStep = precisionStep;
        this.dateTimeFormatter = dateTimeFormatter;
    }

    /* (non-Javadoc)
     * @see cn.com.summall.search.core.index.analysis.NumericAnalyzer#createNumericTokenizer(java.io.Reader, char[])
     */
    @Override
    protected NumericDateTokenizer createNumericTokenizer(Reader reader, char[] buffer) throws IOException {
        return new NumericDateTokenizer(reader, precisionStep, buffer, dateTimeFormatter);
    }
}