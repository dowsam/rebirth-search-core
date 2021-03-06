/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NumericLongAnalyzer.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.util.NumericUtils;

import java.io.IOException;
import java.io.Reader;

/**
 * The Class NumericLongAnalyzer.
 *
 * @author l.xue.nong
 */
public class NumericLongAnalyzer extends NumericAnalyzer<NumericLongTokenizer> {

	/** The precision step. */
	private final int precisionStep;

	/**
	 * Instantiates a new numeric long analyzer.
	 */
	public NumericLongAnalyzer() {
		this(NumericUtils.PRECISION_STEP_DEFAULT);
	}

	/**
	 * Instantiates a new numeric long analyzer.
	 *
	 * @param precisionStep the precision step
	 */
	public NumericLongAnalyzer(int precisionStep) {
		this.precisionStep = precisionStep;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.NumericAnalyzer#createNumericTokenizer(java.io.Reader, char[])
	 */
	@Override
	protected NumericLongTokenizer createNumericTokenizer(Reader reader, char[] buffer) throws IOException {
		return new NumericLongTokenizer(reader, precisionStep, buffer);
	}
}