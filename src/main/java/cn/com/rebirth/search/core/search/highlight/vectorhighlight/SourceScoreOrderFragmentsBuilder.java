/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SourceScoreOrderFragmentsBuilder.java 2012-3-29 15:01:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.highlight.vectorhighlight;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.vectorhighlight.BoundaryScanner;
import org.apache.lucene.search.vectorhighlight.ScoreOrderFragmentsBuilder;

import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.search.internal.SearchContext;
import cn.com.rebirth.search.core.search.lookup.SearchLookup;


/**
 * The Class SourceScoreOrderFragmentsBuilder.
 *
 * @author l.xue.nong
 */
public class SourceScoreOrderFragmentsBuilder extends ScoreOrderFragmentsBuilder {

	
	/** The mapper. */
	private final FieldMapper mapper;

	
	/** The search context. */
	private final SearchContext searchContext;

	
	/**
	 * Instantiates a new source score order fragments builder.
	 *
	 * @param mapper the mapper
	 * @param searchContext the search context
	 * @param preTags the pre tags
	 * @param postTags the post tags
	 * @param boundaryScanner the boundary scanner
	 */
	public SourceScoreOrderFragmentsBuilder(FieldMapper mapper, SearchContext searchContext, String[] preTags,
			String[] postTags, BoundaryScanner boundaryScanner) {
		super(preTags, postTags, boundaryScanner);
		this.mapper = mapper;
		this.searchContext = searchContext;
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.vectorhighlight.BaseFragmentsBuilder#getFields(org.apache.lucene.index.IndexReader, int, java.lang.String)
	 */
	@Override
	protected Field[] getFields(IndexReader reader, int docId, String fieldName) throws IOException {
		
		SearchLookup lookup = searchContext.lookup();
		lookup.setNextReader(reader);
		lookup.setNextDocId(docId);

		List<Object> values = lookup.source().extractRawValues(mapper.names().sourcePath());
		Field[] fields = new Field[values.size()];
		for (int i = 0; i < values.size(); i++) {
			fields[i] = new Field(mapper.names().indexName(), values.get(i).toString(), Field.Store.NO,
					Field.Index.ANALYZED);
		}
		return fields;
	}
}
