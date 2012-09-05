/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SearchSourceBuilder.java 2012-7-6 14:29:11 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.builder;

import gnu.trove.iterator.TObjectFloatIterator;
import gnu.trove.map.hash.TObjectFloatHashMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Similarity;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Unicode;
import cn.com.rebirth.commons.io.BytesStream;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.commons.utils.ExceptionUtils;
import cn.com.rebirth.commons.utils.ObjectToByteUtils;
import cn.com.rebirth.commons.xcontent.ToXContent;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentFactory;
import cn.com.rebirth.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.RestartGenerationException;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.index.query.FilterBuilder;
import cn.com.rebirth.search.core.index.query.QueryBuilder;
import cn.com.rebirth.search.core.search.facet.AbstractFacetBuilder;
import cn.com.rebirth.search.core.search.highlight.HighlightBuilder;
import cn.com.rebirth.search.core.search.sort.SortBuilder;
import cn.com.rebirth.search.core.search.sort.SortBuilders;
import cn.com.rebirth.search.core.search.sort.SortOrder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * The Class SearchSourceBuilder.
 *
 * @author l.xue.nong
 */
public class SearchSourceBuilder implements ToXContent {

	/**
	 * Search source.
	 *
	 * @return the search source builder
	 */
	public static SearchSourceBuilder searchSource() {
		return new SearchSourceBuilder();
	}

	/**
	 * Highlight.
	 *
	 * @return the highlight builder
	 */
	public static HighlightBuilder highlight() {
		return new HighlightBuilder();
	}

	/** The query builder. */
	private QueryBuilder queryBuilder;

	/** The query binary. */
	private byte[] queryBinary;

	/** The query binary offset. */
	private int queryBinaryOffset;

	/** The query binary length. */
	private int queryBinaryLength;

	/** The filter builder. */
	private FilterBuilder filterBuilder;

	/** The filter binary. */
	private byte[] filterBinary;

	/** The filter binary offset. */
	private int filterBinaryOffset;

	/** The filter binary length. */
	private int filterBinaryLength;

	/** The from. */
	private int from = -1;

	/** The size. */
	private int size = -1;

	/** The explain. */
	private Boolean explain;

	/** The version. */
	private Boolean version;

	/** The sorts. */
	private List<SortBuilder> sorts;

	/** The track scores. */
	private boolean trackScores = false;

	/** The min score. */
	private Float minScore;

	/** The timeout in millis. */
	private long timeoutInMillis = -1;

	/** The field names. */
	private List<String> fieldNames;

	/** The script fields. */
	private List<ScriptField> scriptFields;

	/** The partial fields. */
	private List<PartialField> partialFields;

	/** The facets. */
	private List<AbstractFacetBuilder> facets;

	/** The facets binary. */
	private byte[] facetsBinary;

	/** The facet binary offset. */
	private int facetBinaryOffset;

	/** The facet binary length. */
	private int facetBinaryLength;

	/** The highlight builder. */
	private HighlightBuilder highlightBuilder;

	/** The index boost. */
	private TObjectFloatHashMap<String> indexBoost = null;

	/** The stats. */
	private String[] stats;

	/** The similarity. */
	private Similarity similarity;

	/**
	 * Instantiates a new search source builder.
	 */
	public SearchSourceBuilder() {
	}

	/**
	 * Query.
	 *
	 * @param query the query
	 * @return the search source builder
	 */
	public SearchSourceBuilder query(QueryBuilder query) {
		this.queryBuilder = query;
		return this;
	}

	/**
	 * Query.
	 *
	 * @param queryBinary the query binary
	 * @return the search source builder
	 */
	public SearchSourceBuilder query(byte[] queryBinary) {
		return query(queryBinary, 0, queryBinary.length);
	}

	/**
	 * Query.
	 *
	 * @param queryBinary the query binary
	 * @param queryBinaryOffset the query binary offset
	 * @param queryBinaryLength the query binary length
	 * @return the search source builder
	 */
	public SearchSourceBuilder query(byte[] queryBinary, int queryBinaryOffset, int queryBinaryLength) {
		this.queryBinary = queryBinary;
		this.queryBinaryOffset = queryBinaryOffset;
		this.queryBinaryLength = queryBinaryLength;
		return this;
	}

