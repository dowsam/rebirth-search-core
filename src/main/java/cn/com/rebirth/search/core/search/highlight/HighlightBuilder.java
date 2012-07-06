/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core HighlightBuilder.java 2012-7-6 14:28:58 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.highlight;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.List;

import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.ToXContent.Params;

/**
 * The Class HighlightBuilder.
 *
 * @author l.xue.nong
 */
public class HighlightBuilder implements ToXContent {

	/** The fields. */
	private List<Field> fields;

	/** The tags schema. */
	private String tagsSchema;

	/** The pre tags. */
	private String[] preTags;

	/** The post tags. */
	private String[] postTags;

	/** The order. */
	private String order;

	/** The encoder. */
	private String encoder;

	/** The require field match. */
	private Boolean requireFieldMatch;

	/**
	 * Field.
	 *
	 * @param name the name
	 * @return the highlight builder
	 */
	public HighlightBuilder field(String name) {
		if (fields == null) {
			fields = newArrayList();
		}
		fields.add(new Field(name));
		return this;
	}

	/**
	 * Field.
	 *
	 * @param name the name
	 * @param fragmentSize the fragment size
	 * @return the highlight builder
	 */
	public HighlightBuilder field(String name, int fragmentSize) {
		if (fields == null) {
			fields = newArrayList();
		}
		fields.add(new Field(name).fragmentSize(fragmentSize));
		return this;
	}

	/**
	 * Field.
	 *
	 * @param name the name
	 * @param fragmentSize the fragment size
	 * @param numberOfFragments the number of fragments
	 * @return the highlight builder
	 */
	public HighlightBuilder field(String name, int fragmentSize, int numberOfFragments) {
		if (fields == null) {
			fields = newArrayList();
		}
		fields.add(new Field(name).fragmentSize(fragmentSize).numOfFragments(numberOfFragments));
		return this;
	}

	/**
	 * Field.
	 *
	 * @param name the name
	 * @param fragmentSize the fragment size
	 * @param numberOfFragments the number of fragments
	 * @param fragmentOffset the fragment offset
	 * @return the highlight builder
	 */
	public HighlightBuilder field(String name, int fragmentSize, int numberOfFragments, int fragmentOffset) {
		if (fields == null) {
			fields = newArrayList();
		}
		fields.add(new Field(name).fragmentSize(fragmentSize).numOfFragments(numberOfFragments)
				.fragmentOffset(fragmentOffset));
		return this;
	}

	/**
	 * Field.
	 *
	 * @param field the field
	 * @return the highlight builder
	 */
	public HighlightBuilder field(Field field) {
		if (fields == null) {
			fields = newArrayList();
		}
		fields.add(field);
		return this;
	}

	/**
	 * Tags schema.
	 *
	 * @param schemaName the schema name
	 * @return the highlight builder
	 */
	public HighlightBuilder tagsSchema(String schemaName) {
		this.tagsSchema = schemaName;
		return this;
	}

	/**
	 * Encoder.
	 *
	 * @param encoder the encoder
	 * @return the highlight builder
	 */
	public HighlightBuilder encoder(String encoder) {
		this.encoder = encoder;
		return this;
	}

	/**
	 * Pre tags.
	 *
	 * @param preTags the pre tags
	 * @return the highlight builder
	 */
	public HighlightBuilder preTags(String... preTags) {
		this.preTags = preTags;
		return this;
	}

	/**
	 * Post tags.
	 *
	 * @param postTags the post tags
	 * @return the highlight builder
	 */
	public HighlightBuilder postTags(String... postTags) {
		this.postTags = postTags;
		return this;
	}

	/**
	 * Order.
	 *
	 * @param order the order
	 * @return the highlight builder
	 */
	public HighlightBuilder order(String order) {
		this.order = order;
		return this;
	}

	/**
	 * Require field match.
	 *
	 * @param requireFieldMatch the require field match
	 * @return the highlight builder
	 */
	public HighlightBuilder requireFieldMatch(boolean requireFieldMatch) {
		this.requireFieldMatch = requireFieldMatch;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject("highlight");
		if (tagsSchema != null) {
			builder.field("tags_schema", tagsSchema);
		}
		if (preTags != null) {
			builder.array("pre_tags", preTags);
		}
		if (postTags != null) {
			builder.array("post_tags", postTags);
		}
		if (order != null) {
			builder.field("order", order);
		}
		if (encoder != null) {
			builder.field("encoder", encoder);
		}
		if (requireFieldMatch != null) {
			builder.field("require_field_match", requireFieldMatch);
		}
		if (fields != null) {
			builder.startObject("fields");
			for (Field field : fields) {
				builder.startObject(field.name());
				if (field.fragmentSize != -1) {
					builder.field("fragment_size", field.fragmentSize);
				}
				if (field.numOfFragments != -1) {
					builder.field("number_of_fragments", field.numOfFragments);
				}
				if (field.fragmentOffset != -1) {
					builder.field("fragment_offset", field.fragmentOffset);
				}
				if (field.requireFieldMatch != null) {
					builder.field("require_field_match", field.requireFieldMatch);
				}

				builder.endObject();
			}
			builder.endObject();
		}

		builder.endObject();
		return builder;
	}

	/**
	 * The Class Field.
	 *
	 * @author l.xue.nong
	 */
	public static class Field {

		/** The name. */
		final String name;

		/** The fragment size. */
		int fragmentSize = -1;

		/** The fragment offset. */
		int fragmentOffset = -1;

		/** The num of fragments. */
		int numOfFragments = -1;

		/** The require field match. */
		Boolean requireFieldMatch;

		/**
		 * Instantiates a new field.
		 *
		 * @param name the name
		 */
		private Field(String name) {
			this.name = name;
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
		 * Fragment size.
		 *
		 * @param fragmentSize the fragment size
		 * @return the field
		 */
		public Field fragmentSize(int fragmentSize) {
			this.fragmentSize = fragmentSize;
			return this;
		}

		/**
		 * Fragment offset.
		 *
		 * @param fragmentOffset the fragment offset
		 * @return the field
		 */
		public Field fragmentOffset(int fragmentOffset) {
			this.fragmentOffset = fragmentOffset;
			return this;
		}

		/**
		 * Num of fragments.
		 *
		 * @param numOfFragments the num of fragments
		 * @return the field
		 */
		public Field numOfFragments(int numOfFragments) {
			this.numOfFragments = numOfFragments;
			return this;
		}

		/**
		 * Require field match.
		 *
		 * @param requireFieldMatch the require field match
		 * @return the field
		 */
		public Field requireFieldMatch(boolean requireFieldMatch) {
			this.requireFieldMatch = requireFieldMatch;
			return this;
		}
	}
}
