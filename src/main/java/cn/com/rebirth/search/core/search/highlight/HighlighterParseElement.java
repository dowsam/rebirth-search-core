/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core HighlighterParseElement.java 2012-7-6 14:28:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.highlight;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import cn.com.rebirth.search.commons.lucene.search.vectorhighlight.SimpleBoundaryScanner2;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.SearchParseException;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.Lists;

/**
 * The Class HighlighterParseElement.
 *
 * @author l.xue.nong
 */
public class HighlighterParseElement implements SearchParseElement {

	/** The Constant DEFAULT_PRE_TAGS. */
	private static final String[] DEFAULT_PRE_TAGS = new String[] { "<em>" };

	/** The Constant DEFAULT_POST_TAGS. */
	private static final String[] DEFAULT_POST_TAGS = new String[] { "</em>" };

	/** The Constant STYLED_PRE_TAG. */
	private static final String[] STYLED_PRE_TAG = { "<em class=\"hlt1\">", "<em class=\"hlt2\">",
			"<em class=\"hlt3\">", "<em class=\"hlt4\">", "<em class=\"hlt5\">", "<em class=\"hlt6\">",
			"<em class=\"hlt7\">", "<em class=\"hlt8\">", "<em class=\"hlt9\">", "<em class=\"hlt10\">" };

	/** The Constant STYLED_POST_TAGS. */
	private static final String[] STYLED_POST_TAGS = { "</em>" };

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchParseElement#parse(cn.com.rebirth.search.commons.xcontent.XContentParser, cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public void parse(XContentParser parser, SearchContext context) throws Exception {
		XContentParser.Token token;
		String topLevelFieldName = null;
		List<SearchContextHighlight.Field> fields = newArrayList();