	/**
	 * Query.
	 *
	 * @param queryString the query string
	 * @return the search source builder
	 */
	public SearchSourceBuilder query(String queryString) {
		return query(Unicode.fromStringAsBytes(queryString));
	}

	/**
	 * Query.
	 *
	 * @param query the query
	 * @return the search source builder
	 */
	public SearchSourceBuilder query(XContentBuilder query) {
		try {
			return query(query.underlyingBytes(), 0, query.underlyingBytesLength());
		} catch (IOException e) {
			throw new RestartGenerationException("failed to generate query from builder", e);
		}
	}

	/**
	 * Query.
	 *
	 * @param query the query
	 * @return the search source builder
	 */
	public SearchSourceBuilder query(Map query) {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(Requests.CONTENT_TYPE);
			builder.map(query);
			return query(builder);
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + query + "]", e);
		}
	}

	/**
	 * Filter.
	 *
	 * @param filter the filter
	 * @return the search source builder
	 */
	public SearchSourceBuilder filter(FilterBuilder filter) {
		this.filterBuilder = filter;
		return this;
	}

	/**
	 * Filter.
	 *
	 * @param filterString the filter string
	 * @return the search source builder
	 */
	public SearchSourceBuilder filter(String filterString) {
		return filter(Unicode.fromStringAsBytes(filterString));
	}

	/**
	 * Filter.
	 *
	 * @param filter the filter
	 * @return the search source builder
	 */
	public SearchSourceBuilder filter(byte[] filter) {
		return filter(filter, 0, filter.length);
	}

	/**
	 * Filter.
	 *
	 * @param filterBinary the filter binary
	 * @param filterBinaryOffset the filter binary offset
	 * @param filterBinaryLength the filter binary length
	 * @return the search source builder
	 */
	public SearchSourceBuilder filter(byte[] filterBinary, int filterBinaryOffset, int filterBinaryLength) {
		this.filterBinary = filterBinary;
		this.filterBinaryOffset = filterBinaryOffset;
		this.filterBinaryLength = filterBinaryLength;
		return this;
	}

	/**
	 * Filter.
	 *
	 * @param filter the filter
	 * @return the search source builder
	 */
	public SearchSourceBuilder filter(XContentBuilder filter) {
		try {
			return filter(filter.underlyingBytes(), 0, filter.underlyingBytesLength());
		} catch (IOException e) {
			throw new RestartGenerationException("failed to generate filter from builder", e);
		}
	}

	/**
	 * Filter.
	 *
	 * @param filter the filter
	 * @return the search source builder
	 */
	public SearchSourceBuilder filter(Map filter) {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(Requests.CONTENT_TYPE);
			builder.map(filter);
			return filter(builder);
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + filter + "]", e);
		}
	}

	/**
	 * From.
	 *
	 * @param from the from
	 * @return the search source builder
	 */
	public SearchSourceBuilder from(int from) {
		this.from = from;
		return this;
	}

	/**
	 * Size.
	 *
	 * @param size the size
	 * @return the search source builder
	 */
	public SearchSourceBuilder size(int size) {
		this.size = size;
		return this;
	}

	/**
	 * Min score.
	 *
	 * @param minScore the min score
	 * @return the search source builder
	 */
	public SearchSourceBuilder minScore(float minScore) {
		this.minScore = minScore;
		return this;
	}

	/**
	 * Explain.
	 *
	 * @param explain the explain
	 * @return the search source builder
	 */
	public SearchSourceBuilder explain(Boolean explain) {
		this.explain = explain;
		return this;
	}

	/**
	 * Version.
	 *
	 * @param version the version
	 * @return the search source builder
	 */
	public SearchSourceBuilder version(Boolean version) {
		this.version = version;
		return this;
	}

	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the search source builder
	 */
	public SearchSourceBuilder timeout(TimeValue timeout) {
		this.timeoutInMillis = timeout.millis();
		return this;
	}

	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the search source builder
	 */
	public SearchSourceBuilder timeout(String timeout) {
		this.timeoutInMillis = TimeValue.parseTimeValue(timeout, null).millis();
		return this;
	}

	/**
	 * Sort.
	 *
	 * @param name the name
	 * @param order the order
	 * @return the search source builder
	 */
	public SearchSourceBuilder sort(String name, SortOrder order) {
		return sort(SortBuilders.fieldSort(name).order(order));
	}

	/**
	 * Sort.
	 *
	 * @param name the name
	 * @return the search source builder
	 */
	public SearchSourceBuilder sort(String name) {
		return sort(SortBuilders.fieldSort(name));
	}

	/**
	 * Sort.
	 *
	 * @param sort the sort
	 * @return the search source builder
	 */
	public SearchSourceBuilder sort(SortBuilder sort) {
		if (sorts == null) {
			sorts = Lists.newArrayList();
		}
		sorts.add(sort);
		return this;
	}

	/**
	 * Track scores.
	 *
	 * @param trackScores the track scores
	 * @return the search source builder
	 */
	public SearchSourceBuilder trackScores(boolean trackScores) {
		this.trackScores = trackScores;
		return this;
	}

	/**
	 * Facet.
	 *
	 * @param facet the facet
	 * @return the search source builder
	 */
	public SearchSourceBuilder facet(AbstractFacetBuilder facet) {
		if (facets == null) {
			facets = Lists.newArrayList();
		}
		facets.add(facet);
		return this;
	}

	/**
	 * Facets.
	 *
	 * @param facetsBinary the facets binary
	 * @return the search source builder
	 */
	public SearchSourceBuilder facets(byte[] facetsBinary) {
		return facets(facetsBinary, 0, facetsBinary.length);
	}

	/**
	 * Facets.
	 *
	 * @param facetsBinary the facets binary
	 * @param facetBinaryOffset the facet binary offset
	 * @param facetBinaryLength the facet binary length
	 * @return the search source builder
	 */
	public SearchSourceBuilder facets(byte[] facetsBinary, int facetBinaryOffset, int facetBinaryLength) {
		this.facetsBinary = facetsBinary;
		this.facetBinaryOffset = facetBinaryOffset;
		this.facetBinaryLength = facetBinaryLength;
		return this;
	}

	/**
	 * Facets.
	 *
	 * @param facets the facets
	 * @return the search source builder
	 */
	public SearchSourceBuilder facets(XContentBuilder facets) {
		try {
			return facets(facets.underlyingBytes(), 0, facets.underlyingBytesLength());
		} catch (IOException e) {
			throw new RestartGenerationException("failed to generate filter from builder", e);
		}
	}

	/**
	 * Facets.
	 *
	 * @param facets the facets
	 * @return the search source builder
	 */
	public SearchSourceBuilder facets(Map facets) {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(Requests.CONTENT_TYPE);
			builder.map(facets);
			return facets(builder);
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + facets + "]", e);
		}
	}

	/**
	 * Highlighter.
	 *
	 * @return the highlight builder
	 */
	public HighlightBuilder highlighter() {
		if (highlightBuilder == null) {
			highlightBuilder = new HighlightBuilder();
		}
		return highlightBuilder;
	}

	/**
	 * Highlight.
	 *
	 * @param highlightBuilder the highlight builder
	 * @return the search source builder
	 */
	public SearchSourceBuilder highlight(HighlightBuilder highlightBuilder) {
		this.highlightBuilder = highlightBuilder;
		return this;
	}

	/**
	 * No fields.
	 *
	 * @return the search source builder
	 */
	public SearchSourceBuilder noFields() {
		this.fieldNames = ImmutableList.of();
		return this;
	}

	/**
	 * Fields.
	 *
	 * @param fields the fields
	 * @return the search source builder
	 */
	public SearchSourceBuilder fields(List<String> fields) {
		this.fieldNames = fields;
		return this;
	}

	/**
	 * Fields.
	 *
	 * @param fields the fields
	 * @return the search source builder
	 */
	public SearchSourceBuilder fields(String... fields) {
		if (fieldNames == null) {
			fieldNames = new ArrayList<String>();
		}
		for (String field : fields) {
			fieldNames.add(field);
		}
		return this;
	}

	/**
	 * Field.
	 *
	 * @param name the name
	 * @return the search source builder
	 */
	public SearchSourceBuilder field(String name) {
		if (fieldNames == null) {
			fieldNames = new ArrayList<String>();
		}
		fieldNames.add(name);
		return this;
	}

	/**
	 * Script field.
	 *
	 * @param name the name
	 * @param script the script
	 * @return the search source builder
	 */
	public SearchSourceBuilder scriptField(String name, String script) {
		return scriptField(name, null, script, null);
	}

	/**
	 * Script field.
	 *
	 * @param name the name
	 * @param script the script
	 * @param params the params
	 * @return the search source builder
	 */
	public SearchSourceBuilder scriptField(String name, String script, Map<String, Object> params) {
		return scriptField(name, null, script, params);
	}

	/**
	 * Script field.
	 *
	 * @param name the name
	 * @param lang the lang
	 * @param script the script
	 * @param params the params
	 * @return the search source builder
	 */
	public SearchSourceBuilder scriptField(String name, String lang, String script, Map<String, Object> params) {
		if (scriptFields == null) {
			scriptFields = Lists.newArrayList();
		}
		scriptFields.add(new ScriptField(name, lang, script, params));
		return this;
	}

	/**
	 * Partial field.
	 *
	 * @param name the name
	 * @param include the include
	 * @param exclude the exclude
	 * @return the search source builder
	 */
	public SearchSourceBuilder partialField(String name, @Nullable String include, @Nullable String exclude) {
		if (partialFields == null) {
			partialFields = Lists.newArrayList();
		}
		partialFields.add(new PartialField(name, include, exclude));
		return this;
	}

	/**
	 * Partial field.
	 *
	 * @param name the name
	 * @param includes the includes
	 * @param excludes the excludes
	 * @return the search source builder
	 */
	public SearchSourceBuilder partialField(String name, @Nullable String[] includes, @Nullable String[] excludes) {
		if (partialFields == null) {
			partialFields = Lists.newArrayList();
		}
		partialFields.add(new PartialField(name, includes, excludes));
		return this;
	}

	/**
	 * Index boost.
	 *
	 * @param index the index
	 * @param indexBoost the index boost
	 * @return the search source builder
	 */
	public SearchSourceBuilder indexBoost(String index, float indexBoost) {
		if (this.indexBoost == null) {
			this.indexBoost = new TObjectFloatHashMap<String>();
		}
		this.indexBoost.put(index, indexBoost);
		return this;
	}

	/**
	 * Stats.
	 *
	 * @param statsGroups the stats groups
	 * @return the search source builder
	 */
	public SearchSourceBuilder stats(String... statsGroups) {
		this.stats = statsGroups;
		return this;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON).prettyPrint();
			toXContent(builder, ToXContent.EMPTY_PARAMS);
			return builder.string();
		} catch (Exception e) {
			return "{ \"error\" : \"" + e.getMessage() + "\"}";
		}
	}

	/**
	 * Builds the as bytes stream.
	 *
	 * @param contentType the content type
	 * @return the bytes stream
	 * @throws SearchSourceBuilderException the search source builder exception
	 */
	public BytesStream buildAsBytesStream(XContentType contentType) throws SearchSourceBuilderException {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(contentType);
			toXContent(builder, ToXContent.EMPTY_PARAMS);
			return builder.underlyingStream();
		} catch (Exception e) {
			throw new SearchSourceBuilderException("Failed to build search source", e);
		}
	}

	/**
	 * Builds the as bytes.
	 *
	 * @return the byte[]
	 * @throws SearchSourceBuilderException the search source builder exception
	 */
	public byte[] buildAsBytes() throws SearchSourceBuilderException {
		return buildAsBytes(Requests.CONTENT_TYPE);
	}

	/**
	 * Builds the as bytes.
	 *
	 * @param contentType the content type
	 * @return the byte[]
	 * @throws SearchSourceBuilderException the search source builder exception
	 */
	public byte[] buildAsBytes(XContentType contentType) throws SearchSourceBuilderException {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(contentType);
			toXContent(builder, EMPTY_PARAMS);
			return builder.copiedBytes();
		} catch (Exception e) {
			throw new SearchSourceBuilderException("Failed to build search source", e);
		}
	}

	/**
	 * Similarity.
	 *
	 * @param similarity the similarity
	 * @return the search source builder
	 */
	public SearchSourceBuilder similarity(Similarity similarity) {
		this.similarity = similarity;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject();

		if (from != -1) {
			builder.field("from", from);
		}
		if (size != -1) {
			builder.field("size", size);
		}

		if (timeoutInMillis != -1) {
			builder.field("timeout", timeoutInMillis);
		}

		if (queryBuilder != null) {
			builder.field("query");
			queryBuilder.toXContent(builder, params);
		}

		if (queryBinary != null) {
			if (XContentFactory.xContentType(queryBinary, queryBinaryOffset, queryBinaryLength) == builder
					.contentType()) {
				builder.rawField("query", queryBinary, queryBinaryOffset, queryBinaryLength);
			} else {
				builder.field("query_binary", queryBinary, queryBinaryOffset, queryBinaryLength);
			}
		}

		if (filterBuilder != null) {
			builder.field("filter");
			filterBuilder.toXContent(builder, params);
		}

		if (filterBinary != null) {
			if (XContentFactory.xContentType(filterBinary, filterBinaryOffset, filterBinaryLength) == builder
					.contentType()) {
				builder.rawField("filter", filterBinary, filterBinaryOffset, filterBinaryLength);
			} else {
				builder.field("filter_binary", filterBinary, filterBinaryOffset, filterBinaryLength);
			}
		}

		if (minScore != null) {
			builder.field("min_score", minScore);
		}

		if (version != null) {
			builder.field("version", version);
		}

		if (explain != null) {
			builder.field("explain", explain);
		}

		if (fieldNames != null) {
			if (fieldNames.size() == 1) {
				builder.field("fields", fieldNames.get(0));
			} else {
				builder.startArray("fields");
				for (String fieldName : fieldNames) {
					builder.value(fieldName);
				}
				builder.endArray();
			}
		}

		if (partialFields != null) {
			builder.startObject("partial_fields");
			for (PartialField partialField : partialFields) {
				builder.startObject(partialField.name());
				if (partialField.includes() != null) {
					if (partialField.includes().length == 1) {
						builder.field("include", partialField.includes()[0]);
					} else {
						builder.field("include", partialField.includes());
					}
				}
				if (partialField.excludes() != null) {
					if (partialField.excludes().length == 1) {
						builder.field("exclude", partialField.excludes()[0]);
					} else {
						builder.field("exclude", partialField.excludes());
					}
				}
				builder.endObject();
			}
			builder.endObject();
		}

		if (scriptFields != null) {
			builder.startObject("script_fields");
			for (ScriptField scriptField : scriptFields) {
				builder.startObject(scriptField.fieldName());
				builder.field("script", scriptField.script());
				if (scriptField.lang() != null) {
					builder.field("lang", scriptField.lang());
				}
				if (scriptField.params() != null) {
					builder.field("params");
					builder.map(scriptField.params());
				}
				builder.endObject();
			}
			builder.endObject();
		}

		if (sorts != null) {
			builder.startArray("sort");
			for (SortBuilder sort : sorts) {
				builder.startObject();
				sort.toXContent(builder, params);
				builder.endObject();
			}
			builder.endArray();
			if (trackScores) {
				builder.field("track_scores", trackScores);
			}
		}

		if (indexBoost != null) {
			builder.startObject("indices_boost");
			for (TObjectFloatIterator<String> it = indexBoost.iterator(); it.hasNext();) {
				it.advance();
				builder.field(it.key(), it.value());
			}
			builder.endObject();
		}

		if (facets != null) {
			builder.field("facets");
			builder.startObject();
			for (AbstractFacetBuilder facet : facets) {
				facet.toXContent(builder, params);
			}
			builder.endObject();
		}

		if (facetsBinary != null) {
			if (XContentFactory.xContentType(facetsBinary, facetBinaryOffset, facetBinaryLength) == builder
					.contentType()) {
				builder.rawField("facets", facetsBinary, facetBinaryOffset, facetBinaryLength);
			} else {
				builder.field("facets_binary", facetsBinary, facetBinaryOffset, facetBinaryLength);
			}
		}

		if (highlightBuilder != null) {
			highlightBuilder.toXContent(builder, params);
		}

		if (stats != null) {
			builder.startArray("stats");
			for (String stat : stats) {
				builder.value(stat);
			}
			builder.endArray();
		}
		if (similarity != null) {
			byte[] json = toSimilarityJson();
			if (json != null)
				builder.field("similarity", json);
		}

		builder.endObject();
		return builder;
	}

	/**
	 * To similarity json.
	 *
	 * @return the byte[]
	 */
	private byte[] toSimilarityJson() {
		try {
			return ObjectToByteUtils.getBytes(similarity);
		} catch (IOException e) {
			throw ExceptionUtils.unchecked(e);
		}
	}

	/**
	 * The Class ScriptField.
	 *
	 * @author l.xue.nong
	 */
	private static class ScriptField {

		/** The field name. */
		private final String fieldName;

		/** The script. */
		private final String script;

		/** The lang. */
		private final String lang;

		/** The params. */
		private final Map<String, Object> params;

		/**
		 * Instantiates a new script field.
		 *
		 * @param fieldName the field name
		 * @param lang the lang
		 * @param script the script
		 * @param params the params
		 */
		private ScriptField(String fieldName, String lang, String script, Map<String, Object> params) {
			this.fieldName = fieldName;
			this.lang = lang;
			this.script = script;
			this.params = params;
		}

		/**
		 * Field name.
		 *
		 * @return the string
		 */
		public String fieldName() {
			return fieldName;
		}

		/**
		 * Script.
		 *
		 * @return the string
		 */
		public String script() {
			return script;
		}

		/**
		 * Lang.
		 *
		 * @return the string
		 */
		public String lang() {
			return this.lang;
		}

		/**
		 * Params.
		 *
		 * @return the map
		 */
		public Map<String, Object> params() {
			return params;
		}
	}

	/**
	 * The Class PartialField.
	 *
	 * @author l.xue.nong
	 */
	private static class PartialField {

		/** The name. */
		private final String name;

		/** The includes. */
		private final String[] includes;

		/** The excludes. */
		private final String[] excludes;

		/**
		 * Instantiates a new partial field.
		 *
		 * @param name the name
		 * @param includes the includes
		 * @param excludes the excludes
		 */
		private PartialField(String name, String[] includes, String[] excludes) {
			this.name = name;
			this.includes = includes;
			this.excludes = excludes;
		}

		/**
		 * Instantiates a new partial field.
		 *
		 * @param name the name
		 * @param include the include
		 * @param exclude the exclude
		 */
		private PartialField(String name, String include, String exclude) {
			this.name = name;
			this.includes = include == null ? null : new String[] { include };
			this.excludes = exclude == null ? null : new String[] { exclude };
		}

		/**
		 * Name.
		 *
		 * @return the string
		 */
		public String name() {
			return name;
		}

		/**
		 * Includes.
		 *
		 * @return the string[]
		 */
		public String[] includes() {
			return includes;
		}

		/**
		 * Excludes.
		 *
		 * @return the string[]
		 */
		public String[] excludes() {
			return excludes;
		}
	}
}
