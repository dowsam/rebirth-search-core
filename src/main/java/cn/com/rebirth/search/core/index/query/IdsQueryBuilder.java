/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IdsQueryBuilder.java 2012-7-6 14:30:22 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;

/**
 * The Class IdsQueryBuilder.
 *
 * @author l.xue.nong
 */
public class IdsQueryBuilder extends BaseQueryBuilder {

	/** The types. */
	private final List<String> types;

	/** The values. */
	private List<String> values = new ArrayList<String>();

	/** The boost. */
	private float boost = -1;

	/**
	 * Instantiates a new ids query builder.
	 *
	 * @param types the types
	 */
	public IdsQueryBuilder(String... types) {
		this.types = types == null ? null : Arrays.asList(types);
	}

	/**
	 * Adds the ids.
	 *
	 * @param ids the ids
	 * @return the ids query builder
	 */
	public IdsQueryBuilder addIds(String... ids) {
		values.addAll(Arrays.asList(ids));
		return this;
	}

	/**
	 * Ids.
	 *
	 * @param ids the ids
	 * @return the ids query builder
	 */
	public IdsQueryBuilder ids(String... ids) {
		return addIds(ids);
	}

	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the ids query builder
	 */
	public IdsQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(IdsQueryParser.NAME);
		if (types != null) {
			if (types.size() == 1) {
				builder.field("type", types.get(0));
			} else {
				builder.startArray("types");
				for (Object type : types) {
					builder.value(type);
				}
				builder.endArray();
			}
		}
		builder.startArray("values");
		for (Object value : values) {
			builder.value(value);
		}
		builder.endArray();
		if (boost != -1) {
			builder.field("boost", boost);
		}
		builder.endObject();
	}
}