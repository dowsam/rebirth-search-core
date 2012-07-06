/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MultiGetResponse.java 2012-3-29 15:01:43 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.get;

import java.io.IOException;
import java.util.Iterator;

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
 * The Class MultiGetResponse.
 *
 * @author l.xue.nong
 */
public class MultiGetResponse implements ActionResponse, Iterable<MultiGetItemResponse>, ToXContent {

	
	/**
	 * The Class Failure.
	 *
	 * @author l.xue.nong
	 */
	public static class Failure implements Streamable {

		
		/** The index. */
		private String index;

		
		/** The type. */
		private String type;

		
		/** The id. */
		private String id;

		
		/** The message. */
		private String message;

		
		/**
		 * Instantiates a new failure.
		 */
		Failure() {

		}

		
		/**
		 * Instantiates a new failure.
		 *
		 * @param index the index
		 * @param type the type
		 * @param id the id
		 * @param message the message
		 */
		public Failure(String index, String type, String id, String message) {
			this.index = index;
			this.type = type;
			this.id = id;
			this.message = message;
		}

		
		/**
		 * Index.
		 *
		 * @return the string
		 */
		public String index() {
			return this.index;
		}

		
		/**
		 * Gets the index.
		 *
		 * @return the index
		 */
		public String getIndex() {
			return index();
		}

		
		/**
		 * Type.
		 *
		 * @return the string
		 */
		public String type() {
			return type;
		}

		
		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public String getType() {
			return type();
		}

		
		/**
		 * Id.
		 *
		 * @return the string
		 */
		public String id() {
			return id;
		}

		
		/**
		 * Gets the id.
		 *
		 * @return the id
		 */
		public String getId() {
			return this.id;
		}

		
		/**
		 * Message.
		 *
		 * @return the string
		 */
		public String message() {
			return this.message;
		}

		
		/**
		 * Gets the message.
		 *
		 * @return the message
		 */
		public String getMessage() {
			return message();
		}

		
		/**
		 * Read failure.
		 *
		 * @param in the in
		 * @return the failure
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static Failure readFailure(StreamInput in) throws IOException {
			Failure failure = new Failure();
			failure.readFrom(in);
			return failure;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			index = in.readUTF();
			if (in.readBoolean()) {
				type = in.readUTF();
			}
			id = in.readUTF();
			message = in.readUTF();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeUTF(index);
			if (type == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				out.writeUTF(type);
			}
			out.writeUTF(id);
			out.writeUTF(message);
		}
	}

	
	/** The responses. */
	private MultiGetItemResponse[] responses;

	
	/**
	 * Instantiates a new multi get response.
	 */
	MultiGetResponse() {
	}

	
	/**
	 * Instantiates a new multi get response.
	 *
	 * @param responses the responses
	 */
	public MultiGetResponse(MultiGetItemResponse[] responses) {
		this.responses = responses;
	}

	
	/**
	 * Responses.
	 *
	 * @return the multi get item response[]
	 */
	public MultiGetItemResponse[] responses() {
		return this.responses;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<MultiGetItemResponse> iterator() {
		return Iterators.forArray(responses);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject();
		builder.startArray(Fields.DOCS);
		for (MultiGetItemResponse response : responses) {
			if (response.failed()) {
				builder.startObject();
				Failure failure = response.failure();
				builder.field(Fields._INDEX, failure.index());
				builder.field(Fields._TYPE, failure.type());
				builder.field(Fields._ID, failure.id());
				builder.field(Fields.ERROR, failure.message());
				builder.endObject();
			} else {
				GetResponse getResponse = response.getResponse();
				getResponse.toXContent(builder, params);
			}
		}
		builder.endArray();
		builder.endObject();
		return builder;
	}

	
	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		
		/** The Constant DOCS. */
		static final XContentBuilderString DOCS = new XContentBuilderString("docs");

		
		/** The Constant _INDEX. */
		static final XContentBuilderString _INDEX = new XContentBuilderString("_index");

		
		/** The Constant _TYPE. */
		static final XContentBuilderString _TYPE = new XContentBuilderString("_type");

		
		/** The Constant _ID. */
		static final XContentBuilderString _ID = new XContentBuilderString("_id");

		
		/** The Constant ERROR. */
		static final XContentBuilderString ERROR = new XContentBuilderString("error");
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		responses = new MultiGetItemResponse[in.readVInt()];
		for (int i = 0; i < responses.length; i++) {
			responses[i] = MultiGetItemResponse.readItemResponse(in);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVInt(responses.length);
		for (MultiGetItemResponse response : responses) {
			response.writeTo(out);
		}
	}
}