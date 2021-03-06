/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FieldsParseElement.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.fetch;

import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.fetch.script.ScriptFieldsContext;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class FieldsParseElement.
 *
 * @author l.xue.nong
 */
public class FieldsParseElement implements SearchParseElement {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchParseElement#parse(cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public void parse(XContentParser parser, SearchContext context) throws Exception {
		XContentParser.Token token = parser.currentToken();
		if (token == XContentParser.Token.START_ARRAY) {
			boolean added = false;
			while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
				String name = parser.text();
				if (name.contains("_source.") || name.contains("doc[")) {

					SearchScript searchScript = context.scriptService().search(context.lookup(), "mvel", name, null);
					context.scriptFields().add(new ScriptFieldsContext.ScriptField(name, searchScript, true));
				} else {
					added = true;
					context.fieldNames().add(name);
				}
			}
			if (!added) {
				context.emptyFieldNames();
			}
		} else if (token == XContentParser.Token.VALUE_STRING) {
			String name = parser.text();
			if (name.contains("_source.") || name.contains("doc[")) {

				SearchScript searchScript = context.scriptService().search(context.lookup(), "mvel", name, null);
				context.scriptFields().add(new ScriptFieldsContext.ScriptField(name, searchScript, true));
			} else {
				context.fieldNames().add(name);
			}
		}
	}
}
