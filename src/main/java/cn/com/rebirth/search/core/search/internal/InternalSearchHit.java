/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalSearchHit.java 2012-7-6 14:30:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.internal;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.search.Explanation;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.Unicode;
import cn.com.rebirth.commons.compress.lzf.LZF;
import cn.com.rebirth.commons.compress.lzf.LZFDecoder;
import cn.com.rebirth.commons.exception.RebirthParseException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.rest.action.support.RestXContentBuilder;
import cn.com.rebirth.search.core.search.SearchHit;
import cn.com.rebirth.search.core.search.SearchHitField;
import cn.com.rebirth.search.core.search.SearchShardTarget;
import cn.com.rebirth.search.core.search.highlight.HighlightField;

import com.google.common.collect.ImmutableMap;

/**
 * The Class InternalSearchHit.
 *
 * @author l.xue.nong
 */
public class InternalSearchHit implements SearchHit {

	/** The Constant EMPTY_SORT_VALUES. */
	private static final Object[] EMPTY_SORT_VALUES = new Object[0];

	/** The doc id. */
	private transient int docId;

	/** The score. */
	private float score = Float.NEGATIVE_INFINITY;

	/** The id. */
	private String id;

	/** The type. */
	private String type;

	/** The version. */
	private long version = -1;

	/** The source. */
	private BytesHolder source;

	/** The fields. */
	private Map<String, SearchHitField> fields = ImmutableMap.of();

	/** The highlight fields. */
	private Map<String, HighlightField> highlightFields = null;

	/** The sort values. */
	private Object[] sortValues = EMPTY_SORT_VALUES;

	/** The matched filters. */
	private String[] matchedFilters = Strings.EMPTY_ARRAY;

	/** The explanation. */
	private Explanation explanation;

	/** The shard. */
	@Nullable
	private SearchShardTarget shard;

	/** The source as map. */
	private Map<String, Object> sourceAsMap;

	/**
	 * Instantiates a new internal search hit.
	 */
	private InternalSearchHit() {

	}

	/**
	 * Instantiates a new internal search hit.
	 *
	 * @param docId the doc id
	 * @param id the id
	 * @param type the type
	 * @param source the source
	 * @param fields the fields
	 */
	public InternalSearchHit(int docId, String id, String type, byte[] source, Map<String, SearchHitField> fields) {
		this.docId = docId;
		this.id = id;
		this.type = type;
		this.source = source == null ? null : new BytesHolder(source);
		this.fields = fields;
	}

	/**
	 * Doc id.
	 *
	 * @return the int
	 */
	public int docId() {
		return this.docId;
	}

	/**
	 * Shard target.
	 *
	 * @param shardTarget the shard target
	 */
	public void shardTarget(SearchShardTarget shardTarget) {
		this.shard = shardTarget;
	}

