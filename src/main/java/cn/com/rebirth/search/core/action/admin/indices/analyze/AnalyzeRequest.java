/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AnalyzeRequest.java 2012-3-29 15:02:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.analyze;

import java.io.IOException;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.support.single.custom.SingleCustomOperationRequest;


/**
 * The Class AnalyzeRequest.
 *
 * @author l.xue.nong
 */
public class AnalyzeRequest extends SingleCustomOperationRequest {

	
	/** The index. */
	private String index;

	
	/** The text. */
	private String text;

	
	/** The analyzer. */
	private String analyzer;

	
	/** The tokenizer. */
	private String tokenizer;

	
	/** The token filters. */
	private String[] tokenFilters;

	
	/** The field. */
	private String field;

	
	/**
	 * Instantiates a new analyze request.
	 */
	AnalyzeRequest() {

	}

	
	/**
	 * Instantiates a new analyze request.
	 *
	 * @param text the text
	 */
	public AnalyzeRequest(String text) {
		this.text = text;
	}

	
	/**
	 * Instantiates a new analyze request.
	 *
	 * @param index the index
	 * @param text the text
	 */
	public AnalyzeRequest(@Nullable String index, String text) {
		this.index = index;
		this.text = text;
	}

	
	/**
	 * Text.
	 *
	 * @return the string
	 */
	public String text() {
		return this.text;
	}

	
	/**
	 * Index.
	 *
	 * @param index the index
	 * @return the analyze request
	 */
	public AnalyzeRequest index(String index) {
		this.index = index;
		return this;
	}

	
	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		return this.index;
	}

	
	/**
	 * Analyzer.
	 *
	 * @param analyzer the analyzer
	 * @return the analyze request
	 */
	public AnalyzeRequest analyzer(String analyzer) {
		this.analyzer = analyzer;
		return this;
	}

	
	/**
	 * Analyzer.
	 *
	 * @return the string
	 */
	public String analyzer() {
		return this.analyzer;
	}

	
	/**
	 * Tokenizer.
	 *
	 * @param tokenizer the tokenizer
	 * @return the analyze request
	 */
	public AnalyzeRequest tokenizer(String tokenizer) {
		this.tokenizer = tokenizer;
		return this;
	}

	
	/**
	 * Tokenizer.
	 *
	 * @return the string
	 */
	public String tokenizer() {
		return this.tokenizer;
	}

	
	/**
	 * Token filters.
	 *
	 * @param tokenFilters the token filters
	 * @return the analyze request
	 */
	public AnalyzeRequest tokenFilters(String... tokenFilters) {
		this.tokenFilters = tokenFilters;
		return this;
	}

	
	/**
	 * Token filters.
	 *
	 * @return the string[]
	 */
	public String[] tokenFilters() {
		return this.tokenFilters;
	}

	
	/**
	 * Field.
	 *
	 * @param field the field
	 * @return the analyze request
	 */
	public AnalyzeRequest field(String field) {
		this.field = field;
		return this;
	}

	
	/**
	 * Field.
	 *
	 * @return the string
	 */
	public String field() {
		return this.field;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.single.custom.SingleCustomOperationRequest#preferLocal(boolean)
	 */
	@Override
	public AnalyzeRequest preferLocal(boolean preferLocal) {
		super.preferLocal(preferLocal);
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.single.custom.SingleCustomOperationRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = super.validate();
		if (text == null) {
			validationException = ValidateActions.addValidationError("text is missing", validationException);
		}
		return validationException;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.single.custom.SingleCustomOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		index = in.readOptionalUTF();
		text = in.readUTF();
		analyzer = in.readOptionalUTF();
		tokenizer = in.readOptionalUTF();
		int size = in.readVInt();
		if (size > 0) {
			tokenFilters = new String[size];
			for (int i = 0; i < size; i++) {
				tokenFilters[i] = in.readUTF();
			}
		}
		field = in.readOptionalUTF();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.single.custom.SingleCustomOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeOptionalUTF(index);
		out.writeUTF(text);
		out.writeOptionalUTF(analyzer);
		out.writeOptionalUTF(tokenizer);
		if (tokenFilters == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(tokenFilters.length);
			for (String tokenFilter : tokenFilters) {
				out.writeUTF(tokenFilter);
			}
		}
		out.writeOptionalUTF(field);
	}
}
