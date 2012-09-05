/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PartialFieldsParseElement.java 2012-7-6 14:30:24 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.fetch.partial;

import java.util.ArrayList;
import java.util.List;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class PartialFieldsParseElement.
 *
 * @author l.xue.nong
 */
public class PartialFieldsParseElement implements SearchParseElement {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchParseElement#parse(cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public void parse(XContentParser parser, SearchContext context) throws Exception {
		XContentParser.Token token;
		String currentFieldName = null;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				String fieldName = currentFieldName;
				List<String> includes = null;
				List<String> excludes = null;
				while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
					if (token == XContentParser.Token.FIELD_NAME) {
						currentFieldName = parser.currentName();
					} else if (token == XContentParser.Token.START_ARRAY) {
						if ("includes".equals(currentFieldName) || "include".equals(currentFieldName)) {
							if (includes == null) {
								includes = new ArrayList<String>(2);
							}
							while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
								includes.add(parser.text());
							}
						} else if ("excludes".equals(currentFieldName) || "exclude".equals(currentFieldName)) {
							if (excludes == null) {
								excludes = new ArrayList<String>(2);
							}
							while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
								excludes.add(parser.text());
							}
						}
					} else if (token.isValue()) {
						if ("include".equals(currentFieldName)) {
							if (includes == null) {
								includes = new ArrayList<String>(2);
							}
							includes.add(parser.text());
						} else if ("exclude".equals(currentFieldName)) {
							if (excludes == null) {
								excludes = new ArrayList<String>(2);
							}
							excludes.add(parser.text());
						}
					}
				}
				PartialFieldsContext.PartialField field = new PartialFieldsContext.PartialField(fieldName,
						includes == null ? Strings.EMPTY_ARRAY : includes.toArray(new String[includes.size()]),
						excludes == null ? Strings.EMPTY_ARRAY : excludes.toArray(new String[excludes.size()]));
				context.partialFields().add(field);
			}
		}
	}
}