	/**
	 * Score.
	 *
	 * @param score the score
	 */
	public void score(float score) {
		this.score = score;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#score()
	 */
	@Override
	public float score() {
		return this.score;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#getScore()
	 */
	@Override
	public float getScore() {
		return score();
	}

	/**
	 * Version.
	 *
	 * @param version the version
	 */
	public void version(long version) {
		this.version = version;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#version()
	 */
	@Override
	public long version() {
		return this.version;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#getVersion()
	 */
	@Override
	public long getVersion() {
		return this.version;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#index()
	 */
	@Override
	public String index() {
		return shard.index();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#getIndex()
	 */
	@Override
	public String getIndex() {
		return index();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#id()
	 */
	@Override
	public String id() {
		return id;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#getId()
	 */
	@Override
	public String getId() {
		return id();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#type()
	 */
	@Override
	public String type() {
		return type;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#getType()
	 */
	@Override
	public String getType() {
		return type();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#source()
	 */
	@Override
	public byte[] source() {
		if (source == null) {
			return null;
		}
		if (LZF.isCompressed(source.bytes(), source.offset(), source.length())) {
			try {
				this.source = new BytesHolder(LZFDecoder.decode(source.bytes(), source.offset(), source.length()));
			} catch (IOException e) {
				throw new RebirthParseException("failed to decompress source", e);
			}
		}
		return this.source.copyBytes();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#isSourceEmpty()
	 */
	@Override
	public boolean isSourceEmpty() {
		return source == null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#getSource()
	 */
	@Override
	public Map<String, Object> getSource() {
		return sourceAsMap();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#sourceAsString()
	 */
	@Override
	public String sourceAsString() {
		if (source == null) {
			return null;
		}
		return Unicode.fromBytes(source.bytes(), source.offset(), source.length());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#sourceAsMap()
	 */
	@Override
	public Map<String, Object> sourceAsMap() throws RebirthParseException {
		if (source == null) {
			return null;
		}
		if (sourceAsMap != null) {
			return sourceAsMap;
		}
		byte[] source = source();
		XContentParser parser = null;
		try {
			parser = XContentFactory.xContent(source).createParser(source);
			sourceAsMap = parser.map();
			parser.close();
			return sourceAsMap;
		} catch (Exception e) {
			throw new RebirthParseException("Failed to parse source to map", e);
		} finally {
			if (parser != null) {
				parser.close();
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<SearchHitField> iterator() {
		return fields.values().iterator();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#field(java.lang.String)
	 */
	@Override
	public SearchHitField field(String fieldName) {
		return fields().get(fieldName);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#fields()
	 */
	@Override
	public Map<String, SearchHitField> fields() {
		if (fields == null) {
			return ImmutableMap.of();
		}
		return fields;
	}

	/**
	 * Fields or null.
	 *
	 * @return the map
	 */
	public Map<String, SearchHitField> fieldsOrNull() {
		return this.fields;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#getFields()
	 */
	@Override
	public Map<String, SearchHitField> getFields() {
		return fields();
	}

	/**
	 * Fields.
	 *
	 * @param fields the fields
	 */
	public void fields(Map<String, SearchHitField> fields) {
		this.fields = fields;
	}

	/**
	 * Internal highlight fields.
	 *
	 * @return the map
	 */
	public Map<String, HighlightField> internalHighlightFields() {
		return highlightFields;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#highlightFields()
	 */
	@Override
	public Map<String, HighlightField> highlightFields() {
		if (highlightFields == null) {
			return ImmutableMap.of();
		}
		return this.highlightFields;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#getHighlightFields()
	 */
	@Override
	public Map<String, HighlightField> getHighlightFields() {
		return highlightFields();
	}

	/**
	 * Highlight fields.
	 *
	 * @param highlightFields the highlight fields
	 */
	public void highlightFields(Map<String, HighlightField> highlightFields) {
		this.highlightFields = highlightFields;
	}

	/**
	 * Sort values.
	 *
	 * @param sortValues the sort values
	 */
	public void sortValues(Object[] sortValues) {
		this.sortValues = sortValues;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#sortValues()
	 */
	@Override
	public Object[] sortValues() {
		return sortValues;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#getSortValues()
	 */
	@Override
	public Object[] getSortValues() {
		return sortValues();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#explanation()
	 */
	@Override
	public Explanation explanation() {
		return explanation;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#getExplanation()
	 */
	@Override
	public Explanation getExplanation() {
		return explanation();
	}

	/**
	 * Explanation.
	 *
	 * @param explanation the explanation
	 */
	public void explanation(Explanation explanation) {
		this.explanation = explanation;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#shard()
	 */
	@Override
	public SearchShardTarget shard() {
		return shard;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#getShard()
	 */
	@Override
	public SearchShardTarget getShard() {
		return shard();
	}

	/**
	 * Shard.
	 *
	 * @param target the target
	 */
	public void shard(SearchShardTarget target) {
		this.shard = target;
	}

	/**
	 * Matched filters.
	 *
	 * @param matchedFilters the matched filters
	 */
	public void matchedFilters(String[] matchedFilters) {
		this.matchedFilters = matchedFilters;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#matchedFilters()
	 */
	public String[] matchedFilters() {
		return this.matchedFilters;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchHit#getMatchedFilters()
	 */
	@Override
	public String[] getMatchedFilters() {
		return this.matchedFilters;
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	public static class Fields {

		/** The Constant _INDEX. */
		static final XContentBuilderString _INDEX = new XContentBuilderString("_index");

		/** The Constant _TYPE. */
		static final XContentBuilderString _TYPE = new XContentBuilderString("_type");

		/** The Constant _ID. */
		static final XContentBuilderString _ID = new XContentBuilderString("_id");

		/** The Constant _VERSION. */
		static final XContentBuilderString _VERSION = new XContentBuilderString("_version");

		/** The Constant _SCORE. */
		static final XContentBuilderString _SCORE = new XContentBuilderString("_score");

		/** The Constant FIELDS. */
		static final XContentBuilderString FIELDS = new XContentBuilderString("fields");

		/** The Constant HIGHLIGHT. */
		static final XContentBuilderString HIGHLIGHT = new XContentBuilderString("highlight");

		/** The Constant SORT. */
		static final XContentBuilderString SORT = new XContentBuilderString("sort");

		/** The Constant MATCH_FILTERS. */
		static final XContentBuilderString MATCH_FILTERS = new XContentBuilderString("matched_filters");

		/** The Constant _EXPLANATION. */
		static final XContentBuilderString _EXPLANATION = new XContentBuilderString("_explanation");

		/** The Constant VALUE. */
		static final XContentBuilderString VALUE = new XContentBuilderString("value");

		/** The Constant DESCRIPTION. */
		static final XContentBuilderString DESCRIPTION = new XContentBuilderString("description");

		/** The Constant DETAILS. */
		static final XContentBuilderString DETAILS = new XContentBuilderString("details");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject();
		if (explanation() != null) {
			builder.field("_shard", shard.shardId());
			builder.field("_node", shard.nodeId());
		}
		builder.field(Fields._INDEX, shard.index());
		builder.field(Fields._TYPE, type);
		builder.field(Fields._ID, id);
		if (version != -1) {
			builder.field(Fields._VERSION, version);
		}
		if (Float.isNaN(score)) {
			builder.nullField(Fields._SCORE);
		} else {
			builder.field(Fields._SCORE, score);
		}
		if (source != null) {
			RestXContentBuilder.restDocumentSource(source.bytes(), source.offset(), source.length(), builder, params);
		}
		if (fields != null && !fields.isEmpty()) {
			builder.startObject(Fields.FIELDS);
			for (SearchHitField field : fields.values()) {
				if (field.values().isEmpty()) {
					continue;
				}
				if (field.values().size() == 1) {
					builder.field(field.name(), field.values().get(0));
				} else {
					builder.field(field.name());
					builder.startArray();
					for (Object value : field.values()) {
						builder.value(value);
					}
					builder.endArray();
				}
			}
			builder.endObject();
		}
		if (highlightFields != null && !highlightFields.isEmpty()) {
			builder.startObject(Fields.HIGHLIGHT);
			for (HighlightField field : highlightFields.values()) {
				builder.field(field.name());
				if (field.fragments() == null) {
					builder.nullValue();
				} else {
					builder.startArray();
					for (String fragment : field.fragments()) {
						builder.value(fragment);
					}
					builder.endArray();
				}
			}
			builder.endObject();
		}
		if (sortValues != null && sortValues.length > 0) {
			builder.startArray(Fields.SORT);
			for (Object sortValue : sortValues) {
				builder.value(sortValue);
			}
			builder.endArray();
		}
		if (matchedFilters.length > 0) {
			builder.startArray(Fields.MATCH_FILTERS);
			for (String matchedFilter : matchedFilters) {
				builder.value(matchedFilter);
			}
			builder.endArray();
		}
		if (explanation() != null) {
			builder.field(Fields._EXPLANATION);
			buildExplanation(builder, explanation());
		}
		builder.endObject();
		return builder;
	}

	/**
	 * Builds the explanation.
	 *
	 * @param builder the builder
	 * @param explanation the explanation
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void buildExplanation(XContentBuilder builder, Explanation explanation) throws IOException {
		builder.startObject();
		builder.field(Fields.VALUE, explanation.getValue());
		builder.field(Fields.DESCRIPTION, explanation.getDescription());
		Explanation[] innerExps = explanation.getDetails();
		if (innerExps != null) {
			builder.startArray(Fields.DETAILS);
			for (Explanation exp : innerExps) {
				buildExplanation(builder, exp);
			}
			builder.endArray();
		}
		builder.endObject();
	}

	/**
	 * Read search hit.
	 *
	 * @param in the in
	 * @param context the context
	 * @return the internal search hit
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static InternalSearchHit readSearchHit(StreamInput in, InternalSearchHits.StreamContext context)
			throws IOException {
		InternalSearchHit hit = new InternalSearchHit();
		hit.readFrom(in, context);
		return hit;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		readFrom(
				in,
				InternalSearchHits.streamContext().streamShardTarget(
						InternalSearchHits.StreamContext.ShardTargetType.STREAM));
	}

	/**
	 * Read from.
	 *
	 * @param in the in
	 * @param context the context
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void readFrom(StreamInput in, InternalSearchHits.StreamContext context) throws IOException {
		score = in.readFloat();
		id = in.readUTF();
		type = in.readUTF();
		version = in.readLong();
		source = in.readBytesReference();
		if (source.length() == 0) {
			source = null;
		}
		if (in.readBoolean()) {
			explanation = Lucene.readExplanation(in);
		}
		int size = in.readVInt();
		if (size == 0) {
			fields = ImmutableMap.of();
		} else if (size == 1) {
			SearchHitField hitField = InternalSearchHitField.readSearchHitField(in);
			fields = ImmutableMap.of(hitField.name(), hitField);
		} else if (size == 2) {
			SearchHitField hitField1 = InternalSearchHitField.readSearchHitField(in);
			SearchHitField hitField2 = InternalSearchHitField.readSearchHitField(in);
			fields = ImmutableMap.of(hitField1.name(), hitField1, hitField2.name(), hitField2);
		} else if (size == 3) {
			SearchHitField hitField1 = InternalSearchHitField.readSearchHitField(in);
			SearchHitField hitField2 = InternalSearchHitField.readSearchHitField(in);
			SearchHitField hitField3 = InternalSearchHitField.readSearchHitField(in);
			fields = ImmutableMap.of(hitField1.name(), hitField1, hitField2.name(), hitField2, hitField3.name(),
					hitField3);
		} else if (size == 4) {
			SearchHitField hitField1 = InternalSearchHitField.readSearchHitField(in);
			SearchHitField hitField2 = InternalSearchHitField.readSearchHitField(in);
			SearchHitField hitField3 = InternalSearchHitField.readSearchHitField(in);
			SearchHitField hitField4 = InternalSearchHitField.readSearchHitField(in);
			fields = ImmutableMap.of(hitField1.name(), hitField1, hitField2.name(), hitField2, hitField3.name(),
					hitField3, hitField4.name(), hitField4);
		} else if (size == 5) {
			SearchHitField hitField1 = InternalSearchHitField.readSearchHitField(in);
			SearchHitField hitField2 = InternalSearchHitField.readSearchHitField(in);
			SearchHitField hitField3 = InternalSearchHitField.readSearchHitField(in);
			SearchHitField hitField4 = InternalSearchHitField.readSearchHitField(in);
			SearchHitField hitField5 = InternalSearchHitField.readSearchHitField(in);
			fields = ImmutableMap.of(hitField1.name(), hitField1, hitField2.name(), hitField2, hitField3.name(),
					hitField3, hitField4.name(), hitField4, hitField5.name(), hitField5);
		} else {
			ImmutableMap.Builder<String, SearchHitField> builder = ImmutableMap.builder();
			for (int i = 0; i < size; i++) {
				SearchHitField hitField = InternalSearchHitField.readSearchHitField(in);
				builder.put(hitField.name(), hitField);
			}
			fields = builder.build();
		}

		size = in.readVInt();
		if (size == 0) {
			highlightFields = ImmutableMap.of();
		} else if (size == 1) {
			HighlightField field = HighlightField.readHighlightField(in);
			highlightFields = ImmutableMap.of(field.name(), field);
		} else if (size == 2) {
			HighlightField field1 = HighlightField.readHighlightField(in);
			HighlightField field2 = HighlightField.readHighlightField(in);
			highlightFields = ImmutableMap.of(field1.name(), field1, field2.name(), field2);
		} else if (size == 3) {
			HighlightField field1 = HighlightField.readHighlightField(in);
			HighlightField field2 = HighlightField.readHighlightField(in);
			HighlightField field3 = HighlightField.readHighlightField(in);
			highlightFields = ImmutableMap.of(field1.name(), field1, field2.name(), field2, field3.name(), field3);
		} else if (size == 4) {
			HighlightField field1 = HighlightField.readHighlightField(in);
			HighlightField field2 = HighlightField.readHighlightField(in);
			HighlightField field3 = HighlightField.readHighlightField(in);
			HighlightField field4 = HighlightField.readHighlightField(in);
			highlightFields = ImmutableMap.of(field1.name(), field1, field2.name(), field2, field3.name(), field3,
					field4.name(), field4);
		} else {
			ImmutableMap.Builder<String, HighlightField> builder = ImmutableMap.builder();
			for (int i = 0; i < size; i++) {
				HighlightField field = HighlightField.readHighlightField(in);
				builder.put(field.name(), field);
			}
			highlightFields = builder.build();
		}

		size = in.readVInt();
		if (size > 0) {
			sortValues = new Object[size];
			for (int i = 0; i < sortValues.length; i++) {
				byte type = in.readByte();
				if (type == 0) {
					sortValues[i] = null;
				} else if (type == 1) {
					sortValues[i] = in.readUTF();
				} else if (type == 2) {
					sortValues[i] = in.readInt();
				} else if (type == 3) {
					sortValues[i] = in.readLong();
				} else if (type == 4) {
					sortValues[i] = in.readFloat();
				} else if (type == 5) {
					sortValues[i] = in.readDouble();
				} else if (type == 6) {
					sortValues[i] = in.readByte();
				} else if (type == 7) {
					sortValues[i] = in.readShort();
				} else if (type == 8) {
					sortValues[i] = in.readBoolean();
				} else {
					throw new IOException("Can't match type [" + type + "]");
				}
			}
		}

		size = in.readVInt();
		if (size > 0) {
			matchedFilters = new String[size];
			for (int i = 0; i < size; i++) {
				matchedFilters[i] = in.readUTF();
			}
		}

		if (context.streamShardTarget() == InternalSearchHits.StreamContext.ShardTargetType.STREAM) {
			if (in.readBoolean()) {
				shard = SearchShardTarget.readSearchShardTarget(in);
			}
		} else if (context.streamShardTarget() == InternalSearchHits.StreamContext.ShardTargetType.LOOKUP) {
			int lookupId = in.readVInt();
			if (lookupId > 0) {
				shard = context.handleShardLookup().get(lookupId);
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		writeTo(out,
				InternalSearchHits.streamContext().streamShardTarget(
						InternalSearchHits.StreamContext.ShardTargetType.STREAM));
	}

	/**
	 * Write to.
	 *
	 * @param out the out
	 * @param context the context
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeTo(StreamOutput out, InternalSearchHits.StreamContext context) throws IOException {
		out.writeFloat(score);
		out.writeUTF(id);
		out.writeUTF(type);
		out.writeLong(version);
		out.writeBytesHolder(source);
		if (explanation == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			Lucene.writeExplanation(out, explanation);
		}
		if (fields == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(fields.size());
			for (SearchHitField hitField : fields().values()) {
				hitField.writeTo(out);
			}
		}
		if (highlightFields == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(highlightFields.size());
			for (HighlightField highlightField : highlightFields.values()) {
				highlightField.writeTo(out);
			}
		}

		if (sortValues.length == 0) {
			out.writeVInt(0);
		} else {
			out.writeVInt(sortValues.length);
			for (Object sortValue : sortValues) {
				if (sortValue == null) {
					out.writeByte((byte) 0);
				} else {
					Class<?> type = sortValue.getClass();
					if (type == String.class) {
						out.writeByte((byte) 1);
						out.writeUTF((String) sortValue);
					} else if (type == Integer.class) {
						out.writeByte((byte) 2);
						out.writeInt((Integer) sortValue);
					} else if (type == Long.class) {
						out.writeByte((byte) 3);
						out.writeLong((Long) sortValue);
					} else if (type == Float.class) {
						out.writeByte((byte) 4);
						out.writeFloat((Float) sortValue);
					} else if (type == Double.class) {
						out.writeByte((byte) 5);
						out.writeDouble((Double) sortValue);
					} else if (type == Byte.class) {
						out.writeByte((byte) 6);
						out.writeByte((Byte) sortValue);
					} else if (type == Short.class) {
						out.writeByte((byte) 7);
						out.writeShort((Short) sortValue);
					} else if (type == Boolean.class) {
						out.writeByte((byte) 8);
						out.writeBoolean((Boolean) sortValue);
					} else {
						throw new IOException("Can't handle sort field value of type [" + type + "]");
					}
				}
			}
		}

		if (matchedFilters.length == 0) {
			out.writeVInt(0);
		} else {
			out.writeVInt(matchedFilters.length);
			for (String matchedFilter : matchedFilters) {
				out.writeUTF(matchedFilter);
			}
		}

		if (context.streamShardTarget() == InternalSearchHits.StreamContext.ShardTargetType.STREAM) {
			if (shard == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				shard.writeTo(out);
			}
		} else if (context.streamShardTarget() == InternalSearchHits.StreamContext.ShardTargetType.LOOKUP) {
			if (shard == null) {
				out.writeVInt(0);
			} else {
				out.writeVInt(context.shardHandleLookup().get(shard));
			}
		}
	}
}