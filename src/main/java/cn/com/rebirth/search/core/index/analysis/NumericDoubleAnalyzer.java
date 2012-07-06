/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NumericDoubleAnalyzer.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.util.NumericUtils;

import java.io.IOException;
import java.io.Reader;

/**
 * The Class NumericDoubleAnalyzer.
 *
 * @author l.xue.nong
 */
public class NumericDoubleAnalyzer extends NumericAnalyzer<NumericDoubleTokenizer> {

	/** The precision step. */
	private final int precisionStep;

	/**
	 * Instantiates a new numeric double analyzer.
	 */
	public NumericDoubleAnalyzer() {
		this(NumericUtils.PRECISION_STEP_DEFAULT);
	}

	/**
	 * Instantiates a new numeric double analyzer.
	 *
	 * @param precisionStep the precision step
	 */
	public NumericDoubleAnalyzer(int precisionStep) {
		this.precisionStep = precisionStep;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.NumericAnalyzer#createNumericTokenizer(java.io.Reader, char[])
	 */
	@Override
	protected NumericDoubleTokenizer createNumericTokenizer(Reader reader, char[] buffer) throws IOException {
		return new NumericDoubleTokenizer(reader, precisionStep, buffer);
	}
}