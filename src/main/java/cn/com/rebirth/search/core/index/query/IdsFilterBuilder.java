/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IdsFilterBuilder.java 2012-7-6 14:30:24 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.com.rebirth.commons.xcontent.XContentBuilder;

/**
 * The Class IdsFilterBuilder.
 *
 * @author l.xue.nong
 */
public class IdsFilterBuilder extends BaseFilterBuilder {

	/** The types. */
	private final List<String> types;

	/** The values. */
	private List<String> values = new ArrayList<String>();

	/** The filter name. */
	private String filterName;

	/**
	 * Instantiates a new ids filter builder.
	 *
	 * @param types the types
	 */
	public IdsFilterBuilder(String... types) {
		this.types = types == null ? null : Arrays.asList(types);
	}

	/**
	 * Adds the ids.
	 *
	 * @param ids the ids
	 * @return the ids filter builder
	 */
	public IdsFilterBuilder addIds(String... ids) {
		values.addAll(Arrays.asList(ids));
		return this;
	}

	/**
	 * Ids.
	 *
	 * @param ids the ids
	 * @return the ids filter builder
	 */
	public IdsFilterBuilder ids(String... ids) {
		return addIds(ids);
	}

	/**
	 * Filter name.
	 *
	 * @param filterName the filter name
	 * @return the ids filter builder
	 */
	public IdsFilterBuilder filterName(String filterName) {
		this.filterName = filterName;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.query.BaseFilterBuilder#doXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(IdsFilterParser.NAME);
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

		if (filterName != null) {
			builder.field("_name", filterName);
		}

		builder.endObject();
	}
}