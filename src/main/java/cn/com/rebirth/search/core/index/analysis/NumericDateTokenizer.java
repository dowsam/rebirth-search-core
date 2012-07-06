/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NumericDateTokenizer.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.NumericTokenStream;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.Reader;

/**
 * The Class NumericDateTokenizer.
 *
 * @author l.xue.nong
 */
public class NumericDateTokenizer extends NumericTokenizer {

	/**
	 * Instantiates a new numeric date tokenizer.
	 *
	 * @param reader the reader
	 * @param precisionStep the precision step
	 * @param dateTimeFormatter the date time formatter
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NumericDateTokenizer(Reader reader, int precisionStep, DateTimeFormatter dateTimeFormatter)
			throws IOException {
		super(reader, new NumericTokenStream(precisionStep), dateTimeFormatter);
	}

	/**
	 * Instantiates a new numeric date tokenizer.
	 *
	 * @param reader the reader
	 * @param precisionStep the precision step
	 * @param buffer the buffer
	 * @param dateTimeFormatter the date time formatter
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public NumericDateTokenizer(Reader reader, int precisionStep, char[] buffer, DateTimeFormatter dateTimeFormatter)
			throws IOException {
		super(reader, new NumericTokenStream(precisionStep), buffer, dateTimeFormatter);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.analysis.NumericTokenizer#setValue(org.apache.lucene.analysis.NumericTokenStream, java.lang.String)
	 */
	@Override
	protected void setValue(NumericTokenStream tokenStream, String value) {
		tokenStream.setLongValue(((DateTimeFormatter) extra).parseMillis(value));
	}
}