		String[] globalPreTags = DEFAULT_PRE_TAGS;
		String[] globalPostTags = DEFAULT_POST_TAGS;
		boolean globalScoreOrdered = false;
		boolean globalHighlightFilter = false;
		boolean globalRequireFieldMatch = false;
		int globalFragmentSize = 100;
		int globalNumOfFragments = 5;
		String globalEncoder = "default";
		int globalBoundaryMaxScan = SimpleBoundaryScanner2.DEFAULT_MAX_SCAN;
		char[] globalBoundaryChars = SimpleBoundaryScanner2.DEFAULT_BOUNDARY_CHARS;

		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				topLevelFieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_ARRAY) {
				if ("pre_tags".equals(topLevelFieldName) || "preTags".equals(topLevelFieldName)) {
					List<String> preTagsList = Lists.newArrayList();
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						preTagsList.add(parser.text());
					}
					globalPreTags = preTagsList.toArray(new String[preTagsList.size()]);
				} else if ("post_tags".equals(topLevelFieldName) || "postTags".equals(topLevelFieldName)) {
					List<String> postTagsList = Lists.newArrayList();
					while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
						postTagsList.add(parser.text());
					}
					globalPostTags = postTagsList.toArray(new String[postTagsList.size()]);
				}
			} else if (token.isValue()) {
				if ("order".equals(topLevelFieldName)) {
					globalScoreOrdered = "score".equals(parser.text());
				} else if ("tags_schema".equals(topLevelFieldName) || "tagsSchema".equals(topLevelFieldName)) {
					String schema = parser.text();
					if ("styled".equals(schema)) {
						globalPreTags = STYLED_PRE_TAG;
						globalPostTags = STYLED_POST_TAGS;
					}
				} else if ("highlight_filter".equals(topLevelFieldName) || "highlightFilter".equals(topLevelFieldName)) {
					globalHighlightFilter = parser.booleanValue();
				} else if ("fragment_size".equals(topLevelFieldName) || "fragmentSize".equals(topLevelFieldName)) {
					globalFragmentSize = parser.intValue();
				} else if ("number_of_fragments".equals(topLevelFieldName)
						|| "numberOfFragments".equals(topLevelFieldName)) {
					globalNumOfFragments = parser.intValue();
				} else if ("encoder".equals(topLevelFieldName)) {
					globalEncoder = parser.text();
				} else if ("require_field_match".equals(topLevelFieldName)
						|| "requireFieldMatch".equals(topLevelFieldName)) {
					globalRequireFieldMatch = parser.booleanValue();
				} else if ("boundary_max_scan".equals(topLevelFieldName) || "boundaryMaxScan".equals(topLevelFieldName)) {
					globalBoundaryMaxScan = parser.intValue();
				} else if ("boundary_chars".equals(topLevelFieldName) || "boundaryChars".equals(topLevelFieldName)) {
					globalBoundaryChars = parser.text().toCharArray();
				}
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("fields".equals(topLevelFieldName)) {
					String highlightFieldName = null;
					while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
						if (token == XContentParser.Token.FIELD_NAME) {
							highlightFieldName = parser.currentName();
						} else if (token == XContentParser.Token.START_OBJECT) {
							SearchContextHighlight.Field field = new SearchContextHighlight.Field(highlightFieldName);
							String fieldName = null;
							while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
								if (token == XContentParser.Token.FIELD_NAME) {
									fieldName = parser.currentName();
								} else if (token == XContentParser.Token.START_ARRAY) {
									if ("pre_tags".equals(fieldName) || "preTags".equals(fieldName)) {
										List<String> preTagsList = Lists.newArrayList();
										while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
											preTagsList.add(parser.text());
										}
										field.preTags(preTagsList.toArray(new String[preTagsList.size()]));
									} else if ("post_tags".equals(fieldName) || "postTags".equals(fieldName)) {
										List<String> postTagsList = Lists.newArrayList();
										while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
											postTagsList.add(parser.text());
										}
										field.postTags(postTagsList.toArray(new String[postTagsList.size()]));
									}
								} else if (token.isValue()) {
									if ("fragment_size".equals(fieldName) || "fragmentSize".equals(fieldName)) {
										field.fragmentCharSize(parser.intValue());
									} else if ("number_of_fragments".equals(fieldName)
											|| "numberOfFragments".equals(fieldName)) {
										field.numberOfFragments(parser.intValue());
									} else if ("fragment_offset".equals(fieldName)
											|| "fragmentOffset".equals(fieldName)) {
										field.fragmentOffset(parser.intValue());
									} else if ("highlight_filter".equals(fieldName)
											|| "highlightFilter".equals(fieldName)) {
										field.highlightFilter(parser.booleanValue());
									} else if ("order".equals(fieldName)) {
										field.scoreOrdered("score".equals(parser.text()));
									} else if ("require_field_match".equals(fieldName)
											|| "requireFieldMatch".equals(fieldName)) {
										field.requireFieldMatch(parser.booleanValue());
									} else if ("boundary_max_scan".equals(topLevelFieldName)
											|| "boundaryMaxScan".equals(topLevelFieldName)) {
										field.boundaryMaxScan(parser.intValue());
									} else if ("boundary_chars".equals(topLevelFieldName)
											|| "boundaryChars".equals(topLevelFieldName)) {
										field.boundaryChars(parser.text().toCharArray());
									}
								}
							}
							fields.add(field);
						}
					}
				}
			}
		}
		if (globalPreTags != null && globalPostTags == null) {
			throw new SearchParseException(context,
					"Highlighter global preTags are set, but global postTags are not set");
		}

		for (SearchContextHighlight.Field field : fields) {
			if (field.preTags() == null) {
				field.preTags(globalPreTags);
			}
			if (field.postTags() == null) {
				field.postTags(globalPostTags);
			}
			if (field.highlightFilter() == null) {
				field.highlightFilter(globalHighlightFilter);
			}
			if (field.scoreOrdered() == null) {
				field.scoreOrdered(globalScoreOrdered);
			}
			if (field.fragmentCharSize() == -1) {
				field.fragmentCharSize(globalFragmentSize);
			}
			if (field.numberOfFragments() == -1) {
				field.numberOfFragments(globalNumOfFragments);
			}
			if (field.encoder() == null) {
				field.encoder(globalEncoder);
			}
			if (field.requireFieldMatch() == null) {
				field.requireFieldMatch(globalRequireFieldMatch);
			}
			if (field.boundaryMaxScan() == -1) {
				field.boundaryMaxScan(globalBoundaryMaxScan);
			}
			if (field.boundaryChars() == null) {
				field.boundaryChars(globalBoundaryChars);
			}
		}

		context.highlight(new SearchContextHighlight(fields));
	}
}
