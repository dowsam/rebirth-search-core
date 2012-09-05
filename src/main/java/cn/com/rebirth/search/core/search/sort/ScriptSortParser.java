/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ScriptSortParser.java 2012-7-6 14:29:39 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.sort;

import java.util.Map;

import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SortField;

import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.field.function.sort.DoubleFieldsFunctionDataComparator;
import cn.com.rebirth.search.core.index.field.function.sort.StringFieldsFunctionDataComparator;
import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.core.search.SearchParseException;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class ScriptSortParser.
 *
 * @author l.xue.nong
 */
public class ScriptSortParser implements SortParser {

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.sort.SortParser#names()
	 */
	@Override
	public String[] names() {
		return new String[] { "_script" };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.sort.SortParser#parse(cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public SortField parse(XContentParser parser, SearchContext context) throws Exception {
		String script = null;
		String scriptLang = null;
		String type = null;
		Map<String, Object> params = null;
		boolean reverse = false;

		XContentParser.Token token;
		String currentName = parser.currentName();
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				currentName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("params".equals(currentName)) {
					params = parser.map();
				}
			} else if (token.isValue()) {
				if ("reverse".equals(currentName)) {
					reverse = parser.booleanValue();
				} else if ("order".equals(currentName)) {
					reverse = "desc".equals(parser.text());
				} else if ("script".equals(currentName)) {
					script = parser.text();
				} else if ("type".equals(currentName)) {
					type = parser.text();
				} else if ("lang".equals(currentName)) {
					scriptLang = parser.text();
				}
			}
		}

		if (script == null) {
			throw new SearchParseException(context, "_script sorting requires setting the script to sort by");
		}
		if (type == null) {
			throw new SearchParseException(context, "_script sorting requires setting the type of the script");
		}
		SearchScript searchScript = context.scriptService().search(context.lookup(), scriptLang, script, params);
		FieldComparatorSource fieldComparatorSource;
		if ("string".equals(type)) {
			fieldComparatorSource = StringFieldsFunctionDataComparator.comparatorSource(searchScript);
		} else if ("number".equals(type)) {
			fieldComparatorSource = DoubleFieldsFunctionDataComparator.comparatorSource(searchScript);
		} else {
			throw new SearchParseException(context, "custom script sort type [" + type + "] not supported");
		}
		return new SortField("_script", fieldComparatorSource, reverse);
	}
}
