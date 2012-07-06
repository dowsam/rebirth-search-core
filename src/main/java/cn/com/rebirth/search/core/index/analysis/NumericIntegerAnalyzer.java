/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NumericIntegerAnalyzer.java 2012-7-6 14:29:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.util.NumericUtils;

import java.io.IOException;
import java.io.Reader;

/**
 * The Class NumericIntegerAnalyzer.
 *
 * @author l.xue.nong
 */
public class NumericIntegerAnalyzer extends NumericAnalyzer<NumericIntegerTokenizer> {

	/** The precision step. */
	private final int precisionStep;

	/**
	 * Instantiates a new numeric integer analyzer.
	 */
	public NumericIntegerAnalyzer() {
		this(NumericUtils.PRECISION_STEP_DEFAULT);
	}

	/**
	 * Instantiates a new numeric integer analyzer.
	 *
	 * @param precisionStep the precision step
	 */
	public NumericIntegerAnalyzer(int precisionStep) {
		this.precisionStep = precisionStep;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.NumericAnalyzer#createNumericTokenizer(java.io.Reader, char[])
	 */
	@Override
	protected NumericIntegerTokenizer createNumericTokenizer(Reader reader, char[] buffer) throws IOException {
		return new NumericIntegerTokenizer(reader, precisionStep, buffer);
	}
}
