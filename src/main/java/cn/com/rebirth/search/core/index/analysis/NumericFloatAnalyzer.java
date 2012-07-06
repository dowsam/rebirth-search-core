/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NumericFloatAnalyzer.java 2012-7-6 14:30:18 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.util.NumericUtils;

import java.io.IOException;
import java.io.Reader;

/**
 * The Class NumericFloatAnalyzer.
 *
 * @author l.xue.nong
 */
public class NumericFloatAnalyzer extends NumericAnalyzer<NumericFloatTokenizer> {

	/** The precision step. */
	private final int precisionStep;

	/**
	 * Instantiates a new numeric float analyzer.
	 */
	public NumericFloatAnalyzer() {
		this(NumericUtils.PRECISION_STEP_DEFAULT);
	}

	/**
	 * Instantiates a new numeric float analyzer.
	 *
	 * @param precisionStep the precision step
	 */
	public NumericFloatAnalyzer(int precisionStep) {
		this.precisionStep = precisionStep;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.NumericAnalyzer#createNumericTokenizer(java.io.Reader, char[])
	 */
	@Override
	protected NumericFloatTokenizer createNumericTokenizer(Reader reader, char[] buffer) throws IOException {
		return new NumericFloatTokenizer(reader, precisionStep, buffer);
	}
}