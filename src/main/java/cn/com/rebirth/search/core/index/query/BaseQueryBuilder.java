/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BaseQueryBuilder.java 2012-3-29 15:01:13 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.io.BytesStream;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentType;


/**
 * The Class BaseQueryBuilder.
 *
 * @author l.xue.nong
 */
public abstract class BaseQueryBuilder implements QueryBuilder {

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder.prettyPrint();
			toXContent(builder, EMPTY_PARAMS);
			return builder.string();
		} catch (Exception e) {
			throw new QueryBuilderException("Failed to build query", e);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.QueryBuilder#buildAsBytes()
	 */
	@Override
	public BytesStream buildAsBytes() throws QueryBuilderException {
		return buildAsBytes(XContentType.JSON);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.QueryBuilder#buildAsBytes(cn.com.summall.search.commons.xcontent.XContentType)
	 */
	@Override
	public BytesStream buildAsBytes(XContentType contentType) throws QueryBuilderException {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(contentType);
			toXContent(builder, EMPTY_PARAMS);
			return builder.underlyingStream();
		} catch (Exception e) {
			throw new QueryBuilderException("Failed to build query", e);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject();
		doXContent(builder, params);
		builder.endObject();
		return builder;
	}

	
	/**
	 * Do x content.
	 *
	 * @param builder the builder
	 * @param params the params
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected abstract void doXContent(XContentBuilder builder, Params params) throws IOException;
}
