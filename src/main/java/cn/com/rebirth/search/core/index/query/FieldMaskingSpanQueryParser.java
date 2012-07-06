/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FieldMaskingSpanQueryParser.java 2012-7-6 14:29:22 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.FieldMaskingSpanQuery;
import org.apache.lucene.search.spans.SpanQuery;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;

/**
 * The Class FieldMaskingSpanQueryParser.
 *
 * @author l.xue.nong
 */
public class FieldMaskingSpanQueryParser implements QueryParser {

	/** The Constant NAME. */
	public static final String NAME = "field_masking_span";

	/**
	 * Instantiates a new field masking span query parser.
	 */
	@Inject
	public FieldMaskingSpanQueryParser() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { NAME, Strings.toCamelCase(NAME) };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.QueryParser#parse(cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
		XContentParser parser = parseContext.parser();

		float boost = 1.0f;

		SpanQuery inner = null;
		String field = null;

		String currentFieldName = null;
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("query".equals(currentFieldName)) {
					Query query = parseContext.parseInnerQuery();
					if (!(query instanceof SpanQuery)) {
						throw new QueryParsingException(parseContext.index(),
								"[field_masking_span] query] must be of type span query");
					}
					inner = (SpanQuery) query;
				} else {
					throw new QueryParsingException(parseContext.index(),
							"[field_masking_span] query does not support [" + currentFieldName + "]");
				}
			} else {
				if ("boost".equals(currentFieldName)) {
					boost = parser.floatValue();
				} else if ("field".equals(currentFieldName)) {
					field = parser.text();
				} else {
					throw new QueryParsingException(parseContext.index(),
							"[field_masking_span] query does not support [" + currentFieldName + "]");
				}
			}
		}
		if (inner == null) {
			throw new QueryParsingException(parseContext.index(),
					"field_masking_span must have [query] span query clause");
		}
		if (field == null) {
			throw new QueryParsingException(parseContext.index(), "field_masking_span must have [field] set for it");
		}

		FieldMapper mapper = parseContext.fieldMapper(field);
		if (mapper != null) {
			field = mapper.names().indexName();
		}

		FieldMaskingSpanQuery query = new FieldMaskingSpanQuery(inner, field);
		query.setBoost(boost);
		return query;
	}
}