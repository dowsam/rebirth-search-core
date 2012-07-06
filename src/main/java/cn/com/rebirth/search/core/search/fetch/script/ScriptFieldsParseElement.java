/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ScriptFieldsParseElement.java 2012-3-29 15:00:51 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.fetch.script;

import java.util.Map;

import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class ScriptFieldsParseElement.
 *
 * @author l.xue.nong
 */
public class ScriptFieldsParseElement implements SearchParseElement {

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.SearchParseElement#parse(cn.com.summall.search.commons.xcontent.XContentParser, cn.com.summall.search.core.search.internal.SearchContext)
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
				String script = null;
				String scriptLang = null;
				Map<String, Object> params = null;
				boolean ignoreException = false;
				while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
					if (token == XContentParser.Token.FIELD_NAME) {
						currentFieldName = parser.currentName();
					} else if (token == XContentParser.Token.START_OBJECT) {
						params = parser.map();
					} else if (token.isValue()) {
						if ("script".equals(currentFieldName)) {
							script = parser.text();
						} else if ("lang".equals(currentFieldName)) {
							scriptLang = parser.text();
						} else if ("ignore_failure".equals(currentFieldName)) {
							ignoreException = parser.booleanValue();
						}
					}
				}
				SearchScript searchScript = context.scriptService()
						.search(context.lookup(), scriptLang, script, params);
				context.scriptFields().add(
						new ScriptFieldsContext.ScriptField(fieldName, searchScript, ignoreException));
			}
		}
	}
}