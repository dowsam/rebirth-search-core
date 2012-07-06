/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MultiSearchResponse.java 2012-7-6 14:29:09 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.search;

import java.io.IOException;
import java.util.Iterator;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.commons.xcontent.ToXContent.Params;
import cn.com.rebirth.search.core.action.ActionResponse;

import com.google.common.collect.Iterators;

/**
 * The Class MultiSearchResponse.
 *
 * @author l.xue.nong
 */
public class MultiSearchResponse implements ActionResponse, Iterable<MultiSearchResponse.Item>, ToXContent {

	/**
	 * The Class Item.
	 *
	 * @author l.xue.nong
	 */
	public static class Item implements Streamable {

		/** The response. */
		private SearchResponse response;

		/** The failure message. */
		private String failureMessage;

		/**
		 * Instantiates a new item.
		 */
		Item() {

		}

		/**
		 * Instantiates a new item.
		 *
		 * @param response the response
		 * @param failureMessage the failure message
		 */
		public Item(SearchResponse response, String failureMessage) {
			this.response = response;
			this.failureMessage = failureMessage;
		}

		/**
		 * Checks if is failure.
		 *
		 * @return true, if is failure
		 */
		public boolean isFailure() {
			return failureMessage != null;
		}

		/**
		 * Failure message.
		 *
		 * @return the string
		 */
		@Nullable
		public String failureMessage() {
			return failureMessage;
		}

		/**
		 * Gets the failure message.
		 *
		 * @return the failure message
		 */
		@Nullable
		public String getFailureMessage() {
			return failureMessage;
		}

		/**
		 * Response.
		 *
		 * @return the search response
		 */
		@Nullable
		public SearchResponse response() {
			return this.response;
		}

		/**
		 * Gets the response.
		 *
		 * @return the response
		 */
		@Nullable
		public SearchResponse getResponse() {
			return this.response;
		}

		/**
		 * Read item.
		 *
		 * @param in the in
		 * @return the item
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static Item readItem(StreamInput in) throws IOException {
			Item item = new Item();
			item.readFrom(in);
			return item;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			if (in.readBoolean()) {
				this.response = new SearchResponse();
				response.readFrom(in);
			} else {
				failureMessage = in.readUTF();
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			if (response != null) {
				out.writeBoolean(true);
				response.writeTo(out);
			} else {
				out.writeUTF(failureMessage);
			}
		}
	}

	/** The items. */
	private Item[] items;

	/**
	 * Instantiates a new multi search response.
	 */
	MultiSearchResponse() {
	}

	/**
	 * Instantiates a new multi search response.
	 *
	 * @param items the items
	 */
	public MultiSearchResponse(Item[] items) {
		this.items = items;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Item> iterator() {
		return Iterators.forArray(items);
	}

	/**
	 * Responses.
	 *
	 * @return the item[]
	 */
	public Item[] responses() {
		return this.items;
	}

	/**
	 * Gets the responses.
	 *
	 * @return the responses
	 */
	public Item[] getResponses() {
		return this.items;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		items = new Item[in.readVInt()];
		for (int i = 0; i < items.length; i++) {
			items[i] = Item.readItem(in);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVInt(items.length);
		for (Item item : items) {
			item.writeTo(out);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startArray(Fields.RESPONSES);
		for (Item item : items) {
			if (item.isFailure()) {
				builder.startObject();
				builder.field(Fields.ERROR, item.failureMessage());
				builder.endObject();
			} else {
				builder.startObject();
				item.response().toXContent(builder, params);
				builder.endObject();
			}
		}
		builder.endArray();
		return builder;
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant RESPONSES. */
		static final XContentBuilderString RESPONSES = new XContentBuilderString("responses");

		/** The Constant ERROR. */
		static final XContentBuilderString ERROR = new XContentBuilderString("error");
	}
}
