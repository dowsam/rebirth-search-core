/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NumericFloatTokenizer.java 2012-7-6 14:29:10 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.NumericTokenStream;

import java.io.IOException;
import java.io.Reader;

/**
 * The Class NumericFloatTokenizer.
 *
 * @author l.xue.nong
 */
public class NumericFloatTokenizer extends NumericTokenizer {

	/**
	 * Instantiates a new numeric float tokenizer.
	 *
	 * @param reader the reader
	 * @param precisionStep the precision step
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NumericFloatTokenizer(Reader reader, int precisionStep) throws IOException {
		super(reader, new NumericTokenStream(precisionStep), null);
	}

	/**
	 * Instantiates a new numeric float tokenizer.
	 *
	 * @param reader the reader
	 * @param precisionStep the precision step
	 * @param buffer the buffer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NumericFloatTokenizer(Reader reader, int precisionStep, char[] buffer) throws IOException {
		super(reader, new NumericTokenStream(precisionStep), buffer, null);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.NumericTokenizer#setValue(org.apache.lucene.analysis.NumericTokenStream, java.lang.String)
	 */
	@Override
	protected void setValue(NumericTokenStream tokenStream, String value) {
		tokenStream.setFloatValue(Float.parseFloat(value));
	}
}