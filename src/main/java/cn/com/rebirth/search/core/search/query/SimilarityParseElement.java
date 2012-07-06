/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SimilarityParseElement.java 2012-4-17 18:25:06 l.xue.nong$$
 */
package cn.com.rebirth.search.core.search.query;

import java.io.IOException;

import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.Similarity;

import cn.com.rebirth.commons.utils.ObjectToByteUtils;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * The Class SimilarityParseElement.
 *
 * @author l.xue.nong
 */
public class SimilarityParseElement implements SearchParseElement {

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.SearchParseElement#parse(cn.com.summall.search.commons.xcontent.XContentParser, cn.com.summall.search.core.search.internal.SearchContext)
	 */
	@Override
	public void parse(XContentParser parser, SearchContext context) throws Exception {
		byte[] value = parser.binaryValue();
		Similarity similarity = bulid(value);
		if (similarity != null)
			context.similarityService().putCurrentSearchSimilarity(similarity);
	}

	/**
	 * Bulid.
	 *
	 * @param value the value
	 * @return the similarity
	 */
	private Similarity bulid(byte[] value) {
		try {
			return (Similarity) ObjectToByteUtils.getObject(value);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws JsonGenerationException the json generation exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
		SimilarityParseElement element = new SimilarityParseElement();
		element.bulid(ObjectToByteUtils.getBytes(new DefaultSimilarity()));
	}

}
