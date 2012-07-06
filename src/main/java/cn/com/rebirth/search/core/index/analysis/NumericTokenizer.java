/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NumericTokenizer.java 2012-7-6 14:28:53 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.analysis;

import org.apache.lucene.analysis.NumericTokenStream;
import org.apache.lucene.analysis.Tokenizer;

import java.io.IOException;
import java.io.Reader;

/**
 * The Class NumericTokenizer.
 *
 * @author l.xue.nong
 */
public abstract class NumericTokenizer extends Tokenizer {

	/** The numeric token stream. */
	private final NumericTokenStream numericTokenStream;

	/** The extra. */
	protected final Object extra;

	/**
	 * Instantiates a new numeric tokenizer.
	 *
	 * @param reader the reader
	 * @param numericTokenStream the numeric token stream
	 * @param extra the extra
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected NumericTokenizer(Reader reader, NumericTokenStream numericTokenStream, Object extra) throws IOException {
		super(numericTokenStream);
		this.numericTokenStream = numericTokenStream;
		this.extra = extra;
		reset(reader);
	}

	/**
	 * Instantiates a new numeric tokenizer.
	 *
	 * @param reader the reader
	 * @param numericTokenStream the numeric token stream
	 * @param buffer the buffer
	 * @param extra the extra
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected NumericTokenizer(Reader reader, NumericTokenStream numericTokenStream, char[] buffer, Object extra)
			throws IOException {
		super(numericTokenStream);
		this.numericTokenStream = numericTokenStream;
		this.extra = extra;
		reset(reader, buffer);
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.Tokenizer#reset(java.io.Reader)
	 */
	@Override
	public void reset(Reader input) throws IOException {
		char[] buffer = new char[32];
		reset(input, buffer);
	}

	/**
	 * Reset.
	 *
	 * @param input the input
	 * @param buffer the buffer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void reset(Reader input, char[] buffer) throws IOException {
		super.reset(input);
		int len = input.read(buffer);
		String value = new String(buffer, 0, len);
		setValue(numericTokenStream, value);
		numericTokenStream.reset();
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.TokenStream#incrementToken()
	 */
	@Override
	public final boolean incrementToken() throws IOException {
		return numericTokenStream.incrementToken();
	}

	/**
	 * Sets the value.
	 *
	 * @param tokenStream the token stream
	 * @param value the value
	 */
	protected abstract void setValue(NumericTokenStream tokenStream, String value);
}
