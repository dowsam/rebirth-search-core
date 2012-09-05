/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SortParseElement.java 2012-7-6 14:29:19 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.sort;

import java.util.List;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.SearchParseException;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * The Class SortParseElement.
 *
 * @author l.xue.nong
 */
public class SortParseElement implements SearchParseElement {

	/** The Constant SORT_SCORE. */
	private static final SortField SORT_SCORE = new SortField(null, SortField.SCORE);

	/** The Constant SORT_SCORE_REVERSE. */
	private static final SortField SORT_SCORE_REVERSE = new SortField(null, SortField.SCORE, true);

	/** The Constant SORT_DOC. */
	private static final SortField SORT_DOC = new SortField(null, SortField.DOC);

	/** The Constant SORT_DOC_REVERSE. */
	private static final SortField SORT_DOC_REVERSE = new SortField(null, SortField.DOC, true);

	/** The Constant SCORE_FIELD_NAME. */
	public static final String SCORE_FIELD_NAME = "_score";

	/** The Constant DOC_FIELD_NAME. */
	public static final String DOC_FIELD_NAME = "_doc";

	/** The parsers. */
	private final ImmutableMap<String, SortParser> parsers;

	/**
	 * Instantiates a new sort parse element.
	 */
	public SortParseElement() {
		ImmutableMap.Builder<String, SortParser> builder = ImmutableMap.builder();
		addParser(builder, new ScriptSortParser());
		addParser(builder, new GeoDistanceSortParser());
		this.parsers = builder.build();
	}

	/**
	 * Adds the parser.
	 *
	 * @param parsers the parsers
	 * @param parser the parser
	 */
	private void addParser(ImmutableMap.Builder<String, SortParser> parsers, SortParser parser) {
		for (String name : parser.names()) {
			parsers.put(name, parser);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchParseElement#parse(cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public void parse(XContentParser parser, SearchContext context) throws Exception {
		XContentParser.Token token = parser.currentToken();
		List<SortField> sortFields = Lists.newArrayListWithCapacity(2);
		if (token == XContentParser.Token.START_ARRAY) {
			while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
				if (token == XContentParser.Token.START_OBJECT) {
					addCompoundSortField(parser, context, sortFields);
				} else if (token == XContentParser.Token.VALUE_STRING) {
					addSortField(context, sortFields, parser.text(), false, false, null);
				}
			}
		} else {
			addCompoundSortField(parser, context, sortFields);
		}
		if (!sortFields.isEmpty()) {

			boolean sort;
			if (sortFields.size() > 1) {
				sort = true;
			} else {
				SortField sortField = sortFields.get(0);
				if (sortField.getType() == SortField.SCORE && !sortField.getReverse()) {
					sort = false;
				} else {
					sort = true;
				}
			}
			if (sort) {
				context.sort(new Sort(sortFields.toArray(new SortField[sortFields.size()])));
			}
		}
	}

	/**
	 * Adds the compound sort field.
	 *
	 * @param parser the parser
	 * @param context the context
	 * @param sortFields the sort fields
	 * @throws Exception the exception
	 */
	private void addCompoundSortField(XContentParser parser, SearchContext context, List<SortField> sortFields)
			throws Exception {
		XContentParser.Token token;
		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				String fieldName = parser.currentName();
				boolean reverse = false;
				String missing = null;
				String innerJsonName = null;
				boolean ignoreUnmapped = false;
				token = parser.nextToken();
				if (token == XContentParser.Token.VALUE_STRING) {
					String direction = parser.text();
					if (direction.equals("asc")) {
						reverse = SCORE_FIELD_NAME.equals(fieldName);
					} else if (direction.equals("desc")) {
						reverse = !SCORE_FIELD_NAME.equals(fieldName);
					}
					addSortField(context, sortFields, fieldName, reverse, ignoreUnmapped, missing);
				} else {
					if (parsers.containsKey(fieldName)) {
						sortFields.add(parsers.get(fieldName).parse(parser, context));
					} else {
						while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
							if (token == XContentParser.Token.FIELD_NAME) {
								innerJsonName = parser.currentName();
							} else if (token.isValue()) {
								if ("reverse".equals(innerJsonName)) {
									reverse = parser.booleanValue();
								} else if ("order".equals(innerJsonName)) {
									if ("asc".equals(parser.text())) {
										reverse = SCORE_FIELD_NAME.equals(fieldName);
									} else if ("desc".equals(parser.text())) {
										reverse = !SCORE_FIELD_NAME.equals(fieldName);
									}
								} else if ("missing".equals(innerJsonName)) {
									missing = parser.textOrNull();
								} else if ("ignore_unmapped".equals(innerJsonName)
										|| "ignoreUnmapped".equals(innerJsonName)) {
									ignoreUnmapped = parser.booleanValue();
								}
							}
						}
						addSortField(context, sortFields, fieldName, reverse, ignoreUnmapped, missing);
					}
				}
			}
		}
	}

	/**
	 * Adds the sort field.
	 *
	 * @param context the context
	 * @param sortFields the sort fields
	 * @param fieldName the field name
	 * @param reverse the reverse
	 * @param ignoreUnmapped the ignore unmapped
	 * @param missing the missing
	 */
	private void addSortField(SearchContext context, List<SortField> sortFields, String fieldName, boolean reverse,
			boolean ignoreUnmapped, @Nullable final String missing) {
		if (SCORE_FIELD_NAME.equals(fieldName)) {
			if (reverse) {
				sortFields.add(SORT_SCORE_REVERSE);
			} else {
				sortFields.add(SORT_SCORE);
			}
		} else if (DOC_FIELD_NAME.equals(fieldName)) {
			if (reverse) {
				sortFields.add(SORT_DOC_REVERSE);
			} else {
				sortFields.add(SORT_DOC);
			}
		} else {
			FieldMapper fieldMapper = context.smartNameFieldMapper(fieldName);
			if (fieldMapper == null) {
				if (ignoreUnmapped) {
					return;
				}
				throw new SearchParseException(context, "No mapping found for [" + fieldName + "] in order to sort on");
			}
			sortFields.add(new SortField(fieldMapper.names().indexName(), fieldMapper.fieldDataType()
					.newFieldComparatorSource(context.fieldDataCache(), missing), reverse));
		}
	}
